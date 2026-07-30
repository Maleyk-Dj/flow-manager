package com.maleyk.flow_manager;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    protected static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    protected static final MinIOContainer minio =
            new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z");

    static {
        postgres.start();
        kafka.start();
        minio.start();
        createBuckets();
    }
    private static void createBuckets() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(minio.getS3URL())
                    .credentials(minio.getUserName(), minio.getPassword())
                    .build();
            for (String bucket : new String[]{"source-files", "converted-files"}) {
                if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось создать бакеты для тестов", e);
        }
    }


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("minio.endpoint", minio::getS3URL);
        registry.add("minio.access-key", minio::getUserName);
        registry.add("minio.secret-key", minio::getPassword);

        registry.add("eureka.client.enabled", () -> "false");
    }
}
