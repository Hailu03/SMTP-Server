import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;


public class SMTPServer {
    private ServerSocket serverSocket;
    private Connection dbConnection;
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

    // Establish database connection

    public SMTPServer() {
        try {
            serverSocket = new ServerSocket(6423);
            System.out.println("SMTP Server is listening on port 6423...");

            String url = "jdbc:postgresql://localhost:5432/SMTP";
            String user = "postgres";
            String password = "Hailuke!21092003";
            dbConnection = DriverManager.getConnection(url, user, password);
        }  catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection established with " + clientSocket.getInetAddress());
                handleClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out.println("220 Welcome to SMTP Server");

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            String[] command = inputLine.split(" ");
            if (command[0].equals("EHLO")) {
                out.println("250 EHLO");
            } else if (command[0].equals("AUTH")) {
                if (command.length == 3 && command[1].equals("PLAIN")) {
                    if (authenticate(command[2])) {
                        out.println("235 Authentication successful");
                    } else {
                        out.println("535 Authentication failed");
                    }
                } else {
                    out.println("501 Syntax error in parameters or arguments");
                }
            } else if (command[0].equals("MAIL") && command[1].startsWith("FROM:")) {
                out.println("250 OK");
            } else if (command[0].equals("RCPT") && command[1].startsWith("TO:")) {
                out.println("250 OK");
            } else if (command[0].equals("DATA")) {
                out.println("354 Start mail input");
                StringBuilder emailData = new StringBuilder();
                while (!(inputLine = in.readLine()).equals(".")) {
                    emailData.append(inputLine).append("\n");
                }
                saveEmail(emailData.toString());
                out.println("250 OK");
            } else if (command[0].equals("QUIT")) {
                out.println("221 Bye");
                clientSocket.close();
                return;
            } else {
                out.println("502 Command not implemented");
            }
        }
    }

    private boolean authenticate(String authData) {
        String decodedData = new String(Base64.getDecoder().decode(authData)); // decode the authentication data
        String[] credentials = decodedData.split("\u0000");
        return credentials.length >= 2 && credentials[0].equals("haiqua2k3@gmail.com") && credentials[1].equals("HAI210903");
    }

    private void saveEmail(String emailData) {
        Email email = parseEmail(emailData);

        try (PreparedStatement stmt = dbConnection.prepareStatement(
                "INSERT INTO \"Email\" (\"From\", \"To\", subject, message, attachment, \"Date\") VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, email.getFrom());
            stmt.setString(2, email.getTo());
            stmt.setString(3, email.getSubject());
            stmt.setString(4, email.getMessage());

            if (email.getAttachments().isEmpty()) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                StringBuilder attachmentNames = new StringBuilder();
                for (Attachment attachment : email.getAttachments()) {
                    attachmentNames.append(attachment.getFilename()).append(",");
                }
                stmt.setString(5, attachmentNames.toString());
            }

            // Convert java.util.Date to java.sql.Timestamp
            Timestamp timestamp = null;
            timestamp = new Timestamp(sdf.parse(email.getDate()).getTime());
            stmt.setTimestamp(6, timestamp);

            stmt.executeUpdate();
            System.out.println("Email saved to PostgreSQL");

            forwardEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing email date: " + email.getDate(), e);
        }
    }

    private void forwardEmail(Email email) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // create a mail session with the properties
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("haiqua2k3@gmail.com", "imzvqwzcxxwoexhv");
            }
        });

        try {
            // create a new message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email.getFrom()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()));
            message.setSubject(email.getSubject());

            // Set the sent date
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            message.setSentDate(sdf.parse(email.getDate()));

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(email.getMessage());

            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Add attachments if any
            for (Attachment attachment : email.getAttachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                DataSource source = (DataSource) new ByteArrayDataSource(attachment.getData(), "application/octet-stream");
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(attachment.getFilename());
                multipart.addBodyPart(attachmentPart);
            }

            // Send the complete message parts
            message.setContent(multipart);

            // Send the email
            Transport.send(message);
            System.out.println("Email forwarded to external SMTP server");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private Email parseEmail(String emailData) {
        String[] lines = emailData.split("\n");
        StringBuilder emailText = new StringBuilder();
        List<Attachment> attachments = new ArrayList<>();
        Email email = new Email();

        boolean isAttachment = false;
        boolean isMessage = false;
        String currentAttachmentName = null;
        ByteArrayOutputStream currentAttachmentData = null;

        for (String line : lines) {
            if(line.startsWith("Date: ")) {
                System.out.println(line);
                email.setDate(line.substring(6));
            }
            if (line.startsWith("From: ")) {
                email.setFrom(line.substring(6));
            } else if (line.startsWith("To: ")) {
                email.setTo(line.substring(4));
            } else if (line.startsWith("Subject: ")) {
                email.setSubject(line.substring(9));
            } else if (line.startsWith("Content-Type: text/plain")) {
                isMessage = true;
            } else if (line.startsWith("--")) {
                isMessage = false;
            } else if (isMessage && !line.startsWith("--")) {
                emailText.append(line).append("\n");
            } else if (line.startsWith("Content-Disposition: attachment; filename=\"")) {
                isAttachment = true;
                currentAttachmentName = line.split("filename=\"")[1].split("\"")[0];
                currentAttachmentData = new ByteArrayOutputStream();
            } else if (isAttachment && !line.startsWith("--") && !line.isEmpty()) {
                byte[] attachmentBytes = Base64.getDecoder().decode(line);
                try {
                    currentAttachmentData.write(attachmentBytes);
                    byte[] attachmentData = currentAttachmentData.toByteArray();
                    attachments.add(new Attachment(currentAttachmentName, attachmentData));
                    saveAttachmentToFile(currentAttachmentName, attachmentData);
                    isAttachment = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        email.setMessage(emailText.toString());
        email.setAttachments(attachments);

        return email;
    }


    private String saveAttachmentToFile(String filename, byte[] data) {
        String attachmentsDir = "attachments";
        File dir = new File(attachmentsDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Failed to create directory: " + attachmentsDir);
                return null; // Return null to indicate failure
            }
        }

        String filePath = attachmentsDir + File.separator + filename;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
            System.out.println("Attachment saved to: " + filePath);
            return filePath; // Return the file path if successful
        } catch (IOException e) {
            System.err.println("Error saving attachment: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null to indicate failure
        }
    }

    public static void main(String[] args) {
        SMTPServer server = new SMTPServer();
        server.start();
    }
}

