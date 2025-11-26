package projectSem4.com.model.modelViews;

import java.time.LocalDateTime;

public class OrderDiscountView {
	private int orderDiscountId;
	private String orderId;
	private int couponId;
	private String couponCode;
	private String discountType;
	private String couponType;
	private String description;
	private Double discountValue;
	private LocalDateTime createdAt;
	
	public OrderDiscountView() {}
	
	
	public OrderDiscountView(int orderDiscountId, String orderId, int couponId, String couponCode, String discountType,
			String couponType, String description, Double discountValue, LocalDateTime createdAt) {
		super();
		this.orderDiscountId = orderDiscountId;
		this.orderId = orderId;
		this.couponId = couponId;
		this.couponCode = couponCode;
		this.discountType = discountType;
		this.couponType = couponType;
		this.description = description;
		this.discountValue = discountValue;
		this.createdAt = createdAt;
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

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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


	public String getCouponType() {
		return couponType;
	}


	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}


	@Override
	public String toString() {
		return "OrderDiscountView [orderDiscountId=" + orderDiscountId + ", orderId=" + orderId + ", couponId="
				+ couponId + ", couponCode=" + couponCode + ", discountType=" + discountType + ", couponType="
				+ couponType + ", description=" + description + ", discountValue=" + discountValue + ", createdAt="
				+ createdAt + "]";
	}

	
	
	
}
