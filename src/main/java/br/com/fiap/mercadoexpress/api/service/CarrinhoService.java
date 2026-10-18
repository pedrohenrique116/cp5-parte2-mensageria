package br.com.fiap.mercadoexpress.api.service;

import br.com.fiap.mercadoexpress.api.dto.CompraRequest;
import br.com.fiap.mercadoexpress.api.dto.ItemCompraRequest;
import br.com.fiap.mercadoexpress.api.model.Produto;
import br.com.fiap.mercadoexpress.api.repository.ProdutoRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final Carrinho carrinho;
    private final ProdutoRepository produtoRepository;

    @Getter
    @AllArgsConstructor
    public static class Linha {
        private final Produto produto;
        private final int quantidade;
        private final BigDecimal subtotal;
    }

    @Getter
    @AllArgsConstructor
    public static class Resumo {
        private final List<Linha> linhas;
        private final BigDecimal total;
        private final int quantidadeItens;

        public boolean isVazio() {
            return linhas.isEmpty();
        }
    }

    @Transactional(readOnly = true)
    public Resumo resumo() {
        List<Linha> linhas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int quantidade = 0;
        for (Map.Entry<Long, Integer> e : carrinho.getItens().entrySet()) {
            Produto p = produtoRepository.findById(e.getKey()).orElse(null);
            if (p == null) {
                continue;
            }
            BigDecimal subtotal = p.getPrecoVenda().multiply(BigDecimal.valueOf(e.getValue()));
            linhas.add(new Linha(p, e.getValue(), subtotal));
            total = total.add(subtotal);
            quantidade += e.getValue();
        }
        return new Resumo(linhas, total, quantidade);
    }

    public CompraRequest paraCompra(String clienteNome, String clienteEmail) {
        List<ItemCompraRequest> itens = carrinho.getItens().entrySet().stream()
                .map(e -> new ItemCompraRequest(e.getKey(), e.getValue()))
                .toList();
        return new CompraRequest(clienteNome, clienteEmail, itens);
    }
}
