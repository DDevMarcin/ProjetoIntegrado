package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.service.MaterialService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class GerenciarMaterialController {

    @FXML
    private Label labelTotalMateriais;

    @FXML
    private TextField campoIdSelecionado;

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoPrecoUnidade;

    @FXML
    private TextField campoUnidadeMedida;

    @FXML
    private Button botaoAtualizar;

    @FXML
    private Button botaoExcluir;

    @FXML
    private TextField campoFiltroNome;

    @FXML
    private TextField campoFiltroUnidade;

    @FXML
    private TextField campoBuscaId;

    @FXML
    private Label labelStatus;

    @FXML
    private ListView<String> listaMateriais;

    @FXML
    private Label labelDetalhesId;

    @FXML
    private Label labelDetalhesNome;

    @FXML
    private Label labelDetalhesPreco;

    @FXML
    private Label labelDetalhesUnidade;

    private final MaterialService materialService;

    private List<Material> materiaisCarregados = List.of();
    private Material materialSelecionado = null;

    public GerenciarMaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @FXML
    public void initialize() {
        configurarSelecaoLista();
        carregarTodos();
        atualizarEstadoBotoesEdicao();
    }

    private void configurarSelecaoLista() {
        listaMateriais.getSelectionModel().selectedIndexProperty().addListener((obs, antigo, novo) -> {
            int indice = novo.intValue();
            if (indice >= 0 && indice < materiaisCarregados.size()) {
                selecionarMaterial(materiaisCarregados.get(indice));
            }
        });
    }

    private void selecionarMaterial(Material material) {
        this.materialSelecionado = material;
        campoIdSelecionado.setText(String.valueOf(material.getIdMaterial()));
        campoNome.setText(material.getNome());
        campoPrecoUnidade.setText(material.getPrecoUnidade() != null ? material.getPrecoUnidade().toPlainString() : "");
        campoUnidadeMedida.setText(material.getUnidadeMedida());

        labelDetalhesId.setText(String.valueOf(material.getIdMaterial()));
        labelDetalhesNome.setText(material.getNome());
        labelDetalhesPreco.setText("R$ " + material.getPrecoUnidade());
        labelDetalhesUnidade.setText(material.getUnidadeMedida());

        atualizarEstadoBotoesEdicao();
    }

    private void atualizarEstadoBotoesEdicao() {
        boolean temSelecao = (materialSelecionado != null && materialSelecionado.getIdMaterial() != null);
        if (botaoAtualizar != null) {
            botaoAtualizar.setDisable(!temSelecao);
        }
        if (botaoExcluir != null) {
            botaoExcluir.setDisable(!temSelecao);
        }
    }

    @FXML
    public void cadastrarNovo() {
        try {
            String nome = campoNome.getText();
            BigDecimal preco = lerPreco(campoPrecoUnidade.getText());
            String unidade = campoUnidadeMedida.getText();

            Material salvo = materialService.salvar(nome, preco, unidade);

            limparFormulario();
            carregarTodos();
            exibirMensagemSucesso("Material cadastrado com sucesso! ID gerado: " + salvo.getIdMaterial());
        } catch (IllegalArgumentException e) {
            exibirMensagemErro("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao cadastrar: " + e.getMessage());
        }
    }

    @FXML
    public void atualizarSelecionado() {
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            exibirMensagemErro("Selecione um material na lista ou busque por ID antes de atualizar.");
            return;
        }

        try {
            Long id = materialSelecionado.getIdMaterial();
            String nome = campoNome.getText();
            BigDecimal preco = lerPreco(campoPrecoUnidade.getText());
            String unidade = campoUnidadeMedida.getText();

            Material atualizado = materialService.atualizar(id, nome, preco, unidade);

            carregarTodos();
            selecionarMaterialNaListaPorId(atualizado.getIdMaterial());
            exibirMensagemSucesso("Material ID " + atualizado.getIdMaterial() + " atualizado com sucesso!");
        } catch (NoSuchElementException e) {
            exibirMensagemErro("Não encontrado: " + e.getMessage());
            carregarTodos();
        } catch (IllegalArgumentException e) {
            exibirMensagemErro("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao atualizar: " + e.getMessage());
        }
    }

    @FXML
    public void excluirSelecionado() {
        if (materialSelecionado == null || materialSelecionado.getIdMaterial() == null) {
            exibirMensagemErro("Selecione um material na lista ou busque por ID antes de excluir.");
            return;
        }

        Long id = materialSelecionado.getIdMaterial();
        String nome = materialSelecionado.getNome();

        try {
            materialService.excluir(id);
            limparFormulario();
            carregarTodos();
            exibirMensagemSucesso("Material [" + id + " - " + nome + "] excluído com sucesso!");
        } catch (NoSuchElementException e) {
            exibirMensagemErro("Não encontrado: " + e.getMessage());
            carregarTodos();
        } catch (IllegalStateException | IllegalArgumentException e) {
            exibirMensagemErro("Não foi possível excluir: " + e.getMessage());
        } catch (Exception e) {
            exibirMensagemErro("Erro inesperado ao excluir: " + e.getMessage());
        }
    }

    @FXML
    public void carregarTodos() {
        materiaisCarregados = materialService.listarTodos();
        atualizarListaVisual(materiaisCarregados);
        atualizarContadorTotal();
        exibirMensagemInfo("Lista atualizada: " + materiaisCarregados.size() + " material(is) cadastrado(s).");
    }

    @FXML
    public void filtrarMateriais() {
        String nome = campoFiltroNome.getText();
        String unidade = campoFiltroUnidade.getText();

        materiaisCarregados = materialService.consultar(nome, unidade);
        atualizarListaVisual(materiaisCarregados);
        atualizarContadorTotal();

        if (materiaisCarregados.isEmpty()) {
            exibirMensagemInfo("Nenhum material encontrado para os filtros informados.");
        } else {
            exibirMensagemInfo("Filtro aplicado: " + materiaisCarregados.size() + " material(is) encontrado(s).");
        }
    }

    @FXML
    public void buscarPorId() {
        String textoId = campoBuscaId.getText();
        if (textoId == null || textoId.isBlank()) {
            exibirMensagemErro("Informe um ID numérico para buscar.");
            return;
        }

        try {
            Long id = Long.parseLong(textoId.trim());
            Optional<Material> encontrado = materialService.buscarPorId(id);

            if (encontrado.isPresent()) {
                materiaisCarregados = List.of(encontrado.get());
                atualizarListaVisual(materiaisCarregados);
                listaMateriais.getSelectionModel().select(0);
                exibirMensagemSucesso("Material encontrado com ID " + id + ".");
            } else {
                materiaisCarregados = List.of();
                atualizarListaVisual(materiaisCarregados);
                limparDetalhesESelecao();
                exibirMensagemErro("Material não encontrado para o ID " + id + " (404).");
            }
        } catch (NumberFormatException e) {
            exibirMensagemErro("Erro: O ID deve ser um número inteiro válido.");
        }
    }

    @FXML
    public void limparFormulario() {
        campoIdSelecionado.clear();
        campoNome.clear();
        campoPrecoUnidade.clear();
        campoUnidadeMedida.clear();
        listaMateriais.getSelectionModel().clearSelection();
        limparDetalhesESelecao();
        exibirMensagemInfo("Formulário limpo. Pronto para novo cadastro.");
    }

    @FXML
    public void limparFiltros() {
        campoFiltroNome.clear();
        campoFiltroUnidade.clear();
        campoBuscaId.clear();
        carregarTodos();
    }

    private void limparDetalhesESelecao() {
        materialSelecionado = null;
        labelDetalhesId.setText("—");
        labelDetalhesNome.setText("—");
        labelDetalhesPreco.setText("—");
        labelDetalhesUnidade.setText("—");
        atualizarEstadoBotoesEdicao();
    }

    private BigDecimal lerPreco(String textoPreco) {
        if (textoPreco == null || textoPreco.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(textoPreco.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um valor numérico válido para o preço por unidade.");
        }
    }

    private void selecionarMaterialNaListaPorId(Long id) {
        for (int i = 0; i < materiaisCarregados.size(); i++) {
            if (materiaisCarregados.get(i).getIdMaterial().equals(id)) {
                listaMateriais.getSelectionModel().select(i);
                break;
            }
        }
    }

    private void atualizarContadorTotal() {
        int totalBanco = materialService.listarTodos().size();
        labelTotalMateriais.setText("Total de Materiais Cadastrados: " + totalBanco);
    }

    private void atualizarListaVisual(List<Material> materiais) {
        listaMateriais.setItems(
                FXCollections.observableArrayList(
                        materiais.stream().map(this::formatarMaterial).toList()
                )
        );
    }

    private String formatarMaterial(Material m) {
        return String.format("ID: %d | %s | R$ %.2f / %s",
                m.getIdMaterial(),
                m.getNome(),
                m.getPrecoUnidade(),
                m.getUnidadeMedida());
    }

    private void exibirMensagemSucesso(String msg) {
        labelStatus.setStyle("-fx-text-fill: #1b6e2d; -fx-font-weight: bold; -fx-padding: 4 0 4 0;");
        labelStatus.setText(msg);
    }

    private void exibirMensagemErro(String msg) {
        labelStatus.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-padding: 4 0 4 0;");
        labelStatus.setText(msg);
    }

    private void exibirMensagemInfo(String msg) {
        labelStatus.setStyle("-fx-text-fill: #333333; -fx-font-style: italic; -fx-padding: 4 0 4 0;");
        labelStatus.setText(msg);
    }
}

