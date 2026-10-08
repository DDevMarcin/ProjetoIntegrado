package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class ProdutosController {

    // ── Cabeçalho ──────────────────────────────────────────────────────────
    @FXML private TextField campoBusca;

    // ── Tabela ─────────────────────────────────────────────────────────────
    @FXML private TableView<Produto>             tabelaProdutos;
    @FXML private TableColumn<Produto, String>   colNome;
    @FXML private TableColumn<Produto, String>   colCodigo;
    @FXML private TableColumn<Produto, String>   colQuantidade;
    @FXML private TableColumn<Produto, String>   colValor;

    // ── Rodapé da tabela ───────────────────────────────────────────────────
    @FXML private Label  labelTotal;
    @FXML private Label  labelPagina;
    @FXML private Button btnAnterior;
    @FXML private Button btnProximo;

    // ── Painel de detalhes ─────────────────────────────────────────────────
    @FXML private ScrollPane painelDetalhes;
    @FXML private Label      detNome;
    @FXML private Label      detCodigo;
    @FXML private Label      detValor;
    @FXML private Label      detEstoque;
    @FXML private Label      detTotalVendido;
    @FXML private Label      detValorTotal;
    @FXML private Label      detDescricao;
    @FXML private FlowPane   painelMateriais;
    @FXML private VBox       secaoPrazo;
    @FXML private Label      detPrazo;

    // ── Paginação ──────────────────────────────────────────────────────────
    private static final int ITENS_POR_PAGINA = 14;
    private int paginaAtual = 1;

    private List<Produto> todosProdutos    = List.of();
    private List<Produto> produtosFiltrados = List.of();

    private final ProdutoService produtoService;
    private final ApplicationContext springContext;

    public ProdutosController(ProdutoService produtoService, ApplicationContext springContext) {
        this.produtoService = produtoService;
        this.springContext  = springContext;
    }

    // ── Inicialização ──────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        tabelaProdutos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        configurarColunas();
        configurarSelecao();
        carregarProdutos();

        campoBusca.textProperty().addListener((obs, ant, novo) -> filtrar(novo));
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNome()));

        colCodigo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCodigo()));

        colQuantidade.setCellValueFactory(c -> {
            Produto p = c.getValue();
            String qtd = (p.getTipo() == TipoProduto.PRE_PRONTO && p.getQuantidadeEstoque() != null)
                    ? String.format("%02d", p.getQuantidadeEstoque())
                    : "—";
            return new SimpleStringProperty(qtd);
        });

        colValor.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValor();
            return new SimpleStringProperty(v != null ? "R$" + String.format("%.2f", v) : "—");
        });

        colCodigo.setStyle("-fx-alignment: CENTER;");
        colQuantidade.setStyle("-fx-alignment: CENTER;");
        colValor.setStyle("-fx-alignment: CENTER;");
    }

    private void configurarSelecao() {
        tabelaProdutos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, selecionado) -> {
                    if (selecionado != null) {
                        mostrarDetalhes(selecionado);
                    }
                });
    }

    // ── Dados ──────────────────────────────────────────────────────────────

    private void carregarProdutos() {
        todosProdutos = produtoService.listarTodos();
        filtrar(campoBusca.getText());
    }

    // ── Busca e paginação ──────────────────────────────────────────────────

    private void filtrar(String termo) {
        paginaAtual = 1;
        if (termo == null || termo.isBlank()) {
            produtosFiltrados = todosProdutos;
        } else {
            String lower = termo.toLowerCase();
            produtosFiltrados = todosProdutos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(lower)
                              || p.getCodigo().toLowerCase().contains(lower))
                    .toList();
        }
        exibirPagina();
    }

    private void exibirPagina() {
        int total       = produtosFiltrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / ITENS_POR_PAGINA));

        paginaAtual = Math.max(1, Math.min(paginaAtual, totalPaginas));

        int inicio = (paginaAtual - 1) * ITENS_POR_PAGINA;
        int fim    = Math.min(inicio + ITENS_POR_PAGINA, total);

        tabelaProdutos.setItems(FXCollections.observableArrayList(produtosFiltrados.subList(inicio, fim)));

        labelTotal.setText("Total de Produtos: " + total);
        labelPagina.setText("Página " + paginaAtual);
        btnAnterior.setDisable(paginaAtual <= 1);
        btnProximo.setDisable(paginaAtual >= totalPaginas);
    }

    // ── Painel de detalhes ─────────────────────────────────────────────────

    private void mostrarDetalhes(Produto p) {
        // Nome e código
        detNome.setText(p.getNome());
        detCodigo.setText(p.getCodigo());

        // Valor
        detValor.setText(p.getValor() != null
                ? "R$ " + String.format("%.2f", p.getValor())
                : "—");

        // Estoque (apenas PRE_PRONTO)
        if (p.getTipo() == TipoProduto.PRE_PRONTO && p.getQuantidadeEstoque() != null) {
            detEstoque.setText(p.getQuantidadeEstoque() + " unidades");
        } else if (p.getTipo() == TipoProduto.PERSONALIZADO) {
            detEstoque.setText("Produto personalizado");
        } else {
            detEstoque.setText("—");
        }

        // Stats financeiros (placeholder — requer módulo de vendas futuro)
        detTotalVendido.setText("—");
        detValorTotal.setText("—");

        // Descrição
        detDescricao.setText(
                Optional.ofNullable(p.getDescricao())
                        .filter(d -> !d.isBlank())
                        .orElse("Sem descrição cadastrada."));

        // Materiais como tags
        painelMateriais.getChildren().clear();
        List<ProdutoMaterial> materiais = p.getMateriaisUtilizados();
        if (materiais != null && !materiais.isEmpty()) {
            for (ProdutoMaterial pm : materiais) {
                Label tag = new Label(
                        pm.getQuantidadeUtilizada().stripTrailingZeros().toPlainString()
                        + "x " + pm.getMaterial().getNome());
                tag.getStyleClass().add("material-tag");
                painelMateriais.getChildren().add(tag);
            }
        } else {
            Label sem = new Label("Nenhum material registrado.");
            sem.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
            painelMateriais.getChildren().add(sem);
        }

        // Prazo (apenas PERSONALIZADO)
        boolean isPersonalizado = p.getTipo() == TipoProduto.PERSONALIZADO;
        secaoPrazo.setVisible(isPersonalizado);
        secaoPrazo.setManaged(isPersonalizado);
        if (isPersonalizado) {
            detPrazo.setText(p.getPrazoProducaoDias() != null
                    ? p.getPrazoProducaoDias() + " dias"
                    : "Não informado");
        }

        // Exibe o painel
        painelDetalhes.setVisible(true);
        painelDetalhes.setManaged(true);
    }

    // ── Handlers paginação ─────────────────────────────────────────────────

    @FXML
    public void paginaAnterior() {
        if (paginaAtual > 1) { paginaAtual--; exibirPagina(); }
    }

    @FXML
    public void proximaPagina() {
        paginaAtual++;
        exibirPagina();
    }

    // ── Handlers botões criar ──────────────────────────────────────────────

    @FXML
    public void abrirFormPrePronto() {
        javafx.scene.Scene scene = tabelaProdutos.getScene();
        if (scene == null) return;

        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navCadastrarPrePronto();
        }
    }

    @FXML
    public void abrirFormPersonalizado() {
        javafx.scene.Scene scene = tabelaProdutos.getScene();
        if (scene == null) return;

        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navCadastrarPersonalizado();
        }
    }

    // ── Navegação interna ──────────────────────────────────────────────────

    private void navegarPara(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            javafx.scene.Node pagina = loader.load();

            // Sobe pelo grafo até encontrar o StackPane contentArea do MainLayout
            javafx.scene.Node atual = tabelaProdutos.getParent();
            while (atual != null && !(atual instanceof javafx.scene.layout.StackPane)) {
                atual = atual.getParent();
            }
            if (atual instanceof javafx.scene.layout.StackPane sp) {
                sp.getChildren().setAll(pagina);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    // ── Handlers botões do painel de detalhes ──────────────────────────────

    @FXML
    public void deletarProduto() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Dialog<ButtonType> dialog = criarDialogConfirmacao(selecionado.getNome());
        
        dialog.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                produtoService.excluir(selecionado.getIdProduto());
                painelDetalhes.setVisible(false);
                painelDetalhes.setManaged(false);
                carregarProdutos();
            }
        });
    }

    private Dialog<ButtonType> criarDialogConfirmacao(String nomeProduto) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmar exclusão");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        // ═══ Conteúdo customizado ═══
        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24, 28, 24, 28));
        conteudo.setStyle("-fx-background-color: #FFF0F5; -fx-background-radius: 12;");

        // Título
        Label titulo = new Label("Deletar \"" + nomeProduto + "\"?");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 19));
        titulo.setTextFill(Color.web("#D31946"));
        titulo.setWrapText(true);

        // Mensagem
        Label mensagem = new Label("Esta ação não pode ser desfeita.");
        mensagem.setFont(Font.font("System", 15));
        mensagem.setTextFill(Color.web("#555"));
        mensagem.setWrapText(true);

        // Botões customizados
        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1.5; " +
            "-fx-text-fill: #555; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 20 8 20; " +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: #D31946; " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1.5; " +
            "-fx-text-fill: #D31946; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 20 8 20; " +
            "-fx-cursor: hand;"
        ));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1.5; " +
            "-fx-text-fill: #555; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 20 8 20; " +
            "-fx-cursor: hand;"
        ));
        btnCancelar.setOnAction(e -> {
            dialog.setResult(ButtonType.CANCEL);
            dialog.close();
        });

        Button btnDeletar = new Button("Deletar");
        btnDeletar.setStyle(
            "-fx-background-color: #D31946; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 24 8 24; " +
            "-fx-cursor: hand;"
        );
        btnDeletar.setOnMouseEntered(e -> btnDeletar.setStyle(
            "-fx-background-color: #8F031E; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 24 8 24; " +
            "-fx-cursor: hand;"
        ));
        btnDeletar.setOnMouseExited(e -> btnDeletar.setStyle(
            "-fx-background-color: #D31946; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 24 8 24; " +
            "-fx-cursor: hand;"
        ));
        btnDeletar.setOnAction(e -> {
            dialog.setResult(ButtonType.OK);
            dialog.close();
        });

        botoes.getChildren().addAll(btnCancelar, btnDeletar);
        
        conteudo.getChildren().addAll(titulo, mensagem, botoes);
        dialog.getDialogPane().setContent(conteudo);
        
        // Remove os botões padrão do dialog
        dialog.getDialogPane().getButtonTypes().clear();
        
        // Estilo do DialogPane para tirar o fundo branco padrão
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        
        return dialog;
    }

    @FXML
    public void editarProduto() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        javafx.scene.Scene scene = tabelaProdutos.getScene();
        if (scene == null) return;

        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navEditarProduto(selecionado);
        }
    }

    public void recarregar() {
        carregarProdutos();
    }
}
