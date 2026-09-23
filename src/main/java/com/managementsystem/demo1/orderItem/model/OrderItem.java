package com.managementsystem.demo1.orderItem.model;

public class OrderItem {
    private int id;
    private String name;
    private String customizationDescription;
    private int quantity;
    private double price;
    private ItemType type;

    public OrderItem(int id, String name, String customizationDescription, int quantity, double price, ItemType type) {
        this.id = id;
        this.name = name;
        this.customizationDescription = customizationDescription;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    public OrderItem() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomizationDescription() {
        return customizationDescription;
    }

    public void setCustomizationDescription(String customizationDescription) {
        this.customizationDescription = customizationDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
}
