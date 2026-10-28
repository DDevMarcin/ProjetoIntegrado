package com.managementsystem.demo1.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o produto PRÉ-PRONTO do catálogo (RF07/CU-01).
 * Itens personalizados NÃO usam esta entidade — eles são criados
 * dentro de uma Encomenda, como especialização de Item_Encomenda
 * (ver DER: PERSONALIZADO é subtipo de ITEM_ENCOMENDA, não de PRODUTO).
 *
 * Nota: campo "quantidade" (estoque) presente no DER/modelo relacional,
 * mas ausente no diagrama de classes — mantido aqui pois é necessário
 * pro RF13 (Consultar Estoque). Vale alinhar com o grupo.
 */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long idProduto;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoMaterial> materiaisUtilizados = new ArrayList<>();

    public Produto() {
    }

    public List<ProdutoMaterial> getMateriaisUtilizados() {
        return materiaisUtilizados;
    }

    public void setMateriaisUtilizados(List<ProdutoMaterial> materiaisUtilizados) {
        this.materiaisUtilizados = materiaisUtilizados;
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}