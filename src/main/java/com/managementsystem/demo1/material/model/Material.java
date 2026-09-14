package com.managementsystem.demo1.material.model;

public class Material {
    private int id;
    private String name;
    private String unitOfMeasure;
    private double price;
    private double quantity;

    public Material (int id, String name, String unitOfMeasure, double price, double quantity) {
        this.id = id;
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
        this.price = price;
        this.quantity = quantity;
    }

    public Material (){
    }

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

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser menor que zero.");
        }
        this.quantity = quantity;
    }
}
