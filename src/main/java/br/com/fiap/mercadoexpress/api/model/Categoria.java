package br.com.fiap.mercadoexpress.api.model;

public enum Categoria {
    HORTIFRUTI("Hortifrúti"),
    MERCEARIA("Mercearia"),
    BEBIDAS("Bebidas"),
    LATICINIOS("Laticínios"),
    PADARIA("Padaria"),
    LIMPEZA("Limpeza"),
    HIGIENE("Higiene pessoal"),
    CONGELADOS("Congelados");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
