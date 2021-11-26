package com.example.format.service;

import com.alibaba.fastjson.JSONObject;
import com.example.format.feign.CallbackFeign;
import com.example.format.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Slf4j
@Service
public class FormatService {
    @Resource
    private RestTemplate restTemplate;

    @Resource
    private CallbackFeign callbackFeign;



    public  String sendPostDataByMap(String url, Map<String, String> map, String encoding) throws ClientProtocolException, IOException {
        String result = "";
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建post方式请求对象
        url = "http://"+url;

        JSONObject.toJSONString(map);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        // jsonObject.toString() = jsonObject.toJSONString(); 二者就是同一个方法
        HttpEntity<String> httpEntity = new HttpEntity<>(JSONObject.toJSONString(map), headers);
        // 假设规定的返回类型为 String类型
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        String body = responseEntity.getBody();


        return result;
    }

    public  String sendPostDataByList(String url, List<Map<String, String>> map, String encoding) throws ClientProtocolException, IOException {
        String result = "";
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建post方式请求对象
        url = "http://"+url;

        JSONObject.toJSONString(map);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        // jsonObject.toString() = jsonObject.toJSONString(); 二者就是同一个方法
        HttpEntity<String> httpEntity = new HttpEntity<>(JSONObject.toJSONString(map), headers);
        // 假设规定的返回类型为 String类型
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        String body = responseEntity.getBody();


        return result;
    }




