package com.invoiceapp.service.impl;

import com.invoiceapp.config.StorageProperties;
import com.invoiceapp.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

    private final StorageProperties storageProperties;
    private S3Client s3Client;
    private S3Presigner s3Presigner;

    private S3Client getS3Client() {
        if (s3Client == null) {
            StorageProperties.AwsProperties aws = storageProperties.getAws();

            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    aws.getAccessKey(),
                    aws.getSecretKey()
            );

            s3Client = S3Client.builder()
                    .region(Region.of(aws.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
        return s3Client;
    }

    private S3Presigner getS3Presigner() {
        if (s3Presigner == null) {
            StorageProperties.AwsProperties aws = storageProperties.getAws();

            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    aws.getAccessKey(),
                    aws.getSecretKey()
            );

            s3Presigner = S3Presigner.builder()
                    .region(Region.of(aws.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
        return s3Presigner;
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String fileName = UUID.randomUUID() + extension;
        String key = folder + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(storageProperties.getAws().getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            getS3Client().putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            log.info("Uploaded file to S3: {}", key);
            return key;
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName, String contentType, long size, String folder) throws IOException {
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        String newFileName = UUID.randomUUID() + extension;
        String key = folder + "/" + newFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(storageProperties.getAws().getBucket())
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        getS3Client().putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
        log.info("Uploaded file to S3: {}", key);
        return key;
    }

    @Override
    public InputStream get(String storageKey) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getAws().getBucket())
                .key(storageKey)
                .build();

        return getS3Client().getObject(getObjectRequest);
    }

    @Override
    public String getUrl(String storageKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getAws().getBucket())
                .key(storageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = getS3Presigner().presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public void delete(String storageKey) throws IOException {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(storageProperties.getAws().getBucket())
                .key(storageKey)
                .build();

        getS3Client().deleteObject(deleteObjectRequest);
        log.info("Deleted file from S3: {}", storageKey);
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(storageProperties.getAws().getBucket())
                    .key(storageKey)
                    .build();

            getS3Client().headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
