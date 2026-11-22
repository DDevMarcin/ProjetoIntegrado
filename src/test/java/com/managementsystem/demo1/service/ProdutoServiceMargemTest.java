package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProdutoServiceMargemTest {

    private ProdutoRepository produtoRepository;
    private MaterialRepository materialRepository;
    private ProdutoService service;

    @BeforeEach
    void setUp() {
        produtoRepository = mock(ProdutoRepository.class);
        materialRepository = mock(MaterialRepository.class);
        service = new ProdutoService(produtoRepository, materialRepository);

        Material contas = mock(Material.class);
        when(contas.getPrecoUnidade()).thenReturn(new BigDecimal("0.50"));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(contas));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Produto cadastrar(TipoProduto tipo, boolean manual, BigDecimal valorManual) {
        return service.cadastrarComMateriais("T-1", "Teste", null, tipo, null, 7, null,
                manual, valorManual, List.of(new ItemMaterialInput(1L, new BigDecimal("50"))));
    }

    @Test
    void personalizadoPorMateriaisAplicaMargem() {
        assertEquals(new BigDecimal("30.00"), cadastrar(TipoProduto.PERSONALIZADO, false, null).getValor());
    }

    @Test
    void personalizadoManualAplicaMargem() {
        assertEquals(new BigDecimal("120.00"),
                cadastrar(TipoProduto.PERSONALIZADO, true, new BigDecimal("100.00")).getValor());
    }

    @Test
    void preProntoNaoAplicaMargem() {
        assertEquals(0, new BigDecimal("25.00")
                .compareTo(cadastrar(TipoProduto.PRE_PRONTO, false, null).getValor()));
    }
}