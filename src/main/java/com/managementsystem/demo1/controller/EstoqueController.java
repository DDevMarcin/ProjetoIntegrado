package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
import com.managementsystem.demo1.service.MaterialService;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.fxml.FXML;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class EstoqueController {

    // ── Cards de alerta ────────────────────────────────────────────────────
    @FXML private Label labelForaEstoque;
    @FXML private Label labelEstoqueBaixo;
    @FXML private HBox  cardForaEstoque;
    @FXML private HBox  cardEstoqueBaixo;

    // ── Tabela de Materiais ────────────────────────────────────────────────
    @FXML private TextField                        campoBuscaMaterial;
    @FXML private TableView<Material>              tabelaMateriais;
    @FXML private TableColumn<Material, String>    colMatNome;
    @FXML private TableColumn<Material, String>    colMatCodigo;
    @FXML private TableColumn<Material, String>    colMatQuantidade;
    @FXML private TableColumn<Material, String>    colMatCor;
    @FXML private TableColumn<Material, String>    colMatValor;
    @FXML private Label                            labelTotalMateriais;
    @FXML private Label                            labelSubtituloMateriais;
    @FXML private Label                            labelPaginaMat;
    @FXML private Button                           btnAnteriorMat;
    @FXML private Button                           btnProximoMat;

    // ── Tabela de Produtos ─────────────────────────────────────────────────
    @FXML private TextField                        campoBuscaProduto;
    @FXML private TableView<Produto>               tabelaProdutos;
    @FXML private TableColumn<Produto, String>     colProdNome;
    @FXML private TableColumn<Produto, String>     colProdCodigo;
    @FXML private TableColumn<Produto, String>     colProdQuantidade;
    @FXML private TableColumn<Produto, String>     colProdValor;
    @FXML private Label                            labelTotalProdutos;
    @FXML private Label                            labelSubtituloProdutos;
    @FXML private Label                            labelPaginaProd;
    @FXML private Button                           btnAnteriorProd;
    @FXML private Button                           btnProximoProd;

    // ── Painel de detalhes (compartilhado) ────────────────────────────────
    @FXML private ScrollPane painelDetalhes;
    @FXML private Label      detNome;
    @FXML private Label      detCodigo;
    @FXML private Label      detValor;
    @FXML private Label      detEstoque;
    @FXML private Label      detEstoqueLabel;
    @FXML private HBox       secaoUnidade;
    @FXML private Label      detUnidade;
    @FXML private HBox       secaoCor;
    @FXML private Label      detCor;
    @FXML private Label      detStatTitulo1;
    @FXML private Label      detStatValor1;
    @FXML private Label      detStatTitulo2;
    @FXML private Label      detStatValor2;
    @FXML private Label      detDescricaoTitulo;
    @FXML private Label      detDescricao;
    @FXML private VBox       secaoMateriaisProduto;
    @FXML private FlowPane   painelMateriaisProduto;
    @FXML private VBox       secaoPrazo;
    @FXML private Label      detPrazo;
    @FXML private HBox       secaoTipo;
    @FXML private Label      detTipo;
    @FXML private VBox       secaoMovimentacao;

    // ── Paginação ──────────────────────────────────────────────────────────
    private static final int ITENS_POR_PAGINA = 8;
    private static final BigDecimal LIMITE_ESTOQUE_BAIXO = new BigDecimal("5");

    private int paginaMat  = 1;
    private int paginaProd = 1;

    private List<Material> todosMateriais     = List.of();
    private List<Material> materiaisFiltrados = List.of();
    private List<Produto>  todosProdutos      = List.of();
    private List<Produto>  produtosFiltrados  = List.of();

    // estado dos filtros de alerta
    private boolean filtrandoForaEstoque  = false;
    private boolean filtrandoBaixoEstoque = false;


    private Material materialSelecionado = null;
    private Produto  produtoSelecionado  = null;

    // ── Dependências ──────────────────────────────────────────────────────
    private final MaterialService materialService;
    private final ProdutoService  produtoService;

    public EstoqueController(MaterialService materialService, ProdutoService produtoService) {
        this.materialService = materialService;
        this.produtoService  = produtoService;
    }

    // ── Inicialização ──────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        tabelaMateriais.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabelaProdutos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        configurarColunasMateriais();
        configurarColunasProdutos();
        configurarSelecaoMateriais();
        configurarSelecaoProdutos();

        campoBuscaMaterial.textProperty().addListener((obs, ant, novo) -> filtrarMateriais(novo));
        campoBuscaProduto.textProperty().addListener((obs, ant, novo) -> filtrarProdutos(novo));

        // Clique nos cards filtra a tabela de materiais
        cardForaEstoque.setOnMouseClicked(e -> toggleFiltroForaEstoque());
        cardEstoqueBaixo.setOnMouseClicked(e -> toggleFiltroBaixoEstoque());

        carregarTudo();
    }

    private void configurarColunasMateriais() {
        // Código = id formatado com dois dígitos
        colMatCodigo.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%02d", c.getValue().getIdMaterial())));

        colMatNome.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNome()));

        colMatQuantidade.setCellValueFactory(c -> {
            BigDecimal qtd = c.getValue().getQuantidadeEstoque();
            return new SimpleStringProperty(
                    qtd != null ? String.format("%03.0f", qtd) : "000");
        });

        colMatCor.setCellValueFactory(c -> {
            String cor = c.getValue().getCor();
            return new SimpleStringProperty(cor != null && !cor.isBlank() ? cor : "—");
        });

        colMatValor.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getPrecoUnidade();
            return new SimpleStringProperty(v != null ? "R$" + String.format("%05.2f", v) : "—");
        });

        colMatCodigo.setStyle("-fx-alignment: CENTER;");
        colMatQuantidade.setStyle("-fx-alignment: CENTER;");
        colMatValor.setStyle("-fx-alignment: CENTER;");
    }

    private void configurarColunasProdutos() {
        colProdCodigo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCodigo()));

        colProdNome.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNome()));

        colProdQuantidade.setCellValueFactory(c -> {
            Produto p = c.getValue();
            String qtd = (p.getTipo() == TipoProduto.PRE_PRONTO && p.getQuantidadeEstoque() != null)
                    ? String.format("%03d", p.getQuantidadeEstoque())
                    : "—";
            return new SimpleStringProperty(qtd);
        });

        colProdValor.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValor();
            return new SimpleStringProperty(v != null ? "R$" + String.format("%.2f", v) : "—");
        });

        colProdCodigo.setStyle("-fx-alignment: CENTER;");
        colProdQuantidade.setStyle("-fx-alignment: CENTER;");
        colProdValor.setStyle("-fx-alignment: CENTER;");
    }

    private void configurarSelecaoMateriais() {
        tabelaMateriais.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, mat) -> {
                    if (mat != null) {
                        // Limpa seleção da outra tabela
                        tabelaProdutos.getSelectionModel().clearSelection();
                        produtoSelecionado = null;
                        materialSelecionado = mat;
                        mostrarDetalhesMaterial(mat);
                    }
                });
    }

    private void configurarSelecaoProdutos() {
        tabelaProdutos.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, prod) -> {
                    if (prod != null) {
                        // Limpa seleção da outra tabela
                        tabelaMateriais.getSelectionModel().clearSelection();
                        materialSelecionado = null;
                        produtoSelecionado = prod;
                        mostrarDetalhesProduto(prod);
                    }
                });
    }

    // ── Carregar dados ─────────────────────────────────────────────────────

    private void carregarTudo() {
        todosMateriais = materialService.listarTodos();
        todosProdutos  = produtoService.listarEstoque();
        atualizarCards();
        filtrarMateriais(campoBuscaMaterial.getText());
        filtrarProdutos(campoBuscaProduto.getText());
    }

    private void atualizarCards() {
        long fora  = todosMateriais.stream()
                .filter(m -> m.getQuantidadeEstoque() == null
                          || m.getQuantidadeEstoque().compareTo(BigDecimal.ZERO) == 0)
                .count();
        long baixo = todosMateriais.stream()
                .filter(m -> m.getQuantidadeEstoque() != null
                          && m.getQuantidadeEstoque().compareTo(BigDecimal.ZERO) > 0
                          && m.getQuantidadeEstoque().compareTo(LIMITE_ESTOQUE_BAIXO) <= 0)
                .count();

        labelForaEstoque.setText(String.valueOf(fora));
        labelEstoqueBaixo.setText(String.valueOf(baixo));

        // Destaque visual quando o filtro está ativo
        String estiloAtivo   = "-fx-background-color: #FFC1D6; -fx-background-radius: 10; " +
                               "-fx-border-color: #D31946; -fx-border-radius: 10; -fx-border-width: 1.5; " +
                               "-fx-padding: 10 16 10 16; -fx-cursor: hand;";
        String estiloInativo = "-fx-background-color: white; -fx-background-radius: 10; " +
                               "-fx-border-color: #f0d0da; -fx-border-radius: 10; -fx-border-width: 1; " +
                               "-fx-padding: 10 16 10 16; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);";
        cardForaEstoque.setStyle(filtrandoForaEstoque   ? estiloAtivo : estiloInativo);
        cardEstoqueBaixo.setStyle(filtrandoBaixoEstoque ? estiloAtivo : estiloInativo);
    }

    // ── Filtro e paginação — Materiais ─────────────────────────────────────

    private void filtrarMateriais(String termo) {
        paginaMat = 1;

        // Base: aplica filtro de alerta se ativo
        List<Material> base;
        if (filtrandoForaEstoque) {
            base = todosMateriais.stream()
                    .filter(m -> m.getQuantidadeEstoque() == null
                              || m.getQuantidadeEstoque().compareTo(BigDecimal.ZERO) == 0)
                    .toList();
        } else if (filtrandoBaixoEstoque) {
            base = todosMateriais.stream()
                    .filter(m -> m.getQuantidadeEstoque() != null
                              && m.getQuantidadeEstoque().compareTo(BigDecimal.ZERO) > 0
                              && m.getQuantidadeEstoque().compareTo(LIMITE_ESTOQUE_BAIXO) <= 0)
                    .toList();
        } else {
            base = todosMateriais;
        }

        // Aplica busca textual por cima
        if (termo == null || termo.isBlank()) {
            materiaisFiltrados = base;
        } else {
            String lower = termo.toLowerCase();
            materiaisFiltrados = base.stream()
                    .filter(m -> m.getNome().toLowerCase().contains(lower)
                              || (m.getCor() != null && m.getCor().toLowerCase().contains(lower)))
                    .toList();
        }
        exibirPaginaMateriais();
    }

    private void toggleFiltroForaEstoque() {
        filtrandoForaEstoque  = !filtrandoForaEstoque;
        filtrandoBaixoEstoque = false;
        campoBuscaMaterial.clear();
        atualizarCards();
        filtrarMateriais("");
    }

    private void toggleFiltroBaixoEstoque() {
        filtrandoBaixoEstoque = !filtrandoBaixoEstoque;
        filtrandoForaEstoque  = false;
        campoBuscaMaterial.clear();
        atualizarCards();
        filtrarMateriais("");
    }

    private void exibirPaginaMateriais() {
        int total        = materiaisFiltrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / ITENS_POR_PAGINA));
        paginaMat = Math.max(1, Math.min(paginaMat, totalPaginas));

        int inicio = (paginaMat - 1) * ITENS_POR_PAGINA;
        int fim    = Math.min(inicio + ITENS_POR_PAGINA, total);

        tabelaMateriais.setItems(FXCollections.observableArrayList(materiaisFiltrados.subList(inicio, fim)));

        labelTotalMateriais.setText("Total de Materiais: " + total);
        labelSubtituloMateriais.setText(
                Math.min(fim, total) + " de " + todosMateriais.size() + " materiais");
        labelPaginaMat.setText("Página " + paginaMat);
        btnAnteriorMat.setDisable(paginaMat <= 1);
        btnProximoMat.setDisable(paginaMat >= totalPaginas);
    }

    @FXML public void paginaAnteriorMat() { if (paginaMat > 1) { paginaMat--; exibirPaginaMateriais(); } }
    @FXML public void proximaPaginaMat()  { paginaMat++; exibirPaginaMateriais(); }

    // ── Filtro e paginação — Produtos ──────────────────────────────────────

    private void filtrarProdutos(String termo) {
        paginaProd = 1;
        if (termo == null || termo.isBlank()) {
            produtosFiltrados = todosProdutos;
        } else {
            String lower = termo.toLowerCase();
            produtosFiltrados = todosProdutos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(lower)
                              || p.getCodigo().toLowerCase().contains(lower))
                    .toList();
        }
        exibirPaginaProdutos();
    }

    private void exibirPaginaProdutos() {
        int total        = produtosFiltrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / ITENS_POR_PAGINA));
        paginaProd = Math.max(1, Math.min(paginaProd, totalPaginas));

        int inicio = (paginaProd - 1) * ITENS_POR_PAGINA;
        int fim    = Math.min(inicio + ITENS_POR_PAGINA, total);

        tabelaProdutos.setItems(FXCollections.observableArrayList(produtosFiltrados.subList(inicio, fim)));

        labelTotalProdutos.setText("Total de Produtos: " + total);
        labelSubtituloProdutos.setText(
                Math.min(fim, total) + " de " + todosProdutos.size() + " produtos em estoque");
        labelPaginaProd.setText("Página " + paginaProd);
        btnAnteriorProd.setDisable(paginaProd <= 1);
        btnProximoProd.setDisable(paginaProd >= totalPaginas);
    }

    @FXML public void paginaAnteriorProd() { if (paginaProd > 1) { paginaProd--; exibirPaginaProdutos(); } }
    @FXML public void proximaPaginaProd()  { paginaProd++; exibirPaginaProdutos(); }

    // ── Painel de detalhes — Material ──────────────────────────────────────

    private void mostrarDetalhesMaterial(Material m) {
        // Nome (sem badge de código, materiais não têm código)
        detNome.setText(m.getNome());
        detCodigo.setText(String.format("%02d", m.getIdMaterial()));

        // Valor
        detValor.setText(m.getPrecoUnidade() != null
                ? "R$ " + String.format("%.2f", m.getPrecoUnidade())
                : "—");

        // Estoque
        detEstoqueLabel.setText("Estoque:");
        BigDecimal qtd = m.getQuantidadeEstoque() != null ? m.getQuantidadeEstoque() : BigDecimal.ZERO;
        detEstoque.setText(String.format("%.0f", qtd) + " "
                + (m.getUnidadeMedida() != null ? m.getUnidadeMedida() : "un."));

        // Campos específicos de material
        setVisible(secaoUnidade, true);
        detUnidade.setText(m.getUnidadeMedida() != null ? m.getUnidadeMedida() : "—");

        setVisible(secaoCor, true);
        detCor.setText(m.getCor() != null && !m.getCor().isBlank() ? m.getCor() : "—");

        // Stats: total usado em produtos + valor total consumido
        List<com.managementsystem.demo1.model.ProdutoMaterial> usos =
                produtoService.listarTodos().stream()
                        .flatMap(p -> p.getMateriaisUtilizados().stream())
                        .filter(pm -> pm.getMaterial().getIdMaterial().equals(m.getIdMaterial()))
                        .toList();

        BigDecimal totalUsado = usos.stream()
                .map(com.managementsystem.demo1.model.ProdutoMaterial::getQuantidadeUtilizada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorTotalUsado = totalUsado.multiply(
                m.getPrecoUnidade() != null ? m.getPrecoUnidade() : BigDecimal.ZERO);

        detStatTitulo1.setText("Total usado");
        detStatValor1.setText(totalUsado.stripTrailingZeros().toPlainString()
                + " " + (m.getUnidadeMedida() != null ? m.getUnidadeMedida() : "un."));

        detStatTitulo2.setText("Valor total");
        detStatValor2.setText("R$ " + String.format("%.2f", valorTotalUsado));

        // Descrição (materiais não têm descrição)
        detDescricaoTitulo.setText("Observações");
        detDescricao.setText("Nenhuma observação cadastrada.");

        // Seções exclusivas de produto — ocultas
        setVisible(secaoMateriaisProduto, false);
        setVisible(secaoPrazo, false);
        setVisible(secaoTipo, false);

        // Seção de movimentação — visível para materiais
        setVisible(secaoMovimentacao, true);

        exibirPainel();
    }

    // ── Painel de detalhes — Produto ───────────────────────────────────────

    private void mostrarDetalhesProduto(Produto p) {
        detNome.setText(p.getNome());
        detCodigo.setText(p.getCodigo());

        detValor.setText(p.getValor() != null
                ? "R$ " + String.format("%.2f", p.getValor())
                : "—");

        detEstoqueLabel.setText("Estoque:");
        if (p.getTipo() == TipoProduto.PRE_PRONTO && p.getQuantidadeEstoque() != null) {
            detEstoque.setText(p.getQuantidadeEstoque() + " unidades");
        } else {
            detEstoque.setText("—");
        }

        // Campos de material — ocultos para produto
        setVisible(secaoUnidade, false);
        setVisible(secaoCor, false);

        // Stats placeholder (requer módulo de vendas)
        detStatTitulo1.setText("Total vendido");
        detStatValor1.setText("—");
        detStatTitulo2.setText("Valor total");
        detStatValor2.setText("—");

        // Descrição
        detDescricaoTitulo.setText("Descrição");
        detDescricao.setText(
                Optional.ofNullable(p.getDescricao())
                        .filter(d -> !d.isBlank())
                        .orElse("Sem descrição cadastrada."));

        // Materiais do produto
        setVisible(secaoMateriaisProduto, true);
        painelMateriaisProduto.getChildren().clear();
        List<ProdutoMaterial> materiais = p.getMateriaisUtilizados();
        if (materiais != null && !materiais.isEmpty()) {
            for (ProdutoMaterial pm : materiais) {
                Label tag = new Label(
                        pm.getQuantidadeUtilizada().stripTrailingZeros().toPlainString()
                        + "x " + pm.getMaterial().getNome());
                tag.getStyleClass().add("material-tag");
                painelMateriaisProduto.getChildren().add(tag);
            }
        } else {
            Label sem = new Label("Nenhum material registrado.");
            sem.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
            painelMateriaisProduto.getChildren().add(sem);
        }

        // Prazo e tipo
        boolean isPersonalizado = p.getTipo() == TipoProduto.PERSONALIZADO;
        setVisible(secaoPrazo, isPersonalizado);
        if (isPersonalizado) {
            detPrazo.setText(p.getPrazoProducaoDias() != null
                    ? p.getPrazoProducaoDias() + " dias"
                    : "Não informado");
        }

        setVisible(secaoTipo, true);
        detTipo.setText(p.getTipo() == TipoProduto.PRE_PRONTO ? "Pré-pronto" : "Personalizado");

        // Movimentação — somente para materiais
        setVisible(secaoMovimentacao, false);

        exibirPainel();
    }

    private void exibirPainel() {
        painelDetalhes.setVisible(true);
        painelDetalhes.setManaged(true);
    }

    // ── Botões do painel de detalhes ───────────────────────────────────────

    @FXML
    public void deletarSelecionado() {
        if (materialSelecionado != null) {
            deletarMaterial(materialSelecionado);
        } else if (produtoSelecionado != null) {
            deletarProduto(produtoSelecionado);
        }
    }

    @FXML
    public void editarSelecionado() {
        if (materialSelecionado != null) {
            javafx.scene.Scene scene = tabelaMateriais.getScene();
            if (scene == null) return;
            javafx.scene.Node sp = scene.lookup("#contentArea");
            if (sp instanceof javafx.scene.layout.StackPane stackPane
                    && stackPane.getUserData() instanceof MainLayoutController main) {
                main.navEditarMaterial(materialSelecionado);
            }
        } else if (produtoSelecionado != null) {
            javafx.scene.Scene scene = tabelaProdutos.getScene();
            if (scene == null) return;
            javafx.scene.Node sp = scene.lookup("#contentArea");
            if (sp instanceof javafx.scene.layout.StackPane stackPane
                    && stackPane.getUserData() instanceof MainLayoutController main) {
                main.navEditarProduto(produtoSelecionado);
            }
        }
    }

    private void deletarMaterial(Material material) {
        Dialog<ButtonType> dialog = criarDialogConfirmacao(material.getNome());
        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    materialService.excluir(material.getIdMaterial());
                    painelDetalhes.setVisible(false);
                    painelDetalhes.setManaged(false);
                    materialSelecionado = null;
                    carregarTudo();
                } catch (Exception e) {
                    mostrarErro("Não foi possível deletar o material:\n" + e.getMessage());
                }
            }
        });
    }

    private void deletarProduto(Produto produto) {
        Dialog<ButtonType> dialog = criarDialogConfirmacao(produto.getNome());
        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    produtoService.excluir(produto.getIdProduto());
                    painelDetalhes.setVisible(false);
                    painelDetalhes.setManaged(false);
                    produtoSelecionado = null;
                    carregarTudo();
                } catch (Exception e) {
                    mostrarErro("Não foi possível deletar o produto:\n" + e.getMessage());
                }
            }
        });
    }

    // ── Botões de ação rápida (header) ────────────────────────────────────

    @FXML
    public void abrirRegistrarMovimentacao() {
        navegarMovimentacao(materialSelecionado, TipoMovimentacaoMaterial.ENTRADA);
    }

    @FXML
    public void abrirRegistrarPerdaRapida() {
        navegarMovimentacao(materialSelecionado, TipoMovimentacaoMaterial.PERDA);
    }

    private void navegarMovimentacao(Material material, TipoMovimentacaoMaterial tipo) {
        javafx.scene.Scene scene = tabelaMateriais.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navRegistrarMovimentacao(material, tipo);
        }
    }

    // ── Movimentações de Material ──────────────────────────────────────────

    @FXML
    public void registrarEntrada() {
        if (materialSelecionado == null) return;
        javafx.scene.Scene scene = tabelaMateriais.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navRegistrarMovimentacao(materialSelecionado, TipoMovimentacaoMaterial.ENTRADA);
        }
    }

    @FXML
    public void registrarPerda() {
        if (materialSelecionado == null) return;
        javafx.scene.Scene scene = tabelaMateriais.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navRegistrarMovimentacao(materialSelecionado, TipoMovimentacaoMaterial.PERDA);
        }
    }

    private void mostrarDialogMovimentacao(Material material, TipoMovimentacaoMaterial tipo) {
        boolean isPerda   = tipo == TipoMovimentacaoMaterial.PERDA;
        String  tituloStr = isPerda ? "Registrar Perda" : "Registrar Entrada";
        String  corTitulo = isPerda ? "#e65100" : "#2e7d32";
        String  corFundo  = isPerda ? "#fff3e0" : "#e8f5e9";

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(tituloStr);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24, 28, 24, 28));
        conteudo.setStyle("-fx-background-color: " + corFundo + "; -fx-background-radius: 12;");
        conteudo.setPrefWidth(380);

        Label titulo = new Label(tituloStr + ": " + material.getNome());
        titulo.setFont(Font.font("System", FontWeight.BOLD, 17));
        titulo.setTextFill(Color.web(corTitulo));
        titulo.setWrapText(true);

        // Campo quantidade
        VBox campoQtdBox = new VBox(4);
        Label lblQtd = new Label("Quantidade *");
        lblQtd.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextField campoQtd = new TextField();
        campoQtd.setPromptText("ex: 50");
        campoQtd.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0c0cc; -fx-border-radius: 8; -fx-border-width: 1; " +
                "-fx-padding: 8 12 8 12; -fx-font-size: 15px;");
        campoQtdBox.getChildren().addAll(lblQtd, campoQtd);

        // Campo motivo (obrigatório para perda)
        VBox campoMotivoBox = new VBox(4);
        Label lblMotivo = new Label(isPerda ? "Motivo da perda *" : "Motivo (opcional)");
        lblMotivo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextField campoMotivo = new TextField();
        campoMotivo.setPromptText(isPerda ? "ex: Material danificado" : "ex: Compra fornecedor");
        campoMotivo.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0c0cc; -fx-border-radius: 8; -fx-border-width: 1; " +
                "-fx-padding: 8 12 8 12; -fx-font-size: 15px;");
        campoMotivoBox.getChildren().addAll(lblMotivo, campoMotivo);

        // Label de erro
        Label labelErro = new Label();
        labelErro.setStyle("-fx-text-fill: #D31946; -fx-font-size: 13px;");
        labelErro.setWrapText(true);

        // Botões
        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-border-width: 1.5; " +
                "-fx-text-fill: #555; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 18 7 18; -fx-cursor: hand;");

        String corBtnConfirmar = isPerda ? "#e65100" : "#2e7d32";
        Button btnConfirmar = new Button("Confirmar");
        btnConfirmar.setStyle("-fx-background-color: " + corBtnConfirmar + "; -fx-background-radius: 20; " +
                "-fx-border-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 22 7 22; -fx-cursor: hand;");

        btnCancelar.setOnAction(e -> { dialog.setResult(ButtonType.CANCEL); dialog.close(); });

        btnConfirmar.setOnAction(e -> {
            labelErro.setText("");
            BigDecimal qtd;
            try {
                qtd = new BigDecimal(campoQtd.getText().replace(",", "."));
                if (qtd.signum() <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                labelErro.setText("Informe uma quantidade válida (maior que zero).");
                return;
            }
            String motivo = campoMotivo.getText().trim();
            if (isPerda && motivo.isBlank()) {
                labelErro.setText("Motivo da perda é obrigatório.");
                return;
            }
            try {
                materialService.registrarMovimentacao(
                        material.getIdMaterial(), tipo, qtd,
                        motivo.isBlank() ? null : motivo, null);
                dialog.setResult(ButtonType.OK);
                dialog.close();
                carregarTudo();
                // Atualiza o painel com o material atualizado
                materialService.buscarPorId(material.getIdMaterial())
                        .ifPresent(matAtualizado -> {
                            materialSelecionado = matAtualizado;
                            mostrarDetalhesMaterial(matAtualizado);
                        });
            } catch (Exception ex) {
                labelErro.setText(ex.getMessage());
            }
        });

        botoes.getChildren().addAll(btnCancelar, btnConfirmar);
        conteudo.getChildren().addAll(titulo, campoQtdBox, campoMotivoBox, labelErro, botoes);
        dialog.getDialogPane().setContent(conteudo);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");

        dialog.showAndWait();
    }

    // ── Cadastrar material ─────────────────────────────────────────────────

    @FXML
    public void abrirCadastrarMaterial() {
        javafx.scene.Scene scene = tabelaMateriais.getScene();
        if (scene == null) return;
        javafx.scene.Node sp = scene.lookup("#contentArea");
        if (sp instanceof javafx.scene.layout.StackPane stackPane
                && stackPane.getUserData() instanceof MainLayoutController main) {
            main.navCadastrarMaterial();
        }
    }

    // ── Utilitários ────────────────────────────────────────────────────────

    private TextField criarCampo(String labelTxt, String promptTxt) {
        TextField campo = new TextField();
        campo.setPromptText(labelTxt + " — " + promptTxt);
        campo.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0c0cc; -fx-border-radius: 8; -fx-border-width: 1; " +
                "-fx-padding: 8 12 8 12; -fx-font-size: 15px;");
        return campo;
    }

    private Dialog<ButtonType> criarDialogConfirmacao(String nome) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmar exclusão");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24, 28, 24, 28));
        conteudo.setStyle("-fx-background-color: #FFF0F5; -fx-background-radius: 12;");

        Label titulo = new Label("Deletar \"" + nome + "\"?");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 17));
        titulo.setTextFill(Color.web("#D31946"));
        titulo.setWrapText(true);

        Label msg = new Label("Esta ação não pode ser desfeita.");
        msg.setFont(Font.font("System", 15));
        msg.setTextFill(Color.web("#555"));

        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-border-width: 1.5; " +
                "-fx-text-fill: #555; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 18 7 18; -fx-cursor: hand;");
        Button btnDeletar = new Button("Deletar");
        btnDeletar.setStyle("-fx-background-color: #D31946; -fx-background-radius: 20; " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 22 7 22; -fx-cursor: hand;");

        btnCancelar.setOnAction(e -> { dialog.setResult(ButtonType.CANCEL); dialog.close(); });
        btnDeletar.setOnAction(e ->  { dialog.setResult(ButtonType.OK);     dialog.close(); });

        botoes.getChildren().addAll(btnCancelar, btnDeletar);
        conteudo.getChildren().addAll(titulo, msg, botoes);
        dialog.getDialogPane().setContent(conteudo);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        return dialog;
    }

    private void mostrarErro(String mensagem) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Erro");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24, 28, 24, 28));
        conteudo.setStyle("-fx-background-color: #FFF0F5; -fx-background-radius: 12;");
        Label msg = new Label(mensagem);
        msg.setFont(Font.font("System", 15));
        msg.setTextFill(Color.web("#D31946"));
        msg.setWrapText(true);
        Button btnOk = new Button("OK");
        btnOk.setStyle("-fx-background-color: #D31946; -fx-background-radius: 20; " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 7 22 7 22; -fx-cursor: hand;");
        btnOk.setOnAction(e -> { dialog.setResult(ButtonType.OK); dialog.close(); });
        conteudo.getChildren().addAll(msg, btnOk);
        dialog.getDialogPane().setContent(conteudo);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        dialog.showAndWait();
    }

    private void setVisible(javafx.scene.Node node, boolean visivel) {
        node.setVisible(visivel);
        node.setManaged(visivel);
    }
}
