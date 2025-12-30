package com.jaimin.justStore.service;

import com.jaimin.justStore.dto.UploadFileRequestDto;
import com.jaimin.justStore.model.File;
import com.jaimin.justStore.repository.FileRepository;
import com.jaimin.justStore.utils.ChecksumUtil;
import com.jaimin.justStore.utils.CreateVideoUtil;
import com.jaimin.justStore.utils.HashUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    public ResponseEntity<?> uploadFile(UploadFileRequestDto uploadRequest) throws IOException {
        if(uploadRequest.file().isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File bhejna sale ! (Please add file)"
            );
        }

        String originalFileName = uploadRequest.file().getOriginalFilename();
        Long originalFileSizeInByte = uploadRequest.file().getSize();
        String originalFileType = uploadRequest.file().getContentType();

        File newFile = new File(originalFileName, originalFileSizeInByte, originalFileType, uploadRequest.tags());

        if(uploadRequest.secretKey() != null){
            String secretKeyHash = HashUtil.hash(uploadRequest.secretKey());
            newFile.setSecretKeyHash(secretKeyHash);
        }

        byte[] fileBytes = uploadRequest.file().getBytes();

        String fileChecksum = ChecksumUtil.calculateChecksum(fileBytes);
        newFile.setFileChecksum(fileChecksum);

        fileRepository.save(newFile);

        //Encryption if secret key is given
        if(uploadRequest.secretKey() != null){
            //TODO: encryption
            //newFile = EncruptionFuction
            System.out.println("File Encryption need to implemented");
        }

        //Time to create video
        final int width = 1920;
        final int frameRate = 24;
        final int height = 1072;
        byte[] videoBytes = CreateVideoUtil.createVideo(fileBytes, width, height, frameRate);


        //just checking
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(videoBytes);

    }
}
