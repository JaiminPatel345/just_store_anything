package com.jaimin.justStore.controller;

import com.jaimin.justStore.service.YouTubeAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final YouTubeAuthService youTubeAuthService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(YouTubeAuthService youTubeAuthService) {
        this.youTubeAuthService = youTubeAuthService;
    }

    /**
     * Check if user is authenticated with YouTube.
     */
    @GetMapping("/youtube/status")
    public ResponseEntity<?> getAuthStatus() {
        boolean isAuthenticated = youTubeAuthService.isAuthenticated();
        return ResponseEntity.ok(Map.of(
                "authenticated", isAuthenticated,
                "provider", "youtube"
        ));
    }

    /**
     * Get the Google OAuth authorization URL.
     * Frontend should redirect user to this URL.
     */
    @GetMapping("/youtube/login")
    public ResponseEntity<?> getAuthUrl() {
        String authUrl = youTubeAuthService.getAuthorizationUrl();
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    /**
     * Redirect endpoint - redirects directly to Google OAuth.
     * Use this if you want backend to handle the redirect.
     */
    @GetMapping("/youtube/redirect")
    public ResponseEntity<?> redirectToGoogle() {
        String authUrl = youTubeAuthService.getAuthorizationUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", authUrl)
                .build();
    }

    /**
     * OAuth callback - Google redirects here after user authorization.
     */
    @GetMapping("/youtube/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error
    ) {
        if (error != null) {
            logger.error("OAuth error: {}", error);
            // Redirect to frontend with error
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "?auth=error&message=" + error)
                    .build();
        }

        if (code == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No authorization code received"));
        }

        try {
            youTubeAuthService.exchangeCodeForTokens(code);
            logger.info("YouTube OAuth successful");
            
            // Redirect to frontend with success
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "?auth=success")
                    .build();
        } catch (Exception e) {
            logger.error("Failed to exchange code for tokens: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "?auth=error&message=token_exchange_failed")
                    .build();
        }
    }

    /**
     * Logout - revoke and delete the stored token.
     */
    @PostMapping("/youtube/logout")
    public ResponseEntity<?> logout() {
        youTubeAuthService.revokeToken();
        return ResponseEntity.ok(Map.of(
                "message", "Successfully logged out from YouTube",
                "authenticated", false
        ));
    }
}
