package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.Controller.UpdateProcessor;
import org.example.service.Consume;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


@RequiredArgsConstructor
@Service
public class AnswerConsumerImpl implements Consume {

    private final UpdateProcessor updateProcessor;



    @Override
    @RabbitListener(queues = "${spring.rabbitmq.queues.answer-message}")
    public void consume(SendMessage sendMessage) {

        updateProcessor.setView(sendMessage);
    }
}
