package br.com.fiap.bancotranquilo.worker.service;

public record ResultadoTransacao(boolean aprovado, String codigoAutorizacao, String mensagem) {

    public static ResultadoTransacao aprovado(String codigo) {
        return new ResultadoTransacao(true, codigo, "Pagamento autorizado pelo Banco Tranquilo");
    }

    public static ResultadoTransacao recusado(String motivo) {
        return new ResultadoTransacao(false, null, motivo);
    }
}
