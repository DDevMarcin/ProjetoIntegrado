package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MaterialRepository materialRepository;

    public ProdutoService(ProdutoRepository produtoRepository, MaterialRepository materialRepository) {
        this.produtoRepository = produtoRepository;
        this.materialRepository = materialRepository;
    }

    /**
     * Cadastra um produto pré-pronto calculando o valor a partir dos materiais
     * utilizados, conforme o diagrama de sequência (calcularValorProduto ->
     * consultarDadosMateriais -> calcularValorTotal).
     */
    @Transactional
    public Produto cadastrarComMateriais(String nome, String descricao, Integer quantidade,
                                         List<ItemMaterialInput> itensMaterial) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }

        if (quantidade == null || quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        }

        if (itensMaterial == null || itensMaterial.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um material usado no produto.");
        }

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setQuantidade(quantidade);

        BigDecimal valorTotal = BigDecimal.ZERO;

        List<ProdutoMaterial> vinculos = new ArrayList<>();

        for (ItemMaterialInput item : itensMaterial) {

            if (item.quantidadeUtilizada() == null
                    || item.quantidadeUtilizada().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Quantidade utilizada do material deve ser maior que zero."
                );
            }

            Material material = materialRepository.findById(item.idMaterial())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Material não encontrado: id " + item.idMaterial()
                    ));

            BigDecimal valorMaterial = material.getPrecoUnidade()
                    .multiply(item.quantidadeUtilizada());

            valorTotal = valorTotal.add(valorMaterial);

            vinculos.add(
                    new ProdutoMaterial(
                            produto,
                            material,
                            item.quantidadeUtilizada()
                    )
            );
        }

        produto.setValor(valorTotal);
        produto.setMateriaisUtilizados(vinculos);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getNome()
                        .toLowerCase()
                        .contains(nome.toLowerCase()))
                .toList();
    }

    @Transactional
    public Produto atualizar(Long id, String nome, String descricao, Integer quantidade) {

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Produto não encontrado: id " + id
                ));

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }

        if (quantidade == null || quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        }

        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setQuantidade(quantidade);

        // Nota: valor não é recalculado aqui; se os materiais do produto mudarem,
        // criar um método à parte (ex: recalcularValor) quando essa tela existir.

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}