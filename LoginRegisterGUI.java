import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginRegisterGUI {
    private BasicJDBC db;

    public LoginRegisterGUI(BasicJDBC db) {
        this.db = db;
    }

    public void showGUI() {
        JFrame frame = new JFrame("Login/Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        // Modern UI Theme
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
        UIManager.put("Button.background", new Color(70, 130, 180));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", new Color(245, 245, 245));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome to the System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 90));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton exitButton = new JButton("Exit");

        loginButton.addActionListener(e -> {
            try {
                login(frame);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error during login: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            try {
                register();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error during registration: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> frame.dispose());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void login(JFrame frame) {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JPanel loginPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(frame, loginPanel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) return;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username and password cannot be empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = db.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");

                        JOptionPane.showMessageDialog(frame, "Login successful!");
                        frame.dispose();

                        if ("staff".equals(role)) {
                            new StaffInterface(db).showGUI();
                        } else if ("customer".equals(role)) {
                            new CustomerInterface(db, username).showGUI();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Unknown role: " + role,
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error during login: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();

        JPanel registerPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        registerPanel.add(new JLabel("Username:"));
        registerPanel.add(usernameField);
        registerPanel.add(new JLabel("Password:"));
        registerPanel.add(passwordField);
        registerPanel.add(new JLabel("Confirm Password:"));
        registerPanel.add(confirmPasswordField);
        registerPanel.add(new JLabel("Name:"));
        registerPanel.add(nameField);
        registerPanel.add(new JLabel("Address:"));
        registerPanel.add(addressField);

        int option = JOptionPane.showConfirmDialog(null, registerPanel, "Register",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) return;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields must be filled out.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String checkQuery = "SELECT 1 FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = db.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);

                try (ResultSet checkRs = checkStmt.executeQuery()) {
                    if (checkRs.next()) {
                        JOptionPane.showMessageDialog(null, "Username already exists. Please choose another.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String insertQuery = "INSERT INTO users (username, password, role, balance, name, address) VALUES (?, ?, 'customer', 0.00, ?, ?)";
            try (PreparedStatement pstmt = db.prepareStatement(insertQuery)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, address);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Registration successful!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error during registration: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
