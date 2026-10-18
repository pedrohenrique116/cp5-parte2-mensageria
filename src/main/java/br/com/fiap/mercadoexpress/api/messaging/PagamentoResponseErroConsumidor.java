package br.com.fiap.mercadoexpress.api.messaging;

import br.com.fiap.mercadoexpress.api.service.PagamentoFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoResponseErroConsumidor {

    private final PagamentoFacade pagamentoFacade;

    @RabbitListener(queues = "${mq.queues.erro}")
    public void receber(@Payload PagamentoResponseMessage resposta) {
        log.warn("==== RESPOSTA ERRO ==== pedido {} | motivo: {}", resposta.numeroPedido(), resposta.mensagem());
        pagamentoFacade.processarPagamentoRecusado(resposta);
    }
}
