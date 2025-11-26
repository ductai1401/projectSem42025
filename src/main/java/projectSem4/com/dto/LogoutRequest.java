package projectSem4.com.dto;

public class LogoutRequest {
	private String refreshToken;
    private String deviceId;
    
    public LogoutRequest() {}
    
	public LogoutRequest(String refreshToken, String deviceId) {
		super();
		this.refreshToken = refreshToken;
		this.deviceId = deviceId;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	@Override
	public String toString() {
		return "LogoutRequest [refreshToken=" + refreshToken + ", deviceId=" + deviceId + "]";
	}
	    
	    
	    
		
		
		
}
