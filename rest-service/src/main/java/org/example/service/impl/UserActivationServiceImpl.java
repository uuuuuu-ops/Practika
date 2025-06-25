package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDao;
import org.example.service.UserActivationService;
import org.example.utils.Decoder;
import org.springframework.stereotype.Service;

@Log4j
@RequiredArgsConstructor
@Service
public class UserActivationServiceImpl implements UserActivationService {

    private final Decoder decoder;

    private final AppUserDao appUserDao;

    @Override
    public boolean activation(String cryptoUserId) {

        var userId = decoder.idOf(cryptoUserId);
        log.debug(String.format("User activation with user-id=%s", userId));
        if (userId == null) {
            return false;
        }

        var optional = appUserDao.findById(userId);
        if(optional.isPresent()){
            var user = optional.get();
            user.setActive(true);
            appUserDao.save(user);
            return true;
        }
        return false;
    }
}
