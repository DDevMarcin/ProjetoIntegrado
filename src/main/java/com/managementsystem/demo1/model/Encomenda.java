package com.managementsystem.demo1.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "encomenda")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encomenda")
    private Long idEncomenda;

    @Column(name = "nome_cliente", nullable = false, length = 255)
    private String nomeCliente;

    @Column(name = "contato_cliente", length = 50)
    private String contatoCliente;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "data_entrega_prevista")
    private LocalDate dataEntregaPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoEncomenda situacao = SituacaoEncomenda.PENDENTE;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;      // soma dos itens, sem lucro

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;    // subtotal + 20%

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemEncomenda> itens = new ArrayList<>();

    public Encomenda() {}

    public void adicionarItem(ItemEncomenda item) {
        item.setEncomenda(this);
        itens.add(item);
    }

    public Long getIdEncomenda() {
        return idEncomenda;
    }

    public void setIdEncomenda(Long idEncomenda) {
        this.idEncomenda = idEncomenda;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getContatoCliente() {
        return contatoCliente;
    }

    public void setContatoCliente(String contatoCliente) {
        this.contatoCliente = contatoCliente;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public LocalDate getDataEntregaPrevista() {
        return dataEntregaPrevista;
    }

    public void setDataEntregaPrevista(LocalDate dataEntregaPrevista) {
        this.dataEntregaPrevista = dataEntregaPrevista;
    }

    public SituacaoEncomenda getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoEncomenda situacao) {
        this.situacao = situacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemEncomenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemEncomenda> itens) {
        this.itens = itens;
    }

}