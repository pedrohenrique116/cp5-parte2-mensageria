package br.com.fiap.mercadoexpress.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mesma tabela de produtos da Parte 1 (tds_sec_mvc_tb_mercado).
 * Assim o carrinho da Parte 2 vende exatamente o que o gerente cadastrou na Parte 1.
 */
@Entity
@Table(name = "tds_sec_mvc_tb_mercado")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @Column(length = 500)
    private String descricao;

    @Column(name = "preco_custo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    @Column(nullable = false, length = 20)
    private String unidade;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public boolean isEstoqueBaixo() {
        return quantidadeEstoque != null && quantidadeEstoque <= 5;
    }

    public void baixarEstoque(int quantidade) {
        this.quantidadeEstoque = this.quantidadeEstoque - quantidade;
    }

    public void devolverEstoque(int quantidade) {
        this.quantidadeEstoque = this.quantidadeEstoque + quantidade;
    }
}
