package org.example.service;

import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {

    AppDocument processDocument(Message telegrammesage);

    AppPhoto processPhoto(Message telegrammessage);

    String generatelink(Long docid, LinkType linkType);
}
