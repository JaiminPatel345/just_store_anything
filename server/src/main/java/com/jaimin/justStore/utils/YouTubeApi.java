package com.jaimin.justStore.utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * YouTube API Utility for uploading videos to YouTube.
 * This is a pure utility class - no Spring annotations, no DB access.
 * All credentials must be passed in from the service layer.
 */
public class YouTubeApi {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeApi.class);

    private static final String APPLICATION_NAME = "JustStore - File Storage";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final YouTube youtubeService;

    /**
     * Create YouTubeApi with an access token.
     *
     * @param httpTransport The HTTP transport to use.
     * @param accessToken   The OAuth access token.
     */
    public YouTubeApi(NetHttpTransport httpTransport, String accessToken) {
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        this.youtubeService = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Upload a video to YouTube.
     *
     * @param videoFilePath Path to the video file to upload.
     * @param title         Title for the YouTube video.
     * @param description   Description for the YouTube video.
     * @param tags          Tags for the YouTube video.
     * @return YouTubeUploadResult containing video ID and URL.
     * @throws IOException If there is an I/O error during upload.
     */
    public YouTubeUploadResult uploadVideo(String videoFilePath, String title, String description, Set<String> tags)
            throws IOException {

        logger.info("Starting YouTube upload for file: {}", videoFilePath);

        // Define the Video object
        Video video = new Video();

        // Set video snippet (metadata)
        VideoSnippet snippet = new VideoSnippet();
        snippet.setCategoryId("22"); // Category 22 = People & Blogs
        snippet.setTitle(title);
        snippet.setDescription(description);
        if (tags != null && !tags.isEmpty()) {
            snippet.setTags(tags.stream().toList());
        }
        video.setSnippet(snippet);

        // Set video status (unlisted for privacy)
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("unlisted");
        video.setStatus(status);

        // Prepare the video file for upload
        File mediaFile = new File(videoFilePath);
        if (!mediaFile.exists()) {
            throw new IOException("Video file not found: " + videoFilePath);
        }

        InputStreamContent mediaContent = new InputStreamContent(
                "video/*",
                new BufferedInputStream(new FileInputStream(mediaFile))
        );
        mediaContent.setLength(mediaFile.length());

        // Execute the upload
        logger.info("Uploading video to YouTube... File size: {} bytes", mediaFile.length());
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(List.of("snippet", "status"), video, mediaContent);

        Video response = request
                .setNotifySubscribers(false)
                .execute();

        String videoId = response.getId();
        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

        logger.info("Video uploaded successfully! Video ID: {}, URL: {}", videoId, videoUrl);

        return new YouTubeUploadResult(videoId, videoUrl);
    }

    /**
     * Upload a video to YouTube with default description.
     *
     * @param videoFilePath Path to the video file to upload.
     * @param title         Title for the YouTube video.
     * @param tags          Tags for the YouTube video.
     * @return YouTubeUploadResult containing video ID and URL.
     * @throws IOException If there is an I/O error during upload.
     */
    public YouTubeUploadResult uploadVideo(String videoFilePath, String title, Set<String> tags)
            throws IOException {
        return uploadVideo(videoFilePath, title, "Uploaded by JustStore - Secure file storage on YouTube", tags);
    }

    /**
     * Result object for YouTube upload operation.
     */
    public record YouTubeUploadResult(String videoId, String videoUrl) {
    }
}