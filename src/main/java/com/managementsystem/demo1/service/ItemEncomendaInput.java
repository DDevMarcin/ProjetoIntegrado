package com.managementsystem.demo1.service;

import java.math.BigDecimal;
import java.util.List;

public record ItemEncomendaInput(String nome,
                                 String descricao,
                                 Integer prazoProducaoDias,
                                 String observacoes,
                                 boolean valorManual,
                                 BigDecimal valorInformado,
                                 List<ItemMaterialInput> materiais) {
}