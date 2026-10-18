# Teste interno pelo painel do RabbitMQ (RabbitMQ Monitor)

1. Acesse http://localhost:15672 (guest / guest) > aba **Exchanges** > `pagamento-request-exchange`.
2. Abra **Publish message** e preencha:
   - **Routing key:** `pagamento-request-rout-key`
   - **Properties:** `content_type` = `application/json`
   - **Payload:**

```json
{"pedidoId":999,"numeroPedido":"TESTE-RABBIT","clienteNome":"Teste Monitor","clienteEmail":"monitor.aprova@exemplo.com","valor":42.50,"quantidadeItens":1}
```

3. Clique em **Publish message**. O console do `banco-tranquilo-worker` mostra o pedido recebido e a resposta
   publicada em `pagamento-response-sucesso-exchange`. A API registra no log que o pedido 999 não existe
   (é só um teste do broker) e descarta a resposta.

Para ver uma mensagem parada na fila: pare o worker, faça uma compra e abra **Queues > pagamento-request-queue >
Get messages**. Ao religar o worker, a mensagem é consumida e o pedido é finalizado.
