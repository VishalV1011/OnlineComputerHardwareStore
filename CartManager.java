import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartManager {
    private BasicJDBC db;
    private String username;
    private static JFrame instance; // Singleton to prevent multiple windows

    public CartManager(BasicJDBC db, String username) {
        this.db = db;
        this.username = username;
    }

    public void showGUI() {
        if (instance != null) {
            instance.toFront();
            return;
        }

        instance = new JFrame("Cart Manager");
        instance.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        instance.setSize(500, 400);

        instance.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                instance = null;
            }
        });

        JPanel panel = new JPanel(new GridLayout(4, 1));
        JButton listButton = new JButton("List All Items");
        JButton addButton = new JButton("Add Item to Cart");
        JButton viewButton = new JButton("View Items in Cart");
        JButton removeButton = new JButton("Remove Item from Cart");
        JButton checkoutButton = new JButton("Checkout");
        JButton backButton = new JButton("Back");

        listButton.addActionListener(e -> listAllItems());
        addButton.addActionListener(e -> addItemToCart());
        viewButton.addActionListener(e -> viewCart());
        removeButton.addActionListener(e -> removeItemFromCart());
        checkoutButton.addActionListener(e -> checkout());
        backButton.addActionListener(e -> goBack());

        panel.add(listButton);
        panel.add(addButton);
        panel.add(viewButton);
        panel.add(removeButton);
        panel.add(checkoutButton);
        panel.add(backButton);

        instance.add(panel);
        instance.setVisible(true);
    }
    
    private void listAllItems() {
        try {
            String query = "SELECT id, name, stock, price FROM items"; // Query to fetch items
            var stmt = db.prepareStatement(query);
            var rs = stmt.executeQuery();

            // Column names for the item table
            String[] columnNames = {"Item ID", "Name", "Stock", "Price"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

            // Add rows to the table model
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),        // Item ID
                    rs.getString("name"),   // Item Name
                    rs.getInt("stock"),     // Item Stock
                    rs.getDouble("price")   // Item Price
                };
                tableModel.addRow(row);
            }

            // Display the table in a JScrollPane
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            JOptionPane.showMessageDialog(null, scrollPane, "Available Items", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching items: " + e.getMessage());
        }
    }

    
    
    private void viewCart() {
        try {
            String query = """
                SELECT items.id, items.name, cart.quantity, items.price 
                FROM cart 
                JOIN items ON cart.item_id = items.id 
                WHERE cart.username = ?
            """;
            var pstmt = db.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder cartContents = new StringBuilder("Items in your cart:\n");
            boolean hasItems = false;
            double total = 0.0;

            while (rs.next()) {
                hasItems = true;
                int itemId = rs.getInt("id");
                String itemName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double itemTotal = price * quantity;

                cartContents.append("Item ID: ").append(itemId)
                        .append(", Name: ").append(itemName)
                        .append(", Price: $").append(price)
                        .append(", Quantity: ").append(quantity)
                        .append(", Total: $").append(itemTotal)
                        .append("\n");

                total += itemTotal;
            }

            if (!hasItems) {
                JOptionPane.showMessageDialog(null, "Your cart is empty.");
            } else {
                cartContents.append("\nTotal Price: $").append(total);
                JOptionPane.showMessageDialog(null, cartContents.toString());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error viewing cart: " + e.getMessage());
        }
    }

    private void addItemToCart() {
        try {
            String itemID = JOptionPane.showInputDialog("Enter the Item ID to Add:");
            if (itemID == null || itemID.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Item ID cannot be empty.");
                return;
            }

            String checkItemQuery = "SELECT name, price, stock FROM items WHERE id = ?";
            var pstmt = db.prepareStatement(checkItemQuery);
            pstmt.setString(1, itemID);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Item ID not found.");
                return;
            }

            String itemName = rs.getString("name");
            double price = rs.getDouble("price");
            int stock = rs.getInt("stock");

            if (stock <= 0) {
                JOptionPane.showMessageDialog(null, "Item is out of stock.");
                return;
            }

            String quantityStr = JOptionPane.showInputDialog("Enter the quantity of " + itemName + " to add (Available stock: " + stock + "):");
            if (quantityStr == null || quantityStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Quantity cannot be empty.");
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null, "Invalid quantity.");
                return;
            }

            // Check if the entered quantity exceeds available stock
            if (quantity > stock) {
                JOptionPane.showMessageDialog(null, "Only " + stock + " items are available in stock. Adjusting quantity to " + stock + ".");
                quantity = stock; // Adjust the quantity to the available stock
            }

            // Insert item into the cart, including the price
            String addItemQuery = "INSERT INTO cart (username, item_id, quantity, price) VALUES (?, ?, ?, ?) " +
                                  "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
            var addStmt = db.prepareStatement(addItemQuery);
            addStmt.setString(1, username);
            addStmt.setInt(2, Integer.parseInt(itemID));
            addStmt.setInt(3, quantity);
            addStmt.setDouble(4, price);  // Include the price when adding the item
            addStmt.setInt(5, quantity);
            addStmt.executeUpdate();

            // Update stock for the item
//            String updateStockQuery = "UPDATE items SET stock = stock - ? WHERE id = ?";
//            var updateStmt = db.prepareStatement(updateStockQuery);
//            updateStmt.setInt(1, quantity);
//            updateStmt.setInt(2, Integer.parseInt(itemID));
//            updateStmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Item added to cart successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error adding item to cart: " + e.getMessage());
        }
    }


    private void removeItemFromCart() {
        try {
            // Ask for the Item ID
            String itemID = JOptionPane.showInputDialog("Enter the Item ID to Remove:");
            if (itemID == null || itemID.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Item ID cannot be empty.");
                return;
            }

            // Fetch the quantity of the item in the cart
            String quantityQuery = "SELECT quantity FROM cart WHERE username = ? AND item_id = ?";
            var qtyStmt = db.prepareStatement(quantityQuery);
            qtyStmt.setString(1, username);
            qtyStmt.setInt(2, Integer.parseInt(itemID));
            ResultSet rs = qtyStmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Item not found in your cart.");
                return;
            }

            int currentQuantity = rs.getInt("quantity");

            // Ask for the quantity to remove
            String quantityToRemoveStr = JOptionPane.showInputDialog(
                "Current quantity of Item ID " + itemID + " in your cart: " + currentQuantity +
                "\nEnter the quantity to remove:"
            );

            if (quantityToRemoveStr == null || quantityToRemoveStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Quantity to remove cannot be empty.");
                return;
            }

            int quantityToRemove = Integer.parseInt(quantityToRemoveStr);

            if (quantityToRemove <= 0) {
                JOptionPane.showMessageDialog(null, "Quantity to remove must be greater than zero.");
                return;
            }

            if (quantityToRemove > currentQuantity) {
                JOptionPane.showMessageDialog(null, "Cannot remove more than the current quantity in the cart.");
                return;
            }

            // Update or delete the item based on the remaining quantity
            if (quantityToRemove == currentQuantity) {
                // Remove the item entirely from the cart
                String deleteQuery = "DELETE FROM cart WHERE username = ? AND item_id = ?";
                var deleteStmt = db.prepareStatement(deleteQuery);
                deleteStmt.setString(1, username);
                deleteStmt.setInt(2, Integer.parseInt(itemID));
                deleteStmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Item removed from cart successfully!");
            } else {
                // Update the quantity in the cart
                String updateQuery = "UPDATE cart SET quantity = quantity - ? WHERE username = ? AND item_id = ?";
                var updateStmt = db.prepareStatement(updateQuery);
                updateStmt.setInt(1, quantityToRemove);
                updateStmt.setString(2, username);
                updateStmt.setInt(3, Integer.parseInt(itemID));
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Removed " + quantityToRemove + " items successfully!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error removing item from cart: " + e.getMessage());
        }
    }


    private void checkout() {
        try {
            // Fetch the user's balance, name, and address
            String userQuery = "SELECT balance, name, address FROM users WHERE username = ?";
            var userStmt = db.prepareStatement(userQuery);
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) {
                JOptionPane.showMessageDialog(null, "User not found. Cannot proceed to checkout.");
                return;
            }

            double userBalance = userRs.getDouble("balance");
            String userName = userRs.getString("name");
            String userAddress = userRs.getString("address");

            // Fetch cart items and calculate total price while processing
            String cartQuery = """
                SELECT cart.item_id, items.name, cart.quantity, items.price, items.stock 
                FROM cart 
                JOIN items ON cart.item_id = items.id 
                WHERE cart.username = ?
            """;
            var cartStmt = db.prepareStatement(cartQuery);
            cartStmt.setString(1, username);
            ResultSet cartRs = cartStmt.executeQuery();

            StringBuilder receipt = new StringBuilder("Checkout Summary:\n\n");
            double totalPrice = 0.0;
            boolean hasItems = false;

            // Temporary storage for stock updates
            java.util.List<int[]> updates = new java.util.ArrayList<>();

            while (cartRs.next()) {
                hasItems = true;
                int itemId = cartRs.getInt("item_id");
                String itemName = cartRs.getString("name");
                int quantity = cartRs.getInt("quantity");
                double price = cartRs.getDouble("price");
                double itemTotal = price * quantity;

                totalPrice += itemTotal;

                // Add stock update info to list
                updates.add(new int[]{itemId, quantity});

                receipt.append("Item: ").append(itemName)
                       .append(", Quantity: ").append(quantity)
                       .append(", Price: $").append(price)
                       .append(", Total: $").append(itemTotal).append("\n");
            }

            if (!hasItems) {
                JOptionPane.showMessageDialog(null, "Your cart is empty. Cannot proceed to checkout.");
                return;
            }

            receipt.append("\nTotal Price: $").append(totalPrice);

            // Show confirmation dialog
            int confirmation = JOptionPane.showConfirmDialog(null, receipt.toString() + "\n\nDo you want to confirm checkout?", "Confirm Checkout", JOptionPane.YES_NO_OPTION);

            if (confirmation != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Checkout canceled.");
                return;
            }

            // Check if the user has sufficient balance
            if (totalPrice > userBalance) {
                JOptionPane.showMessageDialog(null, "Insufficient balance. Please add funds or remove items from your cart.");
                return;
            }

            // Deduct total price from the user's balance
            String updateBalanceQuery = "UPDATE users SET balance = balance - ? WHERE username = ?";
            var updateBalanceStmt = db.prepareStatement(updateBalanceQuery);
            updateBalanceStmt.setDouble(1, totalPrice);
            updateBalanceStmt.setString(2, username);
            updateBalanceStmt.executeUpdate();

            // Update stock for items
            for (int[] update : updates) {
                String updateStockQuery = "UPDATE items SET stock = stock - ? WHERE id = ?";
                var updateStockStmt = db.prepareStatement(updateStockQuery);
                updateStockStmt.setInt(1, update[1]);
                updateStockStmt.setInt(2, update[0]);
                updateStockStmt.executeUpdate();
            }

            // Insert payment record into the payments table with name and address
            String insertPaymentQuery = "INSERT INTO payments (username, name, address, amount, payment_date, status) VALUES (?, ?, ?, ?, CURDATE(), ?)";
            var insertPaymentStmt = db.prepareStatement(insertPaymentQuery);
            insertPaymentStmt.setString(1, username);
            insertPaymentStmt.setString(2, userName);
            insertPaymentStmt.setString(3, userAddress);
            insertPaymentStmt.setDouble(4, totalPrice);
            insertPaymentStmt.setString(5,  "Pending");
            insertPaymentStmt.executeUpdate();

            // Clear the cart
            String clearCartQuery = "DELETE FROM cart WHERE username = ?";
            var clearCartStmt = db.prepareStatement(clearCartQuery);
            clearCartStmt.setString(1, username);
            clearCartStmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Checkout complete!\nThank you for your purchase.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error during checkout: " + e.getMessage());
        }
    }



    
    private void goBack() {
        if (instance != null) {
            instance.dispose(); // Closes the window
            instance = null;    // Resets the singleton reference
        }
    }


}
