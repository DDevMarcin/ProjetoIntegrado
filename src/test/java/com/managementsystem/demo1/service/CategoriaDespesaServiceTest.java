package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.repository.CategoriaDespesaRepository;
import com.managementsystem.demo1.repository.MovimentacaoFinanceiraRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CategoriaDespesaServiceTest {

    @Autowired
    private CategoriaDespesaService categoriaDespesaService;

    @Autowired
    private MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    @Autowired
    private CategoriaDespesaRepository categoriaDespesaRepository;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @BeforeEach
    void setUp() {
        movimentacaoFinanceiraRepository.deleteAll();
        categoriaDespesaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        movimentacaoFinanceiraRepository.deleteAll();
        categoriaDespesaService.garantirCategoriasPadrao();
    }

    @Test
    void deveCadastrarCategoriaComDadosValidosEFazerTrim() {
        CategoriaDespesa salva = categoriaDespesaService.salvar(
                "   Manutenção de Equipamentos   ",
                "   Reparos de alicates e ferramentas do ateliê   "
        );

        assertNotNull(salva.getIdCategoria());
        assertEquals("Manutenção de Equipamentos", salva.getNome());
        assertEquals("Reparos de alicates e ferramentas do ateliê", salva.getDescricao());

        CategoriaDespesa semDescricao = categoriaDespesaService.salvar(" Eventos e Feiras ");
        assertNotNull(semDescricao.getIdCategoria());
        assertEquals("Eventos e Feiras", semDescricao.getNome());
        assertNull(semDescricao.getDescricao());
    }

    @Test
    void deveRejeitarCadastroComCamposObrigatoriosAusentesOuInvalidos() {
        IllegalArgumentException exNula = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.salvar((CategoriaDespesa) null)
        );
        assertEquals("Categoria de despesa não pode ser nula.", exNula.getMessage());

        IllegalArgumentException exSemNome = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.salvar(null, "Descrição")
        );
        assertEquals("Nome da categoria de despesa é obrigatório.", exSemNome.getMessage());

        IllegalArgumentException exNomeBranco = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.salvar("   ", "Descrição")
        );
        assertEquals("Nome da categoria de despesa é obrigatório.", exNomeBranco.getMessage());

        IllegalArgumentException exNomeLongo = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.salvar("C".repeat(101), "Descrição")
        );
        assertEquals("Nome da categoria de despesa não pode ultrapassar 100 caracteres.", exNomeLongo.getMessage());

        IllegalArgumentException exDescLonga = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.salvar("Categoria Válida", "D".repeat(256))
        );
        assertEquals("Descrição da categoria de despesa não pode ultrapassar 255 caracteres.", exDescLonga.getMessage());
    }

    @Test
    void deveRejeitarCadastroDeCategoriaDuplicadaIgnorandoMaiusculasEEspacos() {
        categoriaDespesaService.salvar("Insumos", "Matérias-primas");

        IllegalArgumentException exDuplicada = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.salvar("  insumos  ", "Outra descrição")
        );
        assertEquals(
                "Já existe uma categoria de despesa cadastrada com o nome informado: insumos",
                exDuplicada.getMessage()
        );
        assertEquals(1, categoriaDespesaRepository.count());
    }

    @Test
    void deveConsultarCategoriasCadastradasOrdenadasPorNome() {
        CategoriaDespesa c1 = categoriaDespesaService.salvar("Frete", "Correios");
        CategoriaDespesa c2 = categoriaDespesaService.salvar("Embalagens", "Caixas");
        CategoriaDespesa c3 = categoriaDespesaService.salvar("Insumos", "Contas e fios");

        List<CategoriaDespesa> todas = categoriaDespesaService.listarTodas();
        assertEquals(3, todas.size());
        assertEquals("Embalagens", todas.get(0).getNome());
        assertEquals("Frete", todas.get(1).getNome());
        assertEquals("Insumos", todas.get(2).getNome());

        List<CategoriaDespesa> filtradas = categoriaDespesaService.buscarPorNome("emb");
        assertEquals(1, filtradas.size());
        assertEquals(c2.getIdCategoria(), filtradas.get(0).getIdCategoria());

        assertTrue(categoriaDespesaService.buscarPorNomeExato("  FRETE ").isPresent());
        assertEquals(c1.getIdCategoria(), categoriaDespesaService.obterPorId(c1.getIdCategoria()).getIdCategoria());
        assertEquals(c3.getIdCategoria(), categoriaDespesaService.buscarPorId(c3.getIdCategoria()).orElseThrow().getIdCategoria());
        assertFalse(categoriaDespesaService.buscarPorId(null).isPresent());

        assertThrows(NoSuchElementException.class, () -> categoriaDespesaService.obterPorId(999999L));
    }

    @Test
    void deveAtualizarCategoriaESincronizarNomeNasDespesasVinculadas() {
        CategoriaDespesa categoria = categoriaDespesaService.salvar("Transporte", "Fretes locais");
        categoriaDespesaService.salvar("Insumos", "Matéria-prima");

        MovimentacaoFinanceira despesa = movimentacaoFinanceiraService.cadastrarDespesaPorCategoria(
                "Entrega Motoboy",
                new BigDecimal("25.00"),
                categoria.getIdCategoria(),
                LocalDate.of(2026, 11, 10)
        );
        assertEquals("Transporte", despesa.getCategoria());

        CategoriaDespesa atualizada = categoriaDespesaService.atualizar(
                categoria.getIdCategoria(),
                "Logística e Entregas",
                "Fretes locais e correios"
        );

        assertEquals("Logística e Entregas", atualizada.getNome());
        assertEquals("Fretes locais e correios", atualizada.getDescricao());

        MovimentacaoFinanceira despesaRecarregada =
                movimentacaoFinanceiraService.obterDespesaPorId(despesa.getIdMovimentacao());
        assertEquals("Logística e Entregas", despesaRecarregada.getCategoria());
        assertEquals(categoria.getIdCategoria(), despesaRecarregada.getCategoriaDespesa().getIdCategoria());

        // Não deve permitir atualizar para o nome de outra categoria já existente
        IllegalArgumentException exConflito = assertThrows(IllegalArgumentException.class, () ->
                categoriaDespesaService.atualizar(categoria.getIdCategoria(), "insumos", "Conflito")
        );
        assertEquals(
                "Já existe outra categoria de despesa cadastrada com o nome informado: insumos",
                exConflito.getMessage()
        );
    }

    @Test
    void deveExcluirCategoriaSemVinculosEBloquearExclusaoQuandoAssociadaADespesas() {
        CategoriaDespesa semVinculo = categoriaDespesaService.salvar("Categoria Temporária", null);
        CategoriaDespesa comVinculo = categoriaDespesaService.salvar("Insumos", "Contas");

        movimentacaoFinanceiraService.cadastrarDespesaPorCategoria(
                "Compra de Pérolas",
                new BigDecimal("80.00"),
                comVinculo.getIdCategoria(),
                LocalDate.of(2026, 11, 10)
        );

        // Exclusão de categoria sem vínculos deve funcionar
        categoriaDespesaService.excluir(semVinculo.getIdCategoria());
        assertFalse(categoriaDespesaService.buscarPorId(semVinculo.getIdCategoria()).isPresent());

        // Exclusão de categoria vinculada a despesa deve ser bloqueada
        IllegalStateException exVinculo = assertThrows(IllegalStateException.class, () ->
                categoriaDespesaService.excluir(comVinculo.getIdCategoria())
        );
        assertEquals(
                "Não é possível excluir a categoria pois ela está vinculada a uma ou mais despesas.",
                exVinculo.getMessage()
        );
        assertTrue(categoriaDespesaService.buscarPorId(comVinculo.getIdCategoria()).isPresent());

        // Tentativa de excluir categoria inexistente
        assertThrows(NoSuchElementException.class, () -> categoriaDespesaService.excluir(999999L));
    }
}
