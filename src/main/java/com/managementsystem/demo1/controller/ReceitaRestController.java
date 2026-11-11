package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.MovimentacaoFinanceira;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import com.managementsystem.demo1.model.TipoMovimentacaoFinanceira;
import com.managementsystem.demo1.service.MovimentacaoFinanceiraService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/receitas", "/financeiro/receitas", "/movimentacoes-financeiras/receitas"})
public class ReceitaRestController {

    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    public ReceitaRestController(MovimentacaoFinanceiraService movimentacaoFinanceiraService) {
        this.movimentacaoFinanceiraService = movimentacaoFinanceiraService;
    }

    public record NovaReceitaDTO(
            String descricao,
            BigDecimal valor,
            String categoria,
            SituacaoMovimentacaoFinanceira situacao,
            LocalDate dataMovimentacao,
            LocalDate dataRecebimento,
            LocalDate dataVencimento,
            String observacoes,
            TipoMovimentacaoFinanceira tipo
    ) {
        public NovaReceitaDTO(
                String descricao,
                BigDecimal valor,
                String categoria,
                SituacaoMovimentacaoFinanceira situacao,
                LocalDate dataMovimentacao,
                LocalDate dataVencimento,
                String observacoes
        ) {
            this(descricao, valor, categoria, situacao, dataMovimentacao, null, dataVencimento, observacoes, null);
        }
    }

    // POST /receitas (também responde em /financeiro/receitas e /movimentacoes-financeiras/receitas)
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody(required = false) NovaReceitaDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Dados da receita são obrigatórios."));
        }

        try {
            if (dto.dataMovimentacao() != null
                    && dto.dataRecebimento() != null
                    && !dto.dataMovimentacao().equals(dto.dataRecebimento())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Data da movimentação e data de recebimento informadas divergem."));
            }

            LocalDate dataEfetiva = dto.dataMovimentacao() != null
                    ? dto.dataMovimentacao()
                    : dto.dataRecebimento();

            MovimentacaoFinanceira receita = new MovimentacaoFinanceira();
            receita.setDescricao(dto.descricao());
            receita.setValor(dto.valor());
            receita.setCategoria(dto.categoria());
            receita.setSituacao(dto.situacao());
            receita.setDataMovimentacao(dataEfetiva);
            receita.setDataVencimento(dto.dataVencimento());
            receita.setObservacoes(dto.observacoes());
            if (dto.tipo() != null) {
                receita.setTipo(dto.tipo());
            }

            MovimentacaoFinanceira salva = movimentacaoFinanceiraService.cadastrarReceita(receita);

            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    public List<MovimentacaoFinanceira> listar() {
        return movimentacaoFinanceiraService.listarReceitas();
    }

    // GET /receitas (suporta filtro opcional por período: dataInicio e dataFim)
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        try {
            if (dataInicio != null || dataFim != null) {
                return ResponseEntity.ok(movimentacaoFinanceiraService.listarReceitasPorPeriodo(dataInicio, dataFim));
            }
            return ResponseEntity.ok(movimentacaoFinanceiraService.listarReceitas());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /receitas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return movimentacaoFinanceiraService.buscarReceitaPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarIdInvalido(MethodArgumentTypeMismatchException e) {
        if ("dataInicio".equals(e.getName()) || "dataFim".equals(e.getName())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Formato de data inválido. Utilize o padrão AAAA-MM-DD."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Identificador de receita inválido."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Formato de requisição inválido ou campos incompatíveis."));
    }
}

