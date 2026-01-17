package ui;
import database.DatabaseHandler;
import database.Utils;
import models.MenuItem;
import models.Order;
import models.OrderItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class OrderForm extends JPanel {
    private List<MenuItem> menuItems;
    private Order currentOrder;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;
    private JTextField customerNameField;
    private JSpinner tableNumberSpinner;
    private JTable menuItemsTable;
    private DefaultTableModel menuItemsTableModel;
    private JButton addToCartButton;
    private JButton removeFromCartButton;
    private JButton placeOrderButton;
    private JButton clearOrderButton;
    private JSpinner quantitySpinner;
    private DatabaseHandler dbHandler;
    private String staffName;

    public OrderForm(String staffName) {
        this.staffName = staffName;
        this.dbHandler = new DatabaseHandler();
        // DatabaseHandler has a static method loadMenuItems() instead of instance method getAllMenuItems()
        this.menuItems = DatabaseHandler.loadMenuItems();
        // Order constructor requires parameters, can't use default constructor
        this.currentOrder = new Order(Utils.generateOrderId(), "", 1, staffName);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize UI components
        initializeUI();

        // Load menu items
        loadMenuItems();
    }

    private void initializeUI() {
        // Create main panels
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        // Set up menu items table with stock column
        String[] menuColumnNames = {"ID", "Name", "Category", "Price", "Available"};
        menuItemsTableModel = new DefaultTableModel(menuColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuItemsTable = new JTable(menuItemsTableModel);
        menuItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuScrollPane = new JScrollPane(menuItemsTable);
        menuScrollPane.setPreferredSize(new Dimension(400, 300));
        leftPanel.add(new JLabel("Menu Items"), BorderLayout.NORTH);
        leftPanel.add(menuScrollPane, BorderLayout.CENTER);


        // Set up cart table
        String[] cartColumnNames = {"Item Name", "Quantity", "Unit Price", "Total"};
        cartTableModel = new DefaultTableModel(cartColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(400, 200));
        rightPanel.add(new JLabel("Current Order"), BorderLayout.NORTH);
        rightPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Set up controls panel
        JPanel controlsPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        // Quantity selection
        JLabel quantityLabel = new JLabel("Quantity:");
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1, 1, 100, 1);
        quantitySpinner = new JSpinner(quantityModel);
        controlsPanel.add(quantityLabel);
        controlsPanel.add(quantitySpinner);

        // Add and remove buttons
        addToCartButton = new JButton("Add to Cart");
        removeFromCartButton = new JButton("Remove Item");
        controlsPanel.add(addToCartButton);
        controlsPanel.add(removeFromCartButton);

        // Customer information
        JLabel customerNameLabel = new JLabel("Customer Name:");
        customerNameField = new JTextField(20);
        controlsPanel.add(customerNameLabel);
        controlsPanel.add(customerNameField);

        JLabel tableNumberLabel = new JLabel("Table Number:");
        SpinnerNumberModel tableModel = new SpinnerNumberModel(1, 1, 50, 1);
        tableNumberSpinner = new JSpinner(tableModel);
        controlsPanel.add(tableNumberLabel);
        controlsPanel.add(tableNumberSpinner);

        centerPanel.add(controlsPanel, BorderLayout.CENTER);

        // Set up order actions panel
        JPanel orderActionsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        placeOrderButton = new JButton("Place Order");
        clearOrderButton = new JButton("Clear Order");
        orderActionsPanel.add(placeOrderButton);
        orderActionsPanel.add(clearOrderButton);

        // Set up total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalPanel.add(totalLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(orderActionsPanel, BorderLayout.CENTER);
        bottomPanel.add(totalPanel, BorderLayout.SOUTH);

        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add all panels to main layout
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Set up event handlers
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        addToCartButton.addActionListener(e -> addToCart());
        removeFromCartButton.addActionListener(e -> removeFromCart());
        placeOrderButton.addActionListener(e -> placeOrder());
        clearOrderButton.addActionListener(e -> clearOrder());
    }

    private void loadMenuItems() {
        menuItemsTableModel.setRowCount(0);
        for (MenuItem item : menuItems) {
            // Only show items that are in stock
            if (item.getStock() > 0) {
                Object[] rowData = {
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        String.format("$%.2f", item.getPrice()),
                        item.getStock() // Add stock column
                };
                menuItemsTableModel.addRow(rowData);
            }
        }
    }

    private void addToCart() {
        int selectedRow = menuItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String menuItemId = (String) menuItemsTableModel.getValueAt(selectedRow, 0);
        MenuItem selectedItem = null;

        for (MenuItem item : menuItems) {
            if (item.getId().equals(menuItemId)) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem != null) {
            int requestedQuantity = (int) quantitySpinner.getValue();

            // Check if we have enough stock
            if (selectedItem.getStock() < requestedQuantity) {
                JOptionPane.showMessageDialog(this,
                        "Not enough stock. Only " + selectedItem.getStock() + " available.",
                        "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if item already exists in order
            boolean itemExists = false;
            for (OrderItem orderItem : currentOrder.getOrderItems()) {
                if (orderItem.getItemId().equals(selectedItem.getId())) {
                    // Check if we have enough stock for the combined quantity
                    int totalQuantity = orderItem.getQuantity() + requestedQuantity;
                    if (selectedItem.getStock() < totalQuantity) {
                        JOptionPane.showMessageDialog(this,
                                "Not enough stock. You already have " + orderItem.getQuantity() +
                                        " in your cart and only " + selectedItem.getStock() + " available.",
                                "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Update quantity
                    orderItem.setQuantity(totalQuantity);
                    itemExists = true;
                    break;
                }
            }

            if (!itemExists) {
                // Create new order item
                OrderItem newItem = new OrderItem(
                        selectedItem.getId(),
                        selectedItem.getName(),
                        requestedQuantity,
                        selectedItem.getPrice()
                );
                currentOrder.addItem(newItem);
            }

            updateCartTable();
            updateTotalAmount();
        }
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String itemName = (String) cartTableModel.getValueAt(selectedRow, 0);

        // Find the item id from the name
        String itemIdToRemove = null;
        for (OrderItem item : currentOrder.getOrderItems()) {
            if (item.getItemName().equals(itemName)) {
                itemIdToRemove = item.getItemId();
                break;
            }
        }

        if (itemIdToRemove != null) {
            currentOrder.removeItem(itemIdToRemove);
        }

        updateCartTable();
        updateTotalAmount();
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        for (OrderItem item : currentOrder.getOrderItems()) {
            double itemTotal = item.getPrice() * item.getQuantity();
            Object[] rowData = {
                    item.getItemName(),
                    item.getQuantity(),
                    String.format("$%.2f", item.getPrice()),
                    String.format("$%.2f", itemTotal)
            };
            cartTableModel.addRow(rowData);
        }
    }

    private void updateTotalAmount() {
        double total = currentOrder.calculateTotal();
        totalLabel.setText("Total: " + String.format("$%.2f", total));
    }

    private void placeOrder() {
        if (currentOrder.getOrderItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add items to the order first.", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerName = customerNameField.getText().trim();
        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate stock availability before placing order
        List<MenuItem> currentMenuItems = DatabaseHandler.loadMenuItems();
        boolean stockAvailable = true;
        StringBuilder unavailableItems = new StringBuilder();

        for (OrderItem orderItem : currentOrder.getOrderItems()) {
            boolean found = false;
            for (MenuItem menuItem : currentMenuItems) {
                if (orderItem.getItemId().equals(menuItem.getId())) {
                    if (menuItem.getStock() < orderItem.getQuantity()) {
                        stockAvailable = false;
                        unavailableItems.append(orderItem.getItemName())
                                .append(" (ordered: ")
                                .append(orderItem.getQuantity())
                                .append(", available: ")
                                .append(menuItem.getStock())
                                .append(")\n");
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                stockAvailable = false;
                unavailableItems.append(orderItem.getItemName())
                        .append(" (not available)\n");
            }
        }

        if (!stockAvailable) {
            JOptionPane.showMessageDialog(this,
                    "Cannot place order due to insufficient stock:\n" + unavailableItems.toString() +
                            "\nPlease adjust your order.",
                    "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentOrder.setCustomerName(customerName);
        currentOrder.setTableNumber((int) tableNumberSpinner.getValue());
        currentOrder.setStatus("PENDING");

        try {
            // Load existing orders
            List<Order> orders = DatabaseHandler.loadOrders();

            // Add current order
            orders.add(currentOrder);

            // Save all orders
            DatabaseHandler.saveOrders(orders);

            // Update stock
            DatabaseHandler.updateStockForOrder(currentOrder);

            // Refresh menu items list after stock deduction
            menuItems = DatabaseHandler.loadMenuItems();
            loadMenuItems();

            JOptionPane.showMessageDialog(this, "Order placed successfully!\nOrder ID: " + currentOrder.getOrderId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            clearOrder();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error placing order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearOrder() {
        // Create a new order with a new ID
        currentOrder = new Order(Utils.generateOrderId(), "", 1, staffName);
        customerNameField.setText("");
        tableNumberSpinner.setValue(1);
        quantitySpinner.setValue(1);
        cartTableModel.setRowCount(0);
        updateTotalAmount();
    }
}