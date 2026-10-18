package br.com.fiap.bancotranquilo.worker.service;

import br.com.fiap.bancotranquilo.worker.messaging.PagamentoRequestMessage;

/**
 * Contrato da "API Plataforma de Pagamentos" do Banco Tranquilo.
 * Neste projeto a implementacao e simulada (BancoTranquiloApiSimulada); para integrar com
 * um banco real basta criar outra implementacao usando RestClient/WebClient.
 */
public interface BancoTranquiloApi {

    ResultadoTransacao autorizar(PagamentoRequestMessage pagamento);
}
