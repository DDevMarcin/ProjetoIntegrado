package com.managementsystem.demo1.order.model;

import com.managementsystem.demo1.core.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public OrderDAO() {
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "delivery_date TEXT, " +
                "customer_name TEXT NOT NULL, " +
                "customer_address TEXT, " +
                "customer_phone TEXT, " +
                "total_amount REAL NOT NULL, " +
                "status TEXT NOT NULL, " +
                "order_date TEXT NOT NULL)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a tabela de encomendas: " + e.getMessage());
        }
    }

    // CREATE
    public void registerOrder(Order order) {
        String sql = "INSERT INTO orders (delivery_date, customer_name, customer_address, customer_phone, total_amount, status, order_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Salvando as datas como texto (ISO-8601) no SQLite
            stmt.setString(1, order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null);
            stmt.setString(2, order.getCustomerName());
            stmt.setString(3, order.getCustomerAddress());
            stmt.setString(4, order.getCustomerPhone());
            stmt.setDouble(5, order.getTotalAmount());
            stmt.setString(6, order.getStatus().name());
            stmt.setString(7, order.getOrderDate().toString());

            stmt.executeUpdate();
            System.out.println("Encomenda registrada com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar a encomenda: " + e.getMessage());
        }
    }

    // READ (All)
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar as encomendas: " + e.getMessage());
        }
        return orders;
    }

    // READ (By Customer Name) - Pode retornar mais de uma se o cliente comprar várias vezes
    public List<Order> findByCustomerName(String customerName) {
        String sql = "SELECT * FROM orders WHERE customer_name LIKE ?";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + customerName + "%"); // Permite buscar por partes do nome
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar encomendas pelo nome do cliente: " + e.getMessage());
        }
        return orders;
    }

    // UPDATE (Baseado no ID, pois encomenda não tem nome único)
    public void update(Order order) {
        String sql = "UPDATE orders SET delivery_date = ?, customer_name = ?, customer_address = ?, customer_phone = ?, total_amount = ?, status = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null);
            stmt.setString(2, order.getCustomerName());
            stmt.setString(3, order.getCustomerAddress());
            stmt.setString(4, order.getCustomerPhone());
            stmt.setDouble(5, order.getTotalAmount());
            stmt.setString(6, order.getStatus().name());
            stmt.setInt(7, order.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a encomenda: " + e.getMessage());
        }
    }

    // DELETE (Baseado no ID)
    public void delete(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a encomenda: " + e.getMessage());
        }
    }

    // Método auxiliar para evitar repetição de código
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));

        String deliveryDateStr = rs.getString("delivery_date");
        if (deliveryDateStr != null) {
            order.setDeliveryDate(LocalDate.parse(deliveryDateStr));
        }

        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerAddress(rs.getString("customer_address"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setOrderDate(LocalDate.parse(rs.getString("order_date")));

        return order;
    }
}