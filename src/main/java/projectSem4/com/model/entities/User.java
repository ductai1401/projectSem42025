package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class User {
	 private Integer userId;
	    private String roleId;
	    private String fullName;
	    private String email;
	    private String passwordHash;
	    private String addresses;
	    private String phoneNumber;
	    private Integer status;
	    private String image;
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;

	    // Constructors
	    public User() {
	    }

	    public User(Integer userId, String roleId, String fullName, String email, String passwordHash, String addresses,
	                 String phoneNumber, Integer status, String image,LocalDateTime createdAt, LocalDateTime updatedAt) {
	        this.userId = userId;
	        this.roleId = roleId;
	        this.fullName = fullName;
	        this.email = email;
	        this.passwordHash = passwordHash;
	        this.addresses = addresses;
	        this.phoneNumber = phoneNumber;
	        this.status = status;
	        this.image= image;
	        this.createdAt = createdAt;
	        this.updatedAt = updatedAt;
	    }

	    // Getters and Setters
	    public Integer getUserId() {
	        return userId;
	    }

	    public void setUserId(Integer userId) {
	        this.userId = userId;
	    }

	    public String getRoleId() {
	        return roleId;
	    }

	    public void setRoleId(String roleId) {
	        this.roleId = roleId;
	    }

	    public String getFullName() {
	        return fullName;
	    }

	    public void setFullName(String fullName) {
	        this.fullName = fullName;
	    }

	    public String getEmail() {
	        return email;
	    }

	    public void setEmail(String email) {
	        this.email = email;
	    }

	    public String getPasswordHash() {
	        return passwordHash;
	    }

	    public void setPasswordHash(String passwordHash) {
	        this.passwordHash = passwordHash;
	    }

	    public String getAddresses() {
	        return addresses;
	    }

	    public void setAddresses(String addresses) {
	        this.addresses = addresses;
	    }

	    public String getPhoneNumber() {
	        return phoneNumber;
	    }

	    public void setPhoneNumber(String phoneNumber) {
	        this.phoneNumber = phoneNumber;
	    }

	    public Integer getStatus() {
	        return status;
	    }

	    public void setStatus(Integer status) {
	        this.status = status;
	    }
	    
	    public String getImage() {
	        return image;
	    }

	    public void setImage(String image) {
	        this.image = image;
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

	    
	    @Override
	    public String toString() {
	        return "Users{" +
	                "userId=" + userId +
	                ", roleId='" + roleId + '\'' +
	                ", fullName='" + fullName + '\'' +
	                ", email='" + email + '\'' +
	                ", passwordHash='" + passwordHash + '\'' +
	                ", addresses='" + addresses + '\'' +
	                ", phoneNumber='" + phoneNumber + '\'' +
	                ", status=" + status +
	                ", image=" + image +
	                ", createdAt=" + createdAt +
	                ", updatedAt=" + updatedAt +
	                '}';
	    }
}
