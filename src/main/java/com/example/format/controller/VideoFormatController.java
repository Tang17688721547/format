package com.example.format.controller;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.format.service.FormatService;
import com.example.format.util.DingCallbackCrypto;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@Slf4j
@RequestMapping("/format")
public class VideoFormatController {

    @Resource
    private FormatService formatService;

    //    @PostMapping("/url")
////    public String  formatUrl(@RequestParam("url") String url,@RequestParam("fileName") String fileName,HttpServletResponse response){
////        System.out.println("开始url转码");
////        fileFormatUrl(url,fileName,response);
////        System.out.println("结束url转码");
////
////    }
    @PostMapping(value = "/file", consumes = MULTIPART_FORM_DATA_VALUE)
    public void formatFile(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName, @RequestParam("resourceId") Integer resourceId, @RequestParam("url") String url) {
        formatService.fileFormatVideo(FormatService.trans(file), fileName, resourceId, url);
    }

    @PostMapping(value = "/files", consumes = MULTIPART_FORM_DATA_VALUE)
    public void formatFile(@RequestPart("files") List<MultipartFile> files, @RequestParam("fileNames") String fileNames, @RequestParam("resourceIds") String resourceIds, @RequestParam("urls") String urls) {
        formatService.fileFormatVideos(files, fileNames, resourceIds, urls);
    }

    @RequestMapping("/callback")
    public void callback(@RequestBody JSONObject object) throws UnirestException {
        log.info("object  = " + object.toString());
        System.out.println(object.toString());
    }

