package br.com.meuprojeto.email.service;

import br.com.meuprojeto.email.domain.EmailModel;
import br.com.meuprojeto.email.enums.EmailStatus;
import br.com.meuprojeto.email.repository.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class EmailService {

    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;
    private final String emailFrom;

    public EmailService(
            EmailRepository emailRepository,
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String emailFrom
    ) {
        this.emailRepository = emailRepository;
        this.javaMailSender = javaMailSender;
        this.emailFrom = emailFrom;
    }

    @Transactional
    public EmailModel sendEmail(EmailModel emailModel) {
        emailModel.setFrom(emailFrom);
        emailModel.setSentDateEmail(LocalDateTime.now());
        emailModel.setStatus(EmailStatus.PENDING);
        emailRepository.saveAndFlush(emailModel);

        try {
            var message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(emailModel.getTo());
            message.setSubject(emailModel.getSubject());
            message.setText(emailModel.getBody());

            javaMailSender.send(message);
            emailModel.setStatus(EmailStatus.SENT);
            log.info("E-mail enviado para {}", emailModel.getTo());
        } catch (MailException exception) {
            emailModel.setStatus(EmailStatus.FAILED);
            log.error(
                    "Falha ao enviar e-mail para {}: {}",
                    emailModel.getTo(),
                    exception.getMessage(),
                    exception
            );
        }

        return emailRepository.saveAndFlush(emailModel);
    }
}
