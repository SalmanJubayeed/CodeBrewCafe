package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private String customerName;
    private int tableNumber;
    private String staffName;
    private Date orderTime;
    private String status; // "PENDING", "COMPLETED", "CANCELLED"
    private List<OrderItem> orderItems;

    public Order(String orderId, String customerName, int tableNumber, String staffName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.tableNumber = tableNumber;
        this.staffName = staffName;
        this.orderItems = new ArrayList<>();
        this.status = "PENDING";
        this.orderTime = new Date();
    }

    public Order(String orderId, String customerName, int tableNumber, String staffName, String status, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.tableNumber = tableNumber;
        this.staffName = staffName;
        this.status = status;
        this.orderItems = orderItems;
        this.orderTime = new Date(); // or pass it as another parameter if needed
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getStaffName() {
        return staffName;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void addItem(OrderItem item) {
        // Check if item already exists, if so, update quantity
        for (OrderItem existingItem : orderItems) {
            if (existingItem.getItemId().equals(item.getItemId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        // If item doesn't exist yet, add it
        orderItems.add(item);
    }

    public void removeItem(String itemId) {
        orderItems.removeIf(item -> item.getItemId().equals(itemId));
    }

    public double calculateTotal() {
        return orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}