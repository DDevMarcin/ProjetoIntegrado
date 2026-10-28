package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}