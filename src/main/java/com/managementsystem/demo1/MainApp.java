package com.managementsystem.demo1;

import com.managementsystem.demo1.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp extends Application {

    private AnnotationConfigApplicationContext springContext;

    @Override
    public void init() {
        // Sobe o contexto do Spring ANTES da tela abrir
        springContext = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cadastro-produto.fxml"));

        // Ponto-chave: em vez do JavaFX instanciar o controller sozinho (construtor vazio),
        // pedimos pro Spring criar o bean -> assim o @Autowired do service funciona.
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();

        stage.setTitle("Gestão de Produtos - Artesã");
        stage.setScene(new Scene(root, 600, 500));
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