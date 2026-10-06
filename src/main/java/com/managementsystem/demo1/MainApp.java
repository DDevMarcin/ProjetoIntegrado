package com.managementsystem.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cadastro-produto.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();

        stage.setTitle("Gestão de Produtos - Artesã");
        stage.setScene(new Scene(root, 650, 600));
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