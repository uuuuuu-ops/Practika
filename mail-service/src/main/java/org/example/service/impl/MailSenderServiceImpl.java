package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.example.dto.MailParams;
import org.example.service.MailSenderService;

@Log4j
@RequiredArgsConstructor
@Service
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${service.activation.uri}")
    private String activationServiceUri;


    @Override
    public void send(MailParams mailParams) {
        var subject = "Активация учетной записи";
        var messageBody = getActivationMailBody(mailParams.getId());
        var emailTo = mailParams.getEmailTo();
        log.debug(String.format("Sending email for mail=[%s]", emailTo));

        if (emailTo == null || emailTo.isBlank()) {
            throw new IllegalArgumentException("emailTo is null or blank!");
        }

        System.out.println("Sending email to: " + emailTo);
        System.out.println("Message: " + messageBody);

        var mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        javaMailSender.send(mailMessage);
    }

    private String getActivationMailBody(String id) {
        var msg = String.format("Для завершения регистрации перейдите по ссылке:\n%s",
                activationServiceUri);
        return msg.replace("{id}", id);
    }
}