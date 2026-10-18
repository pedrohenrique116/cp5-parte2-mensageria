package br.com.fiap.mercadoexpress.api.service;

import br.com.fiap.mercadoexpress.api.dto.CompraRequest;
import br.com.fiap.mercadoexpress.api.dto.ItemCompraRequest;
import br.com.fiap.mercadoexpress.api.messaging.PagamentoRequestMessage;
import br.com.fiap.mercadoexpress.api.messaging.PagamentoRequestProducer;
import br.com.fiap.mercadoexpress.api.messaging.PagamentoResponseMessage;
import br.com.fiap.mercadoexpress.api.model.*;
import br.com.fiap.mercadoexpress.api.repository.PedidoRepository;
import br.com.fiap.mercadoexpress.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Facade do fluxo de pagamento (igual ao diagrama da aula):
 *
 *  API -> Facade -> salva o pedido parcialmente no BD -> Message Producer (pagamento-request-queue)
 *  Sucesso Consumer -> Facade -> finaliza como APROVADO -> envia e-mail -> registra e-mail no BD
 *  Erro Consumer    -> Facade -> finaliza como RECUSADO -> devolve o estoque
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoFacade {

    private static final DateTimeFormatter FORMATO_NUMERO = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final PagamentoRequestProducer pagamentoRequestProducer;
    private final EmailService emailService;

    @Transactional
    public Pedido solicitarPagamento(CompraRequest request) {
        validar(request);

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(gerarNumeroPedido());
        pedido.setClienteNome(request.clienteNome().trim());
        pedido.setClienteEmail(request.clienteEmail().trim().toLowerCase(Locale.ROOT));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        for (ItemCompraRequest item : request.itens()) {
            Produto produto = produtoRepository.findById(item.produtoId())
                    .orElseThrow(() -> new CompraInvalidaException("Produto " + item.produtoId() + " não existe."));
            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw new CompraInvalidaException("O produto " + produto.getNome() + " não está à venda.");
            }
            if (produto.getQuantidadeEstoque() < item.quantidade()) {
                throw new CompraInvalidaException("Estoque insuficiente de " + produto.getNome()
                        + ": restam " + produto.getQuantidadeEstoque() + " " + produto.getUnidade() + ".");
            }
            produto.baixarEstoque(item.quantidade());           // reserva o estoque
            pedido.adicionarItem(new ItemPedido(produto, item.quantidade()));
        }
        pedido.recalcularTotais();

        // 1) Salva a compra parcialmente no banco de dados
        Pedido salvo = pedidoRepository.save(pedido);

        // 2) Publica na fila SOMENTE depois do commit (o worker sempre encontra o pedido salvo)
        PagamentoRequestMessage mensagem = new PagamentoRequestMessage(
                salvo.getId(), salvo.getNumeroPedido(), salvo.getClienteNome(), salvo.getClienteEmail(),
                salvo.getValorTotal(), salvo.getQuantidadeItens(), LocalDateTime.now());
        executarAposCommit(() -> pagamentoRequestProducer.integrar(mensagem));

        log.info("Pedido {} criado (R$ {}) - aguardando Banco Tranquilo", salvo.getNumeroPedido(), salvo.getValorTotal());
        return salvo;
    }

    @Transactional
    public void processarPagamentoAprovado(PagamentoResponseMessage resposta) {
        Pedido pedido = buscarAguardando(resposta);
        if (pedido == null) {
            return;
        }
        pedido.setStatus(StatusPedido.APROVADO);
        pedido.setCodigoAutorizacao(resposta.codigoAutorizacao());
        pedido.setMensagemBanco(resposta.mensagem());
        pedido.setProcessadoEm(LocalDateTime.now());
        pedidoRepository.save(pedido);

        // Compra confirmada -> e-mail de confirmacao de pagamento (registrado em tds_emails_mercado)
        emailService.enviarConfirmacaoPagamento(pedido);
        log.info("Pedido {} APROVADO e e-mail enviado para {}", pedido.getNumeroPedido(), pedido.getClienteEmail());
    }

    @Transactional
    public void processarPagamentoRecusado(PagamentoResponseMessage resposta) {
        Pedido pedido = buscarAguardando(resposta);
        if (pedido == null) {
            return;
        }
        pedido.setStatus(StatusPedido.RECUSADO);
        pedido.setMensagemBanco(resposta.mensagem());
        pedido.setProcessadoEm(LocalDateTime.now());

        // devolve ao estoque o que tinha sido reservado
        for (ItemPedido item : pedido.getItens()) {
            produtoRepository.findById(item.getProdutoId())
                    .ifPresent(p -> p.devolverEstoque(item.getQuantidade()));
        }
        pedidoRepository.save(pedido);
        log.info("Pedido {} RECUSADO: {}", pedido.getNumeroPedido(), resposta.mensagem());
    }

    private Pedido buscarAguardando(PagamentoResponseMessage resposta) {
        if (resposta.pedidoId() == null) {
            log.warn("Resposta sem pedidoId ignorada: {}", resposta);
            return null;
        }
        Pedido pedido = pedidoRepository.findById(resposta.pedidoId()).orElse(null);
        if (pedido == null) {
            log.warn("Pedido {} nao encontrado - resposta ignorada (teste manual pelo painel do RabbitMQ?)",
                    resposta.pedidoId());
            return null;
        }
        if (!pedido.isAguardando()) {
            log.warn("Pedido {} ja foi finalizado como {} - resposta duplicada ignorada",
                    pedido.getNumeroPedido(), pedido.getStatus());
            return null;
        }
        return pedido;
    }

    private void validar(CompraRequest request) {
        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new CompraInvalidaException("O carrinho está vazio.");
        }
        if (request.clienteNome() == null || request.clienteNome().isBlank()) {
            throw new CompraInvalidaException("Informe o nome de quem está comprando.");
        }
        if (request.clienteEmail() == null || !request.clienteEmail().contains("@")) {
            throw new CompraInvalidaException("Informe um e-mail válido para receber a confirmação.");
        }
    }

    private String gerarNumeroPedido() {
        return "MX-" + LocalDateTime.now().format(FORMATO_NUMERO) + "-"
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private void executarAposCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }
}
