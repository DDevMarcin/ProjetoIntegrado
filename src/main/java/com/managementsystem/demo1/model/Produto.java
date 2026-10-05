package com.managementsystem.demo1.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Produto: cobre tanto PRE_PRONTO quanto PERSONALIZADO.
 */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long idProduto;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProduto tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    // true = artesã digitou o valor na mão (campo "Calcular manualmente" do protótipo)
    // false = valor calculado automaticamente a partir dos materiais
    @Column(name = "valor_manual", nullable = false)
    private Boolean valorManual = false;

    // Usado se tipo = PRE_PRONTO
    @Column(name = "quantidade_estoque")
    private Integer quantidadeEstoque;

    // Usado se tipo = PERSONALIZADO
    @Column(name = "prazo_producao_dias")
    private Integer prazoProducaoDias;

    @Column(length = 500)
    private String observacoes;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoMaterial> materiaisUtilizados = new ArrayList<>();

    public Produto() {
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getValorManual() {
        return valorManual;
    }

    public void setValorManual(Boolean valorManual) {
        this.valorManual = valorManual;
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

    public TipoProduto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProduto tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Integer getPrazoProducaoDias() {
        return prazoProducaoDias;
    }

    public void setPrazoProducaoDias(Integer prazoProducaoDias) {
        this.prazoProducaoDias = prazoProducaoDias;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<ProdutoMaterial> getMateriaisUtilizados() {
        return materiaisUtilizados;
    }

    public void setMateriaisUtilizados(List<ProdutoMaterial> materiaisUtilizados) {
        this.materiaisUtilizados = materiaisUtilizados;
    }
}