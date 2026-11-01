package com.managementsystem.demo1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_material")
@SuppressWarnings("JpaDataSourceORMInspection")
public class MovimentacaoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    private Long idMovimentacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "id_material",
            referencedColumnName = "id_material",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_movimentacao_material_material")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Material material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoMaterial tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "saldo_anterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_atual", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoAtual;

    @Column(length = 255)
    private String motivo;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public MovimentacaoMaterial() {
    }

    public MovimentacaoMaterial(Material material, TipoMovimentacaoMaterial tipo, BigDecimal quantidade,
                                BigDecimal saldoAnterior, BigDecimal saldoAtual, String motivo,
                                String observacoes, LocalDateTime dataHora) {
        this.material = material;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.saldoAnterior = saldoAnterior;
        this.saldoAtual = saldoAtual;
        this.motivo = motivo;
        this.observacoes = observacoes;
        this.dataHora = dataHora;
    }

    public Long getIdMovimentacao() {
        return idMovimentacao;
    }

    public void setIdMovimentacao(Long idMovimentacao) {
        this.idMovimentacao = idMovimentacao;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public TipoMovimentacaoMaterial getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacaoMaterial tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public void setSaldoAnterior(BigDecimal saldoAnterior) {
        this.saldoAnterior = saldoAnterior;
    }

    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }

    public void setSaldoAtual(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimentacaoMaterial that = (MovimentacaoMaterial) o;
        return idMovimentacao != null && idMovimentacao.equals(that.idMovimentacao);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

