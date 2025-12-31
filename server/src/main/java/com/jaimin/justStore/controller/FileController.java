package com.jaimin.justStore.controller;

import com.jaimin.justStore.dto.FileDetailResponseDto;
import com.jaimin.justStore.dto.FileSearchResponseDto;
import com.jaimin.justStore.dto.UploadFileRequestDto;
import com.jaimin.justStore.service.FileService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public String isWorking() {
        return "Yes, I am working";
    }

    /**
     * Get all files (returns user-friendly search response DTOs).
     */
    @GetMapping("/files")
    public ResponseEntity<List<FileSearchResponseDto>> getAllFiles() {
        List<FileSearchResponseDto> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    /**
     * Search files with optional filters.
     */
    @GetMapping("/files/search")
    public ResponseEntity<List<FileSearchResponseDto>> searchFiles(
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<FileSearchResponseDto> files = fileService.searchFiles(fileName, tag, startDate, endDate);
        return ResponseEntity.ok(files);
    }

    /**
     * Get full file details by ID (includes YouTube info).
     */
    @GetMapping("/files/{id}")
    public ResponseEntity<FileDetailResponseDto> getFileById(@PathVariable Long id) {
        FileDetailResponseDto file = fileService.getFileById(id);
        return ResponseEntity.ok(file);
    }

    /**
     * Get full file details by YouTube Video ID (includes YouTube info).
     */
    @GetMapping("/files/youtube/{videoId}")
    public ResponseEntity<FileDetailResponseDto> getFileByYoutubeId(@PathVariable String videoId) {
        FileDetailResponseDto file = fileService.getFileByYoutubeVideoId(videoId);
        return ResponseEntity.ok(file);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @ModelAttribute UploadFileRequestDto uploadRequest
    ) {

        try {
            return fileService.uploadFile(uploadRequest);
        } catch (IOException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getLocalizedMessage()));
        }
    }

    /**
     * Get original file bytes by video path (for download).
     */
    @GetMapping("/file")
    public ResponseEntity<?> getFile(
            //TODO: replace with DTO
            @RequestParam
            String videoPath //this is temp and for development
    ){
        try{
            return fileService.getFile(videoPath);
        }catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getLocalizedMessage()));
        }
    }

}

