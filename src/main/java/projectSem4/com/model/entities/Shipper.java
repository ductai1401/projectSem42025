package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class Shipper {
	private Integer shipperID;
	private int userID;
	private String fullName;
	private String phoneNumber;
	private int status; // 1: Active, 2: Inactive
	private boolean isAvailable; // true: rảnh, false: đang giao
	private int totalDeliveries;
	private double rating;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public Shipper() {}
	public Shipper(Integer shipperID, int userID, String fullName, String phoneNumber, int status, boolean isAvailable,
			int totalDeliveries, double rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.shipperID = shipperID;
		this.userID = userID;
		this.fullName = fullName;
		this.phoneNumber = phoneNumber;
		this.status = status;
		this.isAvailable = isAvailable;
		this.totalDeliveries = totalDeliveries;
		this.rating = rating;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	
	public Integer getShipperID() {
		return shipperID;
	}
	public void setShipperID(Integer shipperID) {
		this.shipperID = shipperID;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	public int getTotalDeliveries() {
		return totalDeliveries;
	}
	public void setTotalDeliveries(int totalDeliveries) {
		this.totalDeliveries = totalDeliveries;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
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
		return "Shipper [shipperID=" + shipperID + ", userID=" + userID + ", fullName=" + fullName + ", phoneNumber="
				+ phoneNumber + ", status=" + status + ", isAvailable=" + isAvailable + ", totalDeliveries="
				+ totalDeliveries + ", rating=" + rating + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ "]";
	}
	
	
}
