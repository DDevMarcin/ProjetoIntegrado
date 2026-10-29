package com.managementsystem.demo1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "material")
@SuppressWarnings("JpaDataSourceORMInspection")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long idMaterial;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 50)
    private String cor;

    @Column(name = "preco_unidade", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnidade;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida;

    @Column(name = "quantidade_estoque", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadeEstoque = BigDecimal.ZERO;

    public Material() {
    }

    public Material(String nome, String cor, BigDecimal precoUnidade, String unidadeMedida) {
        this.nome = nome;
        this.cor = cor;
        this.precoUnidade = precoUnidade;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque = BigDecimal.ZERO;
    }

    public Material(String nome, String cor, BigDecimal precoUnidade,
                    String unidadeMedida, BigDecimal quantidadeEstoque) {
        this.nome = nome;
        this.cor = cor;
        this.precoUnidade = precoUnidade;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque =
                quantidadeEstoque != null ? quantidadeEstoque : BigDecimal.ZERO;
    }

    public Material(Long idMaterial, String nome, String cor,
                    BigDecimal precoUnidade, String unidadeMedida) {
        this.idMaterial = idMaterial;
        this.nome = nome;
        this.cor = cor;
        this.precoUnidade = precoUnidade;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque = BigDecimal.ZERO;
    }

    public Material(Long idMaterial, String nome, String cor,
                    BigDecimal precoUnidade, String unidadeMedida,
                    BigDecimal quantidadeEstoque) {
        this.idMaterial = idMaterial;
        this.nome = nome;
        this.cor = cor;
        this.precoUnidade = precoUnidade;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque =
                quantidadeEstoque != null ? quantidadeEstoque : BigDecimal.ZERO;
    }

    public Long getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Long idMaterial) {
        this.idMaterial = idMaterial;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public BigDecimal getPrecoUnidade() {
        return precoUnidade;
    }

    public void setPrecoUnidade(BigDecimal precoUnidade) {
        this.precoUnidade = precoUnidade;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(BigDecimal quantidadeEstoque) {
        this.quantidadeEstoque =
                quantidadeEstoque != null ? quantidadeEstoque : BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Material material = (Material) o;

        return idMaterial != null && idMaterial.equals(material.idMaterial);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Material{" +
                "idMaterial=" + idMaterial +
                ", nome='" + nome + '\'' +
                ", cor='" + cor + '\'' +
                ", precoUnidade=" + precoUnidade +
                ", unidadeMedida='" + unidadeMedida + '\'' +
                ", quantidadeEstoque=" + quantidadeEstoque +
                '}';
    }
}