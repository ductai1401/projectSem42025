package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class Order {
	private String orderId ;
	private int  buyerId ;
	private int  shopId ;
	private LocalDateTime  orderDate ;
	private Double totalAmount ;
	private int  status ;
	private String paymentMethod ;
	private int flashSaleStatus;
	private Double refund ;
	private Double ShippingFee;
	private Address deliveryAddressJson;
	private String orderNote;
	private String cancelReason ;
	private LocalDateTime cancelledAt ;
	private LocalDateTime confirmedAt ;
	private LocalDateTime completedAt;
	
	public Order() {}



	public Order(String orderId, int buyerId, int shopId, LocalDateTime orderDate, Double totalAmount, int status,
			String paymentMethod, int flashSaleStatus, Double refund, Double shippingFee, Address deliveryAddressJson,
			String orderNote, String cancelReason, LocalDateTime cancelledAt, LocalDateTime confirmedAt,
			LocalDateTime completedAt) {
		super();
		this.orderId = orderId;
		this.buyerId = buyerId;
		this.shopId = shopId;
		this.orderDate = orderDate;
		this.totalAmount = totalAmount;
		this.status = status;
		this.paymentMethod = paymentMethod;
		this.flashSaleStatus = flashSaleStatus;
		this.refund = refund;
		ShippingFee = shippingFee;
		this.deliveryAddressJson = deliveryAddressJson;
		this.orderNote = orderNote;
		this.cancelReason = cancelReason;
		this.cancelledAt = cancelledAt;
		this.confirmedAt = confirmedAt;
		this.completedAt = completedAt;
	}



	public String getOrderNote() {
		return orderNote;
	}



	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}



	public String getCancelReason() {
		return cancelReason;
	}



	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}



	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}



	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}



	public LocalDateTime getConfirmedAt() {
		return confirmedAt;
	}



	public void setConfirmedAt(LocalDateTime confirmedAt) {
		this.confirmedAt = confirmedAt;
	}



	public LocalDateTime getCompletedAt() {
		return completedAt;
	}



	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}



	public Address getDeliveryAddressJson() {
		return deliveryAddressJson;
	}


	public void setDeliveryAddressJson(Address deliveryAddressJson) {
		this.deliveryAddressJson = deliveryAddressJson;
	}


	public Double getShippingFee() {
		return ShippingFee;
	}





	public void setShippingFee(Double shippingFee) {
		ShippingFee = shippingFee;
	}





	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(int buyerId) {
		this.buyerId = buyerId;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}


	public int getFlashSaleStatus() {
		return flashSaleStatus;
	}

	public void setFlashSaleStatus(int flashSaleStatus) {
		this.flashSaleStatus = flashSaleStatus;
	}

	public Double getRefund() {
		return refund;
	}


	public void setRefund(Double refund) {
		this.refund = refund;
	}



	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", buyerId=" + buyerId + ", shopId=" + shopId + ", orderDate=" + orderDate
				+ ", totalAmount=" + totalAmount + ", status=" + status + ", paymentMethod=" + paymentMethod
				+ ", flashSaleStatus=" + flashSaleStatus + ", refund=" + refund + ", ShippingFee=" + ShippingFee
				+ ", deliveryAddressJson=" + deliveryAddressJson + ", orderNote=" + orderNote + ", cancelReason="
				+ cancelReason + ", cancelledAt=" + cancelledAt + ", confirmedAt=" + confirmedAt + ", completedAt="
				+ completedAt + "]";
	}


	


	


	
}
