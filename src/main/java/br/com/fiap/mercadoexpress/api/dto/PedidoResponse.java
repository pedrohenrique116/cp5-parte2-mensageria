package br.com.fiap.mercadoexpress.api.dto;

import br.com.fiap.mercadoexpress.api.model.ItemPedido;
import br.com.fiap.mercadoexpress.api.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String numeroPedido,
        String clienteNome,
        String clienteEmail,
        String status,
        String statusDescricao,
        BigDecimal valorTotal,
        String codigoAutorizacao,
        String mensagemBanco,
        LocalDateTime criadoEm,
        LocalDateTime processadoEm,
        List<Item> itens
) {
    public record Item(Long produtoId, String nome, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        static Item de(ItemPedido i) {
            return new Item(i.getProdutoId(), i.getNomeProduto(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(p.getId(), p.getNumeroPedido(), p.getClienteNome(), p.getClienteEmail(),
                p.getStatus().name(), p.getStatus().getDescricao(), p.getValorTotal(), p.getCodigoAutorizacao(),
                p.getMensagemBanco(), p.getCriadoEm(), p.getProcessadoEm(),
                p.getItens().stream().map(Item::de).toList());
    }
}
