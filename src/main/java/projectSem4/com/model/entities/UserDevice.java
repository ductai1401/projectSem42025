package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class UserDevice {
	 private Integer userDeviceId;
	    private Integer userId;
	    private String deviceId;
	    private LocalDateTime lastSeenAt;
	    private String userAgent;
	    private String deviceType;
	    private String ipAddress;

	    public UserDevice() {}

	    public UserDevice(Integer userDeviceId, Integer userId, String deviceId,
	                      LocalDateTime lastSeenAt, String userAgent,
	                      String deviceType, String ipAddress) {
	        this.userDeviceId = userDeviceId;
	        this.userId = userId;
	        this.deviceId = deviceId;
	        this.lastSeenAt = lastSeenAt;
	        this.userAgent = userAgent;
	        this.deviceType = deviceType;
	        this.ipAddress = ipAddress;
	    }

	    public Integer getUserDeviceId() {
	        return userDeviceId;
	    }

	    public void setUserDeviceId(Integer userDeviceId) {
	        this.userDeviceId = userDeviceId;
	    }

	    public Integer getUserId() {
	        return userId;
	    }

	    public void setUserId(Integer userId) {
	        this.userId = userId;
	    }

	    public String getDeviceId() {
	        return deviceId;
	    }

	    public void setDeviceId(String deviceId) {
	        this.deviceId = deviceId;
	    }

	    public LocalDateTime getLastSeenAt() {
	        return lastSeenAt;
	    }

	    public void setLastSeenAt(LocalDateTime lastSeenAt) {
	        this.lastSeenAt = lastSeenAt;
	    }

	    public String getUserAgent() {
	        return userAgent;
	    }

	    public void setUserAgent(String userAgent) {
	        this.userAgent = userAgent;
	    }

	    public String getDeviceType() {
	        return deviceType;
	    }

	    public void setDeviceType(String deviceType) {
	        this.deviceType = deviceType;
	    }

	    public String getIpAddress() {
	        return ipAddress;
	    }

	    public void setIpAddress(String ipAddress) {
	        this.ipAddress = ipAddress;
	    }

	    @Override
	    public String toString() {
	        return "UserDevice{" +
	                "userDeviceId=" + userDeviceId +
	                ", userId=" + userId +
	                ", deviceId='" + deviceId + '\'' +
	                ", lastSeenAt=" + lastSeenAt +
	                ", userAgent='" + userAgent + '\'' +
	                ", deviceType='" + deviceType + '\'' +
	                ", ipAddress='" + ipAddress + '\'' +
	                '}';
	    }
    
}
