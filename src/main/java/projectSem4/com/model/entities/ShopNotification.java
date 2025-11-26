package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class ShopNotification {
	private int notificationId; 
    private int shopId ;
    private String title ;
    private String message; 
    private int type ;
    private String redirectUrl ;
    private int isRead ;
    private LocalDateTime CreatedAt;
	
    public ShopNotification() {}
    
    public ShopNotification(int notificationId, int shopId, String title, String message, int type, String redirectUrl,
			int isRead, LocalDateTime createdAt) {
		super();
		this.notificationId = notificationId;
		this.shopId = shopId;
		this.title = title;
		this.message = message;
		this.type = type;
		this.redirectUrl = redirectUrl;
		this.isRead = isRead;
		CreatedAt = createdAt;
	}



	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public int getIsRead() {
		return isRead;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}

	public LocalDateTime getCreatedAt() {
		return CreatedAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		CreatedAt = createdAt;
	}

	@Override
	public String toString() {
		return "ShopNotification [notificationId=" + notificationId + ", shopId=" + shopId + ", title=" + title
				+ ", message=" + message + ", type=" + type + ", redirectUrl=" + redirectUrl + ", isRead=" + isRead
				+ ", CreatedAt=" + CreatedAt + "]";
	}
    
    
}
