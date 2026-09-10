package com.managementsystem.demo1;

import javafx.application.Application;
import com.managementsystem.demo1.product.model.Product;
import com.managementsystem.demo1.product.model.ProductDAO;

public class Main {
    public static void main(String[] args) {

        System.out.println("Testando o cadastro de produto no SQLite...");

        //TESTE
        //cria um produto
        Product novoProduto = new Product();
        novoProduto.setName("Terço");
        novoProduto.setDescription("Terço simples de madeira");
        novoProduto.setPrice(45.00);

        //chama o DAO para salvar no banco
        ProductDAO productDAO = new ProductDAO();
        productDAO.save(novoProduto);

        //testa se foi salvo
        System.out.println("\n--- Lista de Produtos no Banco ---");
        for (Product p : productDAO.findAll()) {
            System.out.println("ID: " + p.getId() + " | Nome: " + p.getName() + " | Preço: R$ " + p.getPrice());
        }

        Application.launch(App.class, args);
    }
}