package br.com.fiap.mercadoexpress.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCompraRequest(
        @NotNull(message = "Informe o produtoId") Long produtoId,
        @NotNull(message = "Informe a quantidade")
        @Min(value = 1, message = "A quantidade mínima é 1")
        @Max(value = 999, message = "A quantidade máxima é 999") Integer quantidade
) {
}
