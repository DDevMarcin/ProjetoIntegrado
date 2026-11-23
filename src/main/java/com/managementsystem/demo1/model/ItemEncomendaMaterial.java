package com.managementsystem.demo1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_encomenda_material")
public class ItemEncomendaMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_item")
    private ItemEncomenda item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_material")
    private Material material;

    @Column(name = "quantidade_utilizada", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidadeUtilizada;

    public ItemEncomendaMaterial() {}

    public ItemEncomendaMaterial(Material material, BigDecimal quantidadeUtilizada) {
        this.material = material;
        this.quantidadeUtilizada = quantidadeUtilizada;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemEncomenda getItem() {
        return item;
    }

    public void setItem(ItemEncomenda item) {
        this.item = item;
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