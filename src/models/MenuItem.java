package models;

public class MenuItem {
    private String id;
    private String name;
    private double price;
    private String category;
    private int stock; // New field for tracking stock

    public MenuItem(String id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = 0; // Default stock is 0
    }

    // Constructor with stock parameter
    public MenuItem(String id, String name, double price, String category, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Getter and setter for stock
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Method to decrease stock when ordered
    public boolean decreaseStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
            return true;
        }
        return false;
    }

    // Method to check if item is in stock
    public boolean isInStock() {
        return stock > 0;
    }

    @Override
    public String toString() {
        return name + " - $" + price;
    }
}