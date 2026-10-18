package br.com.fiap.mercadoexpress.api.model;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO("Aguardando o banco"),
    APROVADO("Pagamento aprovado"),
    RECUSADO("Pagamento recusado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
