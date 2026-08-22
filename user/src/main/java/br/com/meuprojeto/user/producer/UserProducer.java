package br.com.meuprojeto.user.producer;

import br.com.meuprojeto.user.domain.UserModel;
import br.com.meuprojeto.user.dto.EmailDto;
import com.rabbitmq.client.AMQP;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEvent(UserModel userModel) {
        var emailDto = new EmailDto(
                userModel.getId(),
                userModel.getEmail(),
                "Cadastro realizado",
                "Olá, " + userModel.getName()
                        + "! Seu cadastro foi realizado com sucesso."
        );

        rabbitTemplate.convertAndSend("email-queue", emailDto);

    }



}