package com.example.demo.controller;

import com.example.demo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BilibiliController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BilibiliController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/bilibili/video/{bvid}")
    public Result<Map<String, Object>> getVideoInfo(@PathVariable String bvid) {
        try {
            String url = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://www.bilibili.com");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            headers.set("Origin", "https://www.bilibili.com");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
            
            if (result != null && result.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                Map<String, Object> videoInfo = new HashMap<>();
                videoInfo.put("bvid", data.get("bvid"));
                videoInfo.put("title", data.get("title"));
                String cover = (String) data.get("pic");
                if (cover != null && cover.startsWith("http://")) {
                    cover = cover.replace("http://", "https://");
                }
                videoInfo.put("cover", cover);
                videoInfo.put("duration", data.get("duration"));
                videoInfo.put("view", data.get("stat") != null ? ((Map<String, Object>) data.get("stat")).get("view") : 0);
                videoInfo.put("owner", data.get("owner") != null ? ((Map<String, Object>) data.get("owner")).get("name") : "");
                return Result.success(videoInfo);
            }
            
            return Result.fail(40401, "视频不存在");
        } catch (Exception e) {
            return Result.fail(50001, "获取视频信息失败: " + e.getMessage());
        }
    }
}
