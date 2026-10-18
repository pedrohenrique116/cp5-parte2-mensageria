package br.com.fiap.bancotranquilo.worker.consumer;

import br.com.fiap.bancotranquilo.worker.messaging.PagamentoRequestMessage;
import br.com.fiap.bancotranquilo.worker.messaging.PagamentoResponseMessage;
import br.com.fiap.bancotranquilo.worker.producer.PagamentoErroProdutor;
import br.com.fiap.bancotranquilo.worker.producer.PagamentoSucessoProdutor;
import br.com.fiap.bancotranquilo.worker.service.BancoTranquiloApi;
import br.com.fiap.bancotranquilo.worker.service.ResultadoTransacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consome os pedidos de pagamento da fila pagamento-request-queue,
 * consulta a API do Banco Tranquilo e devolve a resposta pela fila de sucesso ou de erro.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoRequestConsumidor {

    private final BancoTranquiloApi bancoTranquiloApi;
    private final PagamentoSucessoProdutor sucessoProdutor;
    private final PagamentoErroProdutor erroProdutor;

    @RabbitListener(queues = "${mq.queues.request}")
    public void receberMensagem(@Payload PagamentoRequestMessage pagamento) {
        log.info("==== PEDIDO DE PAGAMENTO RECEBIDO ==== pedido {} | cliente {} | valor R$ {}",
                pagamento.numeroPedido(), pagamento.clienteEmail(), pagamento.valor());

        ResultadoTransacao resultado = bancoTranquiloApi.autorizar(pagamento);

        PagamentoResponseMessage resposta = new PagamentoResponseMessage(
                pagamento.pedidoId(),
                pagamento.numeroPedido(),
                resultado.aprovado() ? "APROVADO" : "RECUSADO",
                resultado.codigoAutorizacao(),
                resultado.mensagem(),
                pagamento.valor(),
                LocalDateTime.now()
        );

        if (resultado.aprovado()) {
            sucessoProdutor.enviar(resposta);
        } else {
            erroProdutor.enviar(resposta);
        }
    }
}
