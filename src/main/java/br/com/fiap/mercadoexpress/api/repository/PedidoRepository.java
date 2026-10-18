package br.com.fiap.mercadoexpress.api.repository;

import br.com.fiap.mercadoexpress.api.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByCriadoEmDesc();
}
