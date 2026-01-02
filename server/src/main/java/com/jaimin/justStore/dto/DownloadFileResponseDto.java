package com.jaimin.justStore.dto;

import com.jaimin.justStore.model.File;

public record DownloadFileResponseDto(
        Long videoId,
        String originalFileName,
        Long originalFileSizeInByte,
        String originalFileType,
        String youtubeVideoUrl,
        byte[] fileContent
) {
    public static DownloadFileResponseDto from(File file, byte[] fileContent){
        return new DownloadFileResponseDto(
                file.getId(),
                file.getOriginalFileName(),
                file.getOriginalFileSizeInByte(),
                file.getOriginalFileType(),
                file.getYoutubeVideoUrl(),
                fileContent
        );
    }
}
