package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.BackendApplication;
import com.managementsystem.demo1.model.Material;
import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.repository.MaterialRepository;
import com.managementsystem.demo1.repository.ProdutoMaterialRepository;
import com.managementsystem.demo1.repository.ProdutoRepository;
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
class ProdutoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    @BeforeEach
    void setUp() {
        produtoMaterialRepository.deleteAll();
        produtoRepository.deleteAll();
        materialRepository.deleteAll();
    }

    private Produto cadastrarProduto(String codigo, String nome, TipoProduto tipo, Integer estoque, Integer prazo) {
        return produtoService.cadastrarComMateriais(
                codigo,
                nome,
                "Descrição " + nome,
                tipo,
                estoque,
                prazo,
                tipo == TipoProduto.PERSONALIZADO ? "Sob demanda" : null,
                true,
                new BigDecimal("50.00"),
                List.of()
        );
    }

    private Material criarMaterial(String nome, String preco, String unidade) {
        Material m = new Material(nome, new BigDecimal(preco), unidade);
        return materialRepository.save(m);
    }

    @Test
    void deveListarTodosOsProdutosViaGet() throws Exception {
        cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);
        cadastrarProduto("P-002", "Terço personalizado azul", TipoProduto.PERSONALIZADO, null, 7);

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")))
                .andExpect(jsonPath("$[1].nome", is("Terço personalizado azul")));
    }

    @Test
    void deveFiltrarProdutosPorNomeViaParametroGet() throws Exception {
        cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);
        cadastrarProduto("P-002", "Pulseira de ouro", TipoProduto.PRE_PRONTO, 2, null);

        mockMvc.perform(get("/produtos").param("nome", "madeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Terço de madeira")));
    }

    @Test
    void deveBuscarProdutoPorNomeViaRotaBuscar() throws Exception {
        cadastrarProduto("P-001", "Rosário vermelho", TipoProduto.PRE_PRONTO, 5, null);
        cadastrarProduto("P-002", "Rosário azul", TipoProduto.PERSONALIZADO, null, 10);

        mockMvc.perform(get("/produtos/buscar").param("nome", "vermelho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Rosário vermelho")));
    }

    @Test
    void deveBuscarProdutoPorIdComSucesso() throws Exception {
        Produto produto = cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);

        mockMvc.perform(get("/produtos/" + produto.getIdProduto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProduto", is(produto.getIdProduto().intValue())))
                .andExpect(jsonPath("$.codigo", is("P-001")))
                .andExpect(jsonPath("$.nome", is("Terço de madeira")));
    }

    @Test
    void deveRetornar404AoBuscarProdutoPorIdInexistente() throws Exception {
        mockMvc.perform(get("/produtos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoBuscarProdutoComIdEmFormatoInvalido() throws Exception {
        mockMvc.perform(get("/produtos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Identificador de produto inválido.")));
    }

    @Test
    void deveConsultarTotalEmEstoque() throws Exception {
        cadastrarProduto("P-001", "Terço de madeira", TipoProduto.PRE_PRONTO, 10, null);
        cadastrarProduto("P-002", "Pulseira de prata", TipoProduto.PRE_PRONTO, 15, null);
        cadastrarProduto("P-003", "Personalizado", TipoProduto.PERSONALIZADO, null, 5);

        mockMvc.perform(get("/produtos/total-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(25)));
    }

    @Test
    void deveCadastrarProdutoValidoViaPostRetornando201() throws Exception {
        Material m = criarMaterial("Contas de madeira", "0.50", "un");

        String json = """
                {
                    "codigo": "P-VALID-01",
                    "nome": "Terço artesanal",
                    "descricao": "Feito à mão",
                    "tipo": "PRE_PRONTO",
                    "quantidadeEstoque": 10,
                    "calcularManualmente": false,
                    "materiais": [
                        {
                            "idMaterial": %d,
                            "quantidadeUtilizada": 20
                        }
                    ]
                }
                """.formatted(m.getIdMaterial());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProduto", notNullValue()))
                .andExpect(jsonPath("$.codigo", is("P-VALID-01")))
                .andExpect(jsonPath("$.nome", is("Terço artesanal")))
                .andExpect(jsonPath("$.valor", is(10.00)));

        assertEquals(1, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComBodyNulo() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados do produto são obrigatórios.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComCodigoAusenteVazioOuApenasEspacos() throws Exception {
        String jsonSemCodigo = """
                {
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemCodigo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Código do produto é obrigatório.")));

        String jsonCodigoEspacos = """
                {
                    "codigo": "    ",
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCodigoEspacos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Código do produto é obrigatório.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComCodigoAcimaDe50Caracteres() throws Exception {
        String codigoLongo = "C".repeat(51);
        String json = """
                {
                    "codigo": "%s",
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """.formatted(codigoLongo);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Código do produto não pode ultrapassar 50 caracteres.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComNomeAusenteVazioOuApenasEspacos() throws Exception {
        String jsonSemNome = """
                {
                    "codigo": "P-100",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemNome))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do produto é obrigatório.")));

        String jsonNomeEspacos = """
                {
                    "codigo": "P-100",
                    "nome": "   ",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNomeEspacos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do produto é obrigatório.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComNomeDescricaoOuObservacoesAcimaDoLimite() throws Exception {
        String nome256 = "N".repeat(256);
        String jsonNomeLongo = """
                {
                    "codigo": "P-LIM-1",
                    "nome": "%s",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """.formatted(nome256);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNomeLongo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do produto não pode ultrapassar 255 caracteres.")));

        String desc256 = "D".repeat(256);
        String jsonDescLonga = """
                {
                    "codigo": "P-LIM-2",
                    "nome": "Produto",
                    "descricao": "%s",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """.formatted(desc256);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDescLonga))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Descrição do produto não pode ultrapassar 255 caracteres.")));

        String obs501 = "O".repeat(501);
        String jsonObsLonga = """
                {
                    "codigo": "P-LIM-3",
                    "nome": "Produto Personalizado",
                    "tipo": "PERSONALIZADO",
                    "observacoes": "%s",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """.formatted(obs501);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObsLonga))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Observações do produto não podem ultrapassar 500 caracteres.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoSemTipoOuComTipoInvalido() throws Exception {
        String jsonSemTipo = """
                {
                    "codigo": "P-101",
                    "nome": "Terço",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemTipo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Tipo do produto é obrigatório.")));

        String jsonTipoInvalido = """
                {
                    "codigo": "P-101",
                    "nome": "Terço",
                    "tipo": "TIPO_INEXISTENTE",
                    "calcularManualmente": true,
                    "valorManual": 30.00
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTipoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Formato de requisição inválido ou campos incompatíveis.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComValorManualOuMateriaisInvalidos() throws Exception {
        String jsonSemValorManual = """
                {
                    "codigo": "P-102",
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": true,
                    "valorManual": 0
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemValorManual))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Informe um valor de venda válido.")));

        String jsonSemMateriais = """
                {
                    "codigo": "P-103",
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": false,
                    "materiais": []
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemMateriais))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Informe ao menos um material usado no produto, ou marque 'Calcular manualmente'.")));

        String jsonItemMaterialSemId = """
                {
                    "codigo": "P-104",
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": false,
                    "materiais": [
                        {
                            "idMaterial": null,
                            "quantidadeUtilizada": 5
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonItemMaterialSemId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("ID do material é obrigatório.")));

        Material m = criarMaterial("Contas", "0.50", "un");
        String jsonItemQuantidadeZero = """
                {
                    "codigo": "P-105",
                    "nome": "Terço",
                    "tipo": "PRE_PRONTO",
                    "calcularManualmente": false,
                    "materiais": [
                        {
                            "idMaterial": %d,
                            "quantidadeUtilizada": 0
                        }
                    ]
                }
                """.formatted(m.getIdMaterial());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonItemQuantidadeZero))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Quantidade utilizada do material deve ser maior que zero.")));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComMultiplosErrosDeValidacao() throws Exception {
        String jsonMultiplosErros = """
                {
                    "codigo": "   ",
                    "nome": "",
                    "tipo": null,
                    "quantidadeEstoque": -10,
                    "calcularManualmente": false,
                    "materiais": []
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMultiplosErros))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", notNullValue()));

        assertEquals(0, produtoRepository.count());
    }

    @Test
    void deveExcluirProdutoExistenteRetornando204() throws Exception {
        Produto produto = cadastrarProduto("P-DEL", "Para Deletar", TipoProduto.PRE_PRONTO, 5, null);

        mockMvc.perform(delete("/produtos/" + produto.getIdProduto()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/produtos/" + produto.getIdProduto()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirProdutoInexistente() throws Exception {
        mockMvc.perform(delete("/produtos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComCodigoDuplicado() throws Exception {
        cadastrarProduto("P-DUP", "Original", TipoProduto.PRE_PRONTO, 5, null);

        String json = """
                {
                    "codigo": "P-DUP",
                    "nome": "Duplicado",
                    "descricao": "Desc",
                    "tipo": "PRE_PRONTO",
                    "quantidadeEstoque": 10,
                    "calcularManualmente": true,
                    "valorManual": 30.00,
                    "materiais": []
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe um produto com o código informado")));
    }

    @Test
    void deveRetornar404AoAtualizarProdutoInexistente() throws Exception {
        String json = """
                {
                    "codigo": "P-NOVO",
                    "nome": "Inexistente",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoAtualizarProdutoComBodyNulo() throws Exception {
        Produto p = cadastrarProduto("P-UPD-1", "Produto Original", TipoProduto.PRE_PRONTO, 5, null);

        mockMvc.perform(put("/produtos/" + p.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Dados para atualização são obrigatórios.")));
    }

    @Test
    void deveRetornar400AoAtualizarProdutoComCamposObrigatoriosAusentesOuInvalidosPreservandoDados() throws Exception {
        Produto p = cadastrarProduto("P-UPD-2", "Produto Original", TipoProduto.PRE_PRONTO, 5, null);

        String jsonCodigoEmBranco = """
                {
                    "codigo": "   ",
                    "nome": "Novo Nome",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/" + p.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCodigoEmBranco))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Código do produto é obrigatório.")));

        String jsonCodigoNulo = """
                {
                    "nome": "Novo Nome",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/" + p.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCodigoNulo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Código do produto é obrigatório.")));

        String jsonNomeEmBranco = """
                {
                    "codigo": "P-UPD-2",
                    "nome": "",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/" + p.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNomeEmBranco))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Nome do produto é obrigatório.")));

        String jsonEstoqueNegativo = """
                {
                    "codigo": "P-UPD-2",
                    "nome": "Novo Nome",
                    "descricao": "Desc",
                    "quantidadeEstoque": -3
                }
                """;

        mockMvc.perform(put("/produtos/" + p.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstoqueNegativo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("A quantidade em estoque não pode ser negativa.")));

        Produto noBanco = produtoRepository.findById(p.getIdProduto()).orElseThrow();
        assertEquals("P-UPD-2", noBanco.getCodigo());
        assertEquals("Produto Original", noBanco.getNome());
        assertEquals(5, noBanco.getQuantidadeEstoque());
    }

    @Test
    void deveRetornar400AoAtualizarProdutoComCodigoDuplicado() throws Exception {
        cadastrarProduto("P-01", "Produto 1", TipoProduto.PRE_PRONTO, 5, null);
        Produto p2 = cadastrarProduto("P-02", "Produto 2", TipoProduto.PRE_PRONTO, 5, null);

        String json = """
                {
                    "codigo": "P-01",
                    "nome": "Produto 2 com codigo do 1",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/" + p2.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe outro produto cadastrado")));
    }

    @Test
    void deveRetornar400AoCadastrarProdutoComNomeDuplicado() throws Exception {
        cadastrarProduto("P-NOME-01", "Terço de Madeira Especial", TipoProduto.PRE_PRONTO, 5, null);

        String json = """
                {
                    "codigo": "P-NOME-02",
                    "nome": "  terço de madeira especial  ",
                    "descricao": "Desc",
                    "tipo": "PRE_PRONTO",
                    "quantidadeEstoque": 10,
                    "calcularManualmente": true,
                    "valorManual": 30.00,
                    "materiais": []
                }
                """;

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe um produto cadastrado com o nome informado")));

        assertEquals(1, produtoRepository.count());
    }

    @Test
    void deveRetornar400AoAtualizarProdutoComNomeDuplicadoDeOutroProduto() throws Exception {
        cadastrarProduto("P-N-01", "Terço A", TipoProduto.PRE_PRONTO, 5, null);
        Produto p2 = cadastrarProduto("P-N-02", "Terço B", TipoProduto.PRE_PRONTO, 5, null);

        String json = """
                {
                    "codigo": "P-N-02",
                    "nome": "TERÇO A",
                    "descricao": "Desc",
                    "quantidadeEstoque": 10
                }
                """;

        mockMvc.perform(put("/produtos/" + p2.getIdProduto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Já existe outro produto cadastrado com o nome informado")));
    }
}
