package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.example.dao.AppDocumentDao;
import org.example.dao.AppPhotoDao;
import org.example.dao.BinaryContentDao;
import org.example.entity.AppDocument;
import org.example.entity.BinaryContent;
import org.example.entity.AppPhoto;
import org.example.exceptions.UploadFileException;
import org.example.service.FileService;
import org.example.service.enums.LinkType;
import org.hashids.Hashids;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Log4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${token}")
    private String token;

    @Value("${service.file_info.uri}")
    private String fileInfouri;

    @Value("${service.file_storage.uri}")
    private String fileStorageuri;

    @Value("${link.address}")
    private String LinkAddress;

    private AppDocumentDao appDocumentDao;

    private AppPhotoDao appPhotoDao;

    private BinaryContentDao binaryContentDao;

    private final Hashids hashids;

    @Override
    public AppDocument processDocument(Message telegramMessage) {
        var telegramDoc = telegramMessage.getDocument();
        var fileId = telegramDoc.getFileId();
        var response = getFilePath(fileId);

        if(response.getStatusCode() == HttpStatus.OK){
            var persistentBinaryContent = getPersistentBinaryContent(response);
            var transientAppDoc = buildTransientAppDoc(telegramDoc,persistentBinaryContent);
            return appDocumentDao.save(transientAppDoc);
        }else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }
    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;
        var telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        var fileId = telegramPhoto.getFileId();
        var response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            var persistentBinaryContent = getPersistentBinaryContent(response);
            var transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDao.save(transientAppPhoto);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }



    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        var filepath = getFilePath(response);
        var fileinByte = downloadfile(filepath);
        var transientBinarycontent = BinaryContent.builder()
                .fileAsArrayofBytes(fileinByte)
                .build();
        return  binaryContentDao.save(transientBinarycontent);
    }

    private static String getFilePath(ResponseEntity<String> response) {
        var jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path")
        );
    }



    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .TelegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .TelegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private byte[] downloadfile(String filepath) {
        var fulluri = fileStorageuri.replace("{token}",token)
                .replace("{filepath}",filepath);
        URL urlobject = null;

        try {
            urlobject = new URL(fulluri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }


        try (InputStream is = urlobject.openStream()){
            return  is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlobject.toExternalForm(),e);
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        var restTemplate= new RestTemplate();
        var headers = new HttpHeaders();
        var request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfouri,
                HttpMethod.GET,
                request,
                String.class,
                token,fileId


        );


    }
    @Override
    public String generatelink(Long docId, LinkType linkType) {
        var hash = hashids.encode(docId);
        return "http://" + LinkAddress + "/" + linkType + "?id=" + hash;
    }

}
