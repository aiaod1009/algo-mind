package com.example.demo.util;

import com.example.demo.config.IpLocationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ip2RegionService {

    private static final String UNKNOWN = "未知";

    private final IpLocationProperties properties;
    private final ResourceLoader resourceLoader;

    private Searcher searcher;

    @PostConstruct
    public void init() {
        if (!properties.isEnabled()) {
            log.info("IP location service is disabled by configuration");
            return;
        }

        Resource resource = resourceLoader.getResource(properties.getDbPath());
        if (!resource.exists()) {
            handleInitializationFailure("ip2region database not found: " + properties.getDbPath(), null);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = inputStream.readAllBytes();
            this.searcher = Searcher.newWithBuffer(buffer);
            log.info("Loaded ip2region database from {}", properties.getDbPath());
        } catch (Exception ex) {
            handleInitializationFailure("Failed to initialize ip2region searcher", ex);
        }
    }

    @PreDestroy
    public void destroy() {
        if (searcher == null) {
            return;
        }

        try {
            searcher.close();
        } catch (IOException ex) {
            log.debug("Failed to close ip2region searcher cleanly", ex);
        }
    }

    public IpLocation search(String ip) {
        if (!StringUtils.hasText(ip)) {
            return buildUnknown(ip);
        }

        String normalizedIp = normalizeIp(ip);
        if (!isIpLiteral(normalizedIp)) {
            return buildUnknown(normalizedIp);
        }

        if (isInternalIp(normalizedIp)) {
            return IpLocation.builder()
                    .ip(normalizedIp)
                    .country("内网")
                    .region("内网")
                    .province("内网")
                    .city("内网")
                    .isp("内网")
                    .raw("内网IP")
                    .internal(true)
                    .success(true)
                    .build();
        }

        if (isIpv6(normalizedIp)) {
            return IpLocation.builder()
                    .ip(normalizedIp)
                    .country(UNKNOWN)
                    .region(UNKNOWN)
                    .province(UNKNOWN)
                    .city(UNKNOWN)
                    .isp(UNKNOWN)
                    .raw("暂不支持IPv6定位")
                    .internal(false)
                    .success(false)
                    .build();
        }

        if (searcher == null) {
            return buildUnavailable(normalizedIp);
        }

        try {
            return parseResult(normalizedIp, searcher.search(normalizedIp));
        } catch (Exception ex) {
            log.warn("Failed to resolve location for ip {}", normalizedIp, ex);
            return IpLocation.builder()
                    .ip(normalizedIp)
                    .country(UNKNOWN)
                    .region(UNKNOWN)
                    .province(UNKNOWN)
                    .city(UNKNOWN)
                    .isp(UNKNOWN)
                    .raw("查询失败")
                    .internal(false)
                    .success(false)
                    .build();
        }
    }

    public IpLocation search(HttpServletRequest request) {
        return search(ClientIpUtil.getClientIp(request));
    }

    private IpLocation parseResult(String ip, String regionString) {
        if (!StringUtils.hasText(regionString)) {
            return buildUnknown(ip);
        }

        String[] parts = regionString.split("\\|", -1);
        return IpLocation.builder()
                .ip(ip)
                .country(normalizeField(getPart(parts, 0)))
                .region(normalizeField(getPart(parts, 1)))
                .province(normalizeField(getPart(parts, 2)))
                .city(normalizeField(getPart(parts, 3)))
                .isp(normalizeField(getPart(parts, 4)))
                .raw(regionString)
                .internal(false)
                .success(true)
                .build();
    }

    private String getPart(String[] parts, int index) {
        return parts.length > index ? parts[index] : null;
    }

    private String normalizeField(String value) {
        if (!StringUtils.hasText(value) || "0".equals(value)) {
            return UNKNOWN;
        }
        return value;
    }

    private IpLocation buildUnknown(String ip) {
        return IpLocation.builder()
                .ip(ip)
                .country(UNKNOWN)
                .region(UNKNOWN)
                .province(UNKNOWN)
                .city(UNKNOWN)
                .isp(UNKNOWN)
                .raw(UNKNOWN)
                .internal(false)
                .success(false)
                .build();
    }

    private IpLocation buildUnavailable(String ip) {
        return IpLocation.builder()
                .ip(ip)
                .country(UNKNOWN)
                .region(UNKNOWN)
                .province(UNKNOWN)
                .city(UNKNOWN)
                .isp(UNKNOWN)
                .raw("ip2region database unavailable")
                .internal(false)
                .success(false)
                .build();
    }

    private String normalizeIp(String ip) {
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
        if ("0:0:0:0:0:0:0:1".equals(normalized) || "::1".equals(normalized)) {
            return "127.0.0.1";
        }
        return normalized;
    }

    private boolean isIpLiteral(String ip) {
        if (!StringUtils.hasText(ip) || (!ip.contains(".") && !ip.contains(":"))) {
            return false;
        }
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isIpv6(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address instanceof Inet6Address;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isInternalIp(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (!(address instanceof Inet4Address) && !(address instanceof Inet6Address)) {
                return false;
            }
            return address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress();
        } catch (Exception ex) {
            return false;
        }
    }

    private void handleInitializationFailure(String message, Exception ex) {
        if (properties.isFailFast()) {
            throw new IllegalStateException(message, ex);
        }
        if (ex == null) {
            log.warn(message);
            return;
        }
        log.warn(message, ex);
    }
}
