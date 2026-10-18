package br.com.fiap.mercadoexpress.api.dto;

import br.com.fiap.mercadoexpress.api.model.EmailEnviado;

import java.time.LocalDateTime;

public record EmailResponse(Long id, String numeroPedido, String destinatario, String assunto,
                            String status, String erro, LocalDateTime enviadoEm) {
    public static EmailResponse de(EmailEnviado e) {
        return new EmailResponse(e.getId(), e.getNumeroPedido(), e.getDestinatario(), e.getAssunto(),
                e.getStatus().name(), e.getErro(), e.getEnviadoEm());
    }
}
