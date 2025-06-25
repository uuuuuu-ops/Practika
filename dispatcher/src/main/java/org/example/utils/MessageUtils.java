package org.example.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
@Component
public class MessageUtils {

    public  SendMessage generatedanswer(Update update , String text){
        var message = update.getMessage();
        var sendmessage = new SendMessage();
        sendmessage.setChatId(message.getChatId().toString());
        sendmessage.setText(text);
        return sendmessage;
    }
}
