package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.service.MaterialService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Scope("prototype")
public class EditarMaterialController {

    @FXML private Label     labelTitulo;
    @FXML private TextField campoNome;
    @FXML private TextField campoCor;
    @FXML private TextField campoPreco;
    @FXML private TextField campoUnidade;
    @FXML private TextField campoEstoque;   // somente leitura
    @FXML private Label     labelStatus;

    private Material material;
    private final MaterialService materialService;

    public EditarMaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @FXML
    public void initialize() {
        // campos preenchidos via setMaterial()
    }

    /** Chamado pelo EstoqueController após carregar o FXML. */
    public void setMaterial(Material material) {
        this.material = material;
        labelTitulo.setText("Editar: " + material.getNome());
        campoNome.setText(material.getNome());
        campoCor.setText(material.getCor() != null ? material.getCor() : "");
        campoPreco.setText(material.getPrecoUnidade() != null
                ? material.getPrecoUnidade().toPlainString() : "");
        campoUnidade.setText(material.getUnidadeMedida() != null
                ? material.getUnidadeMedida() : "");
        BigDecimal estoqueAtual = material.getQuantidadeEstoque() != null
                ? material.getQuantidadeEstoque() : BigDecimal.ZERO;
        campoEstoque.setText(estoqueAtual.toPlainString()
                + (material.getUnidadeMedida() != null ? " " + material.getUnidadeMedida() : ""));
    }

    @FXML
    public void salvar() {
        String nome  = campoNome.getText().trim();
        String cor   = campoCor.getText().trim();
        String unid  = campoUnidade.getText().trim();

        if (nome.isBlank())  { setStatus("Nome é obrigatório.", true); return; }
        if (unid.isBlank())  { setStatus("Unidade de medida é obrigatória.", true); return; }

        BigDecimal preco;
        try {
            preco = new BigDecimal(campoPreco.getText().replace(",", "."));
            if (preco.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Informe um preço válido (maior que zero).", true);
            return;
        }

        try {
            materialService.atualizar(material.getIdMaterial(), nome, cor, preco, unid);
            setStatus("Material atualizado com sucesso!", false);

            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(this::voltar);
            }).start();

        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        } catch (Exception e) {
            setStatus("Erro inesperado: " + e.getMessage(), true);
        }
    }

    @FXML
    public void voltar() {
        javafx.scene.Scene scene = campoNome.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navEstoque();
        }
    }

    private void setStatus(String msg, boolean erro) {
        labelStatus.setText(msg);
        labelStatus.setStyle(erro
                ? "-fx-text-fill: #D31946; -fx-font-size: 15px;"
                : "-fx-text-fill: #2e7d32; -fx-font-size: 15px;");
    }
}
