package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/movimentacoes-financeiras", "/financeiro/movimentacoes", "/financeiro/movimentacoes-financeiras"})
public class MovimentacaoFinanceiraRestController {

    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    public MovimentacaoFinanceiraRestController(MovimentacaoFinanceiraService movimentacaoFinanceiraService) {
        this.movimentacaoFinanceiraService = movimentacaoFinanceiraService;
    }

    // GET /movimentacoes-financeiras
    // Suporta listagem completa (receitas e despesas) ou filtros opcionais por tipo, situacao, categoria, período e descricao
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(value = "tipo", required = false) TipoMovimentacaoFinanceira tipo,
            @RequestParam(value = "situacao", required = false) SituacaoMovimentacaoFinanceira situacao,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "descricao", required = false) String descricao
    ) {
        try {
            boolean possuiFiltro = tipo != null
                    || situacao != null
                    || (categoria != null && !categoria.isBlank())
                    || dataInicio != null
                    || dataFim != null
                    || (descricao != null && !descricao.isBlank());

            if (possuiFiltro) {
                return ResponseEntity.ok(
                        movimentacaoFinanceiraService.consultar(
                                tipo,
                                situacao,
                                categoria,
                                dataInicio,
                                dataFim,
                                descricao
                        )
                );
            }

            return ResponseEntity.ok(movimentacaoFinanceiraService.listarMovimentacoes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /movimentacoes-financeiras/buscar?descricao=xxx
    @GetMapping("/buscar")
    public List<MovimentacaoFinanceira> buscarPorDescricao(
            @RequestParam(value = "descricao", required = false) String descricao
    ) {
        return movimentacaoFinanceiraService.buscarPorDescricao(descricao);
    }

    // GET /movimentacoes-financeiras/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return movimentacaoFinanceiraService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarParametroInvalido(MethodArgumentTypeMismatchException e) {
        String parametro = e.getName();
        if ("id".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Identificador de movimentação financeira inválido."));
        }
        if ("tipo".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Tipo de movimentação financeira inválido. Valores aceitos: RECEITA, DESPESA."));
        }
        if ("situacao".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Situação de movimentação financeira inválida. Valores aceitos: PENDENTE, PAGO, CANCELADO."));
        }
        if ("dataInicio".equals(parametro) || "dataFim".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Formato de data inválido. Utilize o padrão AAAA-MM-DD."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Parâmetro de consulta inválido."));
    }
}

