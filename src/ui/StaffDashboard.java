package ui;

import com.sun.tools.javac.Main;
import database.DatabaseHandler;
import database.Logger;
import database.ReceiptGenerator;
import database.Utils;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import models.Staff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDashboard extends JPanel {
    private JFrame parentFrame;
    private Staff staff;
    private JTabbedPane tabbedPane;
    private Runnable logoutCallback;

    private List<MenuItem> menuItems;
    private List<Order> orders;
    private Order currentOrder;

    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;

    public StaffDashboard(JFrame parentFrame, Staff staff, Runnable logoutCallback) {
        this.parentFrame = parentFrame;
        this.staff = staff;
        this.logoutCallback = logoutCallback;

        // Load data
        this.menuItems = DatabaseHandler.loadMenuItems();
        this.orders = DatabaseHandler.loadOrders();

        // Set layout
        setLayout(new BorderLayout());

        // Create welcome panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + staff.getUsername() + " (Staff)");
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
        tabbedPane.addTab("New Order", createOrderPanel());
        tabbedPane.addTab("Order History", createOrderHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create split pane for menu and cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);

        // Menu panel (left side)
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));

        // Category filter
        List<String> categories = Utils.getAllCategories(menuItems);
        JComboBox<String> categoryComboBox = new JComboBox<>(categories.toArray(new String[0]));
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryComboBox);
        menuPanel.add(filterPanel, BorderLayout.NORTH);

        // Menu items table
        DefaultTableModel menuTableModel = new DefaultTableModel(
                new Object[]{"ID", "Item", "Price", "Category", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

// Then modify the code that populates the menu table:
        for (MenuItem item : menuItems) {
            menuTableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    String.format("$%.2f", item.getPrice()),
                    item.getCategory(),
                    item.getStock()
            });
        }

        JTable menuTable = new JTable(menuTableModel);
        JScrollPane menuScrollPane = new JScrollPane(menuTable);
        menuPanel.add(menuScrollPane, BorderLayout.CENTER);

        // Add to cart button
        JPanel menuButtonPanel = new JPanel();
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton addToCartButton = new JButton("Add to Cart");
        menuButtonPanel.add(new JLabel("Quantity:"));
        menuButtonPanel.add(quantitySpinner);
        menuButtonPanel.add(addToCartButton);
        menuPanel.add(menuButtonPanel, BorderLayout.SOUTH);

        // Cart panel (right side)
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        // Customer info panel
        JPanel customerPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        customerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextField customerNameField = new JTextField(15);
        JSpinner tableNumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));

        customerPanel.add(new JLabel("Customer Name:"));
        customerPanel.add(customerNameField);
        customerPanel.add(new JLabel("Table Number:"));
        customerPanel.add(tableNumberSpinner);

        cartPanel.add(customerPanel, BorderLayout.NORTH);

        // Cart items table
        cartTableModel = new DefaultTableModel(
                new Object[]{"Item", "Price", "Qty", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Bottom panel with total and buttons
        JPanel cartBottomPanel = new JPanel(new BorderLayout());

        // Total label
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cartBottomPanel.add(totalLabel, BorderLayout.NORTH);

        // Buttons
        JPanel cartButtonPanel = new JPanel();
        JButton removeItemButton = new JButton("Remove Item");
        JButton clearCartButton = new JButton("Clear Cart");
        JButton processOrderButton = new JButton("Process Order");

        cartButtonPanel.add(removeItemButton);
        cartButtonPanel.add(clearCartButton);
        cartButtonPanel.add(processOrderButton);
        cartBottomPanel.add(cartButtonPanel, BorderLayout.CENTER);

        cartPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        // Add panels to split pane
        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(cartPanel);

        panel.add(splitPane, BorderLayout.CENTER);

        // Initialize current order
        currentOrder = new Order(Utils.generateOrderId(), "", 1, staff.getUsername());

        // Add event listeners
        categoryComboBox.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            updateMenuTable(menuTableModel, selectedCategory);
        });

        addToCartButton.addActionListener(e -> {
            int selectedRow = menuTable.getSelectedRow();
            if (selectedRow != -1) {
                String itemId = (String) menuTableModel.getValueAt(selectedRow, 0);
                String itemName = (String) menuTableModel.getValueAt(selectedRow, 1);
                String priceStr = (String) menuTableModel.getValueAt(selectedRow, 2);
                double price = Double.parseDouble(priceStr.substring(1)); // Remove $ sign
                int quantity = (int) quantitySpinner.getValue();

                addToCart(itemId, itemName, price, quantity);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a menu item",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        removeItemButton.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                removeFromCart(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to remove",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        clearCartButton.addActionListener(e -> clearCart());

        processOrderButton.addActionListener(e -> {
            if (cartTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Cart is empty",
                        "Empty Cart", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String customerName = customerNameField.getText().trim();
            if (customerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter customer name",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int tableNumber = (int) tableNumberSpinner.getValue();

            // Set customer info
            currentOrder.setCustomerName(customerName);
            currentOrder.setTableNumber(tableNumber);

            // Process order
            processOrder();
        });

        return panel;
    }

    private void updateMenuTable(DefaultTableModel menuTableModel, String selectedCategory) {
        menuTableModel.setRowCount(0);

        List<MenuItem> filteredItems = Utils.filterMenuByCategory(menuItems, selectedCategory);
        for (MenuItem item : filteredItems) {
            menuTableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    String.format("$%.2f", item.getPrice()),
                    item.getCategory(),
                    item.getStock()
            });
        }
    }

    private void addToCart(String itemId, String itemName, double price, int quantity) {
        // Check stock availability before adding to cart
        for (MenuItem item : menuItems) {
            if (item.getId().equals(itemId)) {
                int currentQuantityInCart = 0;

                // Check if item already exists in cart and get current quantity
                for (OrderItem existingItem : currentOrder.getOrderItems()) {
                    if (existingItem.getItemId().equals(itemId)) {
                        currentQuantityInCart = existingItem.getQuantity();
                        break;
                    }
                }

                // Calculate total quantity after adding
                int totalQuantity = currentQuantityInCart + quantity;

                if (item.getStock() < totalQuantity) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot add " + quantity + " of " + itemName + " to cart.\n" +
                                    "Available stock: " + item.getStock() + "\n" +
                                    "Already in cart: " + currentQuantityInCart,
                            "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                break;
            }
        }

        // Add to current order
        OrderItem orderItem = new OrderItem(itemId, itemName, quantity, price);
        currentOrder.addItem(orderItem);

        // Update cart table
        updateCartTable();
    }

    private void removeFromCart(int selectedRow) {
        // Get the item name from the cart table
        String itemName = (String) cartTableModel.getValueAt(selectedRow, 0);

        // Find the corresponding order item
        String itemIdToRemove = null;
        for (OrderItem item : currentOrder.getOrderItems()) {
            if (item.getItemName().equals(itemName)) {
                itemIdToRemove = item.getItemId();
                break;
            }
        }

        if (itemIdToRemove != null) {
            currentOrder.removeItem(itemIdToRemove);
            updateCartTable();
        }
    }

    private void clearCart() {
        currentOrder = new Order(Utils.generateOrderId(), "", 1, staff.getUsername());
        updateCartTable();
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        double total = 0;

        for (OrderItem item : currentOrder.getOrderItems()) {
            double itemTotal = item.getPrice() * item.getQuantity();
            total += itemTotal;

            cartTableModel.addRow(new Object[]{
                    item.getItemName(),
                    String.format("$%.2f", item.getPrice()),
                    item.getQuantity(),
                    String.format("$%.2f", itemTotal)
            });
        }

        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    private void processOrder() {
        // Check if all items are in stock before processing
        if (!checkStockAvailability()) {
            return; // Stop processing if any item is out of stock
        }

        // Process order dialog
        JPanel discountPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        JSpinner discountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 5.0));
        discountPanel.add(new JLabel("Discount (%):"));
        discountPanel.add(discountSpinner);

        int result = JOptionPane.showConfirmDialog(this, discountPanel,
                "Apply Discount", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            double discount = (double) discountSpinner.getValue();

            // Generate receipt
            String receipt = ReceiptGenerator.generateReceipt(currentOrder, discount);

            // Show receipt
            JTextArea receiptArea = new JTextArea(receipt);
            receiptArea.setEditable(false);
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(receiptArea);
            scrollPane.setPreferredSize(new Dimension(400, 500));

            JOptionPane.showMessageDialog(this, scrollPane, "Receipt", JOptionPane.PLAIN_MESSAGE);

            // Save receipt
            DatabaseHandler.saveReceipt(receipt);

            // Set order status to completed
            currentOrder.setStatus("COMPLETED");

            // Update stock levels for the order
            DatabaseHandler.updateStockForOrder(currentOrder);

            // Reload the menu items with updated stock
            menuItems = DatabaseHandler.loadMenuItems();

            // Refresh the menu table
            refreshMenuTable();

            // Save order
            orders.add(currentOrder);
            DatabaseHandler.saveOrders(orders);

            // Create new order
            clearCart();

            // Refresh order history tab
            tabbedPane.setComponentAt(1, createOrderHistoryPanel());
        }
    }

    private void refreshMenuTable() {
        // Get the current tab component
        Component orderPanelComponent = tabbedPane.getComponentAt(0);

        if (orderPanelComponent instanceof JPanel) {
            JPanel orderPanel = (JPanel) orderPanelComponent;

            // Find the JSplitPane that contains our menu table
            Component[] components = orderPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JSplitPane) {
                    JSplitPane splitPane = (JSplitPane) component;

                    // Get the left component (menu panel)
                    Component leftComponent = splitPane.getLeftComponent();
                    if (leftComponent instanceof JPanel) {
                        JPanel menuPanel = (JPanel) leftComponent;

                        // Find the menu table inside the menu panel
                        findAndUpdateMenuTable(menuPanel);
                    }
                }
            }
        }
    }

    private void findAndUpdateMenuTable(Container container) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                Component viewComponent = scrollPane.getViewport().getView();

                if (viewComponent instanceof JTable) {
                    JTable menuTable = (JTable) viewComponent;
                    DefaultTableModel menuTableModel = (DefaultTableModel) menuTable.getModel();

                    // Clear and update table
                    menuTableModel.setRowCount(0);

                    // Get the currently selected category if any
                    String selectedCategory = "All";
                    for (Component c : container.getComponents()) {
                        if (c instanceof JPanel) {
                            Component[] panelComponents = ((JPanel) c).getComponents();
                            for (Component pc : panelComponents) {
                                if (pc instanceof JComboBox) {
                                    JComboBox<?> comboBox = (JComboBox<?>) pc;
                                    selectedCategory = (String) comboBox.getSelectedItem();
                                    break;
                                }
                            }
                        }
                    }

                    // Update the table with current data
                    List<MenuItem> filteredItems = selectedCategory.equals("All") ?
                            menuItems : Utils.filterMenuByCategory(menuItems, selectedCategory);

                    for (MenuItem item : filteredItems) {
                        menuTableModel.addRow(new Object[]{
                                item.getId(),
                                item.getName(),
                                String.format("$%.2f", item.getPrice()),
                                item.getCategory(),
                                item.getStock()
                        });
                    }

                    // Force table to repaint
                    menuTable.repaint();
                    return;
                }
            } else if (component instanceof Container) {
                // Recursively search inside nested containers
                findAndUpdateMenuTable((Container) component);
            }
        }
    }

    private boolean checkStockAvailability() {
        List<MenuItem> currentMenuItems = DatabaseHandler.loadMenuItems();
        StringBuilder outOfStockItems = new StringBuilder();

        for (OrderItem orderItem : currentOrder.getOrderItems()) {
            for (MenuItem menuItem : currentMenuItems) {
                if (menuItem.getId().equals(orderItem.getItemId())) {
                    if (menuItem.getStock() < orderItem.getQuantity()) {
                        outOfStockItems.append("- ").append(menuItem.getName())
                                .append(" (Available: ").append(menuItem.getStock())
                                .append(", Ordered: ").append(orderItem.getQuantity())
                                .append(")\n");
                    }
                    break;
                }
            }
        }

        if (outOfStockItems.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "The following items don't have enough stock:\n" + outOfStockItems.toString() +
                            "\nPlease adjust your order or update the stock levels.",
                    "Stock Unavailable", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Order ID", "Customer", "Table", "Items", "Total", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Filter orders for current staff
        List<Order> staffOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStaffName().equals(staff.getUsername())) {
                staffOrders.add(order);
            }
        }

        // Populate table model
        for (Order order : staffOrders) {
            String itemsStr = order.getOrderItems().size() + " items";
            double total = order.calculateTotal();

            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getTableNumber(),
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
                viewOrderDetails(staffOrders.get(selectedRow));
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(viewDetailsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void viewOrderDetails(Order order) {
        // Create details panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Order ID: " + order.getOrderId()));
        panel.add(new JLabel("Customer: " + order.getCustomerName()));
        panel.add(new JLabel("Table: " + order.getTableNumber()));
        panel.add(new JLabel("Status: " + order.getStatus()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Items:"));

        // Create table for order items
        DefaultTableModel itemTableModel = new DefaultTableModel(
                new Object[]{"Item", "Price", "Quantity", "Total"}, 0);

        double orderTotal = 0;
        for (OrderItem item : order.getOrderItems()) {
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

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // ✅ Log logout and session duration
            Logger.logLogout(staff.getUsername());

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