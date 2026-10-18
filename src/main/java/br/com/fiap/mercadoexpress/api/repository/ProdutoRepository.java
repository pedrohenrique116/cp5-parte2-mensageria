package br.com.fiap.mercadoexpress.api.repository;

import br.com.fiap.mercadoexpress.api.model.Categoria;
import br.com.fiap.mercadoexpress.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByNomeAsc();

    List<Produto> findByAtivoTrueAndCategoriaOrderByNomeAsc(Categoria categoria);

    List<Produto> findAllByOrderByQuantidadeEstoqueAsc();
}
