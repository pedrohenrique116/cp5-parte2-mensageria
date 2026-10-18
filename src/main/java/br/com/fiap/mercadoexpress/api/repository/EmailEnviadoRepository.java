package br.com.fiap.mercadoexpress.api.repository;

import br.com.fiap.mercadoexpress.api.model.EmailEnviado;
import br.com.fiap.mercadoexpress.api.model.StatusEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {

    List<EmailEnviado> findAllByOrderByEnviadoEmDesc();

    List<EmailEnviado> findTop10ByOrderByEnviadoEmDesc();

    long countByStatus(StatusEmail status);
}
