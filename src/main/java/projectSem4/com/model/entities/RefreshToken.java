package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class RefreshToken {
	 private Long refreshTokenId;
	    private Integer userId;
	    private String token;
	    private String deviceId;
	    private String deviceType;
	    private String userAgent;
	    private String ipAddress;
	    private LocalDateTime issuedAt;
	    private LocalDateTime expiresAt;
	    private boolean revoked;
	    private LocalDateTime revokedAt;

	    public RefreshToken() {}

		public RefreshToken(Long refreshTokenId, Integer userId, String token, String deviceId, String deviceType,
				String userAgent, String ipAddress, LocalDateTime issuedAt, LocalDateTime expiresAt, boolean revoked,
				LocalDateTime revokedAt) {
			super();
			this.refreshTokenId = refreshTokenId;
			this.userId = userId;
			this.token = token;
			this.deviceId = deviceId;
			this.deviceType = deviceType;
			this.userAgent = userAgent;
			this.ipAddress = ipAddress;
			this.issuedAt = issuedAt;
			this.expiresAt = expiresAt;
			this.revoked = revoked;
			this.revokedAt = revokedAt;
		}

		public Long getRefreshTokenId() {
			return refreshTokenId;
		}

		public void setRefreshTokenId(Long refreshTokenId) {
			this.refreshTokenId = refreshTokenId;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}

		public String getDeviceType() {
			return deviceType;
		}

		public void setDeviceType(String deviceType) {
			this.deviceType = deviceType;
		}

		public String getUserAgent() {
			return userAgent;
		}

		public void setUserAgent(String userAgent) {
			this.userAgent = userAgent;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}

		public LocalDateTime getIssuedAt() {
			return issuedAt;
		}

		public void setIssuedAt(LocalDateTime issuedAt) {
			this.issuedAt = issuedAt;
		}

		public LocalDateTime getExpiresAt() {
			return expiresAt;
		}

		public void setExpiresAt(LocalDateTime expiresAt) {
			this.expiresAt = expiresAt;
		}

		public boolean isRevoked() {
			return revoked;
		}

		public void setRevoked(boolean revoked) {
			this.revoked = revoked;
		}

		public LocalDateTime getRevokedAt() {
			return revokedAt;
		}

		public void setRevokedAt(LocalDateTime revokedAt) {
			this.revokedAt = revokedAt;
		}

		@Override
		public String toString() {
			return "RefreshToken [refreshTokenId=" + refreshTokenId + ", userId=" + userId + ", token=" + token
					+ ", deviceId=" + deviceId + ", deviceType=" + deviceType + ", userAgent=" + userAgent
					+ ", ipAddress=" + ipAddress + ", issuedAt=" + issuedAt + ", expiresAt=" + expiresAt + ", revoked="
					+ revoked + ", revokedAt=" + revokedAt + "]";
		}
	    
	    
	    
}
