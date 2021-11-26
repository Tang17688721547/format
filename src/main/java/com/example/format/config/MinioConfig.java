package com.example.format.config;

import com.example.format.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio文件上传配置文件
 */
@Slf4j
@Configuration
public class MinioConfig {
    @Value(value = "${minio.minio_name}")
    private String minioName;
    @Value(value = "${minio.minio_pass}")
    private String minioPass;
    @Value(value = "${minio.bucketName}")
    private String bucketName;
    @Value(value = "${minio.minio_url}")
    private String minioUrl;

    @Bean
    public void initMinio(){
        log.info("minioName={},minioPass={},bucketName={}",minioName,minioPass,bucketName);
        MinioUtil.setMinioName(minioName);
        MinioUtil.setMinioPass(minioPass);
        MinioUtil.setBucketName(bucketName);
        MinioUtil.setMinioUrl(minioUrl);
    }

}
