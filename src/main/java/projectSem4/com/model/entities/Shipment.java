package projectSem4.com.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Shipment {
	private String shipmentID;
    private Integer shipperID;
    private String pickupAddress;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private int status;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedAt;
    private LocalDateTime deliveredAt;
    private String failedReason;
    private String proofImages; // JSON array string
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal shippingFee; 
    private BigDecimal discountShipping; 
    private BigDecimal finalFee; 
    private BigDecimal payoutToShipper; 
    private String orderID;
    private Integer refundID;
    public Shipment() {}
	


	



	public Shipment(String shipmentID, Integer shipperID, String pickupAddress, String deliveryAddress,
			String recipientName, String recipientPhone, int status, LocalDateTime assignedAt, LocalDateTime pickedAt,
			LocalDateTime deliveredAt, String failedReason, String proofImages, String notes, LocalDateTime createdAt,
			LocalDateTime updatedAt, BigDecimal shippingFee, BigDecimal discountShipping, BigDecimal finalFee,
			BigDecimal payoutToShipper, String orderID, Integer refundID) {
		super();
		this.shipmentID = shipmentID;
		this.shipperID = shipperID;
		this.pickupAddress = pickupAddress;
		this.deliveryAddress = deliveryAddress;
		this.recipientName = recipientName;
		this.recipientPhone = recipientPhone;
		this.status = status;
		this.assignedAt = assignedAt;
		this.pickedAt = pickedAt;
		this.deliveredAt = deliveredAt;
		this.failedReason = failedReason;
		this.proofImages = proofImages;
		this.notes = notes;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.shippingFee = shippingFee;
		this.discountShipping = discountShipping;
		this.finalFee = finalFee;
		this.payoutToShipper = payoutToShipper;
		this.orderID = orderID;
		this.refundID = refundID;
	}







	public String getOrderID() {
		return orderID;
	}







	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}







	public Integer getRefundID() {
		return refundID;
	}







	public void setRefundID(Integer refundID) {
		this.refundID = refundID;
	}







	public BigDecimal getShippingFee() {
		return shippingFee;
	}



	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}



	public BigDecimal getDiscountShipping() {
		return discountShipping;
	}



	public void setDiscountShipping(BigDecimal discountShipping) {
		this.discountShipping = discountShipping;
	}



	public BigDecimal getFinalFee() {
		return finalFee;
	}



	public void setFinalFee(BigDecimal finalFee) {
		this.finalFee = finalFee;
	}



	public BigDecimal getPayoutToShipper() {
		return payoutToShipper;
	}



	public void setPayoutToShipper(BigDecimal payoutToShipper) {
		this.payoutToShipper = payoutToShipper;
	}



	public String getShipmentID() {
		return shipmentID;
	}
	public void setShipmentID(String shipmentID) {
		this.shipmentID = shipmentID;
	}
	public Integer getShipperID() {
		return shipperID;
	}
	public void setShipperID(Integer shipperID) {
		this.shipperID = shipperID;
	}
	public String getPickupAddress() {
		return pickupAddress;
	}
	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
	public String getRecipientPhone() {
		return recipientPhone;
	}
	public void setRecipientPhone(String recipientPhone) {
		this.recipientPhone = recipientPhone;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public LocalDateTime getAssignedAt() {
		return assignedAt;
	}
	public void setAssignedAt(LocalDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}
	public LocalDateTime getPickedAt() {
		return pickedAt;
	}
	public void setPickedAt(LocalDateTime pickedAt) {
		this.pickedAt = pickedAt;
	}
	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}
	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}
	public String getFailedReason() {
		return failedReason;
	}
	public void setFailedReason(String failedReason) {
		this.failedReason = failedReason;
	}
	public String getProofImages() {
		return proofImages;
	}
	public void setProofImages(String proofImages) {
		this.proofImages = proofImages;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
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
		return "Shipment [shipmentID=" + shipmentID + ", shipperID=" + shipperID + ", pickupAddress=" + pickupAddress
				+ ", deliveryAddress=" + deliveryAddress + ", recipientName=" + recipientName + ", recipientPhone="
				+ recipientPhone + ", status=" + status + ", assignedAt=" + assignedAt + ", pickedAt=" + pickedAt
				+ ", deliveredAt=" + deliveredAt + ", failedReason=" + failedReason + ", proofImages=" + proofImages
				+ ", notes=" + notes + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
    
    
}
