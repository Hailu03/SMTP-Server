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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;


public class SMTPServer {
    private ServerSocket serverSocket;
    private Connection dbConnection;

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
                out.println("354 Start mail input; end with <CRLF>.<CRLF>");
                StringBuilder emailData = new StringBuilder();
                while (!(inputLine = in.readLine()).equals(".")) {
                    emailData.append(inputLine).append("\n");
                }
                System.out.println("Received email:");
                System.out.println(emailData.toString());
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
        String decodedData = new String(Base64.getDecoder().decode(authData));
        String[] credentials = decodedData.split("\u0000");
            return credentials.length >= 3 && credentials[1].equals("{your email}") && credentials[2].equals("{password}");
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

            stmt.setDate(6, email.getDate());

            stmt.executeUpdate();
            System.out.println("Email saved to PostgreSQL");

            forwardEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
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
                return new PasswordAuthentication("{your email}", "{password}");
            }
        });

        try {
            // create a new message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email.getFrom()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()));
            message.setSubject(email.getSubject());
            message.setSentDate(email.getDate());

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
        }
    }



    private Email parseEmail(String emailData) {
        String[] lines = emailData.split("\n");
        StringBuilder emailText = new StringBuilder();
        List<Attachment> attachments = new ArrayList<>();
        Email email = new Email();

        boolean isAttachment = false;
        String currentAttachmentName = null;
        ByteArrayOutputStream currentAttachmentData = null;

        for (String line : lines) {
            if (line.startsWith("From: ")) {
                email.setFrom(line.substring(6));
            } else if (line.startsWith("To: ")) {
                email.setTo(line.substring(4));
            } else if (line.startsWith("Subject: ")) {
                email.setSubject(line.substring(9));
            } else if (line.startsWith("Message: ")) {
                email.setMessage(line.substring(9));
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

class Email {
    private String from;
    private String to;
    private String subject;
    private Date date;
    private String message;
    private List<Attachment> attachments;

    public Email() {
        this.attachments = new ArrayList<>();
        this.date = new Date(System.currentTimeMillis()); // Default to the current date
    }

    // Getters and Setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}

class Attachment {
    private String filename;
    private byte[] data;

    public Attachment(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }
}
