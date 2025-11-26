package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class BuyerNotification {
	private Integer notificationId;
	private Integer userId;
	private Integer templateId; // FK tới NotificationTemplate
	private String content; // snapshot từ template
	private String redirectUrl; // snapshot từ template
	private Integer type; // snapshot từ template
	private LocalDateTime createdAt;
	private Boolean isRead;
	private Integer status; // 1 = mới, 2 = đã click/mở (tuỳ bạn quy ước)

	public BuyerNotification() {
	}

	public BuyerNotification(Integer notificationId, Integer userId, Integer templateId, String content,
			String redirectUrl, Integer type, LocalDateTime createdAt, Boolean isRead, Integer status) {
		this.notificationId = notificationId;
		this.userId = userId;
		this.templateId = templateId;
		this.content = content;
		this.redirectUrl = redirectUrl;
		this.type = type;
		this.createdAt = createdAt;
		this.isRead = isRead;
		this.status = status;
	}

	// Getters / Setters
	public Integer getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
