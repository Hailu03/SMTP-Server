import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

public class SMTPClient {
    private Socket socket;
    public PrintWriter out;
    private BufferedReader in;

    public SMTPClient() {
        try {
            socket = new Socket("localhost", 6423);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String encodeCredentials(String username, String password) {
        // Encode credentials as per the authentication method required by the server
        String credentials = username + "\u0000" + username + "\u0000" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public boolean authorize(String username, String password) throws IOException {
        out.println("AUTH PLAIN " + encodeCredentials(username, password));
        String response = in.readLine(); // Receive authentication response from the server
        System.out.println(response);
        return response.equals("235 Authentication successful");
    }

    public void sendEmail(String sender, String recipient, String subject, String body) throws IOException {

        out.println("HELLO localhost");
        System.out.println(in.readLine()); // Receive welcome message

        out.println("From: " + sender);
        out.println("To: " + recipient);
        out.println("Subject: " + subject);
        out.println("Body: " + body);
        out.println(".");
        System.out.println(in.readLine()); // Receive DATA response
    }

    public static void main(String[] args) throws IOException {
        SMTPClient client = new SMTPClient();
        String sender = "sender@example.com";
        String recipient = "recipient@example.com";
        String subject = "Test Email";
        String body = "This is a test email sent via SMTP Client/Server program.";

        // Send email as usual
        client.sendEmail(sender, recipient, subject, body);
    }
}
