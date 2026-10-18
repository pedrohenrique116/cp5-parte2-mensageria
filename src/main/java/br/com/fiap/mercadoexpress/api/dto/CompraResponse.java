package br.com.fiap.mercadoexpress.api.dto;

import br.com.fiap.mercadoexpress.api.model.Pedido;

import java.math.BigDecimal;

public record CompraResponse(
        Long pedidoId,
        String numeroPedido,
        String status,
        BigDecimal valorTotal,
        String mensagem,
        String acompanharEm
) {
    public static CompraResponse de(Pedido pedido) {
        return new CompraResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                "Pagamento aguardando confirmação do Banco Tranquilo...",
                "/api/compras/" + pedido.getId()
        );
    }
}
