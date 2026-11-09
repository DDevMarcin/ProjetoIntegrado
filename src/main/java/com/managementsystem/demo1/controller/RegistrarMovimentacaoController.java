package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
import com.managementsystem.demo1.service.MaterialService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Scope("prototype")
public class RegistrarMovimentacaoController {

    @FXML private Label         labelTitulo;
    @FXML private Label         labelSubtitulo;

    // Formulário
    @FXML private ComboBox<Material> comboMaterial;
    @FXML private RadioButton   rbEntrada;
    @FXML private RadioButton   rbSaida;
    @FXML private RadioButton   rbPerda;
    @FXML private TextField     campoQuantidade;
    @FXML private Label         labelUnidade;
    @FXML private Label         labelMotivo;
    @FXML private TextField     campoMotivo;
    @FXML private TextArea      campoObservacoes;
    @FXML private Label         labelStatus;
    @FXML private Button        btnConfirmar;

    // Painel lateral de info
    @FXML private VBox  painelInfoMaterial;
    @FXML private Label infoNome;
    @FXML private Label infoEstoque;
    @FXML private Label infoCor;
    @FXML private Label infoUnidade;
    @FXML private Label infoPreco;
    @FXML private VBox  painelPreviewSaldo;
    @FXML private Label infoNovoSaldo;

    // Estado
    private TipoMovimentacaoMaterial tipoInicial = null; // se vier pré-selecionado
    private Material materialInicial = null;

    private final MaterialService materialService;

    public RegistrarMovimentacaoController(MaterialService materialService) {
        this.materialService = materialService;
    }

    // ── Chamado pelo EstoqueController para pré-selecionar material e/ou tipo ──

    public void setContexto(Material material, TipoMovimentacaoMaterial tipo) {
        this.materialInicial = material;
        this.tipoInicial     = tipo;
    }

    @FXML
    public void initialize() {
        configurarToggleGroup();
        carregarMateriais();
        configurarListeners();
    }

    /** Chamado pelo MainLayoutController depois do load, com contexto opcional. */
    public void aplicarContexto() {
        if (tipoInicial != null) {
            switch (tipoInicial) {
                case ENTRADA -> rbEntrada.setSelected(true);
                case SAIDA   -> rbSaida.setSelected(true);
                case PERDA   -> rbPerda.setSelected(true);
            }
            atualizarEstiloTipo();
        }
        if (materialInicial != null) {
            comboMaterial.setValue(materialInicial);
            atualizarInfoMaterial(materialInicial);
        }
    }

    private void configurarToggleGroup() {
        ToggleGroup grupo = new ToggleGroup();
        rbEntrada.setToggleGroup(grupo);
        rbSaida.setToggleGroup(grupo);
        rbPerda.setToggleGroup(grupo);
        rbEntrada.setSelected(true);
        atualizarEstiloTipo();

        grupo.selectedToggleProperty().addListener((obs, ant, novo) -> atualizarEstiloTipo());
    }

    private void atualizarEstiloTipo() {
        boolean isPerda = rbPerda.isSelected();

        labelMotivo.setText(isPerda ? "Motivo da perda *" : "Motivo (opcional)");
        labelMotivo.getStyleClass().clear();
        labelMotivo.getStyleClass().add(isPerda ? "form-label-required" : "form-label");

        String tituloBotao = isPerda ? "✓  Registrar Perda" : "✓  Registrar";
        btnConfirmar.setText(tituloBotao);

        // Atualiza preview do saldo se já tem material e quantidade
        atualizarPreviewSaldo();
    }

    private void carregarMateriais() {
        List<Material> lista = materialService.listarTodos();
        comboMaterial.setItems(FXCollections.observableArrayList(lista));
        comboMaterial.setConverter(new StringConverter<>() {
            @Override public String toString(Material m) {
                if (m == null) return "";
                String qtd = m.getQuantidadeEstoque() != null
                        ? m.getQuantidadeEstoque().toPlainString() : "0";
                return m.getNome() + "  (estoque: " + qtd + " " + m.getUnidadeMedida() + ")";
            }
            @Override public Material fromString(String s) { return null; }
        });
    }

    private void configurarListeners() {
        comboMaterial.valueProperty().addListener((obs, ant, mat) -> {
            if (mat != null) {
                atualizarInfoMaterial(mat);
            } else {
                painelInfoMaterial.setVisible(false);
                painelInfoMaterial.setManaged(false);
            }
        });

        campoQuantidade.textProperty().addListener((obs, ant, novo) -> atualizarPreviewSaldo());
    }

