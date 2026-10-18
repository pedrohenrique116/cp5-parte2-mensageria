package br.com.fiap.bancotranquilo.worker.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mensagem recebida da fila pagamento-request-queue (enviada pelo Mercado Express). */
public record PagamentoRequestMessage(
        Long pedidoId,
        String numeroPedido,
        String clienteNome,
        String clienteEmail,
        BigDecimal valor,
        Integer quantidadeItens,
        LocalDateTime solicitadoEm
) {
}
