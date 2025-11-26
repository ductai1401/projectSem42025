package projectSem4.com.model.modelViews;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import projectSem4.com.model.entities.Address;



public class OrderView {
	private String orderId;
	private int buyerId;
	private String buyerName;
	private int shopId;
	private String shopName;
	private LocalDateTime orderDate;
	private Double totalAmount;
	private int status;
	private String paymentMethod;
	private String shipmentId;
	private Integer shipmentStatus;
	private Integer flashSaleStatus = 0;
	private Double refund;
	private Double shippingFee;
	private String orderNote;
	private String cancelReason ;
	private LocalDateTime cancelledAt ;
	private LocalDateTime confirmedAt ;
	private LocalDateTime completedAt;
	private Address deliveryAddressJson;
	private List<OrderItemView> items  = new ArrayList<>();
	private List<OrderDiscountView> discounts = new ArrayList<>();
	private boolean canRequestRefund = false;
	
	
	
	public OrderView() {}




	public OrderView(String orderId, int buyerId, String buyerName, int shopId, String shopName,
			LocalDateTime orderDate, Double totalAmount, int status, String paymentMethod, String shipmentId,
			Integer shipmentStatus, Integer flashSaleStatus, Double refund, Double shippingFee, String orderNote,
			String cancelReason, LocalDateTime cancelledAt, LocalDateTime confirmedAt, LocalDateTime completedAt,
			Address deliveryAddressJson, List<OrderItemView> items, List<OrderDiscountView> discounts,
			boolean canRequestRefund) {
		super();
		this.orderId = orderId;
		this.buyerId = buyerId;
		this.buyerName = buyerName;
		this.shopId = shopId;
		this.shopName = shopName;
		this.orderDate = orderDate;
		this.totalAmount = totalAmount;
		this.status = status;
		this.paymentMethod = paymentMethod;
		this.shipmentId = shipmentId;
		this.shipmentStatus = shipmentStatus;
		this.flashSaleStatus = flashSaleStatus;
		this.refund = refund;
		this.shippingFee = shippingFee;
		this.orderNote = orderNote;
		this.cancelReason = cancelReason;
		this.cancelledAt = cancelledAt;
		this.confirmedAt = confirmedAt;
		this.completedAt = completedAt;
		this.deliveryAddressJson = deliveryAddressJson;
		this.items = items;
		this.discounts = discounts;
		this.canRequestRefund = canRequestRefund;
	}




	public Address getDeliveryAddressJson() {
		return deliveryAddressJson;
	}




	public void setDeliveryAddressJson(Address deliveryAddressJson) {
		this.deliveryAddressJson = deliveryAddressJson;
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





	public boolean isCanRequestRefund() {
		return canRequestRefund;
	}


	public void setCanRequestRefund(boolean canRequestRefund) {
		this.canRequestRefund = canRequestRefund;
	}


	public Integer getShipmentStatus() {
		return shipmentStatus;
	}

	public void setShipmentStatus(Integer shipmentStatus) {
		this.shipmentStatus = shipmentStatus;
	}

	public List<OrderDiscountView> getDiscounts() {
		return discounts;
	}




	public void setDiscounts(List<OrderDiscountView> discounts) {
		this.discounts = discounts;
	}




	public String getBuyerName() {
		return buyerName;
	}




	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}




	public String getShopName() {
		return shopName;
	}




	public void setShopName(String shopName) {
		this.shopName = shopName;
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


	public String getShipmentId() {
		return shipmentId;
	}


	public void setShipmentId(String shipmentId) {
		this.shipmentId = shipmentId;
	}


	public Integer getFlashSaleStatus() {
		return flashSaleStatus;
	}


	public void setFlashSaleStatus(Integer flashSaleStatus) {
		this.flashSaleStatus = flashSaleStatus;
	}


	public Double getRefund() {
		return refund;
	}


	public void setRefund(Double refund) {
		this.refund = refund;
	}


	public Double getShippingFee() {
		return shippingFee;
	}


	public void setShippingFee(Double shippingFee) {
		this.shippingFee = shippingFee;
	}


	public List<OrderItemView> getItems() {
		return items;
	}


	public void setItems(List<OrderItemView> items) {
		this.items = items;
	}


	public String getStatusText() {
	    return switch (status) {
	        case 0 -> "Unpaid";
	        case 1 -> "Pending";
	        case 2 -> "Processing";
	        case 3 -> "Shipped";
	        case 4 -> "Cancel";
	        case 5 -> "Delivered";
	        default -> "Unknown";
	    };
	}





	@Override
	public String toString() {
		return "OrderView [orderId=" + orderId + ", buyerId=" + buyerId + ", buyerName=" + buyerName + ", shopId="
				+ shopId + ", shopName=" + shopName + ", orderDate=" + orderDate + ", totalAmount=" + totalAmount
				+ ", status=" + status + ", paymentMethod=" + paymentMethod + ", shipmentId=" + shipmentId
				+ ", shipmentStatus=" + shipmentStatus + ", flashSaleStatus=" + flashSaleStatus + ", refund=" + refund
				+ ", shippingFee=" + shippingFee + ", orderNote=" + orderNote + ", cancelReason=" + cancelReason
				+ ", cancelledAt=" + cancelledAt + ", confirmedAt=" + confirmedAt + ", completedAt=" + completedAt
				+ ", items=" + items + ", discounts=" + discounts + ", canRequestRefund=" + canRequestRefund + "]";
	}




	
}
