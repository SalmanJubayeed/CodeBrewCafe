package database;

import models.Order;
import models.OrderItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    private static int month,year;

    /**
     * Calculates total revenue from all orders.
     */
    public static double calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .mapToDouble(Order::calculateTotal)
                .sum();
    }

    /**
     * Calculates revenue for a specific month and year.
     */
    public static double calculateMonthlyIncome(List<Order> orders) {
        return orders.stream()
                .filter(order -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(order.getOrderTime());
                    return cal.get(Calendar.MONTH) == (month - 1) && cal.get(Calendar.YEAR) == year;
                })
                .mapToDouble(Order::calculateTotal)
                .sum();
    }

    /**
     * Returns the name of the top-selling item.
     */
    public static String getTopSellingItem(List<Order> orders) {
        Map<String, Integer> itemCountMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                itemCountMap.put(item.getItemName(),
                        itemCountMap.getOrDefault(item.getItemName(), 0) + item.getQuantity());
            }
        }

        return itemCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No Items Found");
    }

    /**
     * Returns a sorted list of all items and their sold quantity (descending).
     */
    public static List<Map.Entry<String, Integer>> getAllItemSalesRanking(List<Order> orders) {
        Map<String, Integer> itemCountMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                itemCountMap.put(item.getItemName(),
                        itemCountMap.getOrDefault(item.getItemName(), 0) + item.getQuantity());
            }
        }

        return itemCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    /**
     * Utility to print item sales ranking (for UI/debug).
     */
    public static void printItemSalesRanking(List<Order> orders) {
        List<Map.Entry<String, Integer>> rankings = getAllItemSalesRanking(orders);
        System.out.println("\n--- Item Sales Ranking ---");
        for (Map.Entry<String, Integer> entry : rankings) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " sold");
        }
    }

    /**
     * Utility to format a Date to Month-Year (like "May 2025")
     */
    public static String formatDate(Date date) {
        return new SimpleDateFormat("MMMM yyyy").format(date);
    }

    public static String findTopSellingItem(List<Order> orders) {
        Map<String, Integer> itemSalesMap = new HashMap<>();

        for (Order order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) continue; // Only count completed orders
            for (OrderItem item : order.getOrderItems()) {
                String name = item.getItemName();
                int qty = item.getQuantity();
                itemSalesMap.put(name, itemSalesMap.getOrDefault(name, 0) + qty);
            }
        }

        String topItem = "N/A";
        int maxQty = 0;

        for (Map.Entry<String, Integer> entry : itemSalesMap.entrySet()) {
            if (entry.getValue() > maxQty) {
                topItem = entry.getKey();
                maxQty = entry.getValue();
            }
        }

        return topItem;
    }

    public static List<Order> loadOrdersFromFile(String s) {
        List<Order> orders = new ArrayList<>();

        File file = new File("files/orders.txt");  // <-- Make sure this is correct
        if (!file.exists()) {
            System.err.println("orders.txt not found at: " + file.getAbsolutePath());
            return orders;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 6) continue;

                String orderId = parts[0];
                String customer = parts[1];
                int table = Integer.parseInt(parts[2]);
                String staff = parts[3];
                String status = parts[4];
                String itemsStr = parts[5];

                List<OrderItem> orderItems = new ArrayList<>();
                for (String item : itemsStr.split(";")) {
                    String[] itemParts = item.split(":");
                    if (itemParts.length == 4) {
                        String itemId = itemParts[0];
                        String itemName = itemParts[1];
                        int qty = Integer.parseInt(itemParts[2]);
                        double price = Double.parseDouble(itemParts[3]);

                        orderItems.add(new OrderItem(itemId, itemName, qty, price));
                    }
                }

                Order order = new Order(orderId, customer, table, staff, status, orderItems);

                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }


}

