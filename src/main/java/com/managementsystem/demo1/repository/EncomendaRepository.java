package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Encomenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    List<Encomenda> findAllByOrderByDataPedidoDescIdEncomendaDesc();
}