package br.com.fiap.mercadoexpress.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * JSON de exemplo (POST /api/compras):
 * {
 *   "clienteNome": "Maria Silva",
 *   "clienteEmail": "maria@exemplo.com",
 *   "itens": [ { "produtoId": 1, "quantidade": 2 }, { "produtoId": 7, "quantidade": 3 } ]
 * }
 */
public record CompraRequest(
        @NotBlank(message = "Informe o nome do cliente") String clienteNome,
        @NotBlank(message = "Informe o e-mail do cliente")
        @Email(message = "E-mail inválido") String clienteEmail,
        @NotEmpty(message = "O carrinho está vazio") List<@Valid ItemCompraRequest> itens
) {
}
