package projectSem4.com.model.modelViews;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import projectSem4.com.model.entities.Addresses;

public class ShopView {
	private int shopId;
	private int userId;
	private String fullName;
	private String shopName;
	private String description;
	private String logo;
	private double rating;
	private int status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String image;
	private String phone;
	private String email;
	private String address;
	private BigDecimal TotalRevenue;
	
	public ShopView() {}
	
	



	public ShopView(int shopId, int userId, String fullName, String shopName, String description, String logo,
			double rating, int status, LocalDateTime createdAt, LocalDateTime updatedAt, String image, String phone,
			String email, String address, BigDecimal totalRevenue) {
		super();
		this.shopId = shopId;
		this.userId = userId;
		this.fullName = fullName;
		this.shopName = shopName;
		this.description = description;
		this.logo = logo;
		this.rating = rating;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.image = image;
		this.phone = phone;
		this.email = email;
		this.address = address;
		TotalRevenue = totalRevenue;
	}





	public BigDecimal getTotalRevenue() {
		return TotalRevenue;
	}





	public void setTotalRevenue(BigDecimal totalRevenue) {
		TotalRevenue = totalRevenue;
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	

	public String getAddress() {
		return address;
	}



	public void setAddress(String address) {
		this.address = address;
	}



	@Override
	public String toString() {
		return "ShopView [shopId=" + shopId + ", userId=" + userId + ", fullName=" + fullName + ", shopName=" + shopName
				+ ", description=" + description + ", logo=" + logo + ", rating=" + rating + ", status=" + status
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", image=" + image + ", phone=" + phone
				+ ", email=" + email + ", address=" + address + "]";
	}



	
	
	
}
