package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.TipoProduto;
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

    @Transactional
    public Produto cadastrarComMateriais(String nome, String descricao, TipoProduto tipo,
                                         Integer quantidadeEstoque, Integer prazoProducaoDias,
                                         String observacoes, List<ItemMaterialInput> itensMaterial) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do produto é obrigatório.");
        }
        if (itensMaterial == null || itensMaterial.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um material usado no produto.");
        }

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setTipo(tipo);

        if (tipo == TipoProduto.PRE_PRONTO) {
            produto.setQuantidadeEstoque(quantidadeEstoque == null ? 0 : quantidadeEstoque);
        } else {
            produto.setPrazoProducaoDias(prazoProducaoDias);
            produto.setObservacoes(observacoes);
        }

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<ProdutoMaterial> vinculos = new ArrayList<>();

        for (ItemMaterialInput item : itensMaterial) {
            if (item.quantidadeUtilizada() == null || item.quantidadeUtilizada().signum() <= 0) {
                throw new IllegalArgumentException("Quantidade utilizada do material deve ser maior que zero.");
            }
            Material material = materialRepository.findById(item.idMaterial())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Material não encontrado: id " + item.idMaterial()));

            valorTotal = valorTotal.add(material.getPrecoUnidade().multiply(item.quantidadeUtilizada()));
            vinculos.add(new ProdutoMaterial(produto, material, item.quantidadeUtilizada()));
        }

        produto.setValor(valorTotal);
        produto.setMateriaisUtilizados(vinculos);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: id " + id));
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .toList();
    }

    @Transactional
    public Produto atualizar(Long id, String nome, String descricao, Integer quantidadeEstoque,
                             Integer prazoProducaoDias, String observacoes) {
        Produto produto = buscarPorId(id);

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        produto.setNome(nome);
        produto.setDescricao(descricao);

        if (produto.getTipo() == TipoProduto.PRE_PRONTO) {
            produto.setQuantidadeEstoque(quantidadeEstoque);
        } else {
            produto.setPrazoProducaoDias(prazoProducaoDias);
            produto.setObservacoes(observacoes);
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}