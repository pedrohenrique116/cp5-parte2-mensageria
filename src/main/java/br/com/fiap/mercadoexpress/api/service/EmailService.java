package br.com.fiap.mercadoexpress.api.service;

import br.com.fiap.mercadoexpress.api.model.EmailEnviado;
import br.com.fiap.mercadoexpress.api.model.Pedido;
import br.com.fiap.mercadoexpress.api.model.StatusEmail;
import br.com.fiap.mercadoexpress.api.repository.EmailEnviadoRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Envia o e-mail de confirmacao de pagamento e guarda o registro no PostgreSQL
 * (tabela tds_emails_mercado) para controle de envios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailEnviadoRepository emailRepository;

    @Value("${app.mail.remetente}")
    private String remetente;

    public EmailEnviado enviarConfirmacaoPagamento(Pedido pedido) {
        Context contexto = new Context(Locale.forLanguageTag("pt-BR"));
        contexto.setVariable("pedido", pedido);
        String html = templateEngine.process("email/confirmacao-pagamento", contexto);
        String assunto = "Pagamento confirmado - pedido " + pedido.getNumeroPedido();

        EmailEnviado registro = new EmailEnviado();
        registro.setPedidoId(pedido.getId());
        registro.setNumeroPedido(pedido.getNumeroPedido());
        registro.setDestinatario(pedido.getClienteEmail());
        registro.setAssunto(assunto);
        registro.setCorpo(html);

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, StandardCharsets.UTF_8.name());
            helper.setFrom(remetente);
            helper.setTo(pedido.getClienteEmail());
            helper.setSubject(assunto);
            helper.setText(html, true);
            mailSender.send(mensagem);
            registro.setStatus(StatusEmail.ENVIADO);
            log.info("E-mail de confirmacao enviado para {}", pedido.getClienteEmail());
        } catch (Exception e) {
            // O pagamento continua aprovado mesmo se o SMTP falhar; a falha fica registrada no BD.
            registro.setStatus(StatusEmail.FALHA);
            String erro = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            registro.setErro(erro.length() > 1000 ? erro.substring(0, 1000) : erro);
            log.error("Falha ao enviar e-mail para {}: {}", pedido.getClienteEmail(), erro);
        }
        return emailRepository.save(registro);
    }
}
