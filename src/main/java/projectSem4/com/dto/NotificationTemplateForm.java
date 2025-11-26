package projectSem4.com.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationTemplateForm {

    @NotBlank
    private String code;

    @NotBlank
    private String content;

    private String redirectUrl;

    @NotNull
    private Integer type;

    private String title;

    private Boolean active = true;

    // === Getters & Setters ===
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
