package br.com.fiap.mercadoexpress.api.controller;

import br.com.fiap.mercadoexpress.api.service.Carrinho;
import br.com.fiap.mercadoexpress.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Dashboard final da gerencia do Mercado Express. */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final Carrinho carrinho;

    @ModelAttribute("qtdCarrinho")
    public int quantidadeNoCarrinho() {
        return carrinho.getQuantidadeTotal();
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("d", dashboardService.gerar());
        return "dashboard";
    }
}
