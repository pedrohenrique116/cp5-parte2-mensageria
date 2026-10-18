package br.com.fiap.mercadoexpress.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Registro de cada e-mail de confirmacao enviado aos clientes. Tabela: tds_emails_mercado */
@Entity
@Table(name = "tds_emails_mercado")
@Getter
@Setter
@NoArgsConstructor
public class EmailEnviado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "numero_pedido", nullable = false, length = 30)
    private String numeroPedido;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Column(columnDefinition = "TEXT")
    private String corpo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmail status;

    @Column(length = 1000)
    private String erro;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    @PrePersist
    void aoCriar() {
        if (enviadoEm == null) {
            enviadoEm = LocalDateTime.now();
        }
    }
}
