package br.com.fiap.mercadoexpress.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Compra realizada no Mercado Express.
 * Salva parcialmente (AGUARDANDO_PAGAMENTO) e finalizada quando o Banco Tranquilo responde.
 * Tabela: tds_pedidos_mercado
 */
@Entity
@Table(name = "tds_pedidos_mercado")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", nullable = false, unique = true, length = 30)
    private String numeroPedido;

    @Column(name = "cliente_nome", nullable = false, length = 100)
    private String clienteNome;

    @Column(name = "cliente_email", nullable = false, length = 120)
    private String clienteEmail;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "custo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status = StatusPedido.AGUARDANDO_PAGAMENTO;

    @Column(name = "codigo_autorizacao", length = 40)
    private String codigoAutorizacao;

    @Column(name = "mensagem_banco", length = 300)
    private String mensagemBanco;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ItemPedido> itens = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        itens.add(item);
    }

    public void recalcularTotais() {
        valorTotal = itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        custoTotal = itens.stream().map(ItemPedido::getCustoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getLucro() {
        return valorTotal.subtract(custoTotal);
    }

    public int getQuantidadeItens() {
        return itens.stream().mapToInt(ItemPedido::getQuantidade).sum();
    }

    public boolean isAguardando() {
        return status == StatusPedido.AGUARDANDO_PAGAMENTO;
    }
}
