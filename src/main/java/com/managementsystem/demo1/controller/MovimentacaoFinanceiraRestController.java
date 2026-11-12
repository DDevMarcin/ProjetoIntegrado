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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping({"/movimentacoes-financeiras", "/financeiro/movimentacoes", "/financeiro/movimentacoes-financeiras"})
public class MovimentacaoFinanceiraRestController {

    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    public MovimentacaoFinanceiraRestController(MovimentacaoFinanceiraService movimentacaoFinanceiraService) {
        this.movimentacaoFinanceiraService = movimentacaoFinanceiraService;
    }

    // GET /movimentacoes-financeiras
    // Suporta listagem completa (receitas e despesas) ou filtros opcionais por período (dataInicio/dataFim),
    // tipo, situacao, idCategoria, categoria e descricao
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(value = "tipo", required = false) TipoMovimentacaoFinanceira tipo,
            @RequestParam(value = "situacao", required = false) SituacaoMovimentacaoFinanceira situacao,
            @RequestParam(value = "idCategoria", required = false) Long idCategoria,
            @RequestParam(value = "idCategoriaDespesa", required = false) Long idCategoriaDespesa,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataInicial", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "dataFinal", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(value = "descricao", required = false) String descricao
    ) {
        return executarConsulta(
                tipo,
                situacao,
                idCategoria,
                idCategoriaDespesa,
                categoria,
                dataInicio,
                dataInicial,
                dataFim,
                dataFinal,
                descricao
        );
    }

    // GET /movimentacoes-financeiras/periodo?dataInicio=AAAA-MM-DD&dataFim=AAAA-MM-DD
    @GetMapping("/periodo")
    public ResponseEntity<?> listarPorPeriodo(
            @RequestParam(value = "tipo", required = false) TipoMovimentacaoFinanceira tipo,
            @RequestParam(value = "situacao", required = false) SituacaoMovimentacaoFinanceira situacao,
            @RequestParam(value = "idCategoria", required = false) Long idCategoria,
            @RequestParam(value = "idCategoriaDespesa", required = false) Long idCategoriaDespesa,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataInicial", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "dataFinal", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(value = "descricao", required = false) String descricao
    ) {
        return executarConsulta(
                tipo,
                situacao,
                idCategoria,
                idCategoriaDespesa,
                categoria,
                dataInicio,
                dataInicial,
                dataFim,
                dataFinal,
                descricao
        );
    }

    private ResponseEntity<?> executarConsulta(
            TipoMovimentacaoFinanceira tipo,
            SituacaoMovimentacaoFinanceira situacao,
            Long idCategoria,
            Long idCategoriaDespesa,
            String categoria,
            LocalDate dataInicio,
            LocalDate dataInicial,
            LocalDate dataFim,
            LocalDate dataFinal,
            String descricao
    ) {
        if (idCategoria != null && idCategoriaDespesa != null && !idCategoria.equals(idCategoriaDespesa)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Identificadores de categoria informados divergem."));
        }
        if (dataInicio != null && dataInicial != null && !dataInicio.equals(dataInicial)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Data inicial informada em parâmetros duplicados diverge."));
        }
        if (dataFim != null && dataFinal != null && !dataFim.equals(dataFinal)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Data final informada em parâmetros duplicados diverge."));
        }

        Long idCategoriaEfetivo = idCategoria != null ? idCategoria : idCategoriaDespesa;
        LocalDate dataInicioEfetiva = dataInicio != null ? dataInicio : dataInicial;
        LocalDate dataFimEfetiva = dataFim != null ? dataFim : dataFinal;

        try {
            boolean possuiFiltro = tipo != null
                    || situacao != null
                    || idCategoriaEfetivo != null
                    || (categoria != null && !categoria.isBlank())
                    || dataInicioEfetiva != null
                    || dataFimEfetiva != null
                    || (descricao != null && !descricao.isBlank());

            if (possuiFiltro) {
                return ResponseEntity.ok(
                        movimentacaoFinanceiraService.consultar(
                                tipo,
                                situacao,
                                idCategoriaEfetivo,
                                categoria,
                                dataInicioEfetiva,
                                dataFimEfetiva,
                                descricao
                        )
                );
            }

            return ResponseEntity.ok(movimentacaoFinanceiraService.listarMovimentacoes());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
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

    // GET /movimentacoes-financeiras/total-receitas?dataInicio=AAAA-MM-DD&dataFim=AAAA-MM-DD
    @GetMapping({"/total-receitas", "/receitas-total"})
    public ResponseEntity<?> calcularTotalReceitasPorPeriodo(
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataInicial", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "dataFinal", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(value = "situacao", required = false) SituacaoMovimentacaoFinanceira situacao
    ) {
        if (dataInicio != null && dataInicial != null && !dataInicio.equals(dataInicial)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Data inicial informada em parâmetros duplicados diverge."));
        }
        if (dataFim != null && dataFinal != null && !dataFim.equals(dataFinal)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Data final informada em parâmetros duplicados diverge."));
        }

        LocalDate dataInicioEfetiva = dataInicio != null ? dataInicio : dataInicial;
        LocalDate dataFimEfetiva = dataFim != null ? dataFim : dataFinal;

        try {
            BigDecimal total = movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                    dataInicioEfetiva,
                    dataFimEfetiva,
                    situacao
            );

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("total", total);
            resposta.put("totalReceitas", total);
            resposta.put("tipo", TipoMovimentacaoFinanceira.RECEITA);
            resposta.put("dataInicio", dataInicioEfetiva);
            resposta.put("dataFim", dataFimEfetiva);
            if (situacao != null) {
                resposta.put("situacao", situacao);
            }

            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /movimentacoes-financeiras/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return movimentacaoFinanceiraService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/fluxo-de-caixa")
    public ResponseEntity<?> consultarFluxoDeCaixa(
            @RequestParam("dataInicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicio,

            @RequestParam("dataFim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFim
    ) {
        try {
            BigDecimal entradas =
                    movimentacaoFinanceiraService.calcularTotalReceitasPorPeriodo(
                            dataInicio,
                            dataFim,
                            SituacaoMovimentacaoFinanceira.PAGO
                    );

            BigDecimal saidas =
                    movimentacaoFinanceiraService.calcularTotalDespesasPorPeriodo(
                            dataInicio,
                            dataFim,
                            SituacaoMovimentacaoFinanceira.PAGO
                    );

            BigDecimal saldoTotal =
                    movimentacaoFinanceiraService.calcularSaldoTotal();

            BigDecimal saldoLiquido =
                    movimentacaoFinanceiraService.calcularSaldoLiquidoPorPeriodo(
                            dataInicio,
                            dataFim
                    );

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("saldoTotal", saldoTotal);
            resposta.put("entradas", entradas);
            resposta.put("saidas", saidas);
            resposta.put("receitasPeriodo", entradas);
            resposta.put("despesasPeriodo", saidas);
            resposta.put("saldoLiquido", saldoLiquido);

            return ResponseEntity.ok(resposta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarParametroInvalido(MethodArgumentTypeMismatchException e) {
        String parametro = e.getName();
        if ("id".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Identificador de movimentação financeira inválido."));
        }
        if ("idCategoria".equals(parametro) || "idCategoriaDespesa".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Identificador de categoria de despesa inválido."));
        }
        if ("tipo".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Tipo de movimentação financeira inválido. Valores aceitos: RECEITA, DESPESA."));
        }
        if ("situacao".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Situação de movimentação financeira inválida. Valores aceitos: PENDENTE, PAGO, CANCELADO."));
        }
        if ("dataInicio".equals(parametro)
                || "dataFim".equals(parametro)
                || "dataInicial".equals(parametro)
                || "dataFinal".equals(parametro)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Formato de data inválido. Utilize o padrão AAAA-MM-DD."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Parâmetro de consulta inválido."));
    }

    // GET /movimentacoes-financeiras/contas-a-receber
    @GetMapping("/contas-a-receber")
    public ResponseEntity<?> listarContasAReceber(
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,

            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        if ((dataInicio == null) != (dataFim == null)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Informe as datas inicial e final para consultar por período."));
        }

        try {
            List<MovimentacaoFinanceira> contas;

            if (dataInicio != null) {
                contas = movimentacaoFinanceiraService
                        .listarContasAReceberPorPeriodo(dataInicio, dataFim);
            } else {
                contas = movimentacaoFinanceiraService.listarContasAReceber();
            }

            return ResponseEntity.ok(contas);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}
