package com.managementsystem.demo1.service;

import com.managementsystem.demo1.model.*;
import com.managementsystem.demo1.repository.EncomendaRepository;
import com.managementsystem.demo1.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EncomendaServiceTest {

    private EncomendaRepository encomendaRepository;
    private EncomendaService service;

    @BeforeEach
    void setUp() {
        encomendaRepository = mock(EncomendaRepository.class);
        MaterialRepository materialRepository = mock(MaterialRepository.class);
        service = new EncomendaService(encomendaRepository, materialRepository);

        Material contas = mock(Material.class);
        when(contas.getPrecoUnidade()).thenReturn(new BigDecimal("0.50"));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(contas));
        when(encomendaRepository.save(any(Encomenda.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ItemEncomendaInput terco() {
        return new ItemEncomendaInput("Terço", null, 7, null, false, null,
                List.of(new ItemMaterialInput(1L, new BigDecimal("50"))));
    }

    private ItemEncomendaInput pulseira() {
        return new ItemEncomendaInput("Pulseira", null, 3, null, true,
                new BigDecimal("40.00"), List.of());
    }

    @Test
    void calculaSubtotalTotalEEntrega() {
        Encomenda e = service.cadastrar("Maria", null, LocalDate.of(2026, 11, 23), null,
                List.of(terco(), pulseira()));

        assertEquals(new BigDecimal("65.00"), e.getSubtotal());
        assertEquals(new BigDecimal("78.00"), e.getValorTotal());
        assertEquals(LocalDate.of(2026, 11, 30), e.getDataEntregaPrevista());
        assertEquals(SituacaoEncomenda.PENDENTE, e.getSituacao());
        assertEquals(2, e.getItens().size());
    }

    @Test
    void semClienteDaErro() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar("  ", null, null, null, List.of(terco())));
    }

    @Test
    void semItensDaErro() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar("Maria", null, null, null, List.of()));
    }

    @Test
    void naoPulaEtapaDaSituacao() {
        Encomenda e = new Encomenda();
        e.setSituacao(SituacaoEncomenda.PENDENTE);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalArgumentException.class,
                () -> service.alterarSituacao(1L, SituacaoEncomenda.ENTREGUE));
    }

    @Test
    void naoMexeEmEncomendaEntregue() {
        Encomenda e = new Encomenda();
        e.setSituacao(SituacaoEncomenda.ENTREGUE);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(IllegalArgumentException.class,
                () -> service.alterarSituacao(1L, SituacaoEncomenda.CANCELADA));
    }

    @Test
    void avancaPeloCaminhoNormal() {
        Encomenda e = new Encomenda();
        e.setSituacao(SituacaoEncomenda.PENDENTE);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertEquals(SituacaoEncomenda.EM_PRODUCAO,
                service.alterarSituacao(1L, SituacaoEncomenda.EM_PRODUCAO).getSituacao());
    }
}