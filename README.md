# Mercado Express × Banco Tranquilo · Microsserviços com RabbitMQ

**CP5 · Parte 2 — Java Advanced (FIAP · TDS)** · Prof. Dr. Marcel Stefan Wagner

Dois microsserviços **Spring Boot** que conversam de forma **assíncrona** pelo **RabbitMQ**: o **Mercado Express** registra a compra e pede o pagamento; o **worker de Integração de Pagamentos** consulta a **API do Banco Tranquilo** e devolve sucesso ou erro. Quando o pagamento é aprovado, o cliente recebe um **e-mail de confirmação**, e o envio fica registrado no **PostgreSQL**. Um **dashboard da gerência** mostra compras, vendas, lucro e estoque.

| Item | Valor |
|---|---|
| IDE utilizada | IntelliJ IDEA |
| Linguagem / Build | Java 17 · Maven |
| Framework | Spring Boot 3.5.6 (Spring Web, Spring for RabbitMQ, Spring Data JPA, Java Mail Sender, Thymeleaf) |
| Broker | RabbitMQ 3.13 (compatível com LavinMQ) |
| Banco de dados | PostgreSQL 16 — o mesmo da Parte 1 |
| E-mail | SMTP (Ethereal para testes, Mailpit local ou Gmail) |
| Vídeo | <https://youtu.be/XXyUisp_pW4> |
| Parte 1 (Spring Security) | repositório: <https://github.com/pedrohenrique116/pt1-mercado-express> · deploy: <https://mercado-express-security.onrender.com> |

## Integrantes

| Nome | RM |
|---|---|
| HENRIQUE MARTINS OLIVEIRA | RM563620 |
| GUILHERME MACEDO MARTINS | RM562396 |
| PEDRO HENRIQUE LUIZ ALVES DUARTE | RM563405 |

---

## 1. Arquitetura

```mermaid
flowchart LR
    subgraph API["mercado-backend-api :8081"]
        WEB[Loja + Carrinho<br/>Thymeleaf] --> F[PagamentoFacade]
        REST[PagamentoApi<br/>REST /api/compras] --> F
        F --> DB[(PostgreSQL)]
        F --> P1[PagamentoRequestProducer]
        C1[ResponseSucessoConsumidor] --> F
        C2[ResponseErroConsumidor] --> F
        F --> MAIL[EmailService] --> SMTP[[SMTP]]
        DASH[Dashboard gerência]
    end

    subgraph MQ["RabbitMQ"]
        Q1[[pagamento-request-queue]]
        Q2[[pagamento-response-sucesso-queue]]
        Q3[[pagamento-response-erro-queue]]
    end

    subgraph W["banco-tranquilo-worker"]
        C3[PagamentoRequestConsumidor] --> BT[API Banco Tranquilo]
        C3 --> P2[PagamentoSucessoProdutor]
        C3 --> P3[PagamentoErroProdutor]
    end

    P1 --> Q1 --> C3
    P2 --> Q2 --> C1
    P3 --> Q3 --> C2
```

### Fluxo de uma compra

1. O cliente finaliza a compra na loja (ou o Postman faz `POST /api/compras`).
2. A **Facade** valida o estoque, reserva os produtos e **salva o pedido parcialmente** como `AGUARDANDO_PAGAMENTO`.
3. Depois do *commit*, o **PagamentoRequestProducer** publica a mensagem na `pagamento-request-exchange` com a routing key `pagamento-request-rout-key`. A API responde **202 Accepted** na hora, sem esperar o banco.
4. O **worker** consome a `pagamento-request-queue`, chama a **API do Banco Tranquilo** e publica a resposta na exchange de **sucesso** ou de **erro**.
5. Na API, o consumidor correspondente chama a Facade, que:
   - **APROVADO** → finaliza o pedido, envia o **e-mail de confirmação** e grava o envio em `tds_emails_mercado`;
   - **RECUSADO** → finaliza o pedido e **devolve o estoque**.

### Topologia no RabbitMQ (igual à aula)

