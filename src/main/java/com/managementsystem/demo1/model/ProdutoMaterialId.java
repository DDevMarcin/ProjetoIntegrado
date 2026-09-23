package com.managementsystem.demo1.model;

import java.io.Serializable;
import java.util.Objects;

public class ProdutoMaterialId implements Serializable {

    private Long produto;
    private Long material;

    public ProdutoMaterialId() {
    }

    public ProdutoMaterialId(Long produto, Long material) {
        this.produto = produto;
        this.material = material;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProdutoMaterialId)) return false;
        ProdutoMaterialId that = (ProdutoMaterialId) o;
        return Objects.equals(produto, that.produto) && Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produto, material);
    }
}