    public static File trans(MultipartFile multipartFile) {
        File dfile = null;
        try {
            dfile = File.createTempFile("prefix", "_" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(dfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dfile;
    }
    /**
     * 视频文件转音频文件
     * @param videoPath
     * @param audioPath
     * @return
     */
    public static boolean videoToAudio(String videoPath, String audioPath) {
        File fileMp4=new File(videoPath);
        File fileMp3=new File(audioPath);

        //Audio Attributes
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(128000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        //Encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        MultimediaObject mediaObject=new MultimediaObject(fileMp4);
        try {
            encoder.encode(mediaObject, fileMp3, attrs);
            //log.info("File MP4 convertito in MP3");
            return true;
        } catch (Exception e) {
            //log.error("File non convertito");
            // log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 获取视频的基本信息，视频长宽高，视频的大小等
     * @param fileSource
     * @return
     */
//    public static VideoItem getVideoInfo(String fileSource) {
//        // String filePath =
//        // Utils.class.getClassLoader().getResource(fileSource).getPath();
//        File source = new File(fileSource);
//        //Encoder encoder = new Encoder();
//        FileInputStream fis = null;
//        FileChannel fc = null;
//        VideoItem videoInfo = null;
//        try {
//            MultimediaObject MultimediaObject=new MultimediaObject(source);
//            MultimediaInfo m = MultimediaObject.getInfo();
//            fis = new FileInputStream(source);
//            fc = fis.getChannel();
//            videoInfo = new VideoItem(m.getVideo().getSize().getWidth(), m.getVideo().getSize().getHeight(), fc.size(),
//                    m.getDuration(), m.getFormat());
//            System.out.println(videoInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (null != fc) {
//                try {
//                    fc.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (null != fis) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return videoInfo;
//    }

    /**
     * 截取视频中某一帧作为图片
     * @param videoPath
     * @param imagePath
     * @return
     */
    public static boolean getVideoProcessImage(String videoPath,String imagePath){
        long times = System.currentTimeMillis();
        File videoSource = new File(videoPath);
        File imageTarget = new File(imagePath);
        MultimediaObject object = new MultimediaObject(videoSource);
        try {
            MultimediaInfo multimediaInfo = object.getInfo();
            VideoInfo videoInfo=multimediaInfo.getVideo();
            VideoAttributes video = new VideoAttributes();
            video.setCodec("png");
            video.setSize(videoInfo.getSize());
            EncodingAttributes attrs = new EncodingAttributes();
            //VideoAttributes attrs = ecodeAttrs.getVideoAttributes().get();
            attrs.setFormat("image2");
            attrs.setOffset(11f);//设置偏移位置，即开始转码位置（11秒）
            attrs.setDuration(0.01f);//设置转码持续时间（1秒）
            attrs.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            encoder.encode(object,imageTarget,attrs);
            return true;
        } catch (EncoderException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * m4r音频格式转换为mp3，audioPath可更换为要转换的音频格式
     * @param audioPath
     * @param mp3Path
     */
    public static void m4rToMp3(String audioPath,String mp3Path){
        File source = new File(audioPath);
        File target = new File(mp3Path);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(new Integer(128000));
        audio.setChannels(new Integer(2));
        audio.setSamplingRate(new Integer(44100));
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从和视频中提取音频wav
     * @param aviPath
     * @param targetWavPath
     */
    public static void videoExtractAudio(String aviPath,String targetWavPath){
        File source = new File(aviPath);
        File target = new File(targetWavPath);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频转换为手机可播放的格式
     * @param sourceVideo sourceVideo.avi
     * @param targetVideo targetVideo.3gp
     */
    public static void videoToMobileVideo(String sourceVideo, String targetVideo){
        File source = new File("source.avi");
        File target = new File("target.3gp");
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libfaac");
        audio.setBitRate(new Integer(128000));
        audio.setSamplingRate(new Integer(44100));
        audio.setChannels(new Integer(2));
        VideoAttributes video = new VideoAttributes();
        video.setCodec("mpeg4");
        video.setBitRate(new Integer(160000));
        video.setFrameRate(new Integer(15));
        video.setSize(new VideoSize(176, 144));
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("3gp");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void fileFormatVideo(File source, String fileName, Integer resoureId, String url) {

        Map<String,String> map = new HashMap<>();
        map.put("resourceId",resoureId+"");
        int status = checkContentType(source.getName());
        if(status!=0){
            log.info("该视频不支持转码");
            map.put("code","500");
            map.put("message","该视频不支持转码");
        }else {
            File target = new File(fileName);
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(new Integer(56000));
            audio.setChannels(new Integer(1));
            audio.setSamplingRate(new Integer(22050));
            VideoAttributes video = new VideoAttributes();
            video.setCodec("libx264");
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("mp4");  //h264编码
            attrs.setAudioAttributes(audio);
            attrs.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            InputStream inputStream = null;
            try {
                encoder.encode(new MultimediaObject(source), target, attrs);
                inputStream = new FileInputStream(target);
                String objectName = MinioUtil.upload("format", inputStream, ".mp4", "video");
                target.delete();
                map.put("objectName", objectName);
                map.put("code", "200");
                map.put("message", "转码成功！");
            } catch (InputFormatException e) {
                map.put("code", "500");
                map.put("message", e.getMessage());
            } catch (EncoderException e) {
                map.put("code", "500");
                map.put("message", e.getMessage());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                map.put("code", "500");
                map.put("message", e.getMessage());
            }
            try {
                sendPostDataByMap(url, map, "utf-8");
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }


    @Async
    public void fileFormatVideos(List<MultipartFile> files, String fileNames, String resoureIds, String url) {
        List<String> fileName = Arrays.asList(fileNames.split(","));
        List<String> resoureId = Arrays.asList(resoureIds.split(","));
        List<Map<String,String>> map = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            Map<String,String> m = new HashMap<>();
            File source = FormatService.trans(files.get(i));
            m.put("resourceId",resoureId.get(i)+"");
            int status = checkContentType(source.getName());
            if(status!=0){
                log.info("该视频不支持转码");
                m.put("code","500");
                m.put("message","该视频不支持转码");
            }else {
                File target = new File(fileName.get(i));
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("libmp3lame");
                audio.setBitRate(new Integer(56000));
                audio.setChannels(new Integer(1));
                audio.setSamplingRate(new Integer(22050));
                VideoAttributes video = new VideoAttributes();
                video.setCodec("libx264");
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp4");  //h264编码
                attrs.setAudioAttributes(audio);
                attrs.setVideoAttributes(video);
                Encoder encoder = new Encoder();
                InputStream inputStream = null;
                try {
                    encoder.encode(new MultimediaObject(source), target, attrs);
                    inputStream = new FileInputStream(target);
                    String objectName = MinioUtil.upload("format", inputStream, ".mp4", "video");
                    target.delete();
                    m.put("objectName", objectName);
                    m.put("code", "200");
                    m.put("message", "转码成功！");
                    map.add(m);
                } catch (InputFormatException e) {
                    m.put("code", "500");
                    m.put("message", e.getMessage());
                } catch (EncoderException e) {
                    m.put("code", "500");
                    m.put("message", e.getMessage());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    m.put("code", "500");
                    m.put("message", e.getMessage());
                }

            }
        }
        try {
            sendPostDataByList(url, map, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void fileFormatUrl(String fileUrl, String targetVideo, HttpServletResponse response) {
        int status = checkContentType(fileUrl);
        if(status==0){
            //log.info("该视频支持转码");
        }else if(status==1){
            // log.info("需要将视频转为其它格式再进行转码");
            response.setStatus(500);
            return;
        }else{
            // log.error("不支持转码该视频类型");
            response.setStatus(500);
            return;
        }
        File target = new File(targetVideo);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(new Integer(56000));
        audio.setChannels(new Integer(1));
        audio.setSamplingRate(new Integer(22050));

        VideoAttributes video = new VideoAttributes();
        video.setCodec("libx264");

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp4");  //h264编码
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(new URL(fileUrl)), target, attrs);
            InputStream fileInputStream = new FileInputStream(target);
            OutputStream outStream = null;
            try {
//                outStream = response.getOutputStream();
//                byte[] bytes = new byte[1024];
//                int len = 0;
//                while ((len = fileInputStream.read(bytes)) != -1) {
//                    outStream.write(bytes, 0, len);
//                }

            }
//            catch (IOException e) {
//               // log.error("exception", e);
//            }
            finally {
                if(fileInputStream!=null){
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //  log.error("exception", e);

                    }
                }
                if(outStream!=null){
                    try {
                        outStream.close();
                        outStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //  log.error("exception", e);
                    }

                }
                //target.delete();
            }
            response.setStatus(200);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ///log.error("文件url不符合规范 fileUrl = "+fileUrl);
            response.setStatus(500);
        } catch (InputFormatException e) {
            e.printStackTrace();
            //log.error("转换失败");
            response.setStatus(500);

        } catch (EncoderException e) {
            e.printStackTrace();
            //log.error("转换失败");
            response.setStatus(500);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // log.error("文件不存在");

        }
    }

    private static int checkContentType(String sourceVideoPath) {
        String type = sourceVideoPath.substring(sourceVideoPath.lastIndexOf(".") + 1).toLowerCase();
        // ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
        if (type.equals("avi")) {
            return 0;
        } else if (type.equals("mpg")) {
            return 0;
        } else if (type.equals("wmv")) {
            return 0;
        } else if (type.equals("3gp")) {
            return 0;
        } else if (type.equals("mov")) {
            return 0;
        } else if (type.equals("mp4")) {
            return 0;
        } else if (type.equals("asf")) {
            return 0;
        } else if (type.equals("asx")) {
            return 0;
        } else if (type.equals("flv")) {
            return 0;
        }
        // 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
        // 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
        else if (type.equals("wmv9")) {
            return 1;
        } else if (type.equals("rm")) {
            return 1;
        } else if (type.equals("rmvb")) {
            return 1;
        }
        return 9;
    }

}
