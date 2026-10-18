package br.com.fiap.mercadoexpress.api;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableRabbit
@SpringBootApplication
@ConfigurationPropertiesScan
public class MercadoBackendApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MercadoBackendApiApplication.class, args);
        System.out.println("Rodando API Mercado Express em http://localhost:8081");
    }
}
