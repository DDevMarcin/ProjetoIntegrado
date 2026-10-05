package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.Produto;
import com.managementsystem.demo1.model.TipoProduto;
import com.managementsystem.demo1.service.ItemMaterialInput;
import com.managementsystem.demo1.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/produtos")
public class ProdutoRestController {

    private final ProdutoService produtoService;

    public ProdutoRestController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public record MaterialUtilizadoDTO(Long idMaterial, BigDecimal quantidadeUtilizada) {}

    public record NovoProdutoDTO(
            String codigo,
            String nome,
            String descricao,
            TipoProduto tipo,
            Integer quantidadeEstoque,      // usado se tipo = PRE_PRONTO
            Integer prazoProducaoDias,      // usado se tipo = PERSONALIZADO
            String observacoes,             // usado se tipo = PERSONALIZADO
            boolean calcularManualmente,    // true = usa valorManual; false = calcula pelos materiais
            BigDecimal valorManual,         // obrigatório se calcularManualmente = true
            List<MaterialUtilizadoDTO> materiais  // obrigatório (não vazio) se calcularManualmente = false
    ) {}

    public record AtualizarProdutoDTO(
            String codigo,
            String nome,
            String descricao,
            Integer quantidadeEstoque,
            Integer prazoProducaoDias,
            String observacoes
    ) {}

    // POST /produtos
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody NovoProdutoDTO dto) {
        try {
            List<ItemMaterialInput> itens = dto.materiais() == null ? List.of() :
                    dto.materiais().stream()
                            .map(m -> new ItemMaterialInput(m.idMaterial(), m.quantidadeUtilizada()))
                            .toList();

            Produto produto = produtoService.cadastrarComMateriais(
                    dto.codigo(), dto.nome(), dto.descricao(), dto.tipo(),
                    dto.quantidadeEstoque(), dto.prazoProducaoDias(), dto.observacoes(),
                    dto.calcularManualmente(), dto.valorManual(), itens);

            return ResponseEntity.status(HttpStatus.CREATED).body(produto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /produtos
    @GetMapping
    public List<Produto> listar() {
        return produtoService.listarTodos();
    }

    // GET /produtos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(produtoService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /produtos/total-estoque  (soma de quantidadeEstoque de todos os produtos)
    @GetMapping("/total-estoque")
    public Map<String, Integer> totalEmEstoque() {
        return Map.of("total", produtoService.totalEmEstoque());
    }

    // GET /produtos/buscar?nome=xxx
    @GetMapping("/buscar")
    public List<Produto> buscarPorNome(@RequestParam String nome) {
        return produtoService.buscarPorNome(nome);
    }

    // PUT /produtos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody AtualizarProdutoDTO dto) {
        try {
            Produto produto = produtoService.atualizar(id, dto.codigo(), dto.nome(), dto.descricao(),
                    dto.quantidadeEstoque(), dto.prazoProducaoDias(), dto.observacoes());
            return ResponseEntity.ok(produto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /produtos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}