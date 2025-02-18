import javax.swing.*;
import java.awt.*;

public class CustomerInterface extends UserInterface {
    private String username;

    public CustomerInterface(BasicJDBC db, String username) {
        super(db);
        this.username = username;
    }

    @Override
    public void showGUI() {
        JFrame frame = new JFrame("Customer Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        // Modern UI Theme
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 14));
        UIManager.put("Button.background", new Color(70, 130, 180));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", new Color(240, 248, 255));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome, " + username, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 90));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        JButton manageCartButton = new JButton("Manage Cart");
        JButton manageAccountButton = new JButton("Manage Account");
        JButton logoutButton = new JButton("Logout");

        manageCartButton.addActionListener(e -> {
            try {
                CartManager cartManager = new CartManager(db, username);
                cartManager.showGUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error opening Cart Manager: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        manageAccountButton.addActionListener(e -> {
            try {
                AccountManager accountManager = new AccountManager(db, username);
                accountManager.showGUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error opening Account Manager: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        logoutButton.addActionListener(e -> logout(frame));

        buttonPanel.add(manageCartButton);
        buttonPanel.add(manageAccountButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    protected void logout(JFrame frame) {
        try {
            frame.dispose();
            LoginRegisterGUI loginRegister = new LoginRegisterGUI(db);
            loginRegister.showGUI();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error during logout: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
