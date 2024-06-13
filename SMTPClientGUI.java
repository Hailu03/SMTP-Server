import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SMTPClientGUI extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel backgroundLabel;
    private SMTPClient smtpClient;

    public SMTPClientGUI() throws IOException {
        setTitle("Login Page");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ImageIcon backgroundImage = new ImageIcon("background.jpg");
        backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(null);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");

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

        loginPanel.setBounds(150, 90, 320, 200);
        backgroundLabel.add(loginPanel);
        setContentPane(backgroundLabel);

        smtpClient = new SMTPClient();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (authenticate(username, password)) {
                showMessagePage(username);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean authenticate(String username, String password) {
        try {
            return smtpClient.authorize(username, password);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showMessagePage(String username) {
        JFrame sendMessageFrame = new JFrame("Send Message Page");
        sendMessageFrame.setSize(600, 400);
        sendMessageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sendMessageFrame.setLocationRelativeTo(null);

        JPanel sendMessagePanel = new JPanel(new BorderLayout());
        sendMessagePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel inputPanel = new JPanel(new BorderLayout()); // BorderLayout for input panel

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // GridLayout with 2 rows for fields, spacing of 10px between rows

        JTextField recipientField = new JTextField();
        JTextField subjectField = new JTextField();

        JTextArea messageArea = new JTextArea();
        messageArea.setPreferredSize(new Dimension(200, 200));

        fieldsPanel.add(createLabeledTextField("Recipient:", recipientField, 200, 20)); // Add recipient field
        fieldsPanel.add(createLabeledTextField("Subject:", subjectField, 200, 20)); // Add subject field // Add subject field

        // Add fields panel to input panel with vertical spacing
        inputPanel.add(fieldsPanel, BorderLayout.NORTH);
        inputPanel.add(Box.createVerticalStrut(40), BorderLayout.CENTER); // Add vertical space between fields and message area
        inputPanel.add(createLabeledTextArea("Message:", messageArea, 200, 170), BorderLayout.SOUTH); // Add message area

        // File attachment field
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton attachButton = new JButton("Attach Files");
        JLabel fileLabel = new JLabel("No files attached");
        List<File> attachments = new ArrayList<>();

        attachButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int result = fileChooser.showOpenDialog(sendMessageFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    attachments.add(file);
                }
                fileLabel.setText("Attached: " + attachments.size() + " files");
            }
        });

        // Change the attachmentPanel layout to BoxLayout
        JPanel attachmentPanel = new JPanel();
        attachmentPanel.setLayout(new BoxLayout(attachmentPanel, BoxLayout.Y_AXIS));

        // Add attach button and file label to the attachment panel
        attachmentPanel.add(attachButton);
        attachmentPanel.add(fileLabel);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(ev -> {
            String recipient = recipientField.getText();
            String subject = subjectField.getText();
            String message = messageArea.getText();

            try {
                smtpClient.sendEmail(username, recipient, subject, message, attachments);
                JOptionPane.showMessageDialog(sendMessageFrame, "Email sent successfully");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(sendMessageFrame, "Failed to send email", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        sendMessagePanel.add(inputPanel, BorderLayout.CENTER);
        buttonPanel.add(attachmentPanel);
        buttonPanel.add(submitButton);
        // Add the button panel to the sendMessagePanel
        sendMessagePanel.add(buttonPanel, BorderLayout.SOUTH);

        sendMessageFrame.add(sendMessagePanel);
        sendMessageFrame.setVisible(true);
    }

    private JPanel createLabeledTextField(String label, JTextField textField, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout(10, 1));
        JLabel jLabel = new JLabel(label);

        // Set the preferred size of the text field
        textField.setPreferredSize(new Dimension(width, height));

        panel.add(jLabel, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledTextArea(String label, JTextArea textArea, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        JLabel jLabel = new JLabel(label);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(width, height));

        // Adjust the height of the label area by specifying BorderLayout.NORTH
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);  // Keep the scroll pane in the center

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SMTPClientGUI gui = null;
            try {
                gui = new SMTPClientGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}
