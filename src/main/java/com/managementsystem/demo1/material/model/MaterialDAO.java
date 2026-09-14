package com.managementsystem.demo1.material.dao;

import com.managementsystem.demo1.core.database.ConnectionFactory;
import com.managementsystem.demo1.material.model.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public MaterialDAO() {
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS materials (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "unit_of_measure TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "quantity REAL NOT NULL)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a tabela de materiais: " + e.getMessage());
        }
    }

    // CREATE
    public void registerMaterial(Material material) {
        String sql = "INSERT INTO materials (name, unit_of_measure, price, quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, material.getName());
            stmt.setString(2, material.getUnitOfMeasure());
            stmt.setDouble(3, material.getPrice());
            stmt.setDouble(4, material.getQuantity());

            stmt.executeUpdate();
            System.out.println("Material cadastrado com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar o material: " + e.getMessage());
        }
    }

    // READ (All)
    public List<Material> findAll() {
        String sql = "SELECT * FROM materials";
        List<Material> materials = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Material material = new Material();
                material.setId(rs.getInt("id"));
                material.setName(rs.getString("name"));
                material.setUnitOfMeasure(rs.getString("unit_of_measure"));
                material.setPrice(rs.getDouble("price"));
                material.setQuantity(rs.getDouble("quantity"));

                materials.add(material);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os materiais: " + e.getMessage());
        }
        return materials;
    }

    // READ (By Name) - Substitui a busca por ID
    public Material findByName(String name) {
        String sql = "SELECT * FROM materials WHERE name = ?";
        Material material = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    material = new Material();
                    material.setId(rs.getInt("id"));
                    material.setName(rs.getString("name"));
                    material.setUnitOfMeasure(rs.getString("unit_of_measure"));
                    material.setPrice(rs.getDouble("price"));
                    material.setQuantity(rs.getDouble("quantity"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o material pelo nome: " + e.getMessage());
        }
        return material;
    }

    // UPDATE
    public void update(Material material) {
        String sql = "UPDATE materials SET unit_of_measure = ?, price = ?, quantity = ? WHERE name = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, material.getUnitOfMeasure());
            stmt.setDouble(2, material.getPrice());
            stmt.setDouble(3, material.getQuantity());
            stmt.setString(4, material.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o material: " + e.getMessage());
        }
    }

    // DELETE (By Name)
    public void delete(String name) {
        String sql = "DELETE FROM materials WHERE name = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o material: " + e.getMessage());
        }
    }

    // --- Métodos de Regra de Negócio (Estoque) ---

    public void updateQuantity(String name, double newQuantity) {
        String sql = "UPDATE materials SET quantity = ? WHERE name = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newQuantity);
            stmt.setString(2, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a quantidade: " + e.getMessage());
        }
    }

    public void recordLoss(String name, double lostQuantity) {
        Material material = findByName(name);
        if (material != null) {
            double currentQuantity = material.getQuantity();
            double finalQuantity = currentQuantity - lostQuantity;

            // Garante que o estoque não fique negativo
            if (finalQuantity < 0) {
                finalQuantity = 0;
            }

            updateQuantity(name, finalQuantity);
            System.out.println("Perda registrada. Estoque atualizado para: " + finalQuantity);
        } else {
            System.out.println("Material não encontrado: " + name);
        }
    }
}