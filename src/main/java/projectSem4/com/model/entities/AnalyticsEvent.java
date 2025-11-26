package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class AnalyticsEvent {
    private Long eventId;
    private Integer userId;
    private String eventType;
    private Integer productId;
    private String searchKeyword;
    private String sessionId;
    private String deviceInfo;
    private Integer eventCount;          // ✅ số lần xảy ra
    private LocalDateTime timestamp;     // thời điểm tạo
    private LocalDateTime updatedAt;     // thời điểm cập nhật lần cuối

    // ==== Getters & Setters ====
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Integer getEventCount() {
        return eventCount;
    }

    public void setEventCount(Integer eventCount) {
        this.eventCount = eventCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ==== Constructors ====
    public AnalyticsEvent(Long eventId, Integer userId, String eventType, Integer productId,
                          String searchKeyword, String sessionId, String deviceInfo,
                          Integer eventCount, LocalDateTime timestamp, LocalDateTime updatedAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.productId = productId;
        this.searchKeyword = searchKeyword;
        this.sessionId = sessionId;
        this.deviceInfo = deviceInfo;
        this.eventCount = eventCount;
        this.timestamp = timestamp;
        this.updatedAt = updatedAt;
    }

    public AnalyticsEvent() {
    }
}