    private void atualizarInfoMaterial(Material m) {
        infoNome.setText(m.getNome());
        BigDecimal estoqueAtual = m.getQuantidadeEstoque() != null
                ? m.getQuantidadeEstoque() : BigDecimal.ZERO;
        infoEstoque.setText(estoqueAtual.toPlainString() + " " + m.getUnidadeMedida());
        infoCor.setText(m.getCor() != null && !m.getCor().isBlank() ? m.getCor() : "—");
        infoUnidade.setText(m.getUnidadeMedida());
        infoPreco.setText(m.getPrecoUnidade() != null
                ? "R$ " + String.format("%.2f", m.getPrecoUnidade()) : "—");
        labelUnidade.setText(m.getUnidadeMedida());

        painelInfoMaterial.setVisible(true);
        painelInfoMaterial.setManaged(true);

        atualizarPreviewSaldo();
    }

    private void atualizarPreviewSaldo() {
        Material mat = comboMaterial.getValue();
        if (mat == null) {
            painelPreviewSaldo.setVisible(false);
            painelPreviewSaldo.setManaged(false);
            return;
        }

        BigDecimal qtdStr;
        try {
            qtdStr = new BigDecimal(campoQuantidade.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            painelPreviewSaldo.setVisible(false);
            painelPreviewSaldo.setManaged(false);
            return;
        }

        BigDecimal atual = mat.getQuantidadeEstoque() != null
                ? mat.getQuantidadeEstoque() : BigDecimal.ZERO;
        BigDecimal novo;

        if (rbEntrada.isSelected()) {
            novo = atual.add(qtdStr);
            infoNovoSaldo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        } else {
            novo = atual.subtract(qtdStr);
            infoNovoSaldo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                    + (novo.signum() < 0 ? "#D31946" : "#D31946") + ";");
        }

        infoNovoSaldo.setText(novo.toPlainString() + " " + mat.getUnidadeMedida());
        painelPreviewSaldo.setVisible(true);
        painelPreviewSaldo.setManaged(true);
    }

    @FXML
    public void registrar() {
        Material mat = comboMaterial.getValue();
        if (mat == null) { setStatus("Selecione um material.", true); return; }

        TipoMovimentacaoMaterial tipo;
        if (rbEntrada.isSelected())     tipo = TipoMovimentacaoMaterial.ENTRADA;
        else if (rbSaida.isSelected())  tipo = TipoMovimentacaoMaterial.SAIDA;
        else                            tipo = TipoMovimentacaoMaterial.PERDA;

        BigDecimal quantidade;
        try {
            quantidade = new BigDecimal(campoQuantidade.getText().replace(",", "."));
            if (quantidade.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Informe uma quantidade válida (maior que zero).", true);
            return;
        }

        String motivo = campoMotivo.getText().trim();
        if (tipo == TipoMovimentacaoMaterial.PERDA && motivo.isBlank()) {
            setStatus("Motivo é obrigatório para registrar uma perda.", true);
            return;
        }

        String obs = campoObservacoes.getText().trim();

        try {
            materialService.registrarMovimentacao(
                    mat.getIdMaterial(), tipo, quantidade,
                    motivo.isBlank() ? null : motivo,
                    obs.isBlank() ? null : obs);

            String tipoStr = switch (tipo) {
                case ENTRADA -> "Entrada";
                case SAIDA   -> "Saída";
                case PERDA   -> "Perda";
            };
            setStatus(tipoStr + " de " + quantidade.toPlainString() + " "
                    + mat.getUnidadeMedida() + " registrada com sucesso!", false);
            limpar();

        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        } catch (Exception e) {
            setStatus("Erro inesperado: " + e.getMessage(), true);
        }
    }

    @FXML
    public void limpar() {
        comboMaterial.setValue(null);
        rbEntrada.setSelected(true);
        campoQuantidade.clear();
        campoMotivo.clear();
        campoObservacoes.clear();
        labelUnidade.setText("");
        painelInfoMaterial.setVisible(false);
        painelInfoMaterial.setManaged(false);
        painelPreviewSaldo.setVisible(false);
        painelPreviewSaldo.setManaged(false);
        atualizarEstiloTipo();
    }

    @FXML
    public void voltar() {
        javafx.scene.Scene scene = comboMaterial.getScene();
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
