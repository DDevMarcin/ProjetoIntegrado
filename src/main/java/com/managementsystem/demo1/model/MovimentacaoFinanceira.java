package com.managementsystem.demo1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimentacao_financeira")
@SuppressWarnings("JpaDataSourceORMInspection")
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    private Long idMovimentacao;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoFinanceira tipo;

    @Column(length = 100)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoMovimentacaoFinanceira situacao = SituacaoMovimentacaoFinanceira.PAGO;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(length = 500)
    private String observacoes;

    public MovimentacaoFinanceira() {
    }

    public MovimentacaoFinanceira(String descricao,
                                  BigDecimal valor,
                                  TipoMovimentacaoFinanceira tipo,
                                  LocalDate dataMovimentacao) {
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.dataMovimentacao = dataMovimentacao;
        this.situacao = SituacaoMovimentacaoFinanceira.PAGO;
    }

    public MovimentacaoFinanceira(String descricao,
                                  BigDecimal valor,
                                  TipoMovimentacaoFinanceira tipo,
                                  String categoria,
                                  SituacaoMovimentacaoFinanceira situacao,
                                  LocalDate dataMovimentacao,
                                  LocalDate dataVencimento,
                                  String observacoes) {
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.categoria = categoria;
        this.situacao = situacao != null ? situacao : SituacaoMovimentacaoFinanceira.PAGO;
        this.dataMovimentacao = dataMovimentacao;
        this.dataVencimento = dataVencimento;
        this.observacoes = observacoes;
    }

    public MovimentacaoFinanceira(Long idMovimentacao,
                                  String descricao,
                                  BigDecimal valor,
                                  TipoMovimentacaoFinanceira tipo,
                                  String categoria,
                                  SituacaoMovimentacaoFinanceira situacao,
                                  LocalDate dataMovimentacao,
                                  LocalDate dataVencimento,
                                  String observacoes) {
        this.idMovimentacao = idMovimentacao;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.categoria = categoria;
        this.situacao = situacao != null ? situacao : SituacaoMovimentacaoFinanceira.PAGO;
        this.dataMovimentacao = dataMovimentacao;
        this.dataVencimento = dataVencimento;
        this.observacoes = observacoes;
    }

    public Long getIdMovimentacao() {
        return idMovimentacao;
    }

    public void setIdMovimentacao(Long idMovimentacao) {
        this.idMovimentacao = idMovimentacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoMovimentacaoFinanceira getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacaoFinanceira tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public SituacaoMovimentacaoFinanceira getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoMovimentacaoFinanceira situacao) {
        this.situacao = situacao != null ? situacao : SituacaoMovimentacaoFinanceira.PAGO;
    }

    public LocalDate getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDate dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimentacaoFinanceira that = (MovimentacaoFinanceira) o;
        return idMovimentacao != null && idMovimentacao.equals(that.idMovimentacao);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MovimentacaoFinanceira{" +
                "idMovimentacao=" + idMovimentacao +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", tipo=" + tipo +
                ", categoria='" + categoria + '\'' +
                ", situacao=" + situacao +
                ", dataMovimentacao=" + dataMovimentacao +
                ", dataVencimento=" + dataVencimento +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }
}

