package br.com.fiap.mercadoexpress.api.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mensagem publicada na pagamento-request-exchange para o Worker do Banco Tranquilo. */
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
