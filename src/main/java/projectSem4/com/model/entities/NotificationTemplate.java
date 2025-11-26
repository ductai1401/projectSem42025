package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class NotificationTemplate {
	private Integer templateId;
	private String code; // UNIQUE
	private String title;
	private String content; // có thể chứa {{placeholder}}
	private String redirectUrl;
	private Integer type; // 0..3
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// getters/setters
	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public NotificationTemplate(Integer templateId, String code, String title, String content, String redirectUrl,
			Integer type, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.templateId = templateId;
		this.code = code;
		this.title = title;
		this.content = content;
		this.redirectUrl = redirectUrl;
		this.type = type;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public NotificationTemplate() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
