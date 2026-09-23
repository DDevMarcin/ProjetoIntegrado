package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.ProdutoMaterial;
import com.managementsystem.demo1.model.ProdutoMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoMaterialRepository extends JpaRepository<ProdutoMaterial, ProdutoMaterialId> {
}