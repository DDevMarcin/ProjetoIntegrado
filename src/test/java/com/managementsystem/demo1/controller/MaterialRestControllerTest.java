package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.MovimentacaoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ItemMaterialInput;
import com.managementsystem.demo1.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class MaterialRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoService produtoService;

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

    private Material criarMaterialComEstoque(String nome, String preco, String unidade, String estoque) {
        Material material = new Material(nome, new BigDecimal(preco), unidade, new BigDecimal(estoque));
        return materialRepository.save(material);
    }

    @Test
    void deveListarTodosOsMateriaisViaGet() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        mockMvc.perform(get("/materiais"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Contas de madeira")))
                .andExpect(jsonPath("$[1].nome", is("Fio encerado")));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverMateriaisCadastrados() throws Exception {
        mockMvc.perform(get("/materiais"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveFiltrarMateriaisPorNomeViaParametroGet() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        mockMvc.perform(get("/materiais").param("nome", "madeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Contas de madeira")));
    }

    @Test
    void deveFiltrarMateriaisPorUnidadeMedidaViaParametroGet() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        criarMaterial("Fio encerado", "1.20", "m");

        mockMvc.perform(get("/materiais").param("unidadeMedida", "m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Fio encerado")));
    }

    @Test
    void deveFiltrarMateriaisPorNomeEUnidadeMedidaCombinadosViaGet() throws Exception {
        criarMaterial("Fio de seda", "2.00", "m");
        criarMaterial("Fio de contas", "15.00", "un");
        criarMaterial("Fita de cetim", "1.00", "m");

        mockMvc.perform(get("/materiais")
                        .param("nome", "Fio")
                        .param("unidadeMedida", "m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Fio de seda")))
                .andExpect(jsonPath("$[0].unidadeMedida", is("m")));
    }

    @Test
    void deveBuscarMaterialPorNomeViaRotaBuscar() throws Exception {
        criarMaterial("Contas de resina", "0.75", "un");
        criarMaterial("Crucifixo dourado", "5.00", "un");

        mockMvc.perform(get("/materiais/buscar").param("nome", "resina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Contas de resina")));
    }

    @Test
    void deveRetornarArrayVazioQuandoBuscaPorNomeNaoTiverCorrespondencia() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");

        mockMvc.perform(get("/materiais/buscar").param("nome", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveBuscarMaterialPorIdComSucesso() throws Exception {
        Material material = criarMaterial("Medalha Milagrosa", "4.50", "un");

        mockMvc.perform(get("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterial", is(material.getIdMaterial().intValue())))
                .andExpect(jsonPath("$.nome", is("Medalha Milagrosa")))
                .andExpect(jsonPath("$.precoUnidade", is(4.50)))
                .andExpect(jsonPath("$.unidadeMedida", is("un")));
    }

    @Test
    void deveRetornar404AoBuscarMaterialPorIdInexistente() throws Exception {
        mockMvc.perform(get("/materiais/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoBuscarMaterialComIdEmFormatoInvalido() throws Exception {
        mockMvc.perform(get("/materiais/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Identificador de material inválido.")));
    }

    @Test
    void deveCadastrarMaterialValidoViaPostRetornando201() throws Exception {
        String json = """
                {
                    "nome": "Contas de madeira",
                    "precoUnidade": 0.50,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idMaterial", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Contas de madeira")))
                .andExpect(jsonPath("$.precoUnidade", is(0.50)))
                .andExpect(jsonPath("$.unidadeMedida", is("un")));

        assertEquals(1, materialRepository.count());
        Material persistido = materialRepository.findAll().get(0);
        assertEquals("Contas de madeira", persistido.getNome());
        assertEquals(0, new BigDecimal("0.50").compareTo(persistido.getPrecoUnidade()));
        assertEquals("un", persistido.getUnidadeMedida());
    }

    @Test
    void deveIgnorarIdEnviadoPeloClienteAoCadastrarMaterialViaPost() throws Exception {
        String json = """
                {
                    "idMaterial": 99999,
                    "nome": "Crucifixo de metal",
                    "precoUnidade": 3.50,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMaterial", notNullValue()))
                .andExpect(jsonPath("$.idMaterial", not(99999)))
                .andExpect(jsonPath("$.nome", is("Crucifixo de metal")));

        assertFalse(materialRepository.existsById(99999L));
        assertEquals(1, materialRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComBodyNulo() throws Exception {
        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados do material são obrigatórios.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComJsonOuTipoIncompativel() throws Exception {
        String jsonInvalido = """
                {
                    "nome": "Contas",
                    "precoUnidade": "valor_invalido",
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Formato de requisição inválido ou campos incompatíveis.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComNomeEmBranco() throws Exception {
        String json = """
                {
                    "nome": "   ",
                    "precoUnidade": 1.00,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do material é obrigatório.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComNomeAcimaDe255Caracteres() throws Exception {
        String nomeLongo = "M".repeat(256);
        String json = """
                {
                    "nome": "%s",
                    "precoUnidade": 1.00,
                    "unidadeMedida": "un"
                }
                """.formatted(nomeLongo);

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do material não pode ultrapassar 255 caracteres.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComPrecoNegativo() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "precoUnidade": -0.50,
                    "unidadeMedida": "m"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Preço do material inválido.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComPrecoNulo() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "unidadeMedida": "m"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Preço do material inválido.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComUnidadeMedidaEmBranco() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "precoUnidade": 1.50,
                    "unidadeMedida": "   "
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Unidade de medida é obrigatória.")));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComUnidadeMedidaInvalida() throws Exception {
        String json = """
                {
                    "nome": "Fio de nylon",
                    "precoUnidade": 1.50,
                    "unidadeMedida": "centimetros"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Unidade de medida não pode ultrapassar 10 caracteres.")));
    }

    @Test
    void deveAtualizarMaterialExistenteViaPutEPersistirNoBanco() throws Exception {
        Material salvo = criarMaterial("Contas simples", "0.40", "un");

        String jsonAtualizacao = """
                {
                    "idMaterial": 99999,
                    "nome": "Contas especiais",
                    "precoUnidade": 0.80,
                    "unidadeMedida": "cx"
                }
                """;

        mockMvc.perform(put("/materiais/" + salvo.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMaterial", is(salvo.getIdMaterial().intValue())))
                .andExpect(jsonPath("$.nome", is("Contas especiais")))
                .andExpect(jsonPath("$.precoUnidade", is(0.80)))
                .andExpect(jsonPath("$.unidadeMedida", is("cx")));

        Material atualizadoNoBanco = materialRepository.findById(salvo.getIdMaterial()).orElseThrow();
        assertEquals(salvo.getIdMaterial(), atualizadoNoBanco.getIdMaterial());
        assertEquals("Contas especiais", atualizadoNoBanco.getNome());
        assertEquals(0, new BigDecimal("0.80").compareTo(atualizadoNoBanco.getPrecoUnidade()));
        assertEquals("cx", atualizadoNoBanco.getUnidadeMedida());
    }

    @Test
    void deveRetornar400AoAtualizarMaterialComBodyNulo() throws Exception {
        Material salvo = criarMaterial("Contas", "0.40", "un");

        mockMvc.perform(put("/materiais/" + salvo.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados do material são obrigatórios.")));
    }

    @Test
    void deveRetornar400AoAtualizarMaterialComDadosInvalidosPreservandoRegistroOriginal() throws Exception {
        Material salvo = criarMaterial("Contas originais", "0.40", "un");

        String jsonNomeInvalido = """
                {
                    "nome": "",
                    "precoUnidade": 0.80,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(put("/materiais/" + salvo.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNomeInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do material é obrigatório.")));

        String jsonPrecoNegativo = """
                {
                    "nome": "Contas alteradas",
                    "precoUnidade": -5.00,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(put("/materiais/" + salvo.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPrecoNegativo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Preço do material inválido.")));

        Material noBanco = materialRepository.findById(salvo.getIdMaterial()).orElseThrow();
        assertEquals("Contas originais", noBanco.getNome());
        assertEquals(0, new BigDecimal("0.40").compareTo(noBanco.getPrecoUnidade()));
    }

    @Test
    void deveRetornar404AoAtualizarMaterialInexistenteViaPut() throws Exception {
        String jsonAtualizacao = """
                {
                    "nome": "Contas",
                    "precoUnidade": 0.80,
                    "unidadeMedida": "un"
                }
                """;

        mockMvc.perform(put("/materiais/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizacao))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExcluirMaterialSemVinculoViaDelete() throws Exception {
        Material salvo = criarMaterial("Material avulso", "2.00", "un");

        mockMvc.perform(delete("/materiais/" + salvo.getIdMaterial()))
                .andExpect(status().isNoContent());

        assertFalse(materialRepository.findById(salvo.getIdMaterial()).isPresent());
    }

    @Test
    void deveRetornar404AoExcluirMaterialInexistenteViaDelete() throws Exception {
        mockMvc.perform(delete("/materiais/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoExcluirMaterialComIdInvalidoViaDelete() throws Exception {
        mockMvc.perform(delete("/materiais/id-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Identificador de material inválido.")));
    }

    @Test
    void deveRetornar400AoExcluirMaterialVinculadoAProdutoViaDelete() throws Exception {
        Material material = criarMaterial("Contas de madeira", "0.50", "un");

        produtoService.cadastrarComMateriais(
                "COD-DEL-01",
                "Rosario",
                "Rosario simples",
                TipoProduto.PRE_PRONTO,
                1,
                null,
                null,
                false,
                null,
                List.of(new ItemMaterialInput(material.getIdMaterial(), new BigDecimal("10")))
        );

        mockMvc.perform(delete("/materiais/" + material.getIdMaterial()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Não é possível excluir o material pois ele está vinculado a um ou mais produtos.")));

        assertTrue(materialRepository.findById(material.getIdMaterial()).isPresent());
    }

    // =========================================================================
    // Testes dos Endpoints de Movimentação de Material
    // =========================================================================

    @Test
    void deveRegistrarMovimentacaoDeEntradaViaPostPorIdRetornando201() throws Exception {
        Material material = criarMaterialComEstoque("Contas de madeira", "0.50", "un", "20.00");

        String json = """
                {
                    "tipo": "ENTRADA",
                    "quantidade": 30.00,
                    "motivo": "Reposição de estoque",
                    "observacoes": "Fornecedor A"
                }
                """;

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao", notNullValue()))
                .andExpect(jsonPath("$.tipo", is("ENTRADA")))
                .andExpect(jsonPath("$.quantidade", is(30.00)))
                .andExpect(jsonPath("$.saldoAnterior", is(20)))
                .andExpect(jsonPath("$.saldoAtual", is(50.00)))
                .andExpect(jsonPath("$.motivo", is("Reposição de estoque")))
                .andExpect(jsonPath("$.observacoes", is("Fornecedor A")))
                .andExpect(jsonPath("$.dataHora", notNullValue()));

        Material atualizado = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(atualizado.getQuantidadeEstoque()));
        assertEquals(1, movimentacaoMaterialRepository.count());
    }

    @Test
    void deveRegistrarMovimentacaoDeSaidaViaPostGeralRetornando201() throws Exception {
        Material material = criarMaterialComEstoque("Fio encerado", "1.20", "m", "15.00");

        String json = """
                {
                    "idMaterial": %d,
                    "tipo": "SAIDA",
                    "quantidade": 5.50,
                    "motivo": "Consumo em encomenda"
                }
                """.formatted(material.getIdMaterial());

        mockMvc.perform(post("/materiais/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo", is("SAIDA")))
                .andExpect(jsonPath("$.quantidade", is(5.50)))
                .andExpect(jsonPath("$.saldoAnterior", is(15)))
                .andExpect(jsonPath("$.saldoAtual", is(9.50)));

        Material atualizado = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("9.50").compareTo(atualizado.getQuantidadeEstoque()));
    }

    @Test
    void deveRetornar404AoMovimentarMaterialInexistente() throws Exception {
        String json = """
                {
                    "tipo": "ENTRADA",
                    "quantidade": 10.00
                }
                """;

        mockMvc.perform(post("/materiais/999999/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoMovimentarComCamposObrigatoriosAusentesOuQuantidadeInvalida() throws Exception {
        Material material = criarMaterialComEstoque("Medalha", "3.00", "un", "10.00");

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados da movimentação são obrigatórios.")));

        String jsonSemTipo = """
                {
                    "quantidade": 5.00
                }
                """;
        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemTipo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Tipo de movimentação é obrigatório.")));

        String jsonQuantidadeZero = """
                {
                    "tipo": "ENTRADA",
                    "quantidade": 0
                }
                """;
        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuantidadeZero))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Quantidade movimentada deve ser maior que zero.")));

        String jsonQuantidadeNegativa = """
                {
                    "tipo": "ENTRADA",
                    "quantidade": -5.00
                }
                """;
        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuantidadeNegativa))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Quantidade movimentada deve ser maior que zero.")));

        assertEquals(0, movimentacaoMaterialRepository.count());
    }

    @Test
    void deveRetornar400AoRealizarSaidaComSaldoInsuficientePreservandoEstoque() throws Exception {
        Material material = criarMaterialComEstoque("Contas", "0.50", "un", "8.00");

        String json = """
                {
                    "tipo": "SAIDA",
                    "quantidade": 12.00,
                    "motivo": "Retirada acima do saldo"
                }
                """;

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Saldo insuficiente em estoque para realizar a saída do material.")));

        Material noBanco = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("8.00").compareTo(noBanco.getQuantidadeEstoque()));
        assertEquals(0, movimentacaoMaterialRepository.count());
    }

    // =========================================================================
    // Testes dos Endpoints de Perda de Material
    // =========================================================================

    @Test
    void deveRegistrarPerdaDeMaterialViaPostPorIdEAtualizarSaldo() throws Exception {
        Material material = criarMaterialComEstoque("Contas de vidro", "0.80", "un", "50.00");

        String json = """
                {
                    "quantidade": 7.00,
                    "motivo": "Quebra durante manuseio",
                    "observacoes": "Descarte imediato"
                }
                """;

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao", notNullValue()))
                .andExpect(jsonPath("$.tipo", is("PERDA")))
                .andExpect(jsonPath("$.quantidade", is(7.00)))
                .andExpect(jsonPath("$.saldoAnterior", is(50)))
                .andExpect(jsonPath("$.saldoAtual", is(43.00)))
                .andExpect(jsonPath("$.motivo", is("Quebra durante manuseio")))
                .andExpect(jsonPath("$.observacoes", is("Descarte imediato")));

        Material atualizado = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("43.00").compareTo(atualizado.getQuantidadeEstoque()));
        assertEquals(1, movimentacaoMaterialRepository.count());
    }

    @Test
    void deveRegistrarPerdaViaPostGeralComIdMaterialNoBody() throws Exception {
        Material material = criarMaterialComEstoque("Fita dourada", "2.50", "m", "20.00");

        String json = """
                {
                    "idMaterial": %d,
                    "quantidade": 3.50,
                    "motivo": "Mancha no tecido"
                }
                """.formatted(material.getIdMaterial());

        mockMvc.perform(post("/materiais/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo", is("PERDA")))
                .andExpect(jsonPath("$.quantidade", is(3.50)))
                .andExpect(jsonPath("$.saldoAnterior", is(20)))
                .andExpect(jsonPath("$.saldoAtual", is(16.50)))
                .andExpect(jsonPath("$.motivo", is("Mancha no tecido")));

        Material atualizado = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("16.50").compareTo(atualizado.getQuantidadeEstoque()));
    }

    @Test
    void deveRetornar404AoRegistrarPerdaParaMaterialInexistente() throws Exception {
        String json = """
                {
                    "quantidade": 2.00,
                    "motivo": "Extravio"
                }
                """;

        mockMvc.perform(post("/materiais/999999/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoRegistrarPerdaSemMotivoOuComQuantidadeInvalidaOuSaldoInsuficiente() throws Exception {
        Material material = criarMaterialComEstoque("Corrente prateada", "5.00", "m", "10.00");

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/perdas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados da perda são obrigatórios.")));

        String jsonSemMotivo = """
                {
                    "quantidade": 2.00,
                    "motivo": "   "
                }
                """;
        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemMotivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Motivo da perda é obrigatório.")));

        String jsonQuantidadeInvalida = """
                {
                    "quantidade": 0,
                    "motivo": "Oxidação"
                }
                """;
        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuantidadeInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Quantidade perdida deve ser maior que zero.")));

        String jsonPerdaAcimaDoSaldo = """
                {
                    "quantidade": 15.00,
                    "motivo": "Oxidação total"
                }
                """;
        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPerdaAcimaDoSaldo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Saldo insuficiente em estoque para registrar a perda do material.")));

        Material noBanco = materialRepository.findById(material.getIdMaterial()).orElseThrow();
        assertEquals(0, new BigDecimal("10.00").compareTo(noBanco.getQuantidadeEstoque()));
        assertEquals(0, movimentacaoMaterialRepository.count());
    }

    @Test
    void deveConsultarHistoricoDeMovimentacoesEPerdasPorMaterial() throws Exception {
        Material material = criarMaterialComEstoque("Contas", "0.50", "un", "0.00");

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipo": "ENTRADA",
                                    "quantidade": 100.00,
                                    "motivo": "Compra inicial"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipo": "SAIDA",
                                    "quantidade": 30.00,
                                    "motivo": "Produção de terços"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/materiais/" + material.getIdMaterial() + "/perdas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantidade": 5.00,
                                    "motivo": "Defeito de fabricação"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/materiais/" + material.getIdMaterial() + "/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].tipo", is("ENTRADA")))
                .andExpect(jsonPath("$[1].tipo", is("SAIDA")))
                .andExpect(jsonPath("$[2].tipo", is("PERDA")))
                .andExpect(jsonPath("$[2].saldoAtual", is(65)));

        mockMvc.perform(get("/materiais/" + material.getIdMaterial() + "/perdas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo", is("PERDA")))
                .andExpect(jsonPath("$[0].motivo", is("Defeito de fabricação")));

        mockMvc.perform(get("/materiais/perdas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deveRetornar400AoCadastrarMaterialComNomeDuplicado() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");

        String jsonDuplicado = """
                {
                    "nome": "  contas de madeira  ",
                    "precoUnidade": 1.00,
                    "unidadeMedida": "cx"
                }
                """;

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDuplicado))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe um material cadastrado com o nome informado")));

        assertEquals(1, materialRepository.count());
    }

    @Test
    void deveRetornar400AoAtualizarMaterialComNomeDuplicadoDeOutroMaterial() throws Exception {
        criarMaterial("Contas de madeira", "0.50", "un");
        Material m2 = criarMaterial("Fio encerado", "1.20", "m");

        String jsonConflito = """
                {
                    "nome": "CONTAS DE MADEIRA",
                    "precoUnidade": 1.50,
                    "unidadeMedida": "m"
                }
                """;

        mockMvc.perform(put("/materiais/" + m2.getIdMaterial())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonConflito))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe outro material cadastrado com o nome informado")));

        Material noBanco = materialRepository.findById(m2.getIdMaterial()).orElseThrow();
        assertEquals("Fio encerado", noBanco.getNome());
    }
}
