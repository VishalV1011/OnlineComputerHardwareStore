import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class StaffInterface extends UserInterface {

    public StaffInterface(BasicJDBC db) {
        super(db);
    }

    @Override
    public void showGUI() {
        JFrame frame = new JFrame("Staff Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Modern UI Theme
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 14));
        UIManager.put("Button.background", new Color(70, 130, 180));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", new Color(240, 248, 255));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Staff Interface", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 90));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        JButton manageItemsButton = new JButton("Manage Items");
        JButton viewPaymentsButton = new JButton("View Payments");
        JButton viewPaymentsArchiveButton = new JButton("View Payments Archive");
        JButton logoutButton = new JButton("Logout");

        manageItemsButton.addActionListener(e -> {
            try {
                ItemManager itemManager = new ItemManager(db);
                itemManager.showGUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error initializing Item Manager: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewPaymentsButton.addActionListener(e -> {
            try {
                displayPaymentsTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error fetching payments: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewPaymentsArchiveButton.addActionListener(e -> {
            try {
                displayPaymentsArchiveTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error fetching payments archive: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        logoutButton.addActionListener(e -> {
            try {
                frame.dispose();
                LoginRegisterGUI loginRegister = new LoginRegisterGUI(db);
                loginRegister.showGUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error during logout: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // Log the exception for debugging purposes
            }
        });

        buttonPanel.add(manageItemsButton);
        buttonPanel.add(viewPaymentsButton);
        buttonPanel.add(viewPaymentsArchiveButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void displayPaymentsTable() throws SQLException {
        JFrame frame = new JFrame("Payments");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Header with a checkbox and title
        JPanel topPanel = new JPanel(new BorderLayout());
        JCheckBox showAllCheckbox = new JCheckBox("Show All Payments");
        showAllCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(showAllCheckbox, BorderLayout.WEST);
        JLabel headerLabel = new JLabel("Payments Table", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        topPanel.add(headerLabel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Table for displaying data
        String[] columnNames = {"Payment ID", "Name", "Address", "Amount", "Payment Date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Editable only for the "Status" column
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add combo box editor for the Status column
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Pending", "Completed"});
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(statusComboBox));

        // Save button at the bottom
        JButton saveButton = new JButton("Save Changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data into the table
        Runnable loadTableData = () -> {
            try {
                String query = showAllCheckbox.isSelected()
                        ? "SELECT * FROM payments"
                        : "SELECT * FROM payments WHERE status = 'Pending'";
                var stmt = db.prepareStatement(query);
                var rs = stmt.executeQuery();

                tableModel.setRowCount(0);

                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getDouble("amount"),
                            rs.getDate("payment_date"),
                            rs.getString("status")
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error fetching payments: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        loadTableData.run();
        showAllCheckbox.addActionListener(e -> loadTableData.run());

        saveButton.addActionListener(e -> {
            try {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int id = (int) tableModel.getValueAt(i, 0);
                    String status = (String) tableModel.getValueAt(i, 5);

                    String updateQuery = "UPDATE payments SET status = ? WHERE id = ?";
                    var stmt = db.prepareStatement(updateQuery);
                    stmt.setString(1, status);
                    stmt.setInt(2, id);
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(frame, "Changes saved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTableData.run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving changes: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private void displayPaymentsArchiveTable() throws SQLException {
        JFrame frame = new JFrame("Payments Archive");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        String query = "SELECT * FROM payments_archive";
        var stmt = db.prepareStatement(query);
        var rs = stmt.executeQuery();

        String[] columnNames = {"Payment ID", "Username", "Amount", "Payment Date"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("amount"),
                    rs.getDate("payment_date")
            };
            tableModel.addRow(row);
        }

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        frame.add(scrollPane);
        frame.setVisible(true);
    }
}
