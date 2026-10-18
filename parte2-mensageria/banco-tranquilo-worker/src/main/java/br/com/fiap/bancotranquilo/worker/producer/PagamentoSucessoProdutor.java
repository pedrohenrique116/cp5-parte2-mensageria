package br.com.fiap.bancotranquilo.worker.producer;

import br.com.fiap.bancotranquilo.worker.config.MqProperties;
import br.com.fiap.bancotranquilo.worker.messaging.PagamentoResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica na pagamento-response-sucesso-exchange com a routing key de sucesso. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoSucessoProdutor {

    private final RabbitTemplate rabbitTemplate;
    private final MqProperties mq;

    public void enviar(PagamentoResponseMessage resposta) {
        rabbitTemplate.convertAndSend(mq.exchanges().sucesso(), mq.routingKeys().sucesso(), resposta);
        log.info("==== RESPOSTA SUCESSO enviada ==== pedido {} | autorizacao {}",
                resposta.numeroPedido(), resposta.codigoAutorizacao());
    }
}
