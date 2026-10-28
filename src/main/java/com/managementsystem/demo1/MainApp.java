package com.managementsystem.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // web(NONE): usa toda a maquinaria do Spring Boot (le application.properties,
        // configura DataSource/JPA certinho) mas NUNCA tenta subir servidor Tomcat
        // -- isso evita o conflito com o javafx-maven-plugin.
        // headless(false): essencial, senao o JavaFX nao consegue desenhar a tela.
        springContext = new SpringApplicationBuilder(BackendApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        TabPane tabPane = new TabPane();

        // Aba 1: Gestão de Materiais (CRUD de Materiais)
        FXMLLoader loaderMateriais = new FXMLLoader(getClass().getResource("/fxml/gerenciar-material.fxml"));
        loaderMateriais.setControllerFactory(springContext::getBean);
        Parent rootMateriais = loaderMateriais.load();
        Tab tabMateriais = new Tab("Materiais (CRUD)", rootMateriais);
        tabMateriais.setClosable(false);

        // Aba 2: Controle de Estoque (teste das atualizações recentes)
        FXMLLoader loaderEstoque = new FXMLLoader(getClass().getResource("/fxml/controle-estoque.fxml"));
        loaderEstoque.setControllerFactory(springContext::getBean);
        Parent rootEstoque = loaderEstoque.load();
        Tab tabEstoque = new Tab("Controle de Estoque", rootEstoque);
        tabEstoque.setClosable(false);

        // Aba 3: Cadastro de Produtos (interface de teste original)
        FXMLLoader loaderCadastro = new FXMLLoader(getClass().getResource("/fxml/cadastro-produto.fxml"));
        loaderCadastro.setControllerFactory(springContext::getBean);
        Parent rootCadastro = loaderCadastro.load();
        Tab tabCadastro = new Tab("Cadastro de Produtos", rootCadastro);
        tabCadastro.setClosable(false);

        tabPane.getTabs().addAll(tabMateriais, tabEstoque, tabCadastro);

        stage.setTitle("Gestão para Artesã - Teste de Materiais, Estoque e Produtos");
        stage.setScene(new Scene(tabPane, 760, 720));
        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
