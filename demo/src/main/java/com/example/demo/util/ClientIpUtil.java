package com.example.demo.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户端 IP 获取工具类。
 */
public final class ClientIpUtil {

    private static final List<String> IP_HEADER_CANDIDATES = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    );

    private ClientIpUtil() {
    }

    /**
     * 获取客户端真实 IP。
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (isValidIpValue(ip)) {
                return normalizeLocalIp(extractFirstIp(ip));
            }
        }

        return normalizeLocalIp(request.getRemoteAddr());
    }

    private static boolean isValidIpValue(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }

    private static String extractFirstIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return ip;
        }
        String normalized = ip.trim();
        if (normalized.contains(",")) {
            normalized = normalized.split(",")[0].trim();
        }
        if (normalized.startsWith("::ffff:")) {
            normalized = normalized.substring("::ffff:".length());
        }
        return normalized;
    }

    private static String normalizeLocalIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return ip;
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
