import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                try {
                    showMessagePage(username);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to fetch email history", "Error", JOptionPane.ERROR_MESSAGE);
                }
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

    private void showMessagePage(String username) throws SQLException {
        JFrame sendMessageFrame = new JFrame("Send Message Page");
        sendMessageFrame.setSize(600, 520);
        sendMessageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sendMessageFrame.setLocationRelativeTo(null);

        JPanel sendMessagePanel = new JPanel(new BorderLayout());
        sendMessagePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel inputPanel = new JPanel(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 1, 0, 10));

        JTextField recipientField = new JTextField();
        JTextField subjectField = new JTextField();

        JTextArea messageArea = new JTextArea();
        messageArea.setPreferredSize(new Dimension(200, 200));

        fieldsPanel.add(createLabeledTextField("Recipient:", recipientField, 200, 20));
        fieldsPanel.add(createLabeledTextField("Subject:", subjectField, 200, 20));

        inputPanel.add(fieldsPanel, BorderLayout.NORTH);
        inputPanel.add(Box.createVerticalStrut(40), BorderLayout.CENTER);
        inputPanel.add(createLabeledTextArea("Message:", messageArea, 200, 170), BorderLayout.SOUTH);

        // File attachment field
        JPanel attachmentPanel = new JPanel(new BorderLayout(10, 10));
        DefaultListModel<File> listModel = new DefaultListModel<>();
        JList<File> fileList = new JList<>(listModel);
        fileList.setVisibleRowCount(4);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(fileList);

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
                    listModel.addElement(file);
                }
                fileLabel.setText("Attached: " + attachments.size() + " files");
            }
        });

        JButton deleteButton = new JButton("Delete Selected File");
        deleteButton.addActionListener(e -> {
            int selectedIndex = fileList.getSelectedIndex();
            if (selectedIndex != -1) {
                attachments.remove(selectedIndex);
                listModel.remove(selectedIndex);
                fileLabel.setText("Attached: " + attachments.size() + " files");
            } else {
                JOptionPane.showMessageDialog(sendMessageFrame, "No file selected to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        attachmentPanel.add(attachButton, BorderLayout.NORTH);
        attachmentPanel.add(listScrollPane, BorderLayout.CENTER);
        attachmentPanel.add(deleteButton, BorderLayout.SOUTH);
        attachmentPanel.add(fileLabel, BorderLayout.EAST);

        JButton showHistoryButton = new JButton("Show Email History");
        showHistoryButton.addActionListener(ev -> {
            try {
                showEmailHistory(username);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(sendMessageFrame, "Failed to fetch email history", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(showHistoryButton);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(ev -> {
            String recipient = recipientField.getText();
            String subject = subjectField.getText();
            String message = messageArea.getText();

            try {
                smtpClient.sendEmail(username, recipient, subject, message, attachments);
                JOptionPane.showMessageDialog(sendMessageFrame, "Email sent successfully");

                recipientField.setText("");
                subjectField.setText("");
                messageArea.setText("");
                attachments.clear();
                listModel.clear();
                fileLabel.setText("No files attached");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(sendMessageFrame, "Failed to send email", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        sendMessagePanel.add(inputPanel, BorderLayout.CENTER);
        buttonPanel.add(submitButton);
        sendMessagePanel.add(buttonPanel, BorderLayout.NORTH);
        sendMessagePanel.add(attachmentPanel, BorderLayout.SOUTH);

        sendMessageFrame.add(sendMessagePanel);
        sendMessageFrame.setVisible(true);
    }

    private JPanel createLabeledTextField(String label, JTextField textField, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout(10, 1));
        JLabel jLabel = new JLabel(label);

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
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void showEmailHistory(String username) throws SQLException {
        ResultSet resultSet = smtpClient.getEmailHistory(username);
        JFrame historyFrame = new JFrame("Email History for " + username);
        historyFrame.setSize(600, 400);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.setLocationRelativeTo(null);
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JTable historyTable = new JTable(buildTableModel(resultSet));
        JScrollPane scrollPane = new JScrollPane(historyTable);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        historyFrame.add(historyPanel);
        historyFrame.setVisible(true);
    }

    private static javax.swing.table.DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        java.sql.ResultSetMetaData metaData = rs.getMetaData();
        java.util.Vector<String> columnNames = new java.util.Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }
        java.util.Vector<java.util.Vector<Object>> data = new java.util.Vector<>();
        while (rs.next()) {
            java.util.Vector<Object> vector = new java.util.Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
        return new javax.swing.table.DefaultTableModel(data, columnNames);
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
