package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.example.service.ConsumerService;
import org.example.service.MainService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;



@RequiredArgsConstructor
@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService mainService;

    @Override
    @RabbitListener(queues = "${spring.rabbitmq.queues.text-message-update}")
    public void consumetextservice(Update update) {
        log.debug("TEXT : message is received");
        mainService.proccesTextMessage(update);
    }

    @Override
    @RabbitListener(queues = "${spring.rabbitmq.queues.doc-message-update}")
    public void consumedocservice(Update update) {
        log.debug("DOC : message is received");
        mainService.proccesDOCMessage(update);

    }

    @Override
    @RabbitListener(queues = "${spring.rabbitmq.queues.photo-message-update}")
    public void consumephotoservice(Update update) {

        log.debug("Photo : message is received");

        try {
            mainService.proccesPhotoMessage(update);
        } catch (Exception ex) {
            log.error("Error while processing photo message", ex);
        }

    }
}
