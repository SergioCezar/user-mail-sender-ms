package br.com.meuprojeto.email.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    private final String queueName = "email-queue";

    @Bean
    public Queue getQueue() {
        return new Queue(queueName, true);
    }


}
