package database;

import models.*;
import java.io.*;
import java.util.*;

public class DatabaseHandler {
    private static final String USERS_FILE = "files/users.txt";
    private static final String MENU_FILE = "files/menu.txt";
    private static final String ORDERS_FILE = "files/orders.txt";
    private static final String RECEIPTS_FILE = "files/receipts.txt";

    // Load users (Admin and Staff)
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String userType = parts[0];
                    String username = parts[1];
                    String password = parts[2];

                    if (userType.equals("ADMIN")) {
                        users.add(new Admin(username, password));
                    } else if (userType.equals("STAFF")) {
                        users.add(new Staff(username, password));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            // Create default admin if file doesn't exist
            users.add(new Admin("admin", "admin"));
        }
        return users;
    }

    // Save users
    public static void saveUsers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                String userType = user instanceof Admin ? "ADMIN" : "STAFF";
                writer.println(userType + "," + user.getUsername() + "," + user.getPassword());
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // Load menu items - Updated to include stock
    public static List<MenuItem> loadMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) { // Updated to check for 5 parts (including stock)
                    String id = parts[0];
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    String category = parts[3];
                    int stock = Integer.parseInt(parts[4]);
                    menuItems.add(new MenuItem(id, name, price, category, stock));
                } else if (parts.length >= 4) { // For backward compatibility
                    String id = parts[0];
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    String category = parts[3];
                    menuItems.add(new MenuItem(id, name, price, category, 0)); // Default stock is 0
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading menu items: " + e.getMessage());
            // Add sample menu items if file doesn't exist
            menuItems.add(new MenuItem("F1", "Coffee", 2.50, "Beverage", 20));
            menuItems.add(new MenuItem("F2", "Sandwich", 5.00, "Food", 15));
        }
        return menuItems;
    }

    // Save menu items - Updated to include stock
    public static void saveMenuItems(List<MenuItem> menuItems) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MENU_FILE))) {
            for (MenuItem item : menuItems) {
                writer.println(item.getId() + "," + item.getName() + "," +
                        item.getPrice() + "," + item.getCategory() + "," + item.getStock());
            }
        } catch (IOException e) {
            System.err.println("Error saving menu items: " + e.getMessage());
        }
    }

    // Load orders
    public static List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] orderParts = line.split("\\|");
                if (orderParts.length >= 5) {
                    String orderId = orderParts[0];
                    String customerName = orderParts[1];
                    int tableNumber = Integer.parseInt(orderParts[2]);
                    String staffName = orderParts[3];
                    String status = orderParts[4];

                    Order order = new Order(orderId, customerName, tableNumber, staffName);
                    order.setStatus(status);

                    // Process order items if they exist
                    if (orderParts.length > 5) {
                        String[] itemsArray = orderParts[5].split(";");
                        for (String itemStr : itemsArray) {
                            String[] itemParts = itemStr.split(":");
                            if (itemParts.length >= 3) {
                                String itemId = itemParts[0];
                                String itemName = itemParts[1];
                                int quantity = Integer.parseInt(itemParts[2]);
                                double price = Double.parseDouble(itemParts[3]);

                                order.addItem(new OrderItem(itemId, itemName, quantity, price));
                            }
                        }
                    }
                    orders.add(order);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        return orders;
    }

    // Save orders
    public static void saveOrders(List<Order> orders) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ORDERS_FILE))) {
            for (Order order : orders) {
                StringBuilder sb = new StringBuilder();
                sb.append(order.getOrderId()).append("|")
                        .append(order.getCustomerName()).append("|")
                        .append(order.getTableNumber()).append("|")
                        .append(order.getStaffName()).append("|")
                        .append(order.getStatus());

                // Append order items
                if (!order.getOrderItems().isEmpty()) {
                    sb.append("|");
                    boolean first = true;
                    for (OrderItem item : order.getOrderItems()) {
                        if (!first) {
                            sb.append(";");
                        }
                        sb.append(item.getItemId()).append(":")
                                .append(item.getItemName()).append(":")
                                .append(item.getQuantity()).append(":")
                                .append(item.getPrice());
                        first = false;
                    }
                }
                writer.println(sb.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving orders: " + e.getMessage());
        }
    }

    // Method to update stock after an order is processed
    public static void updateStockForOrder(Order order) {
        List<MenuItem> menuItems = loadMenuItems();
        for (OrderItem orderItem : order.getOrderItems()) {
            for (MenuItem menuItem : menuItems) {
                if (menuItem.getId().equals(orderItem.getItemId())) {
                    menuItem.decreaseStock(orderItem.getQuantity());
                    break;
                }
            }
        }
        saveMenuItems(menuItems);
    }

    // Save a receipt
    public static void saveReceipt(String receipt) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RECEIPTS_FILE, true))) {
            writer.println("=== NEW RECEIPT ===");
            writer.println(receipt);
            writer.println("===================");
            writer.println();
        } catch (IOException e) {
            System.err.println("Error saving receipt: " + e.getMessage());
        }
    }
}