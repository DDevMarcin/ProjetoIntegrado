package com.managementsystem.demo1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_material")
@IdClass(ProdutoMaterialId.class)
public class ProdutoMaterial {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_produto",
            referencedColumnName = "id_produto",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_produto_material_produto")
    )
    @JsonIgnore
    private Produto produto;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_material",
            referencedColumnName = "id_material",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_produto_material_material")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Material material;

    @Column(name = "quantidade_utilizada", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadeUtilizada;

    public ProdutoMaterial() {
    }

    public ProdutoMaterial(Produto produto, Material material, BigDecimal quantidadeUtilizada) {
        this.produto = produto;
        this.material = material;
        this.quantidadeUtilizada = quantidadeUtilizada;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BigDecimal getQuantidadeUtilizada() {
        return quantidadeUtilizada;
    }

    public void setQuantidadeUtilizada(BigDecimal quantidadeUtilizada) {
        this.quantidadeUtilizada = quantidadeUtilizada;
    }
}