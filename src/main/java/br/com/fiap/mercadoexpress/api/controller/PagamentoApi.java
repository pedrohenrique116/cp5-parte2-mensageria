package br.com.fiap.mercadoexpress.api.controller;

import br.com.fiap.mercadoexpress.api.dto.*;
import br.com.fiap.mercadoexpress.api.model.Pedido;
import br.com.fiap.mercadoexpress.api.repository.EmailEnviadoRepository;
import br.com.fiap.mercadoexpress.api.repository.PedidoRepository;
import br.com.fiap.mercadoexpress.api.repository.ProdutoRepository;
import br.com.fiap.mercadoexpress.api.service.DashboardService;
import br.com.fiap.mercadoexpress.api.service.PagamentoFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST do Mercado Express (usada pelo Postman/Insomnia).
 *
 * POST /api/compras            -> cria a compra e publica o pedido de pagamento na fila (202 Accepted)
 * POST /pagamentos             -> mesmo endpoint, com o caminho usado no exemplo da aula
 * GET  /api/compras            -> lista os pedidos
 * GET  /api/compras/{id}       -> consulta o status do pagamento de um pedido
 * GET  /api/produtos           -> produtos a venda (ids para montar o JSON)
 * GET  /api/emails             -> e-mails de confirmacao registrados no BD
 * GET  /api/dashboard/resumo   -> indicadores de vendas, lucro e estoque
 */
@RestController
@RequiredArgsConstructor
public class PagamentoApi {

    private final PagamentoFacade pagamentoFacade;
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final EmailEnviadoRepository emailRepository;
    private final DashboardService dashboardService;

    @PostMapping({"/api/compras", "/pagamentos"})
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request) {
        Pedido pedido = pagamentoFacade.solicitarPagamento(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(CompraResponse.de(pedido));
    }

    @GetMapping("/api/compras")
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarCompras() {
        return pedidoRepository.findAllByOrderByCriadoEmDesc().stream().map(PedidoResponse::de).toList();
    }

    @GetMapping("/api/compras/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PedidoResponse> consultar(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(PedidoResponse::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/produtos")
    public List<ProdutoResponse> produtos() {
        return produtoRepository.findByAtivoTrueOrderByNomeAsc().stream().map(ProdutoResponse::de).toList();
    }

    @GetMapping("/api/emails")
    public List<EmailResponse> emails() {
        return emailRepository.findAllByOrderByEnviadoEmDesc().stream().map(EmailResponse::de).toList();
    }

    @GetMapping("/api/dashboard/resumo")
    public Map<String, Object> resumo() {
        DashboardDTO d = dashboardService.gerar();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("faturamento", d.getFaturamento());
        r.put("custo", d.getCusto());
        r.put("lucro", d.getLucro());
        r.put("ticketMedio", d.getTicketMedio());
        r.put("totalPedidos", d.getTotalPedidos());
        r.put("aprovados", d.getAprovados());
        r.put("recusados", d.getRecusados());
        r.put("aguardando", d.getAguardando());
        r.put("taxaAprovacaoPercentual", d.getTaxaAprovacao());
        r.put("emailsEnviados", d.getEmailsEnviados());
        r.put("emailsComFalha", d.getEmailsComFalha());
        r.put("produtosEstoqueBaixo", d.getProdutosEstoqueBaixo());
        r.put("graficos", d.getGraficos());
        return r;
    }
}
