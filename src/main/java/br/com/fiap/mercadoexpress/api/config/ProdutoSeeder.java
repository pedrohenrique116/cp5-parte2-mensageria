package br.com.fiap.mercadoexpress.api.config;

import br.com.fiap.mercadoexpress.api.model.Categoria;
import br.com.fiap.mercadoexpress.api.model.Produto;
import br.com.fiap.mercadoexpress.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Se a Parte 1 ainda nao tiver cadastrado produtos neste banco,
 * cria os mesmos produtos de exemplo para o carrinho funcionar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProdutoSeeder implements CommandLineRunner {

    private final ProdutoRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("{} produtos encontrados em tds_sec_mvc_tb_mercado", repository.count());
            return;
        }
        repository.saveAll(List.of(
            p("Banana prata", Categoria.HORTIFRUTI, "3.20", "6.49", 60, "kg"),
            p("Tomate italiano", Categoria.HORTIFRUTI, "4.10", "8.99", 45, "kg"),
            p("Arroz agulhinha tipo 1 5kg", Categoria.MERCEARIA, "19.90", "29.90", 30, "pacote"),
            p("Feijão carioca 1kg", Categoria.MERCEARIA, "5.40", "8.79", 40, "pacote"),
            p("Azeite extra virgem 500ml", Categoria.MERCEARIA, "24.00", "39.90", 40, "garrafa"),
            p("Café torrado e moído 500g", Categoria.MERCEARIA, "13.50", "21.90", 25, "pacote"),
            p("Leite integral 1L", Categoria.LATICINIOS, "3.60", "5.49", 80, "caixa"),
            p("Queijo muçarela fatiado 200g", Categoria.LATICINIOS, "7.20", "12.90", 4, "bandeja"),
            p("Pão francês", Categoria.PADARIA, "9.00", "16.90", 20, "kg"),
            p("Água mineral sem gás 1,5L", Categoria.BEBIDAS, "1.30", "2.99", 120, "garrafa"),
            p("Suco de uva integral 1L", Categoria.BEBIDAS, "8.50", "14.90", 3, "garrafa"),
            p("Detergente neutro 500ml", Categoria.LIMPEZA, "1.40", "2.79", 70, "un"),
            p("Sabão em pó 1,6kg", Categoria.LIMPEZA, "12.30", "19.90", 18, "caixa"),
            p("Creme dental 90g", Categoria.HIGIENE, "2.10", "4.49", 50, "un"),
            p("Pizza congelada muçarela 460g", Categoria.CONGELADOS, "11.00", "18.90", 15, "un")
        ));
        log.info("Produtos de exemplo cadastrados em tds_sec_mvc_tb_mercado");
    }

    private Produto p(String nome, Categoria categoria, String custo, String venda, int estoque, String unidade) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setCategoria(categoria);
        p.setPrecoCusto(new BigDecimal(custo));
        p.setPrecoVenda(new BigDecimal(venda));
        p.setQuantidadeEstoque(estoque);
        p.setUnidade(unidade);
        p.setAtivo(true);
        return p;
    }
}
