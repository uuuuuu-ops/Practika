package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import org.example.dao.AppDocumentDao;
import org.example.dao.AppPhotoDao;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.service.FileService;
import org.springframework.stereotype.Service;

import org.example.utils.Decoder;

@RequiredArgsConstructor
@Log4j
@Service
public class FileServiceImpl implements FileService {

    private final AppDocumentDao appDocumentDao;

    private final AppPhotoDao appPhotoDao;

    private final Decoder decoder;



    @Override
    public AppDocument getDocument(String hash) {

        var id = decoder.idOf(hash);
        if(id == null){
            return null;
        }
        return appDocumentDao.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hash) {

        var id = decoder.idOf(hash);
        if(id == null){
            return null;
        }
        return appPhotoDao.findById(id).orElse(null);
    }


}
