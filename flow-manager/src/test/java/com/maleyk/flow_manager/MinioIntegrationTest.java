package com.maleyk.flow_manager;

import com.maleyk.flow_manager.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Testcontainers
@SpringBootTest(properties = "eureka.client.enabled=false")
class MinioIntegrationTest {

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", minio::getS3URL);
        registry.add("minio.access-key", minio::getUserName);
        registry.add("minio.secret-key", minio::getPassword);
    }

    @Autowired
    private MinioService minioService;
    @Autowired
    private MinioClient minioClient;

    private static final String BUCKET = "test-bucket";

    @BeforeEach
    void setUp() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
    }

    @Test
    void shouldUploadAndDownloadFile() throws Exception {
        byte[] content = "hello world".getBytes();
        minioService.upload(BUCKET, "test.txt", new ByteArrayInputStream(content), content.length, "text/plain");

        byte[] downloaded = minioService.download(BUCKET, "test.txt");

        assertArrayEquals(content, downloaded);
    }
}