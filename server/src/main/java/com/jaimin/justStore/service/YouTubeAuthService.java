package com.jaimin.justStore.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.jaimin.justStore.model.OAuthToken;
import com.jaimin.justStore.repository.OAuthTokenRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Service
public class YouTubeAuthService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeAuthService.class);

    public static final String PROVIDER_YOUTUBE = "youtube";

    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/youtube.upload"
    );

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final OAuthTokenRepository tokenRepository;
    private final ResourceLoader resourceLoader;

    @Value("${youtube.client-secret-file:classpath:client_secret_1004670353314-q5n1627pcmtiv1fdsk3foqo860h5ir42.apps.googleusercontent.com.json}")
    private String clientSecretFile;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private GoogleClientSecrets clientSecrets;
    private NetHttpTransport httpTransport;

    public YouTubeAuthService(OAuthTokenRepository tokenRepository, ResourceLoader resourceLoader) {
        this.tokenRepository = tokenRepository;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Resource resource = resourceLoader.getResource(clientSecretFile);
            this.clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(resource.getInputStream())
            );
            logger.info("YouTube Auth Service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize YouTube Auth Service: {}", e.getMessage());
        }
    }

    /**
     * Generate the Google OAuth authorization URL.
     */
    public String getAuthorizationUrl() {
        GoogleAuthorizationCodeFlow flow = buildFlow();
        return flow.newAuthorizationUrl()
                .setRedirectUri(getRedirectUri())
                .setAccessType("offline")
                .setApprovalPrompt("force") // Force to get refresh token
                .build();
    }

    /**
     * Exchange authorization code for tokens and store in database.
     */
    @Transactional
    public OAuthToken exchangeCodeForTokens(String authorizationCode) throws IOException {
        GoogleAuthorizationCodeFlow flow = buildFlow();

        GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(getRedirectUri())
                .execute();

        // Delete existing token if any
        tokenRepository.findByProvider(PROVIDER_YOUTUBE)
                .ifPresent(token -> tokenRepository.delete(token));

        // Save new token
        OAuthToken oAuthToken = new OAuthToken(
                PROVIDER_YOUTUBE,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresInSeconds()
        );

        OAuthToken savedToken = tokenRepository.save(oAuthToken);
        logger.info("YouTube OAuth tokens saved successfully");
        return savedToken;
    }

    /**
     * Get stored token, refresh if expired.
     */
    @Transactional
    public Optional<OAuthToken> getValidToken() {
        Optional<OAuthToken> tokenOpt = tokenRepository.findByProvider(PROVIDER_YOUTUBE);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        OAuthToken token = tokenOpt.get();

        // If token is expired or about to expire (within 5 minutes), refresh it
        if (token.isExpired() || isAboutToExpire(token)) {
            try {
                return Optional.of(refreshToken(token));
            } catch (IOException e) {
                logger.error("Failed to refresh token: {}", e.getMessage());
                return Optional.empty();
            }
        }

        return Optional.of(token);
    }

    /**
     * Refresh the access token using refresh token.
     */
    @Transactional
    public OAuthToken refreshToken(OAuthToken token) throws IOException {
        if (token.getRefreshToken() == null) {
            throw new IOException("No refresh token available");
        }

        GoogleAuthorizationCodeFlow flow = buildFlow();

        TokenResponse tokenResponse = flow.newTokenRequest(token.getRefreshToken())
                .setGrantType("refresh_token")
                .execute();

        token.setAccessToken(tokenResponse.getAccessToken());
        if (tokenResponse.getRefreshToken() != null) {
            token.setRefreshToken(tokenResponse.getRefreshToken());
        }
        token.setExpiresInSeconds(tokenResponse.getExpiresInSeconds());

        OAuthToken savedToken = tokenRepository.save(token);
        logger.info("YouTube OAuth token refreshed successfully");
        return savedToken;
    }

    /**
     * Check if user is authenticated with YouTube.
     */
    public boolean isAuthenticated() {
        return getValidToken().isPresent();
    }

    /**
     * Revoke and delete the stored token.
     */
    @Transactional
    public void revokeToken() {
        tokenRepository.findByProvider(PROVIDER_YOUTUBE)
                .ifPresent(token -> {
                    tokenRepository.delete(token);
                    logger.info("YouTube OAuth token revoked");
                });
    }

    /**
     * Get the access token string for API calls.
     */
    public String getAccessToken() {
        return getValidToken()
                .map(OAuthToken::getAccessToken)
                .orElse(null);
    }

    public NetHttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        if (httpTransport == null) {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        }
        return httpTransport;
    }

    public JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }

    private GoogleAuthorizationCodeFlow buildFlow() {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();
    }

    private String getRedirectUri() {
        return baseUrl + "/auth/youtube/callback";
    }

    private boolean isAboutToExpire(OAuthToken token) {
        if (token.getExpiresAt() == null) {
            return false;
        }
        // Consider expired if within 5 minutes of expiration
        return token.getExpiresAt().minusMinutes(5).isBefore(java.time.LocalDateTime.now());
    }
}
