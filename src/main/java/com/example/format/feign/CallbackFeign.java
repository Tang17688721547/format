package com.example.format.feign;


import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.Map;


@Service
@FeignClient(url = "${callback.url}",name = "callback")
public interface CallbackFeign {


    @PostMapping(value = "/callback/format",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void formatCallback(@RequestBody Map<String,Object> map);

    @PostMapping(value = "/callback/dwonload",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void dwonloadFormatCallback(@RequestBody Map<String,Object> map);
}
