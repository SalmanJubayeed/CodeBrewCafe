package database;

import models.Order;
import models.OrderItem;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptGenerator {
    public static String generateReceipt(Order order, double discount) {
        StringBuilder receipt = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        // Header
        receipt.append("===================================\n");
        receipt.append("           CODEBREW CAFE            \n");
        receipt.append("===================================\n\n");

        // Receipt details
        receipt.append("Receipt No: ").append(order.getOrderId()).append("\n");
        receipt.append("Date: ").append(dateFormat.format(new Date())).append("\n");
        receipt.append("Staff: ").append(order.getStaffName()).append("\n\n");

        // Customer info
        receipt.append("Customer: ").append(order.getCustomerName()).append("\n");
        receipt.append("Table No: ").append(order.getTableNumber()).append("\n\n");

        // Order items
        receipt.append("Items:\n");
        receipt.append(String.format("%-20s %-5s %-8s %-8s\n", "Item", "Qty", "Price", "Total"));
        receipt.append("-----------------------------------\n");

        double subtotal = 0;
        for (OrderItem item : order.getOrderItems()) {
            double itemTotal = item.getPrice() * item.getQuantity();
            subtotal += itemTotal;
            receipt.append(String.format("%-20s %-5d $%-7.2f $%-7.2f\n",
                    item.getItemName(), item.getQuantity(), item.getPrice(), itemTotal));
        }

        // Totals
        receipt.append("-----------------------------------\n");
        receipt.append(String.format("%-34s $%-7.2f\n", "Subtotal:", subtotal));

        if (discount > 0) {
            double discountAmount = subtotal * (discount / 100);
            receipt.append(String.format("%-34s $%-7.2f\n", "Discount (" + discount + "%):", discountAmount));
            receipt.append(String.format("%-34s $%-7.2f\n", "Total:", subtotal - discountAmount));
        } else {
            receipt.append(String.format("%-34s $%-7.2f\n", "Total:", subtotal));
        }

        // Footer
        receipt.append("\n===================================\n");
        receipt.append("          Thank you for visiting!          \n");
        receipt.append("          Please come again!          \n");
        receipt.append("===================================\n");

        return receipt.toString();
    }
}