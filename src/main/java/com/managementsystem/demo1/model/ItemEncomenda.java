package com.managementsystem.demo1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item_encomenda")
public class ItemEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_encomenda")
    private Encomenda encomenda;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "prazo_producao_dias")
    private Integer prazoProducaoDias;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "valor_manual", nullable = false)
    private boolean valorManual;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(name = "id_produto")
    private Long idProduto;

    @Column(name = "quantidade_produto")
    private Integer quantidadeProduto;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemEncomendaMaterial> materiais = new ArrayList<>();

    public ItemEncomenda() {}

    public void adicionarMaterial(ItemEncomendaMaterial m) {
        m.setItem(this);
        materiais.add(m);
    }

    public Long getIdItem() {
        return idItem;
    }

    public void setIdItem(Long idItem) {
        this.idItem = idItem;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public void setEncomenda(Encomenda encomenda) {
        this.encomenda = encomenda;
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

    public boolean isValorManual() {
        return valorManual;
    }

    public void setValorManual(boolean valorManual) {
        this.valorManual = valorManual;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public Integer getQuantidadeProduto() {
        return quantidadeProduto;
    }

    public void setQuantidadeProduto(Integer quantidadeProduto) {
        this.quantidadeProduto = quantidadeProduto;
    }

    public List<ItemEncomendaMaterial> getMateriais() {
        return materiais;
    }

    public void setMateriais(List<ItemEncomendaMaterial> materiais) {
        this.materiais = materiais;
    }
}