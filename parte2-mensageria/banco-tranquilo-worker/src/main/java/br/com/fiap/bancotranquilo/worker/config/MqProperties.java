package br.com.fiap.bancotranquilo.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Le o bloco "mq" do application.yml. */
@ConfigurationProperties(prefix = "mq")
public record MqProperties(Nomes exchanges, Nomes queues, Nomes routingKeys) {

    public record Nomes(String request, String sucesso, String erro) {
    }
}
