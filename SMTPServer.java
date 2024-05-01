import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SMTPServer {
    private ServerSocket serverSocket;
    private Map<Integer, String> database;

    public SMTPServer() {
        database = new HashMap<>();
        try {
            serverSocket = new ServerSocket(6423);
            System.out.println("SMTP Server is listening on port 6423...");
        } catch (IOException e) {
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

//        out.println("220 Welcome to SMTP Server");

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            String[] command = inputLine.split(" ");
            System.out.println(inputLine);
            if (command[0].equals("HELLO")) {
                out.println("250 Hello");
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
            }
        }
    }


    private boolean authenticate(String authData) {
        // Dummy authentication for demonstration
        String decodedData = new String(Base64.getDecoder().decode(authData));
        String[] credentials = decodedData.split("\u0000");
        return credentials.length >= 3 && credentials[1].equals("sgvt@gmail.com") && credentials[2].equals("123");
    }

    private void saveEmail(String emailData) {
        // Dummy email saving for demonstration
        database.put(database.size() + 1, emailData);
        // print database
        for (Map.Entry<Integer, String> entry : database.entrySet()) {
            System.out.println("Email ID: " + entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    public static void main(String[] args) {
        SMTPServer server = new SMTPServer();
        server.start();
    }
}
