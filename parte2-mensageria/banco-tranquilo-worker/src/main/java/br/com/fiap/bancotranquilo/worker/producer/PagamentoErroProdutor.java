package br.com.fiap.bancotranquilo.worker.producer;

import br.com.fiap.bancotranquilo.worker.config.MqProperties;
import br.com.fiap.bancotranquilo.worker.messaging.PagamentoResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica na pagamento-response-erro-exchange com a routing key de erro. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoErroProdutor {

    private final RabbitTemplate rabbitTemplate;
    private final MqProperties mq;

    public void enviar(PagamentoResponseMessage resposta) {
        rabbitTemplate.convertAndSend(mq.exchanges().erro(), mq.routingKeys().erro(), resposta);
        log.info("==== RESPOSTA ERRO enviada ==== pedido {} | motivo: {}",
                resposta.numeroPedido(), resposta.mensagem());
    }
}
