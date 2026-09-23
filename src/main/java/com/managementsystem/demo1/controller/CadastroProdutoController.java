package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.service.ItemMaterialInput;
import com.managementsystem.demo1.service.MaterialService;
import com.managementsystem.demo1.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CadastroProdutoController {

    @FXML private TextField campoNome;
    @FXML private TextField campoDescricao;
    @FXML private TextField campoQuantidade;

    @FXML private ComboBox<Material> comboMaterial;
    @FXML private TextField campoQuantidadeMaterial;
    @FXML private ListView<String> listaMateriaisAdicionados;

    @FXML private Label labelStatus;
    @FXML private ListView<String> listaProdutos;

    private final ProdutoService produtoService;
    private final MaterialService materialService;

    // Guardam os materiais que a artesã foi adicionando antes de salvar o produto
    private final List<Material> materiaisEscolhidos = new ArrayList<>();
    private final List<BigDecimal> quantidadesEscolhidas = new ArrayList<>();

    public CadastroProdutoController(ProdutoService produtoService, MaterialService materialService) {
        this.produtoService = produtoService;
        this.materialService = materialService;
    }

    @FXML
    public void initialize() {
        carregarMateriaisDisponiveis();
        atualizarListaProdutos();
    }

    private void carregarMateriaisDisponiveis() {
        List<Material> materiais = materialService.listarTodos();
        comboMaterial.setItems(FXCollections.observableArrayList(materiais));
        comboMaterial.setConverter(new StringConverter<>() {
            @Override
            public String toString(Material m) {
                return m == null ? "" : m.getNome() + " (R$ " + m.getPrecoUnidade() + " / " + m.getUnidadeMedida() + ")";
            }
            @Override
            public Material fromString(String s) {
                return null; // não precisa converter de volta, só exibição
            }
        });
    }

    @FXML
    public void adicionarMaterial() {
        Material material = comboMaterial.getValue();
        if (material == null) {
            labelStatus.setText("Selecione um material antes de adicionar.");
            return;
        }
        BigDecimal quantidade;
        try {
            quantidade = new BigDecimal(
                    campoQuantidadeMaterial.getText().replace(",", ".")
            );

            if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            labelStatus.setText("Quantidade do material inválida.");
            return;
        }
        materiaisEscolhidos.add(material);
        quantidadesEscolhidas.add(quantidade);

        listaMateriaisAdicionados.getItems().add(
                material.getNome() + " — " + quantidade + " " + material.getUnidadeMedida());

        campoQuantidadeMaterial.clear();
        comboMaterial.setValue(null);
        labelStatus.setText("");
    }

    @FXML
    public void salvar() {
        try {
            String nome = campoNome.getText();
            String descricao = campoDescricao.getText();
            Integer quantidade = Integer.parseInt(campoQuantidade.getText());

            if (materiaisEscolhidos.isEmpty()) {
                labelStatus.setText("Adicione ao menos um material usado no produto.");
                return;
            }

            List<ItemMaterialInput> itens = new ArrayList<>();
            for (int i = 0; i < materiaisEscolhidos.size(); i++) {
                itens.add(new ItemMaterialInput(
                        materiaisEscolhidos.get(i).getIdMaterial(),
                        quantidadesEscolhidas.get(i)));
            }

            Produto produto = produtoService.cadastrarComMateriais(nome, descricao, quantidade, itens);

            labelStatus.setText("Produto salvo! Valor calculado: R$ " + String.format("%.2f", produto.getValor()));
            limparFormulario();
            atualizarListaProdutos();

        } catch (NumberFormatException e) {
            labelStatus.setText("Erro: quantidade deve ser um número válido.");
        } catch (IllegalArgumentException e) {
            labelStatus.setText("Erro: " + e.getMessage());
        } catch (Exception e) {
            labelStatus.setText("Erro inesperado: " + e.getMessage());
        }
    }

    private void limparFormulario() {
        campoNome.clear();
        campoDescricao.clear();
        campoQuantidade.clear();
        materiaisEscolhidos.clear();
        quantidadesEscolhidas.clear();
        listaMateriaisAdicionados.getItems().clear();
    }

    private void atualizarListaProdutos() {
        listaProdutos.setItems(FXCollections.observableArrayList(
                produtoService.listarTodos().stream()
                        .map(p -> String.format("#%d - %s | qtd: %d | R$ %.2f",
                                p.getIdProduto(), p.getNome(), p.getQuantidade(), p.getValor()))
                        .toList()
        ));
    }
}