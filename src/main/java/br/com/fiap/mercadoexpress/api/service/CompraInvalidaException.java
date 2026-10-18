package br.com.fiap.mercadoexpress.api.service;

public class CompraInvalidaException extends RuntimeException {
    public CompraInvalidaException(String mensagem) {
        super(mensagem);
    }
}
