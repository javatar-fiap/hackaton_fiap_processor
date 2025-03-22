package com.fiap.processor.core.application.usecases;

import com.fiap.processor.core.application.enums.VideoStatus;
import com.fiap.processor.core.domain.VideoMessageModel;
import com.fiap.processor.core.domain.VideoModel;
import com.fiap.processor.infrastructure.adapters.DownloadAdapter;
import com.fiap.processor.infrastructure.adapters.FramesProcessorAdapter;
import com.fiap.processor.infrastructure.adapters.SNSAdapter;
import com.fiap.processor.infrastructure.exception.DownloadException;
import com.fiap.processor.infrastructure.memory.LocalVideoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
public class ProcessorVideoUseCase {

    private final FramesProcessorAdapter framesProcessorAdapter;
    private final LocalVideoRepository videoRepository;
    private final SNSAdapter snsAdapter;
    private final DownloadAdapter s3Service;

    public ProcessorVideoUseCase(FramesProcessorAdapter framesProcessorAdapter,
                                 LocalVideoRepository videoRepository,
                                 SNSAdapter snsAdapter,
                                 DownloadAdapter s3Service) {
        this.framesProcessorAdapter = framesProcessorAdapter;
        this.videoRepository = videoRepository;
        this.snsAdapter = snsAdapter;
        this.s3Service = s3Service;
    }

    public void execute(VideoMessageModel videoMessage) {
        var zipFileName = videoMessage.getKeyS3().replace(".mp4", ".zip");
        int intervalSeconds;

        if (Objects.nonNull(videoMessage.getIntervalSeconds())) {
            intervalSeconds = videoMessage.getIntervalSeconds();
        } else {
            intervalSeconds = 10;
        }

        try {
            snsAdapter.publishMessage(videoMessage, VideoStatus.IN_PROGRESS, zipFileName, VideoStatus.IN_PROGRESS.toString());
            var downloadedFile = s3Service.downloadFile(videoMessage.getKeyS3());
            var video = new VideoModel(downloadedFile.getAbsolutePath(), Duration.ZERO);
            var extractedFrames = framesProcessorAdapter.extractor(video, zipFileName, intervalSeconds);

            if (Objects.nonNull(extractedFrames)) {
                videoRepository.save(video);
                snsAdapter.publishMessage(videoMessage, VideoStatus.COMPLETED, zipFileName, extractedFrames);
            } else {
                snsAdapter.publishMessage(videoMessage, VideoStatus.PROCESSING_ERROR, VideoStatus.PROCESSING_ERROR.toString(), ".zip");
            }
        } catch (Exception e) {
            snsAdapter.publishMessage(videoMessage, VideoStatus.PROCESSING_ERROR, VideoStatus.PROCESSING_ERROR.toString(), ".zip");
        }
    }

    public VideoModel getVideoByKey(String videoKey) {
        try {
            var key = videoKey.replace(".mp4", ".zip");
            var downloadedFile = s3Service.downloadFile(key);
            return new VideoModel(downloadedFile.getAbsolutePath(), Duration.ZERO);
        } catch (Exception e) {
            throw new DownloadException("Erro ao obter .zip do servidor S3: " + e.getMessage(), e);
        }
    }
}
