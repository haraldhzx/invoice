package com.invoiceapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.storage")
@Data
public class StorageProperties {
    private String type = "s3"; // s3, local
    private AwsProperties aws = new AwsProperties();
    private LocalProperties local = new LocalProperties();

    @Data
    public static class AwsProperties {
        private String accessKey;
        private String secretKey;
        private String region;
        private String bucket;
    }

    @Data
    public static class LocalProperties {
        private String uploadDir = "./uploads";
    }
}
