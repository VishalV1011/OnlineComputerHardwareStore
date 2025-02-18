import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AccountManager {
    private BasicJDBC db;
    private String username;

    public AccountManager(BasicJDBC db, String username) {
        this.db = db;
        this.username = username;
    }

    public void showGUI() {
        JFrame frame = new JFrame("Account Manager");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(4, 1));
        JButton updateNameButton = new JButton("Update Name");
        JButton updateAddressButton = new JButton("Update Address");
        JButton reloadBalanceButton = new JButton("Reload Balance");
        JButton viewDetailsButton = new JButton("View Account Details");
        JButton deleteAccountButton = new JButton("Delete Account");
        JButton backButton = new JButton("Back");

        updateNameButton.addActionListener(e -> updateName());
        updateAddressButton.addActionListener(e -> updateAddress());
        reloadBalanceButton.addActionListener(e -> reloadBalance());
        viewDetailsButton.addActionListener(e -> viewAccountDetails());
        deleteAccountButton.addActionListener(e -> deleteAccount(frame));
        backButton.addActionListener(e -> goBack(frame));
        

        panel.add(updateNameButton);
        panel.add(updateAddressButton);
        panel.add(reloadBalanceButton);
        panel.add(viewDetailsButton);
        panel.add(deleteAccountButton);
        panel.add(backButton);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void updateName() {
        String newName = JOptionPane.showInputDialog("Enter New Name:");

        if (newName == null || newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Name cannot be empty.");
            return;
        }

        try (PreparedStatement pstmt = db.prepareStatement("UPDATE users SET name = ? WHERE username = ?")) {
            pstmt.setString(1, newName.trim());
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, rowsAffected > 0 ? 
                "Name updated successfully!" : "Failed to update name. Please try again.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error updating name: " + e.getMessage());
        }
    }

    private void updateAddress() {
        String newAddress = JOptionPane.showInputDialog("Enter New Address:");

        if (newAddress == null || newAddress.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Address cannot be empty.");
            return;
        }

        try (PreparedStatement pstmt = db.prepareStatement("UPDATE users SET address = ? WHERE username = ?")) {
            pstmt.setString(1, newAddress.trim());
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, rowsAffected > 0 ? 
                "Address updated successfully!" : "Failed to update address. Please try again.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error updating address: " + e.getMessage());
        }
    }
    
    private void reloadBalance() {
        String balanceInput = JOptionPane.showInputDialog("Enter Reload Amount:");
        
        if (balanceInput == null || balanceInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Reload Amount cannot be empty.");
            return;
        }
        
        try {
            double reloadAmount = Double.parseDouble(balanceInput.trim());
            
            if (reloadAmount <= 0) {
                JOptionPane.showMessageDialog(null, "Reload Amount must be greater than 0.");
                return;
            }
            
            // Retrieve current balance from the database
            double currentBalance = 0.0;
            try (PreparedStatement pstmtSelect = db.prepareStatement("SELECT balance FROM users WHERE username = ?")) {
                pstmtSelect.setString(1, username);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getDouble("balance");
                    } else {
                        JOptionPane.showMessageDialog(null, "User not found.");
                        return;
                    }
                }
            }
            
            // Calculate the new balance
            double newBalance = currentBalance + reloadAmount;
            
            // Update the balance in the database
            try (PreparedStatement pstmtUpdate = db.prepareStatement("UPDATE users SET balance = ? WHERE username = ?")) {
                pstmtUpdate.setDouble(1, newBalance);
                pstmtUpdate.setString(2, username);
                
                int rowsAffected = pstmtUpdate.executeUpdate();
                JOptionPane.showMessageDialog(null, rowsAffected > 0 ? 
                    "Balance reloaded successfully! New Balance: " + newBalance : 
                    "Failed to reload balance. Please try again.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid numeric value.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reloading balance: " + e.getMessage());
        }
    }


    private void viewAccountDetails() {
        try (PreparedStatement pstmt = db.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String details = "Username: " + rs.getString("username") +
                            "\nName: " + rs.getString("name") +
                            "\nAddress: " + rs.getString("address") +
                            "\nBalance: " + rs.getString("balance");
                    JOptionPane.showMessageDialog(null, details);
                } else {
                    JOptionPane.showMessageDialog(null, "Account not found. Please try again.");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error retrieving account details: " + e.getMessage());
        }
    }

    private void deleteAccount(JFrame frame) {
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete your account?");
        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement pstmt = db.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Account deleted successfully! Logging out...");
                    frame.dispose();
                    System.exit(0); // Terminate the application
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete account. Please try again.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error deleting account: " + e.getMessage());
            }
        }
    }
    
    private void goBack(JFrame frame) {
        if (frame != null) {
            frame.dispose(); // Close the window
        }
    }
    
    
}
