package ru.itis.rgjudge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.itis.rgjudge.config.properties.PenaltyProperties;
import ru.itis.rgjudge.config.properties.RulesProperties;
import ru.itis.rgjudge.config.properties.VideoFileProperties;

@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties({VideoFileProperties.class, PenaltyProperties.class, RulesProperties.class})
public class RgJudgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RgJudgeApplication.class, args);
    }
}
