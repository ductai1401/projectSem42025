package projectSem4.com.model.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Shop {
	private int shopId;
	private int userId;
	private String shopName;
	private String description;
	private String logo;
	private double rating;
	private int status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	
	public Shop() {}
	
	public Shop(int shopId, int userId, String shopName, String description, String logo, double rating, int status,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.shopId = shopId;
		this.userId = userId;
		this.shopName = shopName;
		this.description = description;
		this.logo = logo;
		this.rating = rating;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	public int getShopId() {
		return shopId;
	}
	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
		return "Shop [shopId=" + shopId + ", userId=" + userId + ", shopName=" + shopName + ", description="
				+ description + ", logo=" + logo + ", rating=" + rating + ", status=" + status + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
	
}
