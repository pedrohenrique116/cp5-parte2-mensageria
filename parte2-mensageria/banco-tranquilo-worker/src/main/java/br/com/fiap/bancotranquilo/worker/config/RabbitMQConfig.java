package br.com.fiap.bancotranquilo.worker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara no RabbitMQ a mesma topologia montada manualmente na aula:
 *
 *  pagamento-request-exchange          --(pagamento-request-rout-key)-->          pagamento-request-queue
 *  pagamento-response-sucesso-exchange --(pagamento-response-sucesso-rout-key)--> pagamento-response-sucesso-queue
 *  pagamento-response-erro-exchange    --(pagamento-response-erro-rout-key)-->    pagamento-response-erro-queue
 *
 * Se as filas/exchanges ja existirem (criadas pelo painel), nada e alterado.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Declarables topologiaPagamentos(MqProperties mq) {
        DirectExchange requestEx = new DirectExchange(mq.exchanges().request(), true, false);
        DirectExchange sucessoEx = new DirectExchange(mq.exchanges().sucesso(), true, false);
        DirectExchange erroEx = new DirectExchange(mq.exchanges().erro(), true, false);

        Queue requestQ = new Queue(mq.queues().request(), true);
        Queue sucessoQ = new Queue(mq.queues().sucesso(), true);
        Queue erroQ = new Queue(mq.queues().erro(), true);

        return new Declarables(
                requestEx, sucessoEx, erroEx,
                requestQ, sucessoQ, erroQ,
                BindingBuilder.bind(requestQ).to(requestEx).with(mq.routingKeys().request()),
                BindingBuilder.bind(sucessoQ).to(sucessoEx).with(mq.routingKeys().sucesso()),
                BindingBuilder.bind(erroQ).to(erroEx).with(mq.routingKeys().erro())
        );
    }

    /** Nao derruba a aplicacao se a fila ja existir com argumentos diferentes. */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setIgnoreDeclarationExceptions(true);
        return admin;
    }

    /**
     * Mensagens trafegam em JSON. O tipo Java e deduzido pelo parametro do @RabbitListener,
     * entao a API e o Worker podem ter classes em pacotes diferentes.
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
