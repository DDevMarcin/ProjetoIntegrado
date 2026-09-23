package com.managementsystem.demo1.orderItem.model;

import com.managementsystem.demo1.core.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    public OrderItemDAO() {
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "customization_description TEXT, " +
                "quantity INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "item_type TEXT NOT NULL)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a tabela de itens de encomenda: " + e.getMessage());
        }
    }

    // CREATE
    public void registerOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (name, customization_description, quantity, price, item_type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCustomizationDescription());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getPrice());
            stmt.setString(5, item.getType().name()); // Salva o Enum como String

            stmt.executeUpdate();
            System.out.println("Item de encomenda cadastrado com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar o item de encomenda: " + e.getMessage());
        }
    }

    // READ (All)
    public List<OrderItem> findAll() {
        String sql = "SELECT * FROM order_items";
        List<OrderItem> items = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setCustomizationDescription(rs.getString("customization_description"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                item.setType(ItemType.valueOf(rs.getString("item_type"))); // Retorna a String para Enum

                items.add(item);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os itens de encomenda: " + e.getMessage());
        }
        return items;
    }

    // READ (By Name) - Padrão do sistema
    public OrderItem findByName(String name) {
        String sql = "SELECT * FROM order_items WHERE name = ?";
        OrderItem item = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setName(rs.getString("name"));
                    item.setCustomizationDescription(rs.getString("customization_description"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    item.setType(ItemType.valueOf(rs.getString("item_type")));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o item de encomenda pelo nome: " + e.getMessage());
        }
        return item;
    }

    // UPDATE (Baseado no nome)
    public void update(OrderItem item) {
        String sql = "UPDATE order_items SET customization_description = ?, quantity = ?, price = ?, item_type = ? WHERE name = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getCustomizationDescription());
            stmt.setInt(2, item.getQuantity());
            stmt.setDouble(3, item.getPrice());
            stmt.setString(4, item.getType().name());
            stmt.setString(5, item.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o item de encomenda: " + e.getMessage());
        }
    }

    // DELETE (Baseado no nome)
    public void delete(String name) {
        String sql = "DELETE FROM order_items WHERE name = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o item de encomenda: " + e.getMessage());
        }
    }
}