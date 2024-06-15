import javax.mail.internet.MimeUtility;
import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;

public class SMTPClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Connection connection;
    // Create a date object
    Date date = new Date();

    // Create a date format object
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
    String boundary = "=_Hailu_" + System.currentTimeMillis();


    public SMTPClient() throws IOException {

        socket = new Socket("localhost", 6423);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        try {
            String url = "jdbc:postgresql://localhost:5432/SMTP";
            String user = "postgres";
            String password = "Hailuke!21092003";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the time zone to Vietnam
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        System.out.println(in.readLine()); // Read the server's initial response
    }

    public boolean authorize(String username, String password) throws IOException {
        out.println("EHLO localhost");
        System.out.println(in.readLine()); // Read server response

        String authString = username + "\u0000" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
        out.println("AUTH PLAIN " + encodedAuthString);

        String response = in.readLine();
        System.out.println(response);
        return response.startsWith("235");
    }

    public void sendEmail(String from, String to, String subject, String message, List<File> attachments) throws IOException {
        out.println("MAIL FROM:<" + from + ">");
        System.out.println(in.readLine()); // Read server response

        out.println("RCPT TO:<" + to + ">");
        System.out.println(in.readLine()); // Read server response

        out.println("DATA");
        System.out.println(in.readLine()); // Read server response

        out.println("Date: " + sdf.format(date)); // format the date
        out.println("From: " + from);
        out.println("To: " + to);
        out.println("Subject: " + subject);

        // Start message body
        out.println("MIME-Version: 1.0");
        out.println("Content-Type: multipart/mixed; boundary=\"" + boundary + "\"");
        out.println();

        // Send message text part
        out.println("--" + boundary);
        out.println("Content-Type: text/plain; charset=UTF-8");
        out.println("Content-Transfer-Encoding: 7bit");
        out.println();
        out.println(message);
        out.println();

        for (File attachment : attachments) {
            if (attachment != null && attachment.exists()) {
                String contentType = URLConnection.guessContentTypeFromName(attachment.getName());
                if (contentType == null) {
                    contentType = "application/octet-stream"; // Default to octet-stream if type cannot be determined
                }
                out.println("--" + boundary);
                out.println("Content-Type: " + contentType + "; name=\"" + MimeUtility.encodeText(attachment.getName(), "utf-8", "B") + "\"");
                out.println("Content-Transfer-Encoding: base64");
                out.println("Content-Disposition: attachment; filename=\"" + attachment.getName() + "\"");
                out.println();

                try (FileInputStream fileInputStream = new FileInputStream(attachment);
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    String encodedFileContent = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
                    out.println(encodedFileContent);
                }
            }
        }

        out.println("--" + boundary + "--");
        out.println("."); // End of email
        System.out.println(in.readLine()); // Read server response
    }

    public void quit() throws IOException {
        out.println("QUIT");
        System.out.println(in.readLine()); // Read server response
        socket.close();
    }

    public ResultSet getEmailHistory(String username) throws SQLException {
        String querySQL = "SELECT * FROM \"Email\" WHERE \"From\" = ?";
        PreparedStatement pstmt = connection.prepareStatement(querySQL);
        pstmt.setString(1, username);
        return pstmt.executeQuery();
    }

    public static void main(String[] args) {
        // Example usage
        try {
            SMTPClient client = new SMTPClient();
            if (client.authorize("haiqua2k3@gmail.com", "HAI210903")) {
                System.out.println("Authorization successful!");
                client.sendEmail("sgvt@gmail.com", "recipient@example.com", "Test Subject", "This is a test message.", null);
                client.quit();
            } else {
                System.out.println("Authorization failed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
