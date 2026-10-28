package com.managementsystem.demo1.service;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.MovimentacaoMaterial;
import com.managementsystem.demo1.model.TipoMovimentacaoMaterial;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.MovimentacaoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import com.managementsystem.demo1.model.TipoProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoMaterialRepository movimentacaoMaterialRepository;

    @BeforeEach
    void setUp() {
        movimentacaoMaterialRepository.deleteAll();
        produtoMaterialRepository.deleteAll();
        produtoRepository.deleteAll();
        materialRepository.deleteAll();
    }

    private Material criarMaterial(String nome, String preco, String unidade) {
        Material material = new Material();
        material.setNome(nome);
        material.setPrecoUnidade(new BigDecimal(preco));
        material.setUnidadeMedida(unidade);
        return materialRepository.save(material);
    }

    @Test
    void deveListarTodosOsMateriaisComResultados() {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        List<Material> materiais = materialService.listarTodos();

        assertEquals(2, materiais.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverMateriais() {
        List<Material> materiais = materialService.listarTodos();

        assertNotNull(materiais);
        assertTrue(materiais.isEmpty());
    }

    @Test
    void deveBuscarMaterialPorIdExistente() {
        Material salvo = criarMaterial("Crucifixo prateado", "3.50", "un");

        Optional<Material> encontrado = materialService.buscarPorId(salvo.getIdMaterial());

        assertTrue(encontrado.isPresent());
        assertEquals("Crucifixo prateado", encontrado.get().getNome());
        assertEquals(0, new BigDecimal("3.50").compareTo(encontrado.get().getPrecoUnidade()));
        assertEquals("un", encontrado.get().getUnidadeMedida());
    }

    @Test
    void deveRetornarVazioAoBuscarPorIdInexistente() {
        Optional<Material> encontrado = materialService.buscarPorId(999999L);

        assertFalse(encontrado.isPresent());
    }

    @Test
    void deveRetornarVazioAoBuscarPorIdNulo() {
        Optional<Material> encontrado = materialService.buscarPorId(null);

        assertFalse(encontrado.isPresent());
    }

    @Test
    void deveBuscarMaterialPorNome() {
        criarMaterial("Contas de resina azul", "0.60", "un");
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Medalha de São Bento", "4.00", "un");

        List<Material> resultados = materialService.buscarPorNome("resina");

        assertEquals(1, resultados.size());
        assertEquals("Contas de resina azul", resultados.get(0).getNome());
    }

    @Test
    void deveBuscarMaterialPorNomeIgnorandoMaiusculasEMinusculas() {
        criarMaterial("Fio Encerado Marrom", "1.50", "m");

        List<Material> resultados = materialService.buscarPorNome("fio encerado");

        assertFalse(resultados.isEmpty());
        assertEquals("Fio Encerado Marrom", resultados.get(0).getNome());
    }

    @Test
    void deveBuscarMaterialPorNomeParcial() {
        criarMaterial("Contas de cristal 8mm", "1.00", "un");
        criarMaterial("Contas de acrílico", "0.30", "un");

        List<Material> resultados = materialService.buscarPorNome("Contas");

        assertEquals(2, resultados.size());
    }

    @Test
    void deveRetornarListaVaziaAoBuscarNomeSemCorrespondencia() {
        criarMaterial("Contas de madeira", "0.50", "un");

        List<Material> resultados = materialService.buscarPorNome("ouro");

        assertNotNull(resultados);
        assertTrue(resultados.isEmpty());
    }

    @Test
    void deveRetornarTodosAoBuscarComNomeVazioOuNulo() {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Crucifixo", "3.00", "un");

        assertEquals(2, materialService.buscarPorNome(null).size());
        assertEquals(2, materialService.buscarPorNome("   ").size());
    }

    @Test
    void deveConsultarMaterialPorUnidadeMedida() {
        criarMaterial("Fio de nylon", "0.80", "m");
        criarMaterial("Fio encerado", "1.20", "m");
        criarMaterial("Medalha", "2.00", "un");

        List<Material> metros = materialService.consultar(null, "m");
        List<Material> unidades = materialService.consultar("", "un");

        assertEquals(2, metros.size());
        assertEquals(1, unidades.size());
    }

    @Test
    void deveConsultarMaterialPorNomeEUnidadeMedida() {
        criarMaterial("Fio de seda", "2.00", "m");
        criarMaterial("Fita de cetim", "1.00", "m");
        criarMaterial("Fio de contas", "15.00", "un");

        List<Material> resultados = materialService.consultar("Fio", "m");

        assertEquals(1, resultados.size());
        assertEquals("Fio de seda", resultados.get(0).getNome());
    }

    @Test
    void deveSalvarMaterialValidoComSucesso() {
        Material material = new Material("Contas de madeira", new BigDecimal("0.50"), "un");

        Material salvo = materialService.salvar(material);

        assertNotNull(salvo.getIdMaterial());
        assertTrue(salvo.getIdMaterial() > 0);
        assertEquals("Contas de madeira", salvo.getNome());
        assertEquals(0, new BigDecimal("0.50").compareTo(salvo.getPrecoUnidade()));
        assertEquals("un", salvo.getUnidadeMedida());

        Optional<Material> doBanco = materialRepository.findById(salvo.getIdMaterial());
        assertTrue(doBanco.isPresent());
        assertEquals("Contas de madeira", doBanco.get().getNome());
        assertEquals(0, new BigDecimal("0.50").compareTo(doBanco.get().getPrecoUnidade()));
        assertEquals("un", doBanco.get().getUnidadeMedida());
    }

    @Test
    void deveSalvarMaterialComEspacosEmBrancoEFazerTrim() {
        Material material = new Material("   Contas de cristal   ", new BigDecimal("1.50"), "   un   ");

        Material salvo = materialService.salvar(material);

        assertEquals("Contas de cristal", salvo.getNome());
        assertEquals("un", salvo.getUnidadeMedida());
    }

    @Test
    void deveSalvarMaterialUsandoSobrecargaDeMetodo() {
        Material salvo = materialService.salvar("Fio de nylon", new BigDecimal("2.30"), "m");

        assertNotNull(salvo.getIdMaterial());
        assertEquals("Fio de nylon", salvo.getNome());
        assertEquals(0, new BigDecimal("2.30").compareTo(salvo.getPrecoUnidade()));
        assertEquals("m", salvo.getUnidadeMedida());
    }

    @Test
    void deveAtualizarMaterialExistenteComSucesso() {
        Material salvo = materialService.salvar(new Material("Fio de nylon", new BigDecimal("2.30"), "m"));

        Material atualizado = materialService.atualizar(salvo.getIdMaterial(), "Fio de nylon grosso", new BigDecimal("3.00"), "m");

        assertEquals(salvo.getIdMaterial(), atualizado.getIdMaterial());
        assertEquals("Fio de nylon grosso", atualizado.getNome());
        assertEquals(0, new BigDecimal("3.00").compareTo(atualizado.getPrecoUnidade()));

        Material doBanco = materialRepository.findById(salvo.getIdMaterial()).orElseThrow();
        assertEquals("Fio de nylon grosso", doBanco.getNome());
        assertEquals(0, new BigDecimal("3.00").compareTo(doBanco.getPrecoUnidade()));
    }

    @Test
    void deveAtualizarMaterialUsandoEntidade() {
        Material salvo = materialService.salvar(new Material("Fio de seda", new BigDecimal("4.00"), "m"));
        salvo.setNome("Fio de seda pura");
        salvo.setPrecoUnidade(new BigDecimal("4.50"));
        salvo.setUnidadeMedida("m");

        Material atualizado = materialService.atualizar(salvo.getIdMaterial(), salvo);

        assertEquals("Fio de seda pura", atualizado.getNome());
        assertEquals(0, new BigDecimal("4.50").compareTo(atualizado.getPrecoUnidade()));
    }

    @Test
    void deveLancarExcecaoAoAtualizarMaterialInexistente() {
        assertThrows(NoSuchElementException.class, () ->
                materialService.atualizar(999999L, "Inexistente", new BigDecimal("1.00"), "un")
        );
    }

    @Test
    void deveLancarExcecaoAoAtualizarComIdNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                materialService.atualizar(null, "Nome", new BigDecimal("1.00"), "un")
        );
    }

    @Test
    void deveLancarExcecaoAoAtualizarComDadosNulos() {
        assertThrows(IllegalArgumentException.class, () ->
                materialService.atualizar(1L, (Material) null)
        );
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                materialService.salvar((Material) null)
        );
        assertEquals("Material não pode ser nulo.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialComNomeNuloOuEmBranco() {
        Material m1 = new Material(null, new BigDecimal("1.00"), "un");
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m1));
        assertEquals("Nome do material é obrigatório.", ex1.getMessage());

        Material m2 = new Material("   ", new BigDecimal("1.00"), "un");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m2));
        assertEquals("Nome do material é obrigatório.", ex2.getMessage());
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialComNomeExcedendo255Caracteres() {
        String nomeLongo = "A".repeat(256);
        Material m = new Material(nomeLongo, new BigDecimal("1.00"), "un");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m));
        assertEquals("Nome do material não pode ultrapassar 255 caracteres.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialComPrecoNulo() {
        Material m = new Material("Contas", null, "un");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m));
        assertEquals("Preço do material inválido.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialComPrecoNegativo() {
        Material m = new Material("Contas", new BigDecimal("-0.01"), "un");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m));
        assertEquals("Preço do material inválido.", ex.getMessage());
    }

    @Test
    void devePermitirSalvarMaterialComPrecoZero() {
        Material m = new Material("Material doado", BigDecimal.ZERO, "un");
        Material salvo = materialService.salvar(m);
        assertNotNull(salvo.getIdMaterial());
        assertEquals(0, BigDecimal.ZERO.compareTo(salvo.getPrecoUnidade()));
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialComUnidadeMedidaNulaOuEmBranco() {
        Material m1 = new Material("Contas", new BigDecimal("1.00"), null);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m1));
        assertEquals("Unidade de medida é obrigatória.", ex1.getMessage());

        Material m2 = new Material("Contas", new BigDecimal("1.00"), "  ");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m2));
        assertEquals("Unidade de medida é obrigatória.", ex2.getMessage());
    }

    @Test
    void deveLancarExcecaoAoSalvarMaterialComUnidadeMedidaExcedendo10Caracteres() {
        Material m = new Material("Contas", new BigDecimal("1.00"), "centimetros");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> materialService.salvar(m));
        assertEquals("Unidade de medida não pode ultrapassar 10 caracteres.", ex.getMessage());
    }

    @Test
    void deveExcluirMaterialSemVinculoComProduto() {
        Material salvo = materialService.salvar(new Material("Madeira", new BigDecimal("0.50"), "un"));
        Long id = salvo.getIdMaterial();

        materialService.excluir(id);

        assertFalse(materialRepository.findById(id).isPresent());
    }

    @Test
    void deveLancarExcecaoAoExcluirMaterialInexistente() {
        assertThrows(NoSuchElementException.class, () -> materialService.excluir(999999L));
    }

    @Test
    void deveLancarExcecaoAoExcluirMaterialComIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> materialService.excluir(null));
    }

    @Test
    void deveLancarExcecaoAoExcluirMaterialVinculadoAProduto() {
        Material material = materialService.salvar(new Material("Fio encerado", new BigDecimal("1.20"), "m"));

        produtoService.cadastrarComMateriais(
                "PROD-TEST-001",
                "Pulseira",
                "Pulseira artesanal",
                TipoProduto.PRE_PRONTO,
                5,
                null,
                null,
                false,
                null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("2")))
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                materialService.excluir(material.getIdMaterial())
        );
        assertEquals("Não é possível excluir o material pois ele está vinculado a um ou mais produtos.", ex.getMessage());
        assertTrue(materialRepository.findById(material.getIdMaterial()).isPresent());
    }

    @Test
    void deveGarantirIdentificadoresAutoincrementDistintos() {
        Material m1 = materialService.salvar(new Material("Mat 1", new BigDecimal("1.00"), "un"));
        Material m2 = materialService.salvar(new Material("Mat 2", new BigDecimal("2.00"), "un"));

        assertNotNull(m1.getIdMaterial());
        assertNotNull(m2.getIdMaterial());
        assertNotEquals(m1.getIdMaterial(), m2.getIdMaterial());
        assertTrue(m2.getIdMaterial() > m1.getIdMaterial());
    }

    @Test
    void deveRegistrarMovimentacaoDeEntradaEAtualizarSaldoDoMaterial() {
        Material material = materialService.salvar("Contas de madeira", new BigDecimal("0.50"), "un", new BigDecimal("10.00"));

        MovimentacaoMaterial mov = materialService.registrarMovimentacao(
                material.getIdMaterial(),
                TipoMovimentacaoMaterial.ENTRADA,
                new BigDecimal("25.50"),
                "Compra de fornecedor",
                "Nota fiscal 123"
        );

        assertNotNull(mov.getIdMovimentacao());
        assertEquals(TipoMovimentacaoMaterial.ENTRADA, mov.getTipo());
        assertEquals(0, new BigDecimal("25.50").compareTo(mov.getQuantidade()));
        assertEquals(0, new BigDecimal("10.00").compareTo(mov.getSaldoAnterior()));
        assertEquals(0, new BigDecimal("35.50").compareTo(mov.getSaldoAtual()));
        assertEquals("Compra de fornecedor", mov.getMotivo());
        assertEquals("Nota fiscal 123", mov.getObservacoes());
        assertNotNull(mov.getDataHora());

        Material atualizado = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("35.50").compareTo(atualizado.getQuantidadeEstoque()));
    }

    @Test
    void deveRegistrarMovimentacaoDeSaidaEAtualizarSaldoDoMaterial() {
        Material material = materialService.salvar("Fio encerado", new BigDecimal("1.20"), "m", new BigDecimal("50.00"));

        MovimentacaoMaterial mov = materialService.registrarMovimentacao(
                material.getIdMaterial(),
                TipoMovimentacaoMaterial.SAIDA,
                new BigDecimal("15.00"),
                "Uso na produção",
                null
        );

        assertNotNull(mov.getIdMovimentacao());
        assertEquals(TipoMovimentacaoMaterial.SAIDA, mov.getTipo());
        assertEquals(0, new BigDecimal("50.00").compareTo(mov.getSaldoAnterior()));
        assertEquals(0, new BigDecimal("35.00").compareTo(mov.getSaldoAtual()));

        Material atualizado = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("35.00").compareTo(atualizado.getQuantidadeEstoque()));
    }

    @Test
    void deveRejeitarSaidaComSaldoInsuficientePreservandoSaldoEHistorico() {
        Material material = materialService.salvar("Medalha", new BigDecimal("3.00"), "un", new BigDecimal("5.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                materialService.registrarMovimentacao(
                        material.getIdMaterial(),
                        TipoMovimentacaoMaterial.SAIDA,
                        new BigDecimal("6.00"),
                        "Saída maior que estoque",
                        null
                )
        );

        assertEquals("Saldo insuficiente em estoque para realizar a saída do material.", ex.getMessage());

        Material noBanco = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("5.00").compareTo(noBanco.getQuantidadeEstoque()));
        assertTrue(movimentacaoMaterialRepository.findAll().isEmpty());
    }

    @Test
    void deveRegistrarPerdaDeMaterialEAtualizarSaldoConsistente() {
        Material material = materialService.salvar("Contas de cristal", new BigDecimal("1.00"), "un", new BigDecimal("40.00"));

        MovimentacaoMaterial perda = materialService.registrarPerda(
                material.getIdMaterial(),
                new BigDecimal("8.00"),
                "Contas trincadas no transporte",
                "Lote 04"
        );

        assertNotNull(perda.getIdMovimentacao());
        assertEquals(TipoMovimentacaoMaterial.PERDA, perda.getTipo());
        assertEquals(0, new BigDecimal("8.00").compareTo(perda.getQuantidade()));
        assertEquals(0, new BigDecimal("40.00").compareTo(perda.getSaldoAnterior()));
        assertEquals(0, new BigDecimal("32.00").compareTo(perda.getSaldoAtual()));
        assertEquals("Contas trincadas no transporte", perda.getMotivo());
        assertEquals("Lote 04", perda.getObservacoes());

        Material noBanco = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("32.00").compareTo(noBanco.getQuantidadeEstoque()));
        assertEquals(1, materialService.listarPerdasPorMaterial(material.getIdMaterial()).size());
    }

    @Test
    void deveRejeitarPerdaComSaldoInsuficienteOuSemMotivo() {
        Material material = materialService.salvar("Fita de cetim", new BigDecimal("2.00"), "m", new BigDecimal("10.00"));

        IllegalArgumentException exSemMotivo = assertThrows(IllegalArgumentException.class, () ->
                materialService.registrarPerda(material.getIdMaterial(), new BigDecimal("2.00"), "   ", null)
        );
        assertEquals("Motivo da perda é obrigatório.", exSemMotivo.getMessage());

        IllegalArgumentException exSaldo = assertThrows(IllegalArgumentException.class, () ->
                materialService.registrarPerda(material.getIdMaterial(), new BigDecimal("15.00"), "Avaria por umidade", null)
        );
        assertEquals("Saldo insuficiente em estoque para registrar a perda do material.", exSaldo.getMessage());

        Material noBanco = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("10.00").compareTo(noBanco.getQuantidadeEstoque()));
        assertTrue(movimentacaoMaterialRepository.findAll().isEmpty());
    }

    @Test
    void deveManterConsistenciaEntreMultiplasMovimentacoesEPerdasSemDuplicidade() {
        Material material = materialService.salvar("Crucifixo", new BigDecimal("4.00"), "un", BigDecimal.ZERO);

        materialService.registrarMovimentacao(material.getIdMaterial(), TipoMovimentacaoMaterial.ENTRADA, new BigDecimal("50.00"), "Estoque inicial", null);
        materialService.registrarMovimentacao(material.getIdMaterial(), TipoMovimentacaoMaterial.SAIDA, new BigDecimal("12.00"), "Produção", null);
        materialService.registrarPerda(material.getIdMaterial(), new BigDecimal("3.00"), "Peça oxidada", null);

        Material noBanco = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("35.00").compareTo(noBanco.getQuantidadeEstoque()));

        List<MovimentacaoMaterial> historico = materialService.listarMovimentacoesPorMaterial(material.getIdMaterial());
        assertEquals(3, historico.size());
        assertEquals(0, new BigDecimal("50.00").compareTo(historico.get(0).getSaldoAtual()));
        assertEquals(0, new BigDecimal("38.00").compareTo(historico.get(1).getSaldoAtual()));
        assertEquals(0, new BigDecimal("35.00").compareTo(historico.get(2).getSaldoAtual()));

        List<MovimentacaoMaterial> perdas = materialService.listarPerdasPorMaterial(material.getIdMaterial());
        assertEquals(1, perdas.size());
        assertEquals("Peça oxidada", perdas.get(0).getMotivo());
    }

    @Test
    void naoDeveSalvarMaterialComNomeDuplicadoIgnorandoCaseEEspacos() {
        materialService.salvar("Contas de madeira", new BigDecimal("0.50"), "un");

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                materialService.salvar("Contas de madeira", new BigDecimal("0.80"), "cx")
        );
        assertTrue(ex1.getMessage().contains("Já existe um material cadastrado com o nome informado"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                materialService.salvar("   CONTAS DE MADEIRA   ", new BigDecimal("1.00"), "un")
        );
        assertTrue(ex2.getMessage().contains("Já existe um material cadastrado com o nome informado"));

        assertEquals(1, materialRepository.count());
    }

    @Test
    void naoDeveAtualizarMaterialComNomeDuplicadoDeOutroMaterial() {
        materialService.salvar("Contas de madeira", new BigDecimal("0.50"), "un");
        Material m2 = materialService.salvar("Fio encerado", new BigDecimal("1.20"), "m");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                materialService.atualizar(m2.getIdMaterial(), "  contas de madeira  ", new BigDecimal("1.50"), "m")
        );
        assertTrue(ex.getMessage().contains("Já existe outro material cadastrado com o nome informado"));

        Material atualizadoMesmoNome = materialService.atualizar(m2.getIdMaterial(), "Fio encerado", new BigDecimal("2.00"), "m");
        assertEquals(0, new BigDecimal("2.00").compareTo(atualizadoMesmoNome.getPrecoUnidade()));
    }
}