| Exchange (direct, durable) | Routing key | Queue (durable) |
|---|---|---|
| `pagamento-request-exchange` | `pagamento-request-rout-key` | `pagamento-request-queue` |
| `pagamento-response-sucesso-exchange` | `pagamento-response-sucesso-rout-key` | `pagamento-response-sucesso-queue` |
| `pagamento-response-erro-exchange` | `pagamento-response-erro-rout-key` | `pagamento-response-erro-queue` |

As duas aplicações declaram essa topologia ao iniciar (`RabbitMQConfig`). Se as filas já tiverem sido criadas manualmente pelo painel (como na aula), nada é alterado.

### Regras da API simulada do Banco Tranquilo

| Situação | Resultado |
|---|---|
| E-mail contém `aprova` (ex.: `joao.aprova@exemplo.com`) | sempre **APROVADO** |
| E-mail contém `recusa` (ex.: `ana.recusa@exemplo.com`) | sempre **RECUSADO** (cartão bloqueado) |
| Valor acima de **R$ 500,00** | **RECUSADO** (limite excedido) |
| Demais casos | aleatório, **85 %** de aprovação (Random, como no exemplo da aula) |

Latência simulada de 0,8 s a 2,5 s. Tudo configurável em `banco.tranquilo.*` no `application.yml` do worker.

---

## 2. Estrutura

```
parte2-mensageria/
├── docker-compose.yml            # RabbitMQ + PostgreSQL + Mailpit
├── run-docker.sh
├── postman/CP5_Mercado_Express_Banco_Tranquilo.postman_collection.json
├── scripts/  testes.http · testar-fluxo.sh · rabbitmq-publish-manual.md
├── mercado-backend-api/          # Back End API (porta 8081)
│   └── br/com/fiap/mercadoexpress/api
│       ├── config/       MqProperties, RabbitMQConfig, ProdutoSeeder
│       ├── controller/   PagamentoApi (REST), LojaController, DashboardController, ApiExceptionHandler
│       ├── dto/          CompraRequest, CompraResponse, PedidoResponse, DashboardDTO...
│       ├── messaging/    PagamentoRequestProducer, PagamentoResponseSucessoConsumidor,
│       │                 PagamentoResponseErroConsumidor, mensagens (records)
│       ├── model/        Produto, Pedido, ItemPedido, EmailEnviado, enums
│       ├── repository/
│       └── service/      PagamentoFacade, EmailService, DashboardService, Carrinho
└── banco-tranquilo-worker/       # Integração de Pagamentos
    └── br/com/fiap/bancotranquilo/worker
        ├── config/       MqProperties, RabbitMQConfig, BancoTranquiloProperties
        ├── consumer/     PagamentoRequestConsumidor
        ├── producer/     PagamentoSucessoProdutor, PagamentoErroProdutor
        └── service/      BancoTranquiloApi, BancoTranquiloApiSimulada
```

Trecho principal — produtor e consumidor (como no slide "para produzir informe a Exchange e a Routing Key; para consumir, a Queue"):

```java
// mercado-backend-api: PagamentoRequestProducer
rabbitTemplate.convertAndSend(mq.exchanges().request(), mq.routingKeys().request(), mensagem);

// banco-tranquilo-worker: PagamentoRequestConsumidor
@RabbitListener(queues = "${mq.queues.request}")
public void receberMensagem(@Payload PagamentoRequestMessage pagamento) { ... }
```

As mensagens trafegam em **JSON** (`Jackson2JsonMessageConverter`) e as duas classes principais têm `@EnableRabbit`.

---

## 3. Banco de dados (PostgreSQL)

Mesmo banco da Parte 1 (`mercado_express`). O Hibernate cria as tabelas (`ddl-auto: update`).

| Tabela | Conteúdo |
|---|---|
| `tds_sec_mvc_tb_mercado` | produtos (compartilhada com a Parte 1) |
| `tds_pedidos_mercado` | compras: número, cliente, total, custo, status, código de autorização, resposta do banco |
| `tds_itens_pedido_mercado` | itens de cada compra com preço e custo do momento |
| `tds_emails_mercado` | **controle de envio de e-mails**: destinatário, assunto, corpo HTML, status `ENVIADO`/`FALHA`, erro, data |

