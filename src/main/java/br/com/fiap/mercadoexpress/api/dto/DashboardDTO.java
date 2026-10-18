package br.com.fiap.mercadoexpress.api.dto;

import br.com.fiap.mercadoexpress.api.model.EmailEnviado;
import br.com.fiap.mercadoexpress.api.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Dados do dashboard da gerencia (indicadores + series dos graficos). */
@Getter
@AllArgsConstructor
public class DashboardDTO {
    private final BigDecimal faturamento;
    private final BigDecimal custo;
    private final BigDecimal lucro;
    private final BigDecimal ticketMedio;
    private final long totalPedidos;
    private final long aprovados;
    private final long recusados;
    private final long aguardando;
    private final long taxaAprovacao;
    private final long emailsEnviados;
    private final long emailsComFalha;
    private final long produtosEstoqueBaixo;
    private final Map<String, Object> graficos;
    private final List<Pedido> ultimosPedidos;
    private final List<EmailEnviado> ultimosEmails;
}
