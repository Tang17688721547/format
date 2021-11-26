package com.example.format.service;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SignDemo {
    static final String API_HTTP_METHOD = "POST";
    static final String API_VERSION = "2019-12-30";
    static final java.text.SimpleDateFormat DF = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static void main(String[] args) throws Exception {
        String accessKeyId = "LTAI5t6zWdawmGmxs4uGw2mj";
        String accessSecret = "TlSuranUJb5ACNIcxfeoGFAbPqrfLu";
        DF.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));// 这里一定要设置GMT时区
        Map<String, String> params = new HashMap<>();//业务参数名字是大驼峰
        params.put("ImageURL", "https://dongfangjinxin.oss-cn-shanghai.aliyuncs.com/src%3Dhttp---img.pconline.com.cn-images-upload-upc-tx-itbbs-1710-07-c2-62069295_1507338240548_mthumb.jpg%26refer%3Dhttp---img.pconline.com.jpg?Expires=1635941764&OSSAccessKeyId=TMP.3Kiu34HiMeyU8SmSwwy53YXjdiK89bV8FmENuHgU5bXNeM4JY2G6fLSpturj3XB31L1ZFbZXJ59tmyDQjqxurQSJ34BGxW&Signature=1PL%2FN7t%2BTbT2khdHQE%2FQgfMf%2BhE%3D");
        String action = "PedestrianDetectAttribute";
        execute(action, accessKeyId, accessSecret, params);
    }
    public static void execute(String action, String accessKeyId, String accessSecret, Map<String, String> bizParams) throws Exception {
        java.util.Map<String, String> paras = new java.util.HashMap<String, String>();
        // 1. 系统参数
        paras.put("SignatureVersion", "1.0");
        paras.put("Format", "JSON");

        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureNonce", java.util.UUID.randomUUID().toString());//防止重放攻击

        paras.put("Timestamp", DF.format(new java.util.Date()));

        paras.put("AccessKeyId", accessKeyId);
        System.out.println(DF.format(new Date()));
        // 2. 业务API参数
        paras.put("RegionId", "cn-shanghai");
        paras.put("Version", API_VERSION);
        paras.put("Action", action);
        if (bizParams != null && !bizParams.isEmpty()) {
            paras.putAll(bizParams);
        }
        // 3. 去除签名关键字Key
        if (paras.containsKey("Signature")) {
            paras.remove("Signature");
        }
        // 4. 参数KEY排序
        java.util.TreeMap<String, String> sortParas = new java.util.TreeMap<String, String>();
        sortParas.putAll(paras);
        // 5. 构造待签名的字符串
        java.util.Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paras.get(key)));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1);// 去除第一个多余的&符号
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(API_HTTP_METHOD).append("&");
        stringToSign.append(specialUrlEncode("/")).append("&");
        stringToSign.append(specialUrlEncode(sortedQueryString));
        String sign = sign(accessSecret + "&", stringToSign.toString());
        // 6. 签名最后也要做特殊URL编码
        String signature = specialUrlEncode(sign);
        System.out.println(paras.get("SignatureNonce"));
        System.out.println("\r\n=========\r\n");
        System.out.println(paras.get("Timestamp"));
        System.out.println("\r\n=========\r\n");
        System.out.println(sortedQueryString);
        System.out.println("\r\n=========\r\n");
        System.out.println(stringToSign.toString());
        System.out.println("\r\n=========\r\n");
        System.out.println(sign);
        System.out.println("\r\n=========\r\n");
        System.out.println(signature);
        System.out.println("\r\n=========\r\n");
        // 最终生成出合法请求的URL
        System.out.println("https://facebody.cn-shanghai.aliyuncs.com/?Signature=" + signature + sortQueryStringTmp);

        // 添加直接做post请求的方法
        try {
            // 使用生成的 URL 创建POST请求
            URIBuilder builder = new URIBuilder("http://facebody.cn-shanghai.aliyuncs.com/?Signature=" + signature + sortQueryStringTmp);
            URI uri = builder.build();
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
}