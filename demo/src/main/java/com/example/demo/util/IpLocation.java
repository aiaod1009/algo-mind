package com.example.demo.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IP 地理位置信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpLocation {

    /**
     * 原始 IP。
     */
    private String ip;

    /**
     * 国家。
     */
    private String country;

    /**
     * 区域。
     */
    private String region;

    /**
     * 省份。
     */
    private String province;

    /**
     * 城市。
     */
    private String city;

    /**
     * 运营商。
     */
    private String isp;

    /**
     * 原始查询结果。
     */
    private String raw;

    /**
     * 是否内网 IP。
     */
    private boolean internal;

    /**
     * 是否成功解析。
     */
    private boolean success;

    /**
     * 友好展示名称。
     */
    public String getDisplayName() {
        if (internal) {
            return "内网IP";
        }

        StringBuilder builder = new StringBuilder();
        appendPart(builder, province);
        appendPart(builder, city);

        if (builder.length() == 0) {
            appendPart(builder, country);
        }

        return builder.length() > 0 ? builder.toString() : "未知";
    }

    private void appendPart(StringBuilder builder, String value) {
        if (!isUsable(value)) {
            return;
        }
        if (builder.length() > 0 && builder.toString().equals(value)) {
            return;
        }
        builder.append(value);
    }

    private boolean isUsable(String value) {
        return value != null && !value.isBlank() && !"0".equals(value) && !"未知".equals(value);
    }
}
