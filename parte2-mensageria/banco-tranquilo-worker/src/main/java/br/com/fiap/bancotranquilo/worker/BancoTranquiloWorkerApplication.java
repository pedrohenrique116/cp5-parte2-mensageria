package br.com.fiap.bancotranquilo.worker;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableRabbit
@SpringBootApplication
@ConfigurationPropertiesScan
public class BancoTranquiloWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoTranquiloWorkerApplication.class, args);
        System.out.println("Rodando Worker - Integracao de Pagamentos Banco Tranquilo");
    }
}
