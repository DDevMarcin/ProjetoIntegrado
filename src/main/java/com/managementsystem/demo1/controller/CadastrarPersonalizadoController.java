package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ItemMaterialInput;
import com.managementsystem.demo1.service.MaterialService;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class CadastrarPersonalizadoController {

    // ── Campos do formulário ───────────────────────────────────────────────
    @FXML private TextField  campoCodigo;
    @FXML private TextField  campoNome;
    @FXML private TextArea   campoDescricao;
    @FXML private TextField  campoPrazo;         // prazoProducaoDias
    @FXML private TextArea   campoObservacoes;   // observacoes

    // ── Preço ──────────────────────────────────────────────────────────────
    @FXML private RadioButton rbPrecoManual;
    @FXML private RadioButton rbPrecoMateriais;
    @FXML private TextField   campoValorManual;

    // ── Seção de materiais ─────────────────────────────────────────────────
    @FXML private VBox             secaoMateriais;
    @FXML private ComboBox<Material> comboMaterial;
    @FXML private TextField        campoQtdMaterial;
    @FXML private FlowPane         listaMateriais;

    // ── Preview ────────────────────────────────────────────────────────────
    @FXML private Label    previewNome;
    @FXML private Label    previewCodigo;
    @FXML private Label    previewDescricao;
    @FXML private Label    previewPrazo;
    @FXML private Label    previewObservacoes;
    @FXML private Label    previewValor;
    @FXML private FlowPane previewMateriais;

    // ── Feedback ───────────────────────────────────────────────────────────
    @FXML private Label labelStatus;

    // ── Estado interno ─────────────────────────────────────────────────────
    private final List<Material>   materiaisSelecionados = new ArrayList<>();
    private final List<BigDecimal> quantidades           = new ArrayList<>();

    private final ProdutoService  produtoService;
    private final MaterialService materialService;

    public CadastrarPersonalizadoController(ProdutoService produtoService,
                                            MaterialService materialService) {
        this.produtoService  = produtoService;
        this.materialService = materialService;
    }

    // ── Inicialização ──────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        carregarMateriais();
        configurarRadioButtons();
        configurarPreviewAoVivo();
    }

    private void carregarMateriais() {
        List<Material> lista = materialService.listarTodos();
        comboMaterial.setItems(FXCollections.observableArrayList(lista));
        comboMaterial.setConverter(new StringConverter<>() {
            @Override public String toString(Material m) {
                return m == null ? "" : m.getNome() + " (R$ " + m.getPrecoUnidade() + "/" + m.getUnidadeMedida() + ")";
            }
            @Override public Material fromString(String s) { return null; }
        });
    }

    private void configurarRadioButtons() {
        ToggleGroup grupo = new ToggleGroup();
        rbPrecoManual.setToggleGroup(grupo);
        rbPrecoMateriais.setToggleGroup(grupo);
        rbPrecoMateriais.setSelected(true);

        grupo.selectedToggleProperty().addListener((obs, ant, novo) -> {
            boolean manual = novo == rbPrecoManual;
            campoValorManual.setDisable(!manual);
            secaoMateriais.setDisable(manual);
            atualizarPreviewValor();
        });

        campoValorManual.setDisable(true);
    }

    private void configurarPreviewAoVivo() {
        campoNome.textProperty().addListener((o, a, v) ->
                previewNome.setText(v.isBlank() ? "Nome do produto" : v));
        campoCodigo.textProperty().addListener((o, a, v) ->
                previewCodigo.setText(v.isBlank() ? "—" : v));
        campoDescricao.textProperty().addListener((o, a, v) ->
                previewDescricao.setText(v.isBlank() ? "Sem descrição." : v));
        campoPrazo.textProperty().addListener((o, a, v) ->
                previewPrazo.setText(v.isBlank() ? "—" : v + " dias"));
        campoObservacoes.textProperty().addListener((o, a, v) ->
                previewObservacoes.setText(v.isBlank() ? "—" : v));
        campoValorManual.textProperty().addListener((o, a, v) -> atualizarPreviewValor());
    }

    private void atualizarPreviewValor() {
        if (rbPrecoManual.isSelected()) {
            try {
                BigDecimal v = new BigDecimal(campoValorManual.getText().replace(",", "."));
                previewValor.setText("R$ " + String.format("%.2f", v));
            } catch (NumberFormatException e) {
                previewValor.setText("R$ —");
            }
        } else {
            BigDecimal soma = BigDecimal.ZERO;
            for (int i = 0; i < materiaisSelecionados.size(); i++) {
                soma = soma.add(materiaisSelecionados.get(i).getPrecoUnidade().multiply(quantidades.get(i)));
            }
            previewValor.setText(soma.compareTo(BigDecimal.ZERO) == 0
                    ? "R$ —"
                    : "R$ " + String.format("%.2f", soma));
        }
    }

    // ── Adicionar material ─────────────────────────────────────────────────

    @FXML
    public void adicionarMaterial() {
        Material material = comboMaterial.getValue();
        if (material == null) { setStatus("Selecione um material.", true); return; }

        BigDecimal qtd;
        try {
            qtd = new BigDecimal(campoQtdMaterial.getText().replace(",", "."));
            if (qtd.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Informe uma quantidade válida para o material.", true);
            return;
        }

        materiaisSelecionados.add(material);
        quantidades.add(qtd);

        adicionarTag(listaMateriais, material.getNome() + " × " + qtd.stripTrailingZeros().toPlainString(), material);
        adicionarTag(previewMateriais, material.getNome() + " × " + qtd.stripTrailingZeros().toPlainString(), null);

        comboMaterial.setValue(null);
        campoQtdMaterial.clear();
        setStatus("", false);
        atualizarPreviewValor();
    }

    private void adicionarTag(FlowPane alvo, String texto, Material material) {
        HBox tag = new HBox(4);
        tag.getStyleClass().add("material-tag-removivel");

        Label label = new Label(texto);
        label.setStyle("-fx-text-fill: #8F031E; -fx-font-size: 12px; -fx-font-weight: bold;");
        tag.getChildren().add(label);

        if (material != null) {
            Button remover = new Button("×");
            remover.setStyle("-fx-background-color: transparent; -fx-text-fill: #8F031E; " +
                             "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 2 0 4;");
            remover.setOnAction(e -> removerMaterial(material, tag));
            tag.getChildren().add(remover);
        }

        alvo.getChildren().add(tag);
    }

    private void removerMaterial(Material material, HBox tagNoForm) {
        int idx = materiaisSelecionados.lastIndexOf(material);
        if (idx >= 0) {
            materiaisSelecionados.remove(idx);
            quantidades.remove(idx);
        }
        listaMateriais.getChildren().remove(tagNoForm);

        previewMateriais.getChildren().clear();
        for (int i = 0; i < materiaisSelecionados.size(); i++) {
            adicionarTag(previewMateriais,
                    materiaisSelecionados.get(i).getNome() + " × " + quantidades.get(i).stripTrailingZeros().toPlainString(),
                    null);
        }
        atualizarPreviewValor();
    }

    // ── Salvar ─────────────────────────────────────────────────────────────

    @FXML
    public void salvar() {
        if (campoCodigo.getText().isBlank()) { setStatus("Informe o código do produto.", true); return; }
        if (campoNome.getText().isBlank())   { setStatus("Informe o nome do produto.", true); return; }

        // Prazo é opcional — null se vazio
        Integer prazo = null;
        if (!campoPrazo.getText().isBlank()) {
            try {
                prazo = Integer.parseInt(campoPrazo.getText().trim());
                if (prazo < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                setStatus("Prazo de produção deve ser um número", true);
                return;
            }
        }

        String observacoes = campoObservacoes.getText().trim();

        boolean manual = rbPrecoManual.isSelected();
        BigDecimal valorManual = null;

        if (manual) {
            try {
                valorManual = new BigDecimal(campoValorManual.getText().replace(",", "."));
                if (valorManual.signum() <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                setStatus("Informe um valor de venda válido.", true);
                return;
            }
        } else if (materiaisSelecionados.isEmpty()) {
            setStatus("Adicione ao menos um material ou escolha 'Valor manual'.", true);
            return;
        }

        List<ItemMaterialInput> itens = new ArrayList<>();
        for (int i = 0; i < materiaisSelecionados.size(); i++) {
            itens.add(new ItemMaterialInput(materiaisSelecionados.get(i).getIdMaterial(), quantidades.get(i)));
        }

        try {
            Produto salvo = produtoService.cadastrarComMateriais(
                    campoCodigo.getText().trim(),
                    campoNome.getText().trim(),
                    campoDescricao.getText().trim(),
                    TipoProduto.PERSONALIZADO,
                    null,         // quantidadeEstoque — não se aplica
                    prazo,
                    observacoes.isBlank() ? null : observacoes,
                    manual,
                    valorManual,
                    itens
            );

            setStatus("Produto \"" + salvo.getNome() + "\" cadastrado com sucesso!", false);
            limpar();

        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        } catch (Exception e) {
            setStatus("Erro inesperado: " + e.getMessage(), true);
        }
    }

    // ── Voltar ─────────────────────────────────────────────────────────────

    @FXML
    public void voltar() {
        javafx.scene.Scene scene = campoCodigo.getScene();
        if (scene == null) return;

        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navProdutos();
        }
    }

    // ── Limpar formulário ──────────────────────────────────────────────────

    @FXML
    public void limpar() {
        campoCodigo.clear();
        campoNome.clear();
        campoDescricao.clear();
        campoPrazo.clear();
        campoObservacoes.clear();
        campoValorManual.clear();
        comboMaterial.setValue(null);
        campoQtdMaterial.clear();
        materiaisSelecionados.clear();
        quantidades.clear();
        listaMateriais.getChildren().clear();

        rbPrecoMateriais.setSelected(true);
        campoValorManual.setDisable(true);
        secaoMateriais.setDisable(false);

        previewNome.setText("Nome do produto");
        previewCodigo.setText("—");
        previewDescricao.setText("Sem descrição.");
        previewPrazo.setText("—");
        previewObservacoes.setText("—");
        previewValor.setText("R$ —");
        previewMateriais.getChildren().clear();
    }

    private void setStatus(String msg, boolean erro) {
        labelStatus.setText(msg);
        labelStatus.setStyle(erro
                ? "-fx-text-fill: #D31946; -fx-font-size: 18px;"
                : "-fx-text-fill: #2e7d32; -fx-font-size: 18px;");
    }
}
