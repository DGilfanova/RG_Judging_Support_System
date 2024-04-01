package ru.itis.rgjudge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rules")
public record RulesProperties(
        Double balanceFixationDuration
) {
}
