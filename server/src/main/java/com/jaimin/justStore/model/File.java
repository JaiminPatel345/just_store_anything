package com.jaimin.justStore.model;

import com.jaimin.justStore.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_youtube_video_id", columnList = "youtubeVideoId"),
        @Index(name = "idx_youtube_video_url", columnList = "youtubeVideoURL"),
        /* Easy to search file */
        @Index(name = "idx_file_name", columnList = "originalFileName"),
        @Index(name = "idx_file_type", columnList = "originalFileType"),
        @Index(name = "idx_file_size", columnList = "originalFileSizeInByte"),
        @Index(name = "idx_type_size", columnList = "originalFileType, originalFileSizeInByte"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Metadata */
    @NotBlank(message = "File name can't be blank")
    @Column(length = 255)
    private String originalFileName;
    @Min(value = 1, message = "File size must be greater than 0")
    private Long originalFileSizeInByte; //in kb
    @Column(length = 100)
    private String originalFileType;

    @ElementCollection
    @CollectionTable(name = "file_tags", joinColumns = @JoinColumn(name = "file_id"))
    @Column(name = "tag")
    private Set<String> tags;

    @Column(nullable = true, length = 64) //It is optional, base on user
    private String secretKeyHash;

    @Column(nullable = false, updatable = false)
    private String fileChecksum; // SHA-256 hash for integrity verification


    /*    YouTube      */
    @Column(unique = true)
    private String youtubeVideoId;
    private  String youtubeVideoUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    protected File() {
    }

    public File(String originalFileName, Long originalFileSizeInByte, String originalFileType, Set<String> tags) {
        this.originalFileName = originalFileName;
        this.originalFileSizeInByte = originalFileSizeInByte;
        this.originalFileType = originalFileType;
        this.tags = tags;

    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getYoutubeVideoUrl() {
        return youtubeVideoUrl;
    }

    public void setYoutubeVideoUrl(String youtubeVideoURL) {
        this.youtubeVideoUrl = youtubeVideoURL;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getSecretKeyHash() {
        return secretKeyHash;
    }

    public void setSecretKeyHash(String secretKeyHash) {
        this.secretKeyHash = secretKeyHash;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getOriginalFileType() {
        return originalFileType;
    }

    public void setOriginalFileType(String originalFileType) {
        this.originalFileType = originalFileType;
    }

    public Long getOriginalFileSizeInByte() {
        return originalFileSizeInByte;
    }

    public void setOriginalFileSizeInByte(Long originalFileSize) {
        this.originalFileSizeInByte = originalFileSize;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public Long getId() {
        return id;
    }
}

