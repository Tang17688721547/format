package com.example.format.util;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * minio文件上传工具类
 */
@Slf4j
public class MinioUtil {
    private static String minioName;
    private static String minioPass;
    private static String bucketName;
    private static String minioUrl;

    public static void setMinioName(String minioName) {
        MinioUtil.minioName = minioName;
    }

    public static void setMinioPass(String minioPass) {
        MinioUtil.minioPass = minioPass;
    }

    public static void setMinioUrl(String minioUrl) {
        MinioUtil.minioUrl = minioUrl;
    }


    public static void setBucketName(String bucketName) {
        MinioUtil.bucketName = bucketName;
    }

    private static MinioClient minioClient = null;




    /**
     * 初始化客户端
     * @param minioUrl
     * @param minioName
     * @param minioPass
     * @return
     */
    private static MinioClient initMinio(String minioUrl, String minioName,String minioPass) {
        if (minioClient == null) {
            try {
                minioClient = new MinioClient(minioUrl, minioName,minioPass);
            } catch (InvalidEndpointException e) {
                e.printStackTrace();
            } catch (InvalidPortException e) {
                e.printStackTrace();
            }
        }
        return minioClient;
    }





    public static String upload(String dir,InputStream stream,String suffix,String fore) {
        String objectName="";
        try {
            initMinio(minioUrl, minioName,minioPass);
            // 检查存储桶是否已经存在
            if(minioClient.bucketExists(bucketName)) {
                log.info("Bucket already exists.");
            } else {
                // 创建一个名为test的存储桶
                minioClient.makeBucket(bucketName);
                log.info("create a new bucket.");
            }
            objectName =fore+"/"+dir+"/"+System.currentTimeMillis();
            // 使用putObject上传一个本地文件到存储桶中。
            minioClient.putObject(bucketName,objectName +suffix, stream,stream.available(),"application/octet-stream");

        }catch (IOException e){
            log.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        } catch (NoResponseException e) {
            log.error(e.getMessage(), e);
        } catch (XmlPullParserException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidArgumentException e) {
            log.error(e.getMessage(), e);
        } catch (RegionConflictException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidBucketNameException e) {
            log.error(e.getMessage(), e);
        } catch (ErrorResponseException e) {
            log.error(e.getMessage(), e);
        } catch (InternalException e) {
            log.error(e.getMessage(), e);
        } catch (InsufficientDataException e) {
            log.error(e.getMessage(), e);
        }finally {
            if(stream!=null){
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return objectName;
    }



    public static String removeMinio(String objName,String minioUrl){
        try {
            //创建MinioClient对象
            initMinio(minioUrl, minioName,minioPass);
            minioClient.removeObject(bucketName,objName);
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

    }




    public  static InputStream getFile(String objectName,String minioUrl) {
        try {
            initMinio(minioUrl, minioName,minioPass);
            // 文件是否存在
            minioClient.statObject(bucketName, objectName);
            // 获取文件
            return minioClient.getObject(bucketName, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}文件获取失败", objectName);
            return null;
        }
    }



}