**Configuração enviada ao professor** (`mercado-backend-api/src/main/resources/application.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mercado_express
    username: postgres
    password: postgres
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  mail:
    host: localhost   # Mailpit
    port: 1025
```

Consultas para mostrar no vídeo:

```sql
SELECT numero_pedido, cliente_email, valor_total, status, codigo_autorizacao FROM tds_pedidos_mercado ORDER BY id DESC;
SELECT destinatario, assunto, status, enviado_em FROM tds_emails_mercado ORDER BY id DESC;
SELECT nome, quantidade_estoque FROM tds_sec_mvc_tb_mercado ORDER BY nome;
```

---

## 4. Como executar

Pré-requisitos: JDK 17+, IntelliJ (ou Maven) e Docker Desktop.

```bash
# 1) Infraestrutura (RabbitMQ, PostgreSQL e Mailpit)
cd parte2-mensageria
docker compose up -d

# 2) Worker de pagamentos (terminal 1)
cd banco-tranquilo-worker && mvn spring-boot:run

# 3) Back End API (terminal 2)
cd mercado-backend-api && mvn spring-boot:run
```

No IntelliJ: abra cada pasta como projeto Maven (ou adicione as duas como módulos) e execute `BancoTranquiloWorkerApplication` e `MercadoBackendApiApplication`.

| O quê | Endereço |
|---|---|
| Loja e carrinho | <http://localhost:8081/loja> |
| Dashboard da gerência | <http://localhost:8081/dashboard> |
| Painel do RabbitMQ | <http://localhost:15672> (guest / guest) |
| Caixa de entrada dos e-mails (Mailpit) | <http://localhost:8025> |

> Já está rodando o PostgreSQL da Parte 1? Não suba outro: comente o serviço `postgres` no `docker-compose.yml`.

### E-mail real com Gmail (opcional)

Gere uma *senha de app* em <https://myaccount.google.com/apppasswords> e rode a API com:

```
MAIL_HOST=smtp.gmail.com  MAIL_PORT=587  MAIL_SMTP_AUTH=true  MAIL_STARTTLS=true
MAIL_USERNAME=seu.email@gmail.com  MAIL_PASSWORD=senha-de-app  MAIL_FROM=seu.email@gmail.com
```

No IntelliJ: *Run → Edit Configurations → Environment variables*.

### LavinMQ / CloudAMQP (opcional)

LavinMQ usa o mesmo protocolo AMQP 0-9-1. Crie uma instância gratuita no CloudAMQP e troque, nas duas aplicações:

```yaml
spring:
  rabbitmq:
    addresses: amqps://usuario:senha@host.lmq.cloudamqp.com/vhost
```

---

## 5. Testes

### 5.1 Exemplo de requisição de compra (JSON)

`POST http://localhost:8081/api/compras`

```json
{
  "clienteNome": "Maria Silva",
  "clienteEmail": "maria.silva@exemplo.com",
  "itens": [
    { "produtoId": 1, "quantidade": 2 },
    { "produtoId": 7, "quantidade": 3 }
  ]
}
```

Resposta imediata (`202 Accepted`):

```json
{
  "pedidoId": 1,
  "numeroPedido": "MX-261017193012-482",
  "status": "AGUARDANDO_PAGAMENTO",
  "valorTotal": 29.45,
  "mensagem": "Pagamento aguardando confirmação do Banco Tranquilo...",
  "acompanharEm": "/api/compras/1"
}
```

Alguns segundos depois, `GET /api/compras/1` retorna `"status": "APROVADO"` (com `codigoAutorizacao`) ou `"RECUSADO"` (com `mensagemBanco`).

### 5.2 Postman / Insomnia

Importe `postman/CP5_Mercado_Express_Banco_Tranquilo.postman_collection.json` (o Insomnia também importa). As requisições têm scripts de teste (`pm.test`) e devem ser executadas em ordem — dá para rodar tudo pelo **Collection Runner**.

| # | Requisição | O que verifica |
|---|---|---|
| 1 | Listar produtos | ids disponíveis |
| 2 | Criar compra | 202 e status `AGUARDANDO_PAGAMENTO` |
| 3 | Consultar pedido | o banco respondeu (`APROVADO`/`RECUSADO`) |
| 4 | Compra sempre aprovada | gera e-mail |
| 5 | Compra sempre recusada | estoque devolvido |
| 6 | Compra acima do limite | recusa por limite |
| 7 | Compra inválida | validação 400 |
| 8 | `POST /pagamentos` | endpoint igual ao da aula |
| 9–11 | Compras, e-mails, dashboard | dados gravados no PostgreSQL |
| 12–13 | HTTP API do RabbitMQ | estado das filas e publicação direta na exchange |

Também há `scripts/testes.http` (IntelliJ HTTP Client) e `scripts/testar-fluxo.sh` (curl).

### 5.3 Testes internos pelo RabbitMQ Monitor

Veja [`scripts/rabbitmq-publish-manual.md`](scripts/rabbitmq-publish-manual.md): publicar uma mensagem pela aba **Exchanges → Publish message** e ver o worker processar; parar o worker para ver mensagens acumulando em **Queues → Get messages**.

---

## 6. Telas

![Loja](docs/prints/01-loja.png)
![Carrinho](docs/prints/02-carrinho.png)
![Pedido aprovado](docs/prints/03-pedido-aprovado.png)
![E-mail recebido no Mailpit](docs/prints/04-email.png)
![Dashboard da gerência](docs/prints/05-dashboard.png)
![Filas no RabbitMQ](docs/prints/06-rabbitmq-queues.png)
![Teste via PowerShell (Invoke-RestMethod)](docs/prints/07-teste-api.png)
![Dados persistidos no PostgreSQL — pedidos (tds_pedidos_mercado) e e-mails (tds_emails_mercado), via Dashboard](docs/prints/08-postgres.png)

---

## 7. Roteiro sugerido para o vídeo (5 a 7 min)

| Tempo | O que mostrar |
|---|---|
| 0:00–0:40 | Apresentação do grupo e do diagrama de arquitetura (este README). |
| 0:40–1:40 | `docker compose up -d`, Docker Desktop com os 3 containers, painel do RabbitMQ: **Exchanges**, **Queues** e **Bindings**. |
| 1:40–2:50 | Código: `PagamentoApi` → `PagamentoFacade` → `PagamentoRequestProducer`; no worker, `PagamentoRequestConsumidor` → `BancoTranquiloApiSimulada` → produtores de sucesso/erro; consumidores na API; `EmailService`. |
| 2:50–3:50 | Subir worker e API; no painel **Connections/Channels** aparecem as 2 conexões. Compra pela loja → página do pedido muda de "Aguardando" para "Aprovado" → e-mail no Mailpit. |
| 3:50–4:50 | Postman: Collection Runner com as requisições 1–11; logs `RESPOSTA SUCESSO`/`RESPOSTA ERRO` nos consoles; gráfico **Message rates** do RabbitMQ. |
| 4:50–5:30 | Teste interno: parar o worker, fazer 3 compras, mostrar as mensagens paradas em `pagamento-request-queue` (Get messages), religar o worker e ver a fila esvaziar. |
| 5:30–6:30 | Dashboard da gerência (faturamento, lucro, gráficos, estoque, e-mails) e `SELECT` em `tds_emails_mercado` e `tds_pedidos_mercado`. |

---

## 8. Tecnologias

Java 17 · Spring Boot 3.5 · Spring Web · Spring for RabbitMQ (AMQP 0-9-1) · Spring Data JPA · Spring Mail · Thymeleaf · Chart.js · Lombok · PostgreSQL 16 · RabbitMQ 3.13 · Mailpit · Docker Compose · Postman
