package projectSem4.com.dto;

import java.util.List;

public class AuthResponse {
	private String accessToken;
    private String refreshToken;
    private int userId;
    private List<String> roles;

    public AuthResponse() {}
    
    

	public AuthResponse(String accessToken, String refreshToken, int userId, List<String> roles) {
		super();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.userId = userId;
		this.roles = roles;
	}



	public int getUserId() {
		return userId;
	}



	public void setUserId(int userId) {
		this.userId = userId;
	}



	public List<String> getRoles() {
		return roles;
	}



	public void setRoles(List<String> roles) {
		this.roles = roles;
	}



	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}



	@Override
	public String toString() {
		return "AuthResponse [accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", userId=" + userId
				+ ", roles=" + roles + "]";
	}

		
    
    
}
