package com.maleyk.flow_manager.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    public void upload(String bucket, String objectKey,
                       InputStream stream, long size, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(stream, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public byte[] download(String bucket, String path) throws Exception {
        byte[] fileBytes;
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .build()
        )) {
            fileBytes = inputStream.readAllBytes();
        }
        return fileBytes;
    }
}
