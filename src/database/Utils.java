package database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import models.MenuItem;
import models.User;

public class Utils {
    private static final Random random = new Random();

    // Generate a unique order ID
    public static String generateOrderId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String datePrefix = dateFormat.format(new Date());
        int randomNum = 10000 + random.nextInt(90000); // 5-digit random number
        return "ORD-" + datePrefix + "-" + randomNum;
    }

    // Validate login
    public static User validateLogin(List<User> users, String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // Filter menu items by category
    public static List<MenuItem> filterMenuByCategory(List<MenuItem> allItems, String category) {
        return allItems.stream()
                .filter(item -> category.equals("All") || item.getCategory().equals(category))
                .collect(java.util.stream.Collectors.toList());
    }

    // Get all categories from menu items
    public static List<String> getAllCategories(List<MenuItem> menuItems) {
        List<String> categories = menuItems.stream()
                .map(MenuItem::getCategory)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        categories.add(0, "All"); // Add "All" option at the beginning
        return categories;
    }

    // Calculate total from order items
    public static double calculateTotal(List<models.OrderItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    // Calculate discounted total
    public static double calculateDiscountedTotal(double total, double discountPercentage) {
        return total - (total * (discountPercentage / 100));
    }

    // Ensure directories exist
    public static void ensureDirectoriesExist() {
        new java.io.File("files").mkdirs();
    }
}