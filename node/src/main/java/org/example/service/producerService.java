package org.example.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface producerService {

    void produceranswer(SendMessage sendMessage);
}
