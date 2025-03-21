package com.fiap.processor.infrastructure.adapters;

import com.fiap.processor.config.S3Configuration;
import com.fiap.processor.infrastructure.exception.DownloadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Component
public class DownloadAdapter {

    private final S3Configuration s3Configuration;

    @Value("${aws.s3.bucketVideo}")
    private String bucketVideoName;

    public DownloadAdapter(S3Configuration s3Configuration) {
        this.s3Configuration = s3Configuration;
    }

    public File downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketVideoName)
                    .key(key)
                    .build();

            var tempFile = Files.createTempFile("video", ".mp4").toFile();
            try (InputStream inputStream = s3Configuration.getS3Client().getObject(request);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;
        } catch (Exception e) {
            throw new DownloadException("Erro ao baixar vídeo do servidor S3", e);
        }
    }
}
