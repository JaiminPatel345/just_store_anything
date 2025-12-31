package com.jaimin.justStore.service;

import com.jaimin.justStore.dto.UploadFileRequestDto;
import com.jaimin.justStore.enums.Status;
import com.jaimin.justStore.model.File;
import com.jaimin.justStore.repository.FileRepository;
import com.jaimin.justStore.utils.ChecksumUtil;
import com.jaimin.justStore.utils.CreateVideoUtil;
import com.jaimin.justStore.utils.HashUtil;
import com.jaimin.justStore.utils.RetrieveVideo;
import com.jaimin.justStore.utils.YouTubeApi;
import org.jcodec.api.JCodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final YouTubeAuthService youTubeAuthService;

    public FileService(FileRepository fileRepository, YouTubeAuthService youTubeAuthService) {
        this.fileRepository = fileRepository;
        this.youTubeAuthService = youTubeAuthService;
    }

    public ResponseEntity<?> uploadFile(UploadFileRequestDto uploadRequest) throws IOException {
        // Check if authenticated with YouTube
        if (!youTubeAuthService.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please authenticate with YouTube first. Visit /auth/youtube/login"
            );
        }

        if (uploadRequest.file().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File bhejna sale ! (Please add file)"
            );
        }

        String originalFileName = uploadRequest.file().getOriginalFilename();
        Long originalFileSizeInByte = uploadRequest.file().getSize();
        String originalFileType = uploadRequest.file().getContentType();

        File newFile = new File(originalFileName, originalFileSizeInByte, originalFileType, uploadRequest.tags());

        if (uploadRequest.secretKey() != null) {
            String secretKeyHash = HashUtil.hash(uploadRequest.secretKey());
            newFile.setSecretKeyHash(secretKeyHash);
        }

        byte[] fileBytes = uploadRequest.file().getBytes();

        String fileChecksum = ChecksumUtil.calculateChecksum(fileBytes);
        newFile.setFileChecksum(fileChecksum);

        // Save file with PENDING status initially
        newFile = fileRepository.save(newFile);
        logger.info("File record created with ID: {}, Status: PENDING", newFile.getId());

        // Encryption if secret key is given
        if (uploadRequest.secretKey() != null) {
            // TODO: encryption
            logger.info("File Encryption need to be implemented");
        }

        // Time to create video
        final int width = 1920;
        final int frameRate = 24;
        final int height = 1072;
        final String tempOutputPath = "/tmp/jaimin_" + newFile.getId() + ".mp4";

        try {
            logger.info("Creating video from file bytes...");
            CreateVideoUtil.createVideo(fileBytes, width, height, frameRate, tempOutputPath);
            logger.info("Video created successfully at: {}", tempOutputPath);

            // Get access token from auth service
            String accessToken = youTubeAuthService.getAccessToken();
            if (accessToken == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "YouTube access token not available. Please re-authenticate."
                );
            }

            // Create YouTubeApi instance with access token
            YouTubeApi youTubeApi = new YouTubeApi(
                    youTubeAuthService.getHttpTransport(),
                    accessToken
            );

            // Upload to YouTube
            String videoTitle = "JustStore_" + newFile.getId() + "_" + originalFileName;
            logger.info("Uploading video to YouTube with title: {}", videoTitle);

            YouTubeApi.YouTubeUploadResult uploadResult = youTubeApi.uploadVideo(
                    tempOutputPath,
                    videoTitle,
                    uploadRequest.tags()
            );

            // Update file record with YouTube info
            newFile.setYoutubeVideoId(uploadResult.videoId());
            newFile.setYoutubeVideoUrl(uploadResult.videoUrl());
            newFile.setStatus(Status.UPLOADED);
            fileRepository.save(newFile);

            logger.info("File uploaded successfully! YouTube Video ID: {}", uploadResult.videoId());

            // Clean up temp file
            java.io.File tempFile = new java.io.File(tempOutputPath);
            if (tempFile.exists() && tempFile.delete()) {
                logger.info("Temporary video file deleted: {}", tempOutputPath);
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "File uploaded successfully",
                            "fileId", newFile.getId(),
                            "youtubeVideoId", uploadResult.videoId(),
                            "youtubeVideoUrl", uploadResult.videoUrl()
                    ));

        } catch (GeneralSecurityException e) {
            logger.error("YouTube authentication error: {}", e.getMessage());
            newFile.setStatus(Status.FAILED);
            fileRepository.save(newFile);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "YouTube authentication failed: " + e.getMessage()
            );
        } catch (IOException e) {
            logger.error("Error during upload: {}", e.getMessage());
            newFile.setStatus(Status.FAILED);
            fileRepository.save(newFile);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload failed: " + e.getMessage()
            );
        }
    }

    public ResponseEntity<?> getFile(String videoPath) throws IOException, JCodecException {
        if (videoPath == null || videoPath.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Give correct input video path"
            );
        }

        if (!videoPath.endsWith(".mp4")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Input video path must end with .mp4"
            );
        }

        byte[] fileBytes = RetrieveVideo.decodeVideo(videoPath);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fileBytes);
    }
}