    static final java.text.SimpleDateFormat DF = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @RequestMapping("/demo")
    public void demo(HttpServletRequest httpServletRequest) throws Exception {
        String accessKeyId = httpServletRequest.getParameter("AccessKeyId");
        String accessSecret = httpServletRequest.getParameter("accessSecret");
        String method = httpServletRequest.getMethod().toUpperCase();
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        Map<String, String[]> querys = new HashMap<>();
        querys.putAll(parameterMap);

        DF.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));// 这里一定要设置GMT时区

        //去掉不需要进行加密的参数
        if (querys.containsKey("accessSecret")) {
            querys.remove("accessSecret");
        }
        if (querys.containsKey("Signature")) {
            querys.remove("Signature");
        }
        if (querys.containsKey("method")) {
            querys.remove("method");
        }
        if (querys.containsKey("userName")) {
            querys.remove("userName");
        }
        String[] s = {java.util.UUID.randomUUID().toString()};
        String[] s1 = {DF.format(new java.util.Date())};

        querys.put("SignatureNonce", s);//防止重放攻击

        querys.put("Timestamp", s1);


        StringBuilder builder = new StringBuilder();
        //将参数进行排序 格式化追加
        TreeMap<String, String[]> sortParas = new java.util.TreeMap<String, String[]>();
        sortParas.putAll(querys);
        Iterator<String> it = sortParas.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            builder.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(querys.get(key)[0]));
        }
        String sortedQueryString = builder.substring(1);// 去除第一个多余的&符号
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(method).append("&");
        stringToSign.append(specialUrlEncode("/")).append("&");
        stringToSign.append(specialUrlEncode(sortedQueryString));
        //accessSecret与参数字符进行签名
        String sign = sign(accessSecret + "&", stringToSign.toString());
        // 签名最后也要做特殊URL编码
        String signature = specialUrlEncode(sign);
        //   signature=signature+"&";
        System.out.println("https://facebody.cn-shanghai.aliyuncs.com/?Signature=" + signature + "&" + sortedQueryString);
        try {
            // 使用生成的 URL 创建POST请求
            URIBuilder b = new URIBuilder("https://facebody.cn-shanghai.aliyuncs.com/?Signature=" + signature + "&" + sortedQueryString);
            URI uri = b.build();
            HttpPost request = new HttpPost(uri);
            HttpClient httpclient = HttpClients.createDefault();
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                System.out.println(EntityUtils.toString(entity));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static String specialUrlEncode(String value) throws Exception {
        return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    public static String sign(String accessSecret, String stringToSign) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(accessSecret.getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return new sun.misc.BASE64Encoder().encode(signData);
    }

    @RequestMapping("/test")
    public Map<String, String> callBack(
            @RequestParam(value = "msg_signature", required = false) String msg_signature,
            @RequestParam(value = "timestamp", required = false) String timeStamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestBody(required = false) JSONObject json) {
        try {
            // 1. 从http请求中获取加解密参数

            // 2. 使用加解密类型
            // Constant.OWNER_KEY 说明：
            // 1、开发者后台配置的订阅事件为应用级事件推送，此时OWNER_KEY为应用的APP_KEY。
            // 2、调用订阅事件接口订阅的事件为企业级事件推送，
            //      此时OWNER_KEY为：企业的appkey（企业内部应用）或SUITE_KEY（三方应用）
            DingCallbackCrypto callbackCrypto = new DingCallbackCrypto("M3LIBxRdO8", "n1J1cofuKlwuy2ON3gghMJTZPSuuVkBzgsLwLF4nybO", "ding2e9fdba44a2a6e90f5bf40eda33b7ba0");
            String encryptMsg = json.getString("encrypt");
            String decryptMsg = callbackCrypto.getDecryptMsg(msg_signature, timeStamp, nonce, encryptMsg);

            // 3. 反序列化回调事件json数据
            JSONObject eventJson = JSON.parseObject(decryptMsg);
            String eventType = eventJson.getString("EventType");
            System.out.println(eventJson);
            // 4. 根据EventType分类处理
            if ("check_url".equals(eventType)) {
                // 测试回调url的正确性
                log.info("测试回调url的正确性");
            } else if ("user_add_org".equals(eventType)) {
                // 处理通讯录用户增加事件
                log.info("发生了：" + eventType + "事件");
            } else {
                // 添加其他已注册的
                log.info("发生了：" + eventType + "事件");
            }

            // 5. 返回success的加密数据
            Map<String, String> successMap = callbackCrypto.getEncryptedMap("success");
            return successMap;

        } catch (DingCallbackCrypto.DingTalkEncryptException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/testDemo")
    public Map<String, String> callBack1(
            @RequestParam(value = "msg_signature", required = false) String msg_signature,
            @RequestParam(value = "timestamp", required = false) String timeStamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestBody(required = false) JSONObject json) {
        try {
            // 1. 从http请求中获取加解密参数

            // 2. 使用加解密类型
            // Constant.OWNER_KEY 说明：
            // 1、开发者后台配置的订阅事件为应用级事件推送，此时OWNER_KEY为应用的APP_KEY。
            // 2、调用订阅事件接口订阅的事件为企业级事件推送，
            //      此时OWNER_KEY为：企业的appkey（企业内部应用）或SUITE_KEY（三方应用）
            DingCallbackCrypto callbackCrypto = new DingCallbackCrypto("M3LIBxRdO8", "n1J1cofuKlwuy2ON3gghMJTZPSuuVkBzgsLwLF4nybO", "ding2e9fdba44a2a6e90f5bf40eda33b7ba0");
            String encryptMsg = json.getString("encrypt");
            String decryptMsg = callbackCrypto.getDecryptMsg(msg_signature, timeStamp, nonce, encryptMsg);

            // 3. 反序列化回调事件json数据
            JSONObject eventJson = JSON.parseObject(decryptMsg);
            String eventType = eventJson.getString("EventType");

            // 4. 根据EventType分类处理
            if ("check_url".equals(eventType)) {
                // 测试回调url的正确性
                log.info("测试回调url的正确性");
            } else if ("user_add_org".equals(eventType)) {
                // 处理通讯录用户增加事件
                log.info("发生了：" + eventType + "事件");
            } else {
                // 添加其他已注册的
                log.info("发生了：" + eventType + "事件");
            }

            // 5. 返回success的加密数据
            Map<String, String> successMap = callbackCrypto.getEncryptedMap("success");
            return successMap;

        } catch (DingCallbackCrypto.DingTalkEncryptException e) {
            e.printStackTrace();
        }
        return null;
    }

  
}
