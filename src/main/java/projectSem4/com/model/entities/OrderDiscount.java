package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class OrderDiscount {
	private int orderDiscountId;
	private String orderId;
	private int couponId;
	private String discountType;
	private String description;
	private Double discountValue;
	private LocalDateTime createdAt;
	
	public OrderDiscount() {}
	
	

	public OrderDiscount(int orderDiscountId, String orderId, int couponId, String discountType, String description,
			Double discountValue, LocalDateTime createdAt) {
		super();
		this.orderDiscountId = orderDiscountId;
		this.orderId = orderId;
		this.couponId = couponId;
		this.discountType = discountType;
		this.description = description;
		this.discountValue = discountValue;
		this.createdAt = createdAt;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public int getOrderDiscountId() {
		return orderDiscountId;
	}

	public void setOrderDiscountId(int orderDiscountId) {
		this.orderDiscountId = orderDiscountId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getCouponId() {
		return couponId;
	}

	public void setCouponId(int couponId) {
		this.couponId = couponId;
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}

	public Double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(Double discountValue) {
		this.discountValue = discountValue;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}



	@Override
	public String toString() {
		return "OrderDiscount [orderDiscountId=" + orderDiscountId + ", orderId=" + orderId + ", couponId=" + couponId
				+ ", discountType=" + discountType + ", description=" + description + ", discountValue=" + discountValue
				+ ", createdAt=" + createdAt + "]";
	}

	
	
	
}
