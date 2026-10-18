package br.com.fiap.mercadoexpress.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Item de um pedido. Guarda nome e precos do momento da compra. Tabela: tds_itens_pedido_mercado */
@Entity
@Table(name = "tds_itens_pedido_mercado")
@Getter
@Setter
@NoArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "nome_produto", nullable = false, length = 100)
    private String nomeProduto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "custo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoUnitario;

    public ItemPedido(Produto produto, int quantidade) {
        this.produtoId = produto.getId();
        this.nomeProduto = produto.getNome();
        this.categoria = produto.getCategoria();
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPrecoVenda();
        this.custoUnitario = produto.getPrecoCusto();
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public BigDecimal getCustoTotal() {
        return custoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
