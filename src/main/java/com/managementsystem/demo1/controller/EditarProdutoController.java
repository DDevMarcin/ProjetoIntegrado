package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Edição de produto — pré-pronto ou personalizado.
 *
 * Limitações do backend atual (repassar para a equipe):
 *   1. Valor/preço não pode ser alterado.
 *   2. Materiais utilizados não podem ser alterados.
 *
 * Campos editáveis: código, nome, descrição, quantidade (PRE_PRONTO)
 *                   ou prazo + observações (PERSONALIZADO).
 */
@Component
@Scope("prototype")
public class EditarProdutoController {

    // ── Campos comuns ──────────────────────────────────────────────────────
    @FXML private Label     labelTitulo;
    @FXML private TextField campoCodigo;
    @FXML private TextField campoNome;
    @FXML private TextArea  campoDescricao;

    // ── PRE_PRONTO ─────────────────────────────────────────────────────────
    @FXML private VBox      vboxQuantidade;
    @FXML private TextField campoQuantidade;

    // ── PERSONALIZADO ──────────────────────────────────────────────────────
    @FXML private VBox      vboxPrazo;
    @FXML private TextField campoPrazo;
    @FXML private VBox      vboxObservacoes;
    @FXML private TextArea  campoObservacoes;

    // ── Feedback ───────────────────────────────────────────────────────────
    @FXML private Label labelStatus;

    // ── Estado interno ─────────────────────────────────────────────────────
    private Produto produto;
    private final ProdutoService produtoService;

    public EditarProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @FXML
    public void initialize() {
        // Campos específicos de tipo ficam ocultos até setProduto() ser chamado
        setVisible(vboxQuantidade, false);
        setVisible(vboxPrazo, false);
        setVisible(vboxObservacoes, false);
    }

    /**
     * Chamado pelo ProdutosController logo após carregar o FXML,
     * antes de exibir a tela.
     */
    public void setProduto(Produto produto) {
        this.produto = produto;
        preencherFormulario();
    }

    // ── Preenchimento inicial ──────────────────────────────────────────────

    private void preencherFormulario() {
        labelTitulo.setText("Editar: " + produto.getNome());
        campoCodigo.setText(produto.getCodigo());
        campoNome.setText(produto.getNome());
        campoDescricao.setText(nvl(produto.getDescricao()));

        if (produto.getTipo() == TipoProduto.PRE_PRONTO) {
            campoQuantidade.setText(produto.getQuantidadeEstoque() != null
                    ? produto.getQuantidadeEstoque().toString() : "0");
            setVisible(vboxQuantidade, true);

        } else { // PERSONALIZADO
            campoPrazo.setText(produto.getPrazoProducaoDias() != null
                    ? produto.getPrazoProducaoDias().toString() : "");
            campoObservacoes.setText(nvl(produto.getObservacoes()));
            setVisible(vboxPrazo, true);
            setVisible(vboxObservacoes, true);
        }
    }

    // ── Salvar ─────────────────────────────────────────────────────────────

    @FXML
    public void salvar() {
        if (campoNome.getText().isBlank()) {
            setStatus("Nome do produto é obrigatório.", true);
            return;
        }

        Integer qtdEstoque = null;
        Integer prazo = null;
        String  observacoes = null;

        try {
            if (produto.getTipo() == TipoProduto.PRE_PRONTO) {
                qtdEstoque = Integer.parseInt(campoQuantidade.getText().trim());
                if (qtdEstoque < 0) {
                    setStatus("Quantidade não pode ser negativa.", true);
                    return;
                }
            } else {
                String prazoTxt = campoPrazo.getText().trim();
                if (!prazoTxt.isBlank()) {
                    prazo = Integer.parseInt(prazoTxt);
                    if (prazo < 0) {
                        setStatus("Prazo não pode ser negativo.", true);
                        return;
                    }
                }
                String obsTxt = campoObservacoes.getText().trim();
                observacoes = obsTxt.isBlank() ? null : obsTxt;
            }
        } catch (NumberFormatException e) {
            setStatus("Valores numéricos inválidos.", true);
            return;
        }

        try {
            produtoService.atualizar(
                    produto.getIdProduto(),
                    campoCodigo.getText().trim(),
                    campoNome.getText().trim(),
                    campoDescricao.getText().trim(),
                    qtdEstoque,
                    prazo,
                    observacoes
            );

            setStatus("Produto atualizado com sucesso!", false);

            // Volta para a tela de produtos após breve pausa para o usuário ver o sucesso
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

    // ── Utilitários ────────────────────────────────────────────────────────

    private void setVisible(VBox vbox, boolean visivel) {
        vbox.setVisible(visivel);
        vbox.setManaged(visivel);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private void setStatus(String msg, boolean erro) {
        labelStatus.setText(msg);
        labelStatus.setStyle(erro
                ? "-fx-text-fill: #D31946; -fx-font-size: 18px;"
                : "-fx-text-fill: #2e7d32; -fx-font-size: 18px;");
    }
}
