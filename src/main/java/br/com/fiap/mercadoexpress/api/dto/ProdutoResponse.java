package br.com.fiap.mercadoexpress.api.dto;

import br.com.fiap.mercadoexpress.api.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(Long id, String nome, String categoria, BigDecimal preco,
                              String unidade, Integer estoque) {
    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getCategoria().getDescricao(),
                p.getPrecoVenda(), p.getUnidade(), p.getQuantidadeEstoque());
    }
}
