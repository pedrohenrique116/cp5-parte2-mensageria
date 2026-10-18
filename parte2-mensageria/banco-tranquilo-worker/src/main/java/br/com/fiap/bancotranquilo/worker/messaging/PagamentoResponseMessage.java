package br.com.fiap.bancotranquilo.worker.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resposta publicada nas exchanges de sucesso ou de erro. */
public record PagamentoResponseMessage(
        Long pedidoId,
        String numeroPedido,
        String status,              // APROVADO | RECUSADO
        String codigoAutorizacao,   // preenchido quando aprovado
        String mensagem,            // motivo / descricao do banco
        BigDecimal valor,
        LocalDateTime processadoEm
) {
}
