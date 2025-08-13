package com.tientvv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class TimeZoneConfig {

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.of("+07:00");
    
    @Bean
    @Primary
    public ZoneId vietnamZoneId() {
        return VIETNAM_ZONE;
    }
    
    /**
     * Lấy thời gian hiện tại theo múi giờ Việt Nam
     */
    public static ZonedDateTime getCurrentVietnamTime() {
        return ZonedDateTime.now(VIETNAM_ZONE);
    }
    
    /**
     * Chuyển đổi OffsetDateTime sang múi giờ Việt Nam
     */
    public static ZonedDateTime toVietnamTime(java.time.OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return offsetDateTime.atZoneSameInstant(VIETNAM_ZONE);
    }
    
    /**
     * Chuyển đổi ZonedDateTime sang OffsetDateTime với múi giờ Việt Nam
     */
    public static java.time.OffsetDateTime toOffsetDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return zonedDateTime.toOffsetDateTime();
    }
    
    /**
     * Format thời gian theo định dạng Việt Nam
     */
    public static String formatVietnamTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return zonedDateTime.format(formatter);
    }
    
    /**
     * Format thời gian theo định dạng ngắn gọn
     */
    public static String formatShortVietnamTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return zonedDateTime.format(formatter);
    }
}
