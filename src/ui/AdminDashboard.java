package ui;

import com.sun.tools.javac.Main;
import database.DatabaseHandler;
import database.Logger;
import database.ReportGenerator;
import models.Admin;
import models.MenuItem;
import models.Order;
import models.Staff;
import models.User;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import java.time.LocalDateTime;
import java.io.FileWriter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JPanel {
    private JFrame parentFrame;
    private Admin admin;
    private JTabbedPane tabbedPane;
    private Runnable logoutCallback;

    private List<MenuItem> menuItems;
    private List<Order> orders;
    private List<User> users;

    public AdminDashboard(JFrame parentFrame, Admin admin, Runnable logoutCallback) {
        this.parentFrame = parentFrame;
        this.admin = admin;

        // Load data
        this.menuItems = DatabaseHandler.loadMenuItems();
        this.orders = DatabaseHandler.loadOrders();
        this.users = DatabaseHandler.loadUsers();
        this.logoutCallback = logoutCallback;

        // Set layout
        setLayout(new BorderLayout());

        // Create welcome panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getUsername() + " (Admin)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(logoutButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Menu Management", createMenuPanel());
        tabbedPane.addTab("User Management", createUserPanel());
        tabbedPane.addTab("Order History", createOrderHistoryPanel());
        tabbedPane.add("Reports", createReportPanel());
        tabbedPane.addTab("Log History", createLogPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }



// This is a partial update for the AdminDashboard.java file to add stock management
// Replace the existing createMenuPanel() and related methods with these versions

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model - Updated to include stock column
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Price", "Category", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate table model
        for (MenuItem item : menuItems) {
            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getPrice(),
                    item.getCategory(),
                    item.getStock()
            });
        }

        // Create table
        JTable menuTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Item");
        JButton editButton = new JButton("Edit Item");
        JButton deleteButton = new JButton("Delete Item");
        JButton updateStockButton = new JButton("Update Stock"); // New button for stock

        addButton.addActionListener(e -> addMenuItem());
        editButton.addActionListener(e -> {
            int selectedRow = menuTable.getSelectedRow();
            if (selectedRow != -1) {
                editMenuItem(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to edit",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = menuTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteMenuItem(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to delete",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        updateStockButton.addActionListener(e -> {
            int selectedRow = menuTable.getSelectedRow();
            if (selectedRow != -1) {
                updateItemStock(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to update stock",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateStockButton); // Add the new button
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addMenuItem() {
        JTextField idField = new JTextField(10);
        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(10);
        JTextField categoryField = new JTextField(15);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1)); // Stock spinner

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Initial Stock:"));
        panel.add(stockSpinner); // Add stock field

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Menu Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                String category = categoryField.getText().trim();
                int stock = (int) stockSpinner.getValue(); // Get stock value

                if (id.isEmpty() || name.isEmpty() || category.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check for duplicate ID
                boolean duplicateId = menuItems.stream()
                        .anyMatch(item -> item.getId().equals(id));
                if (duplicateId) {
                    JOptionPane.showMessageDialog(this, "Item ID already exists",
                            "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                MenuItem newItem = new MenuItem(id, name, price, category, stock); // Create with stock
                menuItems.add(newItem);
                DatabaseHandler.saveMenuItems(menuItems);

                // Refresh menu tab
                tabbedPane.setComponentAt(0, createMenuPanel());

                JOptionPane.showMessageDialog(this, "Menu item added successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price format",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editMenuItem(int selectedRow) {
        MenuItem item = menuItems.get(selectedRow);

        JTextField idField = new JTextField(item.getId(), 10);
        idField.setEditable(false);
        JTextField nameField = new JTextField(item.getName(), 20);
        JTextField priceField = new JTextField(String.valueOf(item.getPrice()), 10);
        JTextField categoryField = new JTextField(item.getCategory(), 15);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(item.getStock(), 0, 1000, 1)); // Stock spinner with current value

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockSpinner); // Add stock field

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Menu Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                String category = categoryField.getText().trim();
                int stock = (int) stockSpinner.getValue(); // Get stock value

                if (name.isEmpty() || category.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                item.setName(name);
                item.setPrice(price);
                item.setCategory(category);
                item.setStock(stock); // Update stock

                DatabaseHandler.saveMenuItems(menuItems);

                // Refresh menu tab
                tabbedPane.setComponentAt(0, createMenuPanel());

                JOptionPane.showMessageDialog(this, "Menu item updated successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price format",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // New method to update stock only
    private void updateItemStock(int selectedRow) {
        MenuItem item = menuItems.get(selectedRow);

        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(item.getStock(), 0, 1000, 1));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Update Stock for: " + item.getName()));
        panel.add(new JLabel("Current Stock: " + item.getStock()));
        panel.add(new JLabel("New Stock:"));
        panel.add(stockSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Stock",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int newStock = (int) stockSpinner.getValue();
            item.setStock(newStock);
            DatabaseHandler.saveMenuItems(menuItems);

            // Refresh menu tab
            tabbedPane.setComponentAt(0, createMenuPanel());

            JOptionPane.showMessageDialog(this, "Stock updated successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteMenuItem(int selectedRow) {
        MenuItem item = menuItems.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the item: " + item.getName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            menuItems.remove(selectedRow);
            DatabaseHandler.saveMenuItems(menuItems);

            // Refresh menu tab
            tabbedPane.setComponentAt(0, createMenuPanel());

            JOptionPane.showMessageDialog(this, "Menu item deleted successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate table model
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getUsername(),
                    user.getRole()
            });
        }

        // Create table
        JTable userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add User");
        JButton resetPasswordButton = new JButton("Reset Password");
        JButton deleteButton = new JButton("Delete User");

        addButton.addActionListener(e -> addUser());
        resetPasswordButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                resetPassword(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteUser(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(resetPasswordButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addUser() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN", "STAFF"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check for duplicate username
            boolean duplicateUsername = users.stream()
                    .anyMatch(user -> user.getUsername().equals(username));
            if (duplicateUsername) {
                JOptionPane.showMessageDialog(this, "Username already exists",
                        "Duplicate Username", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser;
            if ("ADMIN".equals(role)) {
                newUser = new Admin(username, password);
            } else {
                newUser = new Staff(username, password);
            }

            users.add(newUser);
            DatabaseHandler.saveUsers(users);

            // Refresh user tab
            tabbedPane.setComponentAt(1, createUserPanel());

            JOptionPane.showMessageDialog(this, "User added successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetPassword(int selectedRow) {
        User user = users.get(selectedRow);

        JPasswordField passwordField = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("New Password for " + user.getUsername() + ":"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Reset Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(passwordField.getPassword());

            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            user.setPassword(newPassword);
            DatabaseHandler.saveUsers(users);

            JOptionPane.showMessageDialog(this, "Password reset successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteUser(int selectedRow) {
        User user = users.get(selectedRow);

        // Prevent deleting the current admin
        if (user.getUsername().equals(admin.getUsername())) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account",
                    "Operation Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the user: " + user.getUsername() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            users.remove(selectedRow);
            DatabaseHandler.saveUsers(users);

            // Refresh user tab
            tabbedPane.setComponentAt(1, createUserPanel());

            JOptionPane.showMessageDialog(this, "User deleted successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Order ID", "Customer", "Table", "Staff", "Items", "Total", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate table model
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Order order : orders) {
            String itemsStr = order.getOrderItems().size() + " items";
            double total = order.calculateTotal();

            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getTableNumber(),
                    order.getStaffName(),
                    itemsStr,
                    String.format("$%.2f", total),
                    order.getStatus()
            });
        }

        // Create table
        JTable orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton viewDetailsButton = new JButton("View Details");

        viewDetailsButton.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                viewOrderDetails(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(viewDetailsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void viewOrderDetails(int selectedRow) {
        Order order = orders.get(selectedRow);

        // Create details panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Order ID: " + order.getOrderId()));
        panel.add(new JLabel("Customer: " + order.getCustomerName()));
        panel.add(new JLabel("Table: " + order.getTableNumber()));
        panel.add(new JLabel("Staff: " + order.getStaffName()));
        panel.add(new JLabel("Status: " + order.getStatus()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Items:"));

        // Create table for order items
        DefaultTableModel itemTableModel = new DefaultTableModel(
                new Object[]{"Item", "Price", "Quantity", "Total"}, 0);

        double orderTotal = 0;
        for (models.OrderItem item : order.getOrderItems()) {
            double itemTotal = item.getPrice() * item.getQuantity();
            orderTotal += itemTotal;

            itemTableModel.addRow(new Object[]{
                    item.getItemName(),
                    String.format("$%.2f", item.getPrice()),
                    item.getQuantity(),
                    String.format("$%.2f", itemTotal)
            });
        }

        JTable itemTable = new JTable(itemTableModel);
        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        panel.add(scrollPane);

        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Total: $" + String.format("%.2f", orderTotal)));

        // Show dialog
        JOptionPane.showMessageDialog(this, panel, "Order Details", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10)); // 3 rows, 2 columns, padding

        // Load orders from file
        List<Order> orders = ReportGenerator.loadOrdersFromFile("files/orders.txt");

        // Get values
        double totalRevenue = ReportGenerator.calculateTotalRevenue(orders);
        double monthlyIncome = ReportGenerator.calculateMonthlyIncome(orders);
        String topItem = ReportGenerator.findTopSellingItem(orders);

        // Labels
        JLabel totalRevenueLabel = new JLabel("Total Revenue:");
        JLabel totalRevenueValue = new JLabel(String.format("$%.2f", totalRevenue));

        JLabel monthlyIncomeLabel = new JLabel("Monthly Income:");
        JLabel monthlyIncomeValue = new JLabel(String.format("$%.2f", monthlyIncome));

        JLabel topItemLabel = new JLabel("Top Selling Item:");
        JLabel topItemValue = new JLabel(topItem);

        // Add to panel
        panel.add(totalRevenueLabel);
        panel.add(totalRevenueValue);

        panel.add(monthlyIncomeLabel);
        panel.add(monthlyIncomeValue);

        panel.add(topItemLabel);
        panel.add(topItemValue);

        return panel;
    }



    private JPanel createLogPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        /* Create tabbed pane */
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add login activity tab
        tabbedPane.addTab("Login Attempts", createLoginActivityPanel());

        // Add session log tab
        tabbedPane.addTab("Session Logs", createSessionLogPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createLoginActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Timestamp", "Activity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        JTable logTable = new JTable(tableModel);

        // Set column widths
        logTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(350);

        // Load log data
        loadLoginActivityData(tableModel);

        JScrollPane scrollPane = new JScrollPane(logTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // Clear table
            clearTableModel(tableModel);
            // Reload data
            loadLoginActivityData(tableModel);
        });

        JButton clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Are you sure you want to clear all login logs?",
                    "Confirm Clear Logs", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    new FileWriter("files/login_activity.txt", false).close();
                    // Clear table
                    clearTableModel(tableModel);
                    JOptionPane.showMessageDialog(panel,
                            "Log file cleared successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel,
                            "Failed to clear logs: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSessionLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns for session data
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"User", "Login Time", "Logout Time", "Duration"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        JTable logTable = new JTable(tableModel);

        // Set column widths
        logTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Load log data
        loadSessionLogData(tableModel);

        JScrollPane scrollPane = new JScrollPane(logTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // Clear table
            clearTableModel(tableModel);
            // Reload data
            loadSessionLogData(tableModel);
        });

        JButton clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Are you sure you want to clear all session logs?",
                    "Confirm Clear Logs", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    new FileWriter("files/session_log.txt", false).close();
                    // Clear table
                    clearTableModel(tableModel);
                    JOptionPane.showMessageDialog(panel,
                            "Log file cleared successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel,
                            "Failed to clear logs: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadLoginActivityData(DefaultTableModel tableModel) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("files/login_activity.txt"));
            for (String line : lines) {
                // Parse the log entry (format: "2025-05-11T12:34:56.789 - User logged in")
                String[] parts = new String[]{"", ""};
                int dashIndex = line.indexOf(" - ");
                if (dashIndex > 0) {
                    parts[0] = line.substring(0, dashIndex);
                    parts[1] = line.substring(dashIndex + 3);
                } else {
                    parts[0] = "";
                    parts[1] = line;
                }
                tableModel.addRow(parts);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not load login logs: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSessionLogData(DefaultTableModel tableModel) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("files/session_log.txt"));
            for (String line : lines) {
                // Parse the session log entry
                // Format: "User: admin | Login: 2025-05-15T14:05:23 | Logout: 2025-05-15T14:47:10 | Session Duration: 42 minute(s)"
                String username = "";
                String loginTime = "";
                String logoutTime = "";
                String duration = "";

                // Extract username
                int userStart = line.indexOf("User: ");
                int userEnd = line.indexOf(" | Login:");
                if (userStart >= 0 && userEnd > userStart) {
                    username = line.substring(userStart + 6, userEnd);
                }

                // Extract login time
                int loginStart = line.indexOf("Login: ");
                int loginEnd = line.indexOf(" | Logout:");
                if (loginStart >= 0 && loginEnd > loginStart) {
                    loginTime = line.substring(loginStart + 7, loginEnd);
                }

                // Extract logout time
                int logoutStart = line.indexOf("Logout: ");
                int logoutEnd = line.indexOf(" | Session Duration:");
                if (logoutStart >= 0 && logoutEnd > logoutStart) {
                    logoutTime = line.substring(logoutStart + 8, logoutEnd);
                }

                // Extract duration
                int durationStart = line.indexOf("Session Duration: ");
                if (durationStart >= 0) {
                    duration = line.substring(durationStart + 17);
                }

                // Add the row to the table
                tableModel.addRow(new Object[]{username, loginTime, logoutTime, duration});
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not load session logs: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearTableModel(DefaultTableModel tableModel) {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // ✅ Log the session info before logging out
            Logger.logLogout(admin.getUsername());

            parentFrame.getContentPane().removeAll();
            parentFrame.setSize(400, 300);
            parentFrame.setLocationRelativeTo(null);

            SwingUtilities.invokeLater(() -> {
                try {
                    Main.main(new String[0]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}