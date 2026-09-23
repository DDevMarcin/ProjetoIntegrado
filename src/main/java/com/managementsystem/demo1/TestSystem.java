package com.managementsystem.demo1;

import com.managementsystem.demo1.material.dao.MaterialDAO;
import com.managementsystem.demo1.material.model.Material;
import com.managementsystem.demo1.product.model.Product;
import com.managementsystem.demo1.product.model.ProductDAO;
import com.managementsystem.demo1.orderItem.model.ItemType;
import com.managementsystem.demo1.orderItem.model.OrderItem;
import com.managementsystem.demo1.orderItem.model.OrderItemDAO;
import com.managementsystem.demo1.order.model.Order;
import com.managementsystem.demo1.order.model.OrderDAO;

import java.time.LocalDate;
import java.util.Scanner;

public class TestSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MaterialDAO materialDAO = new MaterialDAO();
        ProductDAO productDAO = new ProductDAO();
        OrderItemDAO orderItemDAO = new OrderItemDAO();
        OrderDAO orderDAO = new OrderDAO(); // <-- Nova DAO instanciada

        boolean running = true;

        while (running) {
            System.out.println("\n=== SISTEMA DE GERENCIAMENTO DA ARTESÃ ===");
            System.out.println("1. Gerenciar Materiais");
            System.out.println("2. Gerenciar Produtos");
            System.out.println("3. Gerenciar Itens de Encomenda");
            System.out.println("4. Gerenciar Encomendas"); // <-- Nova opção
            System.out.println("0. Sair do Sistema");
            System.out.print("Escolha um módulo: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    manageMaterials(scanner, materialDAO);
                    break;
                case "2":
                    manageProducts(scanner, productDAO);
                    break;
                case "3":
                    manageOrderItems(scanner, orderItemDAO);
                    break;
                case "4":
                    manageOrders(scanner, orderDAO); // <-- Chamada do novo método
                    break;
                case "0":
                    running = false;
                    System.out.println("Encerrando o sistema... Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
        scanner.close();
    }

    // ==========================================
    // MÓDULO DE MATERIAIS
    // ==========================================
    private static void manageMaterials(Scanner scanner, MaterialDAO dao) {
        System.out.println("\n--- GERENCIAR MATERIAIS ---");
        System.out.println("1. Cadastrar | 2. Listar Todos | 3. Buscar por Nome | 0. Voltar");
        System.out.print("Opção: ");
        String opt = scanner.nextLine();

        try {
            if (opt.equals("1")) {
                Material m = new Material();
                System.out.print("Nome: "); m.setName(scanner.nextLine());
                System.out.print("Unidade de Medida (ex: metros, kg): "); m.setUnitOfMeasure(scanner.nextLine());
                System.out.print("Preço: "); m.setPrice(Double.parseDouble(scanner.nextLine()));
                System.out.print("Quantidade em Estoque: "); m.setQuantity(Double.parseDouble(scanner.nextLine()));
                dao.registerMaterial(m);
            } else if (opt.equals("2")) {
                System.out.println("\nLista de Materiais:");
                for (Material m : dao.findAll()) {
                    System.out.println("- " + m.getName() + " | Estoque: " + m.getQuantity() + " " + m.getUnitOfMeasure());
                }
            } else if (opt.equals("3")) {
                System.out.print("Nome do material: ");
                Material m = dao.findByName(scanner.nextLine());
                if (m != null) System.out.println("Encontrado: " + m.getName() + " | Preço: R$" + m.getPrice());
                else System.out.println("Material não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro na operação: " + e.getMessage());
        }
    }

    // ==========================================
    // MÓDULO DE PRODUTOS
    // ==========================================
    private static void manageProducts(Scanner scanner, ProductDAO dao) {
        System.out.println("\n--- GERENCIAR PRODUTOS ---");
        System.out.println("1. Cadastrar | 2. Listar Todos | 3. Buscar por Nome | 0. Voltar");
        System.out.print("Opção: ");
        String opt = scanner.nextLine();

        try {
            if (opt.equals("1")) {
                Product p = new Product();
                System.out.print("Nome do Produto: "); p.setName(scanner.nextLine());
                System.out.print("Descrição: "); p.setDescription(scanner.nextLine());
                System.out.print("Preço de Venda: "); p.setPrice(Double.parseDouble(scanner.nextLine()));
                dao.registerProduct(p);
            } else if (opt.equals("2")) {
                System.out.println("\nLista de Produtos:");
                for (Product p : dao.findAll()) {
                    System.out.println("- " + p.getName() + " | R$ " + p.getPrice());
                }
            } else if (opt.equals("3")) {
                System.out.print("Nome do produto: ");
                Product p = dao.findByName(scanner.nextLine());
                if (p != null) System.out.println("Encontrado: " + p.getName() + " | Descrição: " + p.getDescription());
                else System.out.println("Produto não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro na operação: " + e.getMessage());
        }
    }

    // ==========================================
    // MÓDULO DE ITENS DE ENCOMENDA
    // ==========================================
    private static void manageOrderItems(Scanner scanner, OrderItemDAO dao) {
        System.out.println("\n--- GERENCIAR ITENS DE ENCOMENDA ---");
        System.out.println("1. Cadastrar | 2. Listar Todos | 3. Excluir | 0. Voltar");
        System.out.print("Opção: ");
        String opt = scanner.nextLine();

        try {
            if (opt.equals("1")) {
                OrderItem item = new OrderItem();
                System.out.print("Nome do Item: "); item.setName(scanner.nextLine());
                System.out.print("Detalhes da Personalização: "); item.setCustomizationDescription(scanner.nextLine());
                System.out.print("Quantidade: "); item.setQuantity(Integer.parseInt(scanner.nextLine()));
                System.out.print("Valor: "); item.setPrice(Double.parseDouble(scanner.nextLine()));

                System.out.print("Tipo (1 - Pré-Pronto | 2 - Customizado): ");
                if (scanner.nextLine().equals("1")) item.setType(ItemType.READY_MADE);
                else item.setType(ItemType.CUSTOMIZED);

                dao.registerOrderItem(item);
            } else if (opt.equals("2")) {
                System.out.println("\nLista de Itens de Encomenda:");
                for (OrderItem item : dao.findAll()) {
                    System.out.println("- " + item.getName() + " | Tipo: " + item.getType().name() + " | Qtd: " + item.getQuantity());
                }
            } else if (opt.equals("3")) {
                System.out.print("Nome do item para excluir: ");
                dao.delete(scanner.nextLine());
                System.out.println("Comando de exclusão finalizado.");
            }
        } catch (Exception e) {
            System.out.println("Erro na operação: " + e.getMessage());
        }
    }

    // ==========================================
    // MÓDULO DE ENCOMENDAS
    // ==========================================
    private static void manageOrders(Scanner scanner, OrderDAO dao) {
        System.out.println("\n--- GERENCIAR ENCOMENDAS ---");
        System.out.println("1. Cadastrar | 2. Listar Todas | 3. Buscar por Cliente | 0. Voltar");
        System.out.print("Opção: ");
        String opt = scanner.nextLine();

        try {
            if (opt.equals("1")) {
                Order order = new Order();
                System.out.print("Nome do Cliente: "); order.setCustomerName(scanner.nextLine());
                System.out.print("Telefone: "); order.setCustomerPhone(scanner.nextLine());
                System.out.print("Endereço: "); order.setCustomerAddress(scanner.nextLine());
                System.out.print("Valor Total (R$): "); order.setTotalAmount(Double.parseDouble(scanner.nextLine()));

                System.out.print("Data de Entrega (AAAA-MM-DD) ou deixe vazio para não definir agora: ");
                String dateInput = scanner.nextLine();
                if (!dateInput.trim().isEmpty()) {
                    order.setDeliveryDate(LocalDate.parse(dateInput));
                }

                dao.registerOrder(order);

            } else if (opt.equals("2")) {
                System.out.println("\nLista de Encomendas:");
                for (Order order : dao.findAll()) {
                    System.out.println("- ID: " + order.getId() +
                            " | Cliente: " + order.getCustomerName() +
                            " | Status: " + order.getStatus().name() +
                            " | Total: R$ " + order.getTotalAmount());
                }
            } else if (opt.equals("3")) {
                System.out.print("Digite o nome (ou parte do nome) do cliente: ");
                String nameToSearch = scanner.nextLine();
                System.out.println("\nResultados da Busca:");
                for (Order order : dao.findByCustomerName(nameToSearch)) {
                    System.out.println("- ID: " + order.getId() +
                            " | Cliente: " + order.getCustomerName() +
                            " | Data do Pedido: " + order.getOrderDate());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro na operação: Verifique o formato dos dados digitados. Detalhes: " + e.getMessage());
        }
    }
}