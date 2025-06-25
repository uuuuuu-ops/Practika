package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.hashids.Hashids;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.example.dao.AppUserDao;
import org.example.entity.AppUser;
import org.example.service.AppUserService;


import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static org.example.enums.UserState.BASIC_STATE;
import static org.example.enums.UserState.WAIT_FOR_EMAIL_STATE;

@RequiredArgsConstructor
@Log4j
@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserDao appUserDao;

    private final Hashids hashids;

    @Value("${spring.rabbitmq.queues.registration-mail}")
    private String registrationMailQueue;


    private final RabbitTemplate rabbitTemplate;



    @Override
    public String registerUser(AppUser appUser) {
        if(appUser.isActive()){
            return "You are already regisered";

        } else if (appUser.getEmail() !=null) {

            return "We send mail to you"+
                    "Go to Link";
        }
        appUser.setUserState(WAIT_FOR_EMAIL_STATE);
        appUserDao.save(appUser);

        return "Write your email please:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            var emailAddr = new InternetAddress(email);
            emailAddr.validate();
        }catch (AddressException e){
            return "Please enter correct email. Enter command /cancel and try again";
        }

        var appUserOpt = appUserDao.findByEmail(email);
        if(appUserOpt.isEmpty()){
            appUser.setEmail(email);
            appUser.setUserState(BASIC_STATE);
            appUser = appUserDao.save(appUser);

            var cryptoUserId = hashids.encode(appUser.getId());
            sendRegistrationMail(cryptoUserId, email);

            return "We send email to you";
        }else {
            return "This email is registered ";
        }
    }

    private void sendRegistrationMail(String cryptoUserId, String email) {
        var MailParams = org.example.dto.MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        rabbitTemplate.convertAndSend(registrationMailQueue, MailParams);
    }


}
