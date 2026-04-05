package com.example.demo.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * IP 归属地工具类，对外保持兼容接口。
 */
@Component
@RequiredArgsConstructor
public class IpLocationUtil {

    private final Ip2RegionService ip2RegionService;

    /**
     * 获取客户端真实 IP。
     */
    public String getClientIp(HttpServletRequest request) {
        return ClientIpUtil.getClientIp(request);
    }

    /**
     * 根据 IP 获取结构化归属地信息。
     */
    public IpLocation getLocationDetailByIp(String ip) {
        return ip2RegionService.search(ip);
    }

    /**
     * 根据请求获取结构化归属地信息。
     */
    public IpLocation getLocationDetail(HttpServletRequest request) {
        return ip2RegionService.search(request);
    }

    /**
     * 根据 IP 获取简要位置。
     */
    public String getLocationByIp(String ip) {
        return ip2RegionService.search(ip).getDisplayName();
    }

    /**
     * 根据请求获取简要位置。
     */
    public String getLocation(HttpServletRequest request) {
        return ip2RegionService.search(request).getDisplayName();
    }
}
