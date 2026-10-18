package br.com.fiap.mercadoexpress.api.messaging;

import br.com.fiap.mercadoexpress.api.config.MqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Para produzir uma mensagem informamos a Exchange e a Routing Key. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoRequestProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MqProperties mq;

    public void integrar(PagamentoRequestMessage mensagem) {
        rabbitTemplate.convertAndSend(mq.exchanges().request(), mq.routingKeys().request(), mensagem);
        log.info("==== PAGAMENTO ENVIADO AO BANCO ==== pedido {} | R$ {} -> {} / {}",
                mensagem.numeroPedido(), mensagem.valor(), mq.exchanges().request(), mq.routingKeys().request());
    }
}
