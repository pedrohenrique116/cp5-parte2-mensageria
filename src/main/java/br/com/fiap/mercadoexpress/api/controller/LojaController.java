package br.com.fiap.mercadoexpress.api.controller;

import br.com.fiap.mercadoexpress.api.model.Categoria;
import br.com.fiap.mercadoexpress.api.model.Pedido;
import br.com.fiap.mercadoexpress.api.repository.PedidoRepository;
import br.com.fiap.mercadoexpress.api.repository.ProdutoRepository;
import br.com.fiap.mercadoexpress.api.service.Carrinho;
import br.com.fiap.mercadoexpress.api.service.CarrinhoService;
import br.com.fiap.mercadoexpress.api.service.CompraInvalidaException;
import br.com.fiap.mercadoexpress.api.service.PagamentoFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas da loja: catalogo, carrinho, finalizacao da compra e acompanhamento do pagamento. */
@Controller
@RequiredArgsConstructor
public class LojaController {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final Carrinho carrinho;
    private final CarrinhoService carrinhoService;
    private final PagamentoFacade pagamentoFacade;

    @ModelAttribute("qtdCarrinho")
    public int quantidadeNoCarrinho() {
        return carrinho.getQuantidadeTotal();
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/loja";
    }

    @GetMapping("/loja")
    public String loja(@RequestParam(required = false) Categoria categoria, Model model) {
        model.addAttribute("produtos", categoria == null
                ? produtoRepository.findByAtivoTrueOrderByNomeAsc()
                : produtoRepository.findByAtivoTrueAndCategoriaOrderByNomeAsc(categoria));
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("categoriaSelecionada", categoria);
        return "loja";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionar(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") int quantidade,
                            RedirectAttributes redirect) {
        produtoRepository.findById(produtoId).ifPresent(p -> {
            carrinho.adicionar(produtoId, Math.max(1, quantidade));
            redirect.addFlashAttribute("sucesso", p.getNome() + " foi para o carrinho.");
        });
        return "redirect:/loja";
    }

    @GetMapping("/carrinho")
    public String verCarrinho(Model model) {
        model.addAttribute("resumo", carrinhoService.resumo());
        return "carrinho";
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizar(@RequestParam Long produtoId, @RequestParam int quantidade) {
        carrinho.atualizar(produtoId, quantidade);
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/remover")
    public String remover(@RequestParam Long produtoId) {
        carrinho.remover(produtoId);
        return "redirect:/carrinho";
    }

    /** Finalizar compra -> executa a API de Integracao de Pagamentos com o Banco Tranquilo. */
    @PostMapping("/carrinho/finalizar")
    public String finalizar(@RequestParam String clienteNome, @RequestParam String clienteEmail,
                            RedirectAttributes redirect) {
        try {
            Pedido pedido = pagamentoFacade.solicitarPagamento(carrinhoService.paraCompra(clienteNome, clienteEmail));
            carrinho.limpar();
            return "redirect:/pedidos/" + pedido.getId();
        } catch (CompraInvalidaException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            redirect.addFlashAttribute("clienteNome", clienteNome);
            redirect.addFlashAttribute("clienteEmail", clienteEmail);
            return "redirect:/carrinho";
        }
    }

    @GetMapping("/pedidos/{id}")
    public String pedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        if (pedido == null) {
            return "redirect:/loja";
        }
        model.addAttribute("pedido", pedido);
        return "pedido";
    }
}
