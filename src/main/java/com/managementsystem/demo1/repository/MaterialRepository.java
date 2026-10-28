package com.managementsystem.demo1.repository;

import com.managementsystem.demo1.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
}