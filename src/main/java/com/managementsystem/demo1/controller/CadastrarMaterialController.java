package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.service.MaterialService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Scope("prototype")
public class CadastrarMaterialController {

    // ── Campos do formulário ───────────────────────────────────────────────
    @FXML private TextField campoNome;
    @FXML private TextField campoTipo;       // usado como "tipo/categoria", mapeado para cor por enquanto
    @FXML private TextField campoCor;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoUnidade;
    @FXML private TextArea  campoDetalhe;    // exibido no preview, sem campo correspondente no model
    @FXML private TextField campoPreco;

    // ── Preview ────────────────────────────────────────────────────────────
    @FXML private Label previewNome;
    @FXML private Label previewTipoBadge;
    @FXML private Label previewPreco;
    @FXML private Label previewEstoque;
    @FXML private Label previewCor;
    @FXML private Label previewDetalhe;

    // ── Feedback ───────────────────────────────────────────────────────────
    @FXML private Label labelStatus;

    private final MaterialService materialService;

    public CadastrarMaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    // ── Inicialização ──────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        configurarPreviewAoVivo();
    }

    private void configurarPreviewAoVivo() {
        campoNome.textProperty().addListener((o, a, v) ->
                previewNome.setText(v.isBlank() ? "Nome do material" : v));

        campoTipo.textProperty().addListener((o, a, v) ->
                previewTipoBadge.setText(v.isBlank() ? "—" : v));

        campoCor.textProperty().addListener((o, a, v) ->
                previewCor.setText(v.isBlank() ? "—" : v));

        campoDetalhe.textProperty().addListener((o, a, v) ->
                previewDetalhe.setText(v.isBlank() ? "Sem descricao." : v));

        campoPreco.textProperty().addListener((o, a, v) -> {
            try {
                BigDecimal preco = new BigDecimal(v.replace(",", "."));
                previewPreco.setText("R$ " + String.format("%.2f", preco));
            } catch (NumberFormatException e) {
                previewPreco.setText("R$ —");
            }
        });

        campoQuantidade.textProperty().addListener((o, a, v) -> atualizarPreviewEstoque());
        campoUnidade.textProperty().addListener((o, a, v) -> atualizarPreviewEstoque());
    }

    private void atualizarPreviewEstoque() {
        String qtd   = campoQuantidade.getText().trim();
        String unid  = campoUnidade.getText().trim();
        if (qtd.isBlank()) {
            previewEstoque.setText("—");
        } else {
            previewEstoque.setText(qtd + (unid.isBlank() ? "" : " " + unid));
        }
    }

    // ── Salvar ─────────────────────────────────────────────────────────────

    @FXML
    public void salvar() {
        String nome  = campoNome.getText().trim();
        String tipo  = campoTipo.getText().trim();
        String cor   = campoCor.getText().trim();
        String unid  = campoUnidade.getText().trim();

        if (nome.isBlank())  { setStatus("Informe o nome do material.", true); return; }
        if (tipo.isBlank())  { setStatus("Informe o tipo do material.", true); return; }
        if (unid.isBlank())  { setStatus("Informe a unidade de medida.", true); return; }

        BigDecimal preco;
        try {
            preco = new BigDecimal(campoPreco.getText().replace(",", "."));
            if (preco.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("Informe um preço válido (maior que zero).", true);
            return;
        }

        BigDecimal qtdEstoque = BigDecimal.ZERO;
        if (!campoQuantidade.getText().isBlank()) {
            try {
                qtdEstoque = new BigDecimal(campoQuantidade.getText().replace(",", "."));
                if (qtdEstoque.signum() < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                setStatus("Quantidade em estoque deve ser um número não-negativo.", true);
                return;
            }
        }

        // O campo "Tipo de Material" do protótipo não tem mapeamento direto no model;
        // usamos o valor do tipo como cor se o campo cor estiver vazio,
        // ou concatenamos como "tipo — cor" para deixar claro no campo cor do model.
        String corFinal = cor.isBlank() ? tipo : tipo + (cor.isBlank() ? "" : " — " + cor);

        try {
            Material salvo = materialService.salvar(nome, corFinal, preco, unid, qtdEstoque);
            setStatus("Material \"" + salvo.getNome() + "\" cadastrado com sucesso!", false);
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
        javafx.scene.Scene scene = campoNome.getScene();
        if (scene == null) return;

        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navEstoque();
        }
    }

    // ── Limpar formulário ──────────────────────────────────────────────────

    @FXML
    public void limpar() {
        campoNome.clear();
        campoTipo.clear();
        campoCor.clear();
        campoQuantidade.clear();
        campoUnidade.clear();
        campoDetalhe.clear();
        campoPreco.clear();

        previewNome.setText("Nome do material");
        previewTipoBadge.setText("—");
        previewPreco.setText("R$ —");
        previewEstoque.setText("—");
        previewCor.setText("—");
        previewDetalhe.setText("Sem descricao.");
    }

    private void setStatus(String msg, boolean erro) {
        labelStatus.setText(msg);
        labelStatus.setStyle(erro
                ? "-fx-text-fill: #D31946; -fx-font-size: 15px;"
                : "-fx-text-fill: #2e7d32; -fx-font-size: 15px;");
    }
}
