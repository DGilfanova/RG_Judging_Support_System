package ru.itis.rgjudge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@RequiredArgsConstructor
public class PoseResponse {

    @JsonProperty("video_link")
    private String videoLink;

    @JsonProperty("pose_data")
    private List<PoseData> poseData;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("fps")
    private Integer fps;

    @JsonProperty("duration")
    private Double duration;

    @JsonProperty("body_parts")
    private List<String> bodyParts;

    @Data
    @Builder
    public static class PoseData {

        @JsonProperty("time")
        private Double time;

        @JsonProperty("coordinates")
        private List<Coordinate> coordinates;
    }

    @Data
    @Builder
    public static class Coordinate {

        @JsonProperty("x")
        private Double x;

        @JsonProperty("y")
        private Double y;

        @JsonProperty("z")
        private Double z;
    }
}
