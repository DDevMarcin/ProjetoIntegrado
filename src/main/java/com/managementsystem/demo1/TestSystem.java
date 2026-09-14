package com.managementsystem.demo1;

import com.managementsystem.demo1.material.dao.MaterialDAO;
import com.managementsystem.demo1.material.model.Material;
import com.managementsystem.demo1.product.model.Product;
import com.managementsystem.demo1.product.model.ProductDAO;

public class TestSystem {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTES DO SISTEMA ===");

        // --- TESTE DE MATERIAIS ---
        MaterialDAO materialDAO = new MaterialDAO();

        Material novoMaterial = new Material();
        novoMaterial.setName("Linha de Seda preta");
        novoMaterial.setUnitOfMeasure("metros");
        novoMaterial.setPrice(19.50);
        novoMaterial.setQuantity(100.0);

        try {
            materialDAO.registerMaterial(novoMaterial);
            System.out.println("Material cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar material (talvez já exista): " + e.getMessage());
        }

        // Testando busca por nome
        Material materialEncontrado = materialDAO.findByName("Linha de Seda Azul");
        if (materialEncontrado != null) {
            System.out.println("Material encontrado: " + materialEncontrado.getName() + " | Estoque: " + materialEncontrado.getQuantity());
        }

        // --- TESTE DE PRODUTOS ---
        ProductDAO productDAO = new ProductDAO();

        Product novoProduto = new Product();
        novoProduto.setName("Terço de madeira preto");
        novoProduto.setDescription("terço preto de madeira com contas de 6mm");
        novoProduto.setPrice(100.0);

        try {
            productDAO.registerProduct(novoProduto);
            System.out.println("Produto cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar produto (talvez já exista): " + e.getMessage());
        }

        // Listando todos os produtos
        System.out.println("\n--- LISTA DE PRODUTOS CADASTRADOS ---");
        for (Product p : productDAO.findAll()) {
            System.out.println("- " + p.getName() + " | Preço: R$ " + p.getPrice());
        }

        System.out.println("\n--- LISTA DE MATERIAIS CADASTRADOS ---");
        for (Material m : materialDAO.findAll()) {
            System.out.println("- " + m.getName() + " | Estoque: " + m.getQuantity());
        }

        System.out.println("=== TESTES CONCLUÍDOS ===");
    }
}