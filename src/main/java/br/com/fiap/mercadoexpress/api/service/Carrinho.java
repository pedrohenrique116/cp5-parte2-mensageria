package br.com.fiap.mercadoexpress.api.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Carrinho de compras guardado na sessao HTTP de cada visitante (produtoId -> quantidade). */
@Component
@SessionScope
public class Carrinho implements Serializable {

    private final Map<Long, Integer> itens = new LinkedHashMap<>();

    public void adicionar(Long produtoId, int quantidade) {
        if (quantidade > 0) {
            itens.merge(produtoId, quantidade, Integer::sum);
        }
    }

    public void atualizar(Long produtoId, int quantidade) {
        if (quantidade <= 0) {
            itens.remove(produtoId);
        } else {
            itens.put(produtoId, quantidade);
        }
    }

    public void remover(Long produtoId) {
        itens.remove(produtoId);
    }

    public void limpar() {
        itens.clear();
    }

    public Map<Long, Integer> getItens() {
        return Collections.unmodifiableMap(itens);
    }

    public int getQuantidadeTotal() {
        return itens.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }
}
