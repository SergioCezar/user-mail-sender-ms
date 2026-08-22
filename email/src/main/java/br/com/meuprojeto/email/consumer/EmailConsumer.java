package br.com.meuprojeto.email.consumer;

import br.com.meuprojeto.email.domain.EmailModel;
import br.com.meuprojeto.email.dto.EmailDto;
import br.com.meuprojeto.email.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "email-queue")
    public void listenEmailQueue (@Payload EmailDto emailDto) {
        var emailModel = new EmailModel();
        emailModel.setUserId(emailDto.userId());
        emailModel.setTo(emailDto.to());
        emailModel.setSubject(emailDto.subject());
        emailModel.setBody(emailDto.body());

        emailService.sendEmail(emailModel);
    }

}
