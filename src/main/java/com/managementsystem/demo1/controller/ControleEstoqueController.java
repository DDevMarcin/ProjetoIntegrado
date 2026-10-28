package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ControleEstoqueController {

    @FXML
    private Label labelTotalEstoque;

    @FXML
    private TextField campoBuscaNome;

    @FXML
    private TextField campoBuscaId;

    @FXML
    private ListView<String> listaProdutosEstoque;

    @FXML
    private Label labelStatus;

    @FXML
    private Label labelDetalhesId;

    @FXML
    private Label labelDetalhesCodigo;

    @FXML
    private Label labelDetalhesNome;

    @FXML
    private Label labelDetalhesPreco;

    @FXML
    private Label labelDetalhesQuantidade;

    @FXML
    private Label labelDetalhesDescricao;

    @FXML
    private Label labelDetalhesMateriais;

    private final ProdutoService produtoService;

    private List<Produto> produtosCarregados = List.of();

    public ControleEstoqueController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @FXML
    public void initialize() {
        carregarTodos();
        configurarSelecao();
    }

    private void configurarSelecao() {
        listaProdutosEstoque.getSelectionModel().selectedIndexProperty().addListener((obs, antigo, novo) -> {
            int indice = novo.intValue();
            if (indice >= 0 && indice < produtosCarregados.size()) {
                exibirDetalhes(produtosCarregados.get(indice));
            } else {
                limparDetalhes();
            }
        });
    }

    @FXML
    public void carregarTodos() {
        produtosCarregados = produtoService.listarEstoque();
        atualizarLista(produtosCarregados);
        atualizarTotalizador();
        labelStatus.setText("Estoque carregado: " + produtosCarregados.size() + " produto(s) encontrado(s).");
    }

    @FXML
    public void buscarPorNome() {
        String termo = campoBuscaNome.getText();
        if (termo == null || termo.isBlank()) {
            carregarTodos();
            return;
        }

        produtosCarregados = produtoService.buscarEstoquePorNome(termo);
        atualizarLista(produtosCarregados);
        atualizarTotalizador();

        if (produtosCarregados.isEmpty()) {
            labelStatus.setText("Nenhum produto em estoque encontrado com o nome: \"" + termo.trim() + "\".");
        } else {
            labelStatus.setText("Busca por \"" + termo.trim() + "\": " + produtosCarregados.size() + " produto(s) encontrado(s).");
        }
    }

    @FXML
    public void buscarPorId() {
        String textoId = campoBuscaId.getText();
        if (textoId == null || textoId.isBlank()) {
            labelStatus.setText("Informe um ID numérico para pesquisar.");
            return;
        }

        try {
            Long id = Long.parseLong(textoId.trim());
            Optional<Produto> produtoOpt = produtoService.buscarEstoquePorId(id);

            if (produtoOpt.isPresent()) {
                produtosCarregados = List.of(produtoOpt.get());
                atualizarLista(produtosCarregados);
                listaProdutosEstoque.getSelectionModel().select(0);
                labelStatus.setText("Produto encontrado com ID " + id + ".");
            } else {
                produtosCarregados = List.of();
                atualizarLista(produtosCarregados);
                limparDetalhes();
                labelStatus.setText("Nenhum produto em estoque encontrado com o ID " + id + " (ou produto é personalizado/inexistente).");
            }
        } catch (NumberFormatException e) {
            labelStatus.setText("Erro: O ID deve ser um número inteiro válido.");
        }
    }

    @FXML
    public void limparFiltros() {
        campoBuscaNome.clear();
        campoBuscaId.clear();
        limparDetalhes();
        carregarTodos();
        labelStatus.setText("Filtros limpos.");
    }

    private void atualizarTotalizador() {
        int total = produtoService.totalEmEstoque();
        labelTotalEstoque.setText("Total de Unidades em Estoque: " + total);
    }

    private void atualizarLista(List<Produto> produtos) {
        listaProdutosEstoque.setItems(
                FXCollections.observableArrayList(
                        produtos.stream().map(this::formatarProduto).toList()
                )
        );
    }

    private String formatarProduto(Produto p) {
        String statusEstoque = (p.getQuantidadeEstoque() != null && p.getQuantidadeEstoque() > 0)
                ? p.getQuantidadeEstoque() + " un."
                : "ESGOTADO (0 un.)";

        return String.format("[%s] %s | R$ %.2f | Estoque: %s | ID: %d",
                p.getCodigo(),
                p.getNome(),
                p.getValor(),
                statusEstoque,
                p.getIdProduto());
    }

    private void exibirDetalhes(Produto p) {
        labelDetalhesId.setText(String.valueOf(p.getIdProduto()));
        labelDetalhesCodigo.setText(p.getCodigo() != null ? p.getCodigo() : "—");
        labelDetalhesNome.setText(p.getNome() != null ? p.getNome() : "—");
        labelDetalhesPreco.setText(p.getValor() != null ? "R$ " + p.getValor().toString() : "—");
        labelDetalhesQuantidade.setText(p.getQuantidadeEstoque() != null ? p.getQuantidadeEstoque() + " unidades" : "0 unidades");
        labelDetalhesDescricao.setText(p.getDescricao() != null && !p.getDescricao().isBlank() ? p.getDescricao() : "Sem descrição");

        if (p.getMateriaisUtilizados() != null && !p.getMateriaisUtilizados().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ProdutoMaterial pm : p.getMateriaisUtilizados()) {
                if (pm.getMaterial() != null) {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(pm.getMaterial().getNome())
                      .append(" (").append(pm.getQuantidadeUtilizada()).append(" ").append(pm.getMaterial().getUnidadeMedida()).append(")");
                }
            }
            labelDetalhesMateriais.setText(sb.toString());
        } else {
            labelDetalhesMateriais.setText("Nenhum material associado (preço manual).");
        }
    }

    private void limparDetalhes() {
        labelDetalhesId.setText("—");
        labelDetalhesCodigo.setText("—");
        labelDetalhesNome.setText("—");
        labelDetalhesPreco.setText("—");
        labelDetalhesQuantidade.setText("—");
        labelDetalhesDescricao.setText("—");
        labelDetalhesMateriais.setText("—");
    }
}
