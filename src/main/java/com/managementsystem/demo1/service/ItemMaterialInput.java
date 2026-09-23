package com.managementsystem.demo1.service;

import java.math.BigDecimal;

/**
 * Entrada usada na tela de cadastro: cada linha "material + quantidade utilizada"
 * que a artesã informa ao montar um produto.
 */
public record ItemMaterialInput(Long idMaterial, BigDecimal quantidadeUtilizada) {
}