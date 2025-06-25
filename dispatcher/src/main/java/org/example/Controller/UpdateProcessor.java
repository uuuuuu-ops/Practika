package org.example.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.example.configuration.RabbitmqConfiguration;
import org.example.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.example.service.UpdateProducer;

import static org.example.model.RabbitQueue.*;

@RequiredArgsConstructor
@Component
@Log4j
public class UpdateProcessor {

    private TelegramBot telegramBot;

    private final MessageUtils messageUtils;

    private final UpdateProducer updateProducer;

    private final RabbitmqConfiguration rabbitmqConfiguration;
    public void registerBot(TelegramBot telegramBot){

        this.telegramBot = telegramBot;
     }

     public void proccesupdate(Update update){
         if(update == null){
             log.error("update dont received");
             return;


         }

         if(update.hasMessage()){
            distributeMessagesByType(update);
         }
         else{
             log.error("Received unsuppported message type :"+ update);
         }
     }

     public void distributeMessagesByType(Update update){
         var message = update.getMessage();
         if(message.hasText()){
             proccesTextMessage(update);

         } else if (message.hasDocument()) {

             processdocmessage(update);
         } else if (message.hasPhoto()) {

             processPhotoMessages(update);
         }else{
             unsapportedType(update);
         }
     }

    private void unsapportedType(Update update) {
        var sendmessage = messageUtils.generatedanswer(update,
                "Unsapported Message");
        setView(sendmessage);
    }

    public void setView(SendMessage sendmessage) {
        telegramBot.sendAnswerMessage(sendmessage);
    }

    private void setfilereceivedview(Update update) {
        var sendmessage = messageUtils.generatedanswer(update,"File is received. Wait ......");
        setView(sendmessage);
    }

    private void processPhotoMessages(Update update) {
        updateProducer.produce(rabbitmqConfiguration.getPhotoMessageUpdateQueue(),update);
        setfilereceivedview(update);
    }


    private void proccesTextMessage(Update update) {

        updateProducer.produce(rabbitmqConfiguration.getTextMessageUpdateQueue(),update);
    }

    private void processdocmessage(Update update) {
        updateProducer.produce(rabbitmqConfiguration.getDocMessageUpdateQueue(),update);
        setfilereceivedview(update);
    }

}
