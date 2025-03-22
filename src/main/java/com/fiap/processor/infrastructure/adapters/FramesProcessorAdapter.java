package com.fiap.processor.infrastructure.adapters;

import com.fiap.processor.config.S3Configuration;
import com.fiap.processor.core.domain.VideoModel;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static javax.imageio.ImageIO.write;

@Component
public class FramesProcessorAdapter {

    private final S3Configuration s3Configuration;

    @Value("${aws.s3.bucketZip}")
    private String bucketZipName;

    public FramesProcessorAdapter(S3Configuration s3Configuration) {
        this.s3Configuration = s3Configuration;
    }

    public String extractor(VideoModel video, String zipFileName, int intervalSeconds) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video.getPath());
             var arrayOutputStream = new ByteArrayOutputStream();
             var zipOutputStream = new ZipOutputStream(arrayOutputStream);
             var frameConverter = new Java2DFrameConverter()) {

            grabber.start();

            int frameRate = (int) grabber.getFrameRate();
            int frameInterval = frameRate * intervalSeconds;
            int frameNumber = 0;
            Frame frame;

            while ((frame = grabber.grabImage()) != null) {
                if (frameNumber % frameInterval == 0) {
                    BufferedImage bufferedImage = frameConverter.getBufferedImage(frame);
                    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                        write(bufferedImage, "jpg", byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        var fileName = "video_frame_" + String.format("%04d", frameNumber) + ".jpg";
                        zipOutputStream.putNextEntry(new ZipEntry(fileName));
                        zipOutputStream.write(imageBytes);
                        zipOutputStream.closeEntry();
                    }
                }
                frameNumber++;
            }

            grabber.stop();
            zipOutputStream.finish();
            return uploader(zipFileName, arrayOutputStream.toByteArray());

        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String uploader(String fileName, byte[] zipData) {
        try {
            var putObject = PutObjectRequest.builder()
                    .bucket(bucketZipName)
                    .key(fileName)
                    .build();

            s3Configuration.getS3Client().putObject(putObject, RequestBody.fromBytes(zipData));

            return s3Configuration.getS3Client()
                    .utilities()
                    .getUrl(
                            GetUrlRequest.builder()
                                    .bucket(bucketZipName)
                                    .key(fileName)
                                    .build()
                    ).toString();

        } catch (Exception e) {
            return null;
        }
    }
}
