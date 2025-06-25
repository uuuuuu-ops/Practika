package org.example.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ConsumerService {

    void consumetextservice(Update update);

    void consumedocservice(Update update);

    void consumephotoservice(Update update);
}
