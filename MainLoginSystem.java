import javax.swing.*;
import java.sql.ResultSet;

public class MainLoginSystem {
    private BasicJDBC db;
    private JFrame loginFrame;

    public MainLoginSystem(BasicJDBC db) {
        this.db = db;
        try {
            checkAndCreateRootUser(); // Check and initialize the root user
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Error during application initialization: " + e.getMessage(),
                "Initialization Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void checkAndCreateRootUser() {
        try {
            // Check if the root user exists
            String query = "SELECT 1 FROM users WHERE username = 'root'";
            ResultSet rs = db.executeQuery(query);

            if (!rs.next()) {
                // If the root user doesn't exist, create it
                String defaultPassword = "RootUser!234"; // Default password for root
                if (!PasswordUtil.isValidPassword(defaultPassword)) {
                    throw new IllegalArgumentException("Default password does not meet the requirements.");
                }

                // Hash the password before storing it
                String hashedPassword = PasswordUtil.hashPassword(defaultPassword);

                // Insert the root user with the hashed password
                String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                var pstmt = db.prepareStatement(insertQuery);
                pstmt.setString(1, "root"); // Root username
                pstmt.setString(2, defaultPassword); // Hashed password
                pstmt.setString(3, "staff"); // Role as 'staff'
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(
                    null,
                    "Root user created successfully with default password: " + defaultPassword,
                    "Root User Initialization",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing root user: " + e.getMessage(), e);
        }
    }

    public void showLogin() {
        loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Username and password cannot be empty.",
                        "Input Error",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (authenticateUser(username, password)) {
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    loginFrame.dispose();

                    // Determine user role and open appropriate interface
                    String role = getUserRole(username);
                    if ("staff".equals(role)) {
                        new ItemManager(db).showGUI(); // Open staff interface
                    } else if ("customer".equals(role)) {
                        new CustomerInterface(db, username).showGUI(); // Open customer interface
                    } else {
                        JOptionPane.showMessageDialog(
                            null,
                            "Unknown role: " + role,
                            "Role Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } else {
                    JOptionPane.showMessageDialog(
                        null,
                        "Invalid username or password.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    null,
                    "An error occurred during login: " + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace(); // Log error for debugging
            }
        });

        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    private boolean authenticateUser(String username, String password) {
        try {
            String query = "SELECT password FROM users WHERE username = ?";
            var pstmt = db.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                return PasswordUtil.verifyPassword(password, hashedPassword);
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "User not found.",
                    "Authentication Error",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Error authenticating user: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
        return false;
    }

    private String getUserRole(String username) {
        try {
            String query = "SELECT role FROM users WHERE username = ?";
            var pstmt = db.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Error retrieving user role: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
        return null;
    }
}
