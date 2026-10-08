package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private ImageView logoImageView;

    @FXML private Button btnDashboard;
    @FXML private Button btnProdutos;
    @FXML private Button btnEncomendas;
    @FXML private Button btnEstoque;
    @FXML private Button btnFinanceiro;

    private final ApplicationContext springContext;

    // Página ativa no momento
    private Button activeButton;

    public MainLayoutController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        var logoUrl = getClass().getResource("/images/logo.png");
        if (logoUrl != null) {
            logoImageView.setImage(new Image(logoUrl.toExternalForm()));
        }

        // Guarda referência deste controller no contentArea
        // para que páginas filhas possam chamar navProdutos(), etc.
        contentArea.setUserData(this);

        setActive(btnDashboard);
        loadPage("/fxml/Dashboard.fxml");
    }

    // ──────────────────────────────────────────
    //  Handlers de navegação
    // ──────────────────────────────────────────

    @FXML
    public void navDashboard() {
        setActive(btnDashboard);
        loadPage("/fxml/Dashboard.fxml");
    }

    @FXML
    public void navProdutos() {
        setActive(btnProdutos);
        loadPage("/fxml/Produtos.fxml");
    }

    public void navCadastrarPrePronto() {
        setActive(btnProdutos);
        loadPage("/fxml/CadastrarPrePronto.fxml");
    }

    public void navCadastrarPersonalizado() {
        setActive(btnProdutos);
        loadPage("/fxml/CadastrarPersonalizado.fxml");
    }

    public void navEditarProduto(Produto produto) {
        setActive(btnProdutos);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditarProduto.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Node page = loader.load();

            // Injeta o produto no controller APÓS o load (campos @FXML já injetados)
            EditarProdutoController controller = loader.getController();
            controller.setProduto(produto);

            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Label erro = new javafx.scene.control.Label("Erro ao abrir edição:\n" + e.getMessage());
            erro.setStyle("-fx-text-fill: #D31946; -fx-font-size: 13px; -fx-padding: 20;");
            contentArea.getChildren().setAll(erro);
        }
    }

    @FXML
    public void navEncomendas() {
        setActive(btnEncomendas);
        loadPage("/fxml/Placeholder.fxml");
    }

    @FXML
    public void navEstoque() {
        setActive(btnEstoque);
        loadPage("/fxml/Placeholder.fxml");
    }

    @FXML
    public void navFinanceiro() {
        setActive(btnFinanceiro);
        loadPage("/fxml/Placeholder.fxml");
    }

    // ──────────────────────────────────────────
    //  Utilitários internos
    // ──────────────────────────────────────────

    /**
     * Carrega um FXML na área de conteúdo central.
     * O Spring é responsável por instanciar o controller (injeção via construtor).
     */
    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Node page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            // Mostra erro visível na área de conteúdo em vez de tela em branco
            javafx.scene.control.Label erro = new javafx.scene.control.Label(
                "Erro ao carregar página:\n" + e.getMessage()
            );
            erro.setStyle("-fx-text-fill: #D31946; -fx-font-size: 15px; -fx-padding: 20;");
            contentArea.getChildren().setAll(erro);
        }
    }

    /**
     * Marca o botão como ativo (estilo rosa escuro) e remove o estilo dos demais.
     */
    private void setActive(Button button) {
        List<Button> all = List.of(
                btnDashboard, btnProdutos, btnEncomendas, btnEstoque, btnFinanceiro
        );

        for (Button btn : all) {
            btn.getStyleClass().remove("nav-item-active");
        }

        if (!button.getStyleClass().contains("nav-item-active")) {
            button.getStyleClass().add("nav-item-active");
        }

        activeButton = button;
    }
}
