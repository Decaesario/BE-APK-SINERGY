package com.impal.gabungyuk.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public String normalizeProfilePictureUrl(String profilePicture) {
        if (profilePicture == null || profilePicture.isBlank()) {
            return null;
        }

        String trimmed = profilePicture.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        String base = (appBaseUrl != null && !appBaseUrl.isBlank())
                ? appBaseUrl.stripTrailing().replaceAll("/+$", "")
                : "";

        if (trimmed.startsWith("/")) {
            return base + trimmed;
        }

        return base + "/" + trimmed;
    }
}
