package com.jaimin.justStore.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for search results - contains only user-friendly information.
 * Does not expose technical details like YouTube URLs.
 */
public record FileSearchResponseDto(
        Long id,
        String originalFileName,
        String originalFileSizeFormatted,  // Formatted like "2.5 MB"
        Long originalFileSizeInByte,
        String originalFileType,
        Set<String> tags,
        String status,
        LocalDateTime createdAt
) {
    /**
     * Helper method to format file size into human-readable format.
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
