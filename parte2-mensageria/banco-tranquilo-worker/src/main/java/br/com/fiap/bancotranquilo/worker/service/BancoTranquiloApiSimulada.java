package br.com.fiap.bancotranquilo.worker.service;

import br.com.fiap.bancotranquilo.worker.config.BancoTranquiloProperties;
import br.com.fiap.bancotranquilo.worker.messaging.PagamentoRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simula a resposta do Banco Tranquilo (como no exemplo da aula, com Random),
 * mas com regras previsiveis para facilitar a demonstracao no video:
 *
 *  1. valor invalido (nulo ou <= 0)            -> RECUSADO
 *  2. e-mail contendo "recusa"                 -> RECUSADO (cenario de teste forcado)
 *  3. e-mail contendo "aprova"                 -> APROVADO (cenario de teste forcado)
 *  4. valor acima do limite por compra          -> RECUSADO (limite excedido)
 *  5. demais casos                              -> aleatorio com taxa de aprovacao configuravel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BancoTranquiloApiSimulada implements BancoTranquiloApi {

    private final BancoTranquiloProperties props;

    @Override
    public ResultadoTransacao autorizar(PagamentoRequestMessage pagamento) {
        simularLatenciaDaRede();

        String email = pagamento.clienteEmail() == null ? "" : pagamento.clienteEmail().toLowerCase(Locale.ROOT);
        BigDecimal valor = pagamento.valor();

        if (valor == null || valor.signum() <= 0) {
            return ResultadoTransacao.recusado("Valor da transação inválido");
        }
        if (email.contains("recusa")) {
            return ResultadoTransacao.recusado("Cartão bloqueado pelo emissor (cenário de teste)");
        }
        if (email.contains("aprova")) {
            return ResultadoTransacao.aprovado(gerarCodigoAutorizacao());
        }
        if (valor.compareTo(props.limitePorCompra()) > 0) {
            return ResultadoTransacao.recusado("Limite por compra excedido (máximo R$ "
                    + props.limitePorCompra().setScale(2) + ")");
        }
        boolean aprovado = ThreadLocalRandom.current().nextDouble() < props.taxaAprovacao();
        return aprovado
                ? ResultadoTransacao.aprovado(gerarCodigoAutorizacao())
                : ResultadoTransacao.recusado("Saldo insuficiente na conta do cliente");
    }

    private String gerarCodigoAutorizacao() {
        return "BT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    private void simularLatenciaDaRede() {
        long min = Math.max(0, props.latenciaMinMs());
        long max = Math.max(min + 1, props.latenciaMaxMs());
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(min, max));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
