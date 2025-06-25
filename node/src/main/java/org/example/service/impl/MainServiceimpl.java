package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDao;
import org.example.dao.Rawdatadao;
import org.example.entity.AppDocument;
import org.example.entity.AppUser;
import org.example.entity.RawData;
import org.example.entity.AppPhoto;
import org.example.exceptions.UploadFileException;
import org.example.service.AppUserService;
import org.example.service.FileService;
import org.example.service.MainService;
import org.example.service.enums.LinkType;
import org.example.service.enums.ServiceCommands;
import org.example.service.producerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.transaction.Transactional;

import static org.example.enums.UserState.BASIC_STATE;
import static org.example.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static org.example.service.enums.ServiceCommands.CANCEL;
import static org.example.service.enums.ServiceCommands.HELP;
import static org.example.service.enums.ServiceCommands.REGISTRATION;
import static org.example.service.enums.ServiceCommands.START;

@RequiredArgsConstructor
@Service
@Log4j
public class MainServiceimpl implements MainService {

    private final Rawdatadao rawdatadao;

    private final producerService producerservice;

    private final AppUserDao appUserDao;

    private final FileService fileService;

    private final AppUserService appUserService;
    @Transactional
    @Override
    public void proccesTextMessage(Update update) {
        saveRawData(update);
        var appUser = FindorSaveAppUser(update);
        var userState = appUser.getUserState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommands.fromValue(text);

        if(CANCEL.equals(serviceCommand)){
            output = cancelProcess(appUser);

        }else if(BASIC_STATE.equals(userState)){
            output = proccesServiceCommand(appUser,text);

        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser , text);

        }else {
            log.error("Unknown userstate :"+userState);
            output = "Unknown user state enter /cancel and do it again";
        }
        var ChatID = update.getMessage().getChatId();
        sendanswer(output,ChatID);

    }

    @Override
    public void proccesDOCMessage(Update update) {

        saveRawData(update);
        var appUser = FindorSaveAppUser(update);
        var ChatID = update.getMessage().getChatId();
        if(isNotAllowedtodownloadcontent(ChatID,appUser)){
            return ;
        }


        try {
            AppDocument document = fileService.processDocument(update.getMessage());
            String link = fileService.generatelink(document.getId(), LinkType.Get_DOC);
            var answer = "Document succesfully uploaded " + "Link : " + link;
            sendanswer(answer, ChatID);
        }catch (UploadFileException ex){
            log.error(ex);
            String error = "Sorry something went with doc wrong try again!";
            sendanswer(error,ChatID);
        }

    }
    @Override
    public void proccesPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = FindorSaveAppUser(update);
        var ChatID = update.getMessage().getChatId();
        if(isNotAllowedtodownloadcontent(ChatID,appUser)){
            return ;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generatelink(photo.getId(), LinkType.Get_PHOTO);

            var answer = "photo is succesfully uploaded " + "Link :" + link;

            sendanswer(answer,ChatID);
        }catch (UploadFileException ex){
            log.error(ex);
            String error = "Sorry something went with photo wrong try again!";
            sendanswer(error,ChatID);
        }
    }
    private boolean isNotAllowedtodownloadcontent(Long chatID, AppUser appUser) {
        var userState = appUser.getUserState();
        if(!appUser.isActive()){
            var error = "Register or sign in to your account for downlaoding content.";
            sendanswer(error,chatID);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Cancel this command with /cancel and try again";
            sendanswer(error,chatID);
            return true;
        }
        return false;
    }



    private void sendanswer(String output, Long chatID) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText(output);
        producerservice.produceranswer(sendMessage);
    }

    private String proccesServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommands.fromValue(cmd);
    if(REGISTRATION.equals(serviceCommand)){
        return appUserService.registerUser(appUser);

    }else if(HELP.equals(serviceCommand)){
        return help();

    } else if (START.equals(serviceCommand)) {
        return "Hello click /help to see commands";

    }else{
        return "Unknown command click /help to see commands";

    }
    }

    private String help() {
        return "COMMANDS:\n"
                +"/cancel - cancel current command\n"
                +"/registration - register user";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Command canceled";
    }

    private AppUser FindorSaveAppUser( Update update){
        var telegramUser = update.getMessage().getFrom();
        var AppUseropt = appUserDao.findByTelegramUserId(telegramUser.getId());
        if(AppUseropt.isEmpty()){
            AppUser desient = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .userState(BASIC_STATE)
                    .build();
            return appUserDao.save(desient);
        }
        return AppUseropt.get();
    }

    private void saveRawData(Update update) {
        var rawdata = RawData.builder()
                .event(update)
                .build();
        rawdatadao.save(rawdata);
    }
}
