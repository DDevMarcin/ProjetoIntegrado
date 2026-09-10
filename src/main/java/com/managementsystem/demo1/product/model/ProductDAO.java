package com.managementsystem.demo1.product.model;

import com.managementsystem.demo1.core.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public ProductDAO() {
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL NOT NULL)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating products table: " + e.getMessage());
        }
    }

    // CREATE
    public void save(Product product) {
        String sql = "INSERT INTO products (name, description, price) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());

            stmt.executeUpdate();
            System.out.println("Product saved successfully!");

        } catch (SQLException e) {
            throw new RuntimeException("Error saving product: " + e.getMessage());
        }
    }

    // READ
    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));

                products.add(product);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products: " + e.getMessage());
        }
        return products;
    }

    // UPDATE
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating product: " + e.getMessage());
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product: " + e.getMessage());
        }
    }
}