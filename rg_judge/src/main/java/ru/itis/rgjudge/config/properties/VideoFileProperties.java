package ru.itis.rgjudge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("video")
public record VideoFileProperties(
        int maxSize,
        int minFps
) {
}
