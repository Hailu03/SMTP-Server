import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SMTPClientGUI extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel backgroundLabel;
    private SMTPClient smtpClient;

    public SMTPClientGUI() {
        setTitle("Login Page");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Load background image
        ImageIcon backgroundImage = new ImageIcon("background.jpg");
        backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(null); // Set layout to null for manual positioning

        // Create login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setOpaque(true); // Make panel transparent

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");

        // Set background color to make components visible
        usernameField.setBackground(Color.WHITE);
        passwordField.setBackground(Color.WHITE);
        loginButton.setBackground(Color.WHITE);

        loginButton.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        // Position the login panel manually within the background label
        loginPanel.setBounds(150, 90, 320, 200);

        // Add login panel to the background label
        backgroundLabel.add(loginPanel);

        // Set the background label as the content pane
        setContentPane(backgroundLabel);

        // Initialize SMTPClient
        smtpClient = new SMTPClient();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);

            // Dummy authentication for demonstration
            if (authenticate(username, password)) {
                // If authentication succeeds, show send message page
                System.out.println("Login successful");
                showMessagePage(username);

                // Kill the login page
                this.dispose();
            } else {
                // If authentication fails, show error message
                System.out.println("Login failed");
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean authenticate(String username, String password) {
        // Communicate with the server for authentication
        try {
            // Attempt to authenticate with the server
            return smtpClient.authorize(username, password);
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Authentication failed due to communication error
        }
    }

    private void showMessagePage(String username) {
        // Create a new JFrame for the send message page
        JFrame sendMessageFrame = new JFrame("Send Message Page");
        sendMessageFrame.setSize(600, 400);
        sendMessageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sendMessageFrame.setLocationRelativeTo(null); // Center the window

        // Create send message panel
        JPanel sendMessagePanel = new JPanel(new BorderLayout());
        sendMessagePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Create panel for input fields
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        // Add components for recipient, subject, and message
        JTextField recipientField = new JTextField();
        JTextField subjectField = new JTextField();
        recipientField.setPreferredSize(new Dimension(200, 10)); // Set preferred size for recipient field
        subjectField.setPreferredSize(new Dimension(200, 10)); // Set preferred size for subject field

        JTextArea messageArea = new JTextArea();

        messageArea.setRows(10); // Set number of visible rows for the message area
        // Set line wrap and wrap style properties for the message area
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        // Customize input fields
        recipientField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        subjectField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Add components for recipient, subject, and message with custom sizes
        inputPanel.add(createLabeledTextField("Recipient:", recipientField, 200, 10)); // Adjust width and height as needed
        inputPanel.add(createLabeledTextField("Subject:", subjectField, 200, 10)); // Adjust width and height as needed
        inputPanel.add(createLabeledTextArea("Message:", messageArea));

        // Create submit button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // Get recipient, subject, and message
            String recipient = recipientField.getText();
            String subject = subjectField.getText();
            String message = messageArea.getText();

            // Send email
            try {
                smtpClient.sendEmail(username, recipient, subject, message);
                JOptionPane.showMessageDialog(sendMessageFrame, "Email sent successfully");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(sendMessageFrame, "Failed to send email", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // if user closes the send message page, close the login page as well
        sendMessageFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // close the socket connection
                smtpClient.out.println("QUIT");
                System.exit(0);
            }
        });

        // Add input panel and submit button to the main panel
        sendMessagePanel.add(inputPanel, BorderLayout.CENTER);
        sendMessagePanel.add(submitButton, BorderLayout.SOUTH);

        // Set content pane and make the frame visiblerec
        sendMessageFrame.setContentPane(sendMessagePanel);
        sendMessageFrame.setVisible(true);
    }

    // Modify createLabeledTextField method to set preferred sizes
    private JPanel createLabeledTextField(String labelText, JTextField textField, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        textField.setPreferredSize(new Dimension(width, height)); // Set preferred size
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledTextArea(String labelText, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SMTPClientGUI loginFrame = new SMTPClientGUI();
            loginFrame.setVisible(true);
        });
    }
}
