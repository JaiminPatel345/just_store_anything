package com.jaimin.justStore.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for full file details - includes all information including YouTube data.
 * Used when user requests to view/download a specific file.
 */
public record FileDetailResponseDto(
        Long id,
        String originalFileName,
        String originalFileSizeFormatted,
        Long originalFileSizeInByte,
        String originalFileType,
        Set<String> tags,
        String youtubeVideoId,
        String youtubeVideoUrl,
        String status,
        boolean isEncrypted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
