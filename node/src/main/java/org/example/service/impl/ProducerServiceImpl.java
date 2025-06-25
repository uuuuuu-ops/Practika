package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.example.service.producerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


@RequiredArgsConstructor
@Service
public class ProducerServiceImpl implements producerService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.queues.answer-message}")
    private String answerMessageQueue;


    @Override
    public void produceranswer(SendMessage sendMessage) {

        rabbitTemplate.convertAndSend(answerMessageQueue,sendMessage);
    }
}
