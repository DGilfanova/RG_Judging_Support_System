package ru.itis.rgjudge.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.rgjudge.config.properties.VideoFileProperties;
import ru.itis.rgjudge.db.enums.EstimationType;
import ru.itis.rgjudge.db.repository.ElementRepository;
import ru.itis.rgjudge.dto.DefaultResponseDto;
import ru.itis.rgjudge.dto.ElementReport;
import ru.itis.rgjudge.dto.PoseResponse;
import ru.itis.rgjudge.dto.enums.BodyPart;
import ru.itis.rgjudge.dto.internal.EstimatorResponse;
import ru.itis.rgjudge.dto.internal.ReportData;
import ru.itis.rgjudge.exception.ElementEvaluationException;
import ru.itis.rgjudge.exception.ElementNotFoundException;
import ru.itis.rgjudge.client.PoseEstimatorClient;
import ru.itis.rgjudge.service.ElementEvaluationService;
import ru.itis.rgjudge.service.ReportService;
import ru.itis.rgjudge.service.estimator.Estimator;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static ru.itis.rgjudge.utils.BodyPostureUtils.getBodyPositionType;
import static ru.itis.rgjudge.utils.Constant.DECIMAL_FORMAT;
import static ru.itis.rgjudge.utils.Constant.INVALID_VIDEO_FILE_SIZE_ERROR;
import static ru.itis.rgjudge.utils.Constant.MB_IN_BYTES;
import static ru.itis.rgjudge.utils.Constant.NO_PENALTY;

@Service
public class ElementEvaluationServiceImpl implements ElementEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(ElementEvaluationServiceImpl.class);

    private final List<Estimator> estimators;
    private final ReportService reportService;
    private final PoseEstimatorClient poseEstimatorClient;
    private final ElementRepository elementRepository;
    private final VideoFileProperties videoFileProperties;
    private final ObjectMapper objectMapper;

    public ElementEvaluationServiceImpl(List<Estimator> estimators, ReportService reportService,
                                        PoseEstimatorClient poseEstimatorClient, ElementRepository elementRepository,
                                        VideoFileProperties videoFileProperties, ObjectMapper objectMapper) {
        this.estimators = estimators;
        this.reportService = reportService;
        this.poseEstimatorClient = poseEstimatorClient;
        this.elementRepository = elementRepository;
        this.videoFileProperties = videoFileProperties;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public ElementReport evaluateElement(Integer elementId, MultipartFile videoFile, MultipartFile gymnastPhoto) {
        var element = elementRepository.findByIdFull(elementId)
                        .orElseThrow(() -> new ElementNotFoundException(elementId));

        Thread.sleep(1000);
        validateVideoFile(videoFile);
        logger.info("Start element evaluation process. Start to detect pose for {} video", videoFile.getOriginalFilename());

//        var poseData = poseEstimatorClient.detectPose(videoFile).body();
        try (FileInputStream fis = new FileInputStream("/Users/d.gilfanova/RG_Judging_Support_System/rg_judge/src/main/resources/test_result_data/la_yk_8.txt")) {
            String stringTooLong = IOUtils.toString(fis, Charset.defaultCharset());
            PoseResponse poseData = objectMapper.readValue(stringTooLong, new TypeReference<DefaultResponseDto<PoseResponse>>() {}).body();
            logger.info("End to detect pose for {} video. Video params: duration = {}, frames = {}, height = {}, width = {}",
                    videoFile.getOriginalFilename(), poseData.getDuration(), poseData.getFrameCount(), poseData.getHeight(), poseData.getWidth());

            var bodyParts = BodyPart.getByStringList(poseData.getBodyParts());
            var bodyPositionType = getBodyPositionType(poseData.getPoseData(), bodyParts, element.typeBySupportLeg(), poseData.getFrameCount());
            logger.info("Body position type = {}", bodyPositionType.name());

            List<EstimatorResponse> estimatorResponses = new ArrayList<>();
            var currentEvalPoseData = poseData.getPoseData();
//            for (Estimator estimator : estimators) {
//                if (estimator.isApplicableToElement(element)) {
//                    logger.info("Start evaluate by {}", estimator.getClass());
//                    var estimatorResponse = estimator.estimateElement(currentEvalPoseData, bodyParts, element, bodyPositionType);
//                    estimatorResponses.add(estimatorResponse);
//                    if (estimatorResponse.isValid() && estimatorResponse.elementExecutionPoseData() != null) {
//                        currentEvalPoseData = estimatorResponse.elementExecutionPoseData();
//                    }
//                }
//            }
//            return reportService.createReport(element, estimatorResponses, poseData.getVideoLink());
            return reportService.createReport(element, List.of(
                EstimatorResponse.builder()
                    .isValid(true)
                    .penalty(0.0)
                    .estimationType(EstimationType.FULL_CHECK)
                    .reportData(ReportData.builder()
                        .estimatorName("trf")
                        .expectedBehavior("jflfglf")
                        .actualBehavior("dkffod")
                        .isCounted(Boolean.TRUE.toString())
                        .penalty(DECIMAL_FORMAT.format(NO_PENALTY))
                        .build())
                    .build(),
                EstimatorResponse.builder()
                    .isValid(false)
                    .penalty(0.3)
                    .estimationType(EstimationType.FULL_CHECK)
                    .reportData(ReportData.builder()
                        .estimatorName("trf")
                        .expectedBehavior("jflfglf")
                        .actualBehavior("dkffod")
                        .isCounted(Boolean.FALSE.toString())
                        .penalty("0.3")
                        .build())
                    .build(),
                EstimatorResponse.builder()
                    .isValid(true)
                    .penalty(0.3)
                    .estimationType(EstimationType.FULL_CHECK)
                    .reportData(ReportData.builder()
                        .estimatorName("trf")
                        .expectedBehavior("jflfglf")
                        .actualBehavior("dkffod")
                        .isCounted(Boolean.TRUE.toString())
                        .penalty("0.3")
                        .build())
                    .build()
                ), "jfjfjf");
        }
    }

    private void validateVideoFile(MultipartFile videoFile) {
        var videoSize = videoFile.getSize() / MB_IN_BYTES;
        if (videoSize > videoFileProperties.maxSize()) {
            throw new ElementEvaluationException(INVALID_VIDEO_FILE_SIZE_ERROR.formatted(videoSize, videoFileProperties.maxSize()));
        }
    }
}
