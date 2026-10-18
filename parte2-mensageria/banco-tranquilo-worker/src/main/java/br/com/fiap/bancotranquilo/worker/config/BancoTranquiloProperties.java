package br.com.fiap.bancotranquilo.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "banco.tranquilo")
public record BancoTranquiloProperties(double taxaAprovacao, BigDecimal limitePorCompra,
                                       long latenciaMinMs, long latenciaMaxMs) {
}
