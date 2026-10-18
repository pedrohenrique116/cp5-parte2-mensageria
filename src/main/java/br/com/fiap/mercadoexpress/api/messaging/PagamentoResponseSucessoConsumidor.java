package br.com.fiap.mercadoexpress.api.messaging;

import br.com.fiap.mercadoexpress.api.service.PagamentoFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Para consumir basta informar a Queue. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoResponseSucessoConsumidor {

    private final PagamentoFacade pagamentoFacade;

    @RabbitListener(queues = "${mq.queues.sucesso}")
    public void receber(@Payload PagamentoResponseMessage resposta) {
        log.info("==== RESPOSTA SUCESSO ==== pedido {} | autorizacao {}",
                resposta.numeroPedido(), resposta.codigoAutorizacao());
        pagamentoFacade.processarPagamentoAprovado(resposta);
    }
}
