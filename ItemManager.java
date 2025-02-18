import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;

public class ItemManager {
    private BasicJDBC db;

    public ItemManager(BasicJDBC db) {
        this.db = db;
    }

    public void showGUI() {
        JFrame frame = new JFrame("Item Manager");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 400);

        JPanel panel = new JPanel(new GridLayout(4, 1));
        JButton addButton = new JButton("Add Item");
        JButton updateButton = new JButton("Update Item Stock");
        JButton viewButton = new JButton("View Items");
        JButton deleteButton = new JButton("Delete Item");

        addButton.addActionListener(e -> {
            try {
                addItem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error adding item: " + ex.getMessage());
            }
        });

        updateButton.addActionListener(e -> {
            try {
                updateItemStock();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error updating item stock: " + ex.getMessage());
            }
        });

        viewButton.addActionListener(e -> {
            try {
                viewItems();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error viewing items: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            try {
                deleteItem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error deleting item: " + ex.getMessage());
            }
        });

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(viewButton);
        panel.add(deleteButton);

        frame.add(panel);
        frame.setVisible(true);
    }

    // Method to add an item along with its price
    private void addItem() {
        try {
            String itemName = JOptionPane.showInputDialog("Enter Item Name:");
            if (itemName == null || itemName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Item name cannot be empty.");
                return;
            }

            String itemStock = JOptionPane.showInputDialog("Enter Stock Quantity:");
            if (itemStock == null || itemStock.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Stock quantity cannot be empty.");
                return;
            }

            int stock = Integer.parseInt(itemStock);
            if (stock < 0) {
                JOptionPane.showMessageDialog(null, "Stock quantity must be a positive number.");
                return;
            }

            String itemPrice = JOptionPane.showInputDialog("Enter Item Price:");
            if (itemPrice == null || itemPrice.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Item price cannot be empty.");
                return;
            }

            double price = Double.parseDouble(itemPrice);
            if (price < 0) {
                JOptionPane.showMessageDialog(null, "Price must be a positive number.");
                return;
            }

            String query = "INSERT INTO items (name, stock, price) VALUES (?, ?, ?)";
            var pstmt = db.prepareStatement(query);
            pstmt.setString(1, itemName);
            pstmt.setInt(2, stock);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Item added successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid stock or price. Please enter valid numbers.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error adding item: " + ex.getMessage());
        }
    }

    // Method to view all items including their prices
    private void viewItems() {
        try {
            String query = "SELECT * FROM items";
            var rs = db.executeQuery(query);

            StringBuilder items = new StringBuilder("Items:\n");
            boolean hasItems = false;

            while (rs.next()) {
                hasItems = true;
                items.append("ID: ").append(rs.getInt("id"))
                     .append(", Name: ").append(rs.getString("name"))
                     .append(", Stock: ").append(rs.getInt("stock"))
                     .append(", Price: $").append(rs.getDouble("price"))
                     .append("\n");
            }

            if (!hasItems) {
                JOptionPane.showMessageDialog(null, "No items found in the system.");
            } else {
                JOptionPane.showMessageDialog(null, items.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error viewing items: " + ex.getMessage());
        }
    }

    // Method to update item stock and price
    private void updateItemStock() {
        try {
            viewItems();
            String itemID = JOptionPane.showInputDialog("Enter Item ID:");
            if (itemID == null || itemID.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Item ID cannot be empty.");
                return;
            }

            String newStock = JOptionPane.showInputDialog("Enter New Stock Quantity:");
            if (newStock == null || newStock.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "New stock quantity cannot be empty.");
                return;
            }

            int stock = Integer.parseInt(newStock);
            if (stock < 0) {
                JOptionPane.showMessageDialog(null, "Stock quantity must be a positive number.");
                return;
            }

            String newPrice = JOptionPane.showInputDialog("Enter New Item Price:");
            if (newPrice == null || newPrice.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "New item price cannot be empty.");
                return;
            }

            double price = Double.parseDouble(newPrice);
            if (price < 0) {
                JOptionPane.showMessageDialog(null, "Price must be a positive number.");
                return;
            }

            String query = "UPDATE items SET stock = ?, price = ? WHERE id = ?";
            var pstmt = db.prepareStatement(query);
            pstmt.setInt(1, stock);
            pstmt.setDouble(2, price);
            pstmt.setString(3, itemID);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Item stock and price updated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Item ID not found.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid stock or price. Please enter valid numbers.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error updating item stock: " + ex.getMessage());
        }
    }

    // Method to delete an item
    private void deleteItem() {
        try {
            String itemID = JOptionPane.showInputDialog("Enter Item ID to Delete:");
            if (itemID == null || itemID.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Item ID cannot be empty.");
                return;
            }

            String query = "DELETE FROM items WHERE id = ?";
            var pstmt = db.prepareStatement(query);
            pstmt.setString(1, itemID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Item deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Item ID not found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error deleting item: " + ex.getMessage());
        }
    }
}
