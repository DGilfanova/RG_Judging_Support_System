package ru.itis.rgjudge.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.rgjudge.dto.DefaultResponseDto;
import ru.itis.rgjudge.dto.PoseResponse;

@FeignClient(name = "poseEstimatorServiceClient", url = "${integration.pose-estimator.url}")
public interface PoseEstimatorClient {

    @PostMapping(value = "/detect-pose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    DefaultResponseDto<PoseResponse> detectPose(@RequestPart MultipartFile video);
}
