package com.managementsystem.demo1.order.model;

import com.managementsystem.demo1.orderItem.model.OrderItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private LocalDate deliveryDate;
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private double totalAmount;
    private OrderStatus status;
    private LocalDate orderDate;

    // Relacionamento: Uma Encomenda contém vários Itens (Composição)
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.AWAITING_DOWN_PAYMENT; // Status inicial padrão
        this.orderDate = LocalDate.now(); // Pega a data atual automaticamente
    }

    // --- REGRAS DE NEGÓCIO DO DIAGRAMA ---

    public void registerPayment() {
        this.status = OrderStatus.IN_PRODUCTION;
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELED;
    }

    public void calculateTotalAmount() {
        this.totalAmount = 0.0;
        for (OrderItem item : items) {
            this.totalAmount += (item.getPrice() * item.getQuantity());
        }
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotalAmount(); // Atualiza o valor total sempre que um item for adicionado
    }

    // --- GETTERS E SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public List<OrderItem> getItems() { return items; }
}