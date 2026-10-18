package br.com.fiap.mercadoexpress.api.service;

import br.com.fiap.mercadoexpress.api.dto.DashboardDTO;
import br.com.fiap.mercadoexpress.api.model.*;
import br.com.fiap.mercadoexpress.api.repository.EmailEnviadoRepository;
import br.com.fiap.mercadoexpress.api.repository.PedidoRepository;
import br.com.fiap.mercadoexpress.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** Calcula os indicadores e os dados dos graficos do dashboard da gerencia. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final EmailEnviadoRepository emailRepository;

    @Transactional(readOnly = true)
    public DashboardDTO gerar() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByCriadoEmDesc();
        List<Pedido> aprovados = pedidos.stream().filter(p -> p.getStatus() == StatusPedido.APROVADO).toList();
        long recusados = pedidos.stream().filter(p -> p.getStatus() == StatusPedido.RECUSADO).count();
        long aguardando = pedidos.stream().filter(Pedido::isAguardando).count();

        BigDecimal faturamento = soma(aprovados.stream().map(Pedido::getValorTotal).toList());
        BigDecimal custo = soma(aprovados.stream().map(Pedido::getCustoTotal).toList());
        BigDecimal lucro = faturamento.subtract(custo);
        BigDecimal ticketMedio = aprovados.isEmpty() ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(aprovados.size()), 2, RoundingMode.HALF_UP);
        long finalizados = aprovados.size() + recusados;
        long taxaAprovacao = finalizados == 0 ? 0 : Math.round(aprovados.size() * 100.0 / finalizados);

        List<Produto> produtos = produtoRepository.findAllByOrderByQuantidadeEstoqueAsc();
        long estoqueBaixo = produtos.stream().filter(Produto::isEstoqueBaixo).count();

        Map<String, Object> graficos = new LinkedHashMap<>();
        graficos.put("vendasPorDia", vendasUltimos7Dias(aprovados));
        graficos.put("status", Map.of(
                "labels", List.of("Aprovados", "Recusados", "Aguardando"),
                "valores", List.<Long>of((long) aprovados.size(), recusados, aguardando)));
        graficos.put("topProdutos", topProdutos(aprovados));
        graficos.put("categorias", vendasPorCategoria(aprovados));
        graficos.put("estoque", estoque(produtos));

        return new DashboardDTO(faturamento, custo, lucro, ticketMedio,
                pedidos.size(), aprovados.size(), recusados, aguardando, taxaAprovacao,
                emailRepository.countByStatus(StatusEmail.ENVIADO),
                emailRepository.countByStatus(StatusEmail.FALHA),
                estoqueBaixo, graficos,
                pedidos.stream().limit(10).toList(),
                emailRepository.findTop10ByOrderByEnviadoEmDesc());
    }

    private Map<String, Object> vendasUltimos7Dias(List<Pedido> aprovados) {
        LocalDate hoje = LocalDate.now();
        Map<LocalDate, BigDecimal> receita = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> lucro = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            receita.put(hoje.minusDays(i), BigDecimal.ZERO);
            lucro.put(hoje.minusDays(i), BigDecimal.ZERO);
        }
        for (Pedido p : aprovados) {
            LocalDate dia = p.getCriadoEm().toLocalDate();
            if (receita.containsKey(dia)) {
                receita.merge(dia, p.getValorTotal(), BigDecimal::add);
                lucro.merge(dia, p.getLucro(), BigDecimal::add);
            }
        }
        return Map.of(
                "labels", receita.keySet().stream().map(DIA::format).toList(),
                "receita", new ArrayList<>(receita.values()),
                "lucro", new ArrayList<>(lucro.values()));
    }

    private Map<String, Object> topProdutos(List<Pedido> aprovados) {
        Map<String, Integer> quantidades = new HashMap<>();
        for (Pedido p : aprovados) {
            for (ItemPedido i : p.getItens()) {
                quantidades.merge(i.getNomeProduto(), i.getQuantidade(), Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> top = quantidades.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(8)
                .toList();
        return Map.of(
                "labels", top.stream().map(Map.Entry::getKey).toList(),
                "valores", top.stream().map(Map.Entry::getValue).toList());
    }

    private Map<String, Object> vendasPorCategoria(List<Pedido> aprovados) {
        Map<String, BigDecimal> porCategoria = aprovados.stream()
                .flatMap(p -> p.getItens().stream())
                .collect(Collectors.groupingBy(i -> i.getCategoria().getDescricao(), TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, ItemPedido::getSubtotal, BigDecimal::add)));
        return Map.of(
                "labels", new ArrayList<>(porCategoria.keySet()),
                "valores", new ArrayList<>(porCategoria.values()));
    }

    private Map<String, Object> estoque(List<Produto> produtos) {
        List<Produto> lista = produtos.stream().filter(p -> Boolean.TRUE.equals(p.getAtivo())).limit(15).toList();
        return Map.of(
                "labels", lista.stream().map(Produto::getNome).toList(),
                "valores", lista.stream().map(Produto::getQuantidadeEstoque).toList(),
                "baixo", lista.stream().map(Produto::isEstoqueBaixo).toList());
    }

    private BigDecimal soma(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
