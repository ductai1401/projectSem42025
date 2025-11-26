package projectSem4.com.model.modelViews;

import java.time.LocalDateTime;
import java.util.List;

public class RefundView {
	private Integer refundId;
    private String orderId;
    private String paymentId;
    private Integer buyerId;
    private String buyerPhone;
    private String buyerName;
    private Integer shopId;
    private String shopName;
    private Double amount;
    private String reason;
    private String status;
    private String evidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processDate;
    private String refundType;
    private String notes;
    private Double originalAmount;
    private String history;
    private String refundMethod;
    private Integer processedBy;
    private LocalDateTime adminApprovedAt;
    private LocalDateTime adminRejectedAt;
    private String adminNotes;
    private Integer processedByAdmin;
    private LocalDateTime shopRejectedAt;
    private List<RefundItemView> refundItem;

    
    public RefundView( ) {}
    



	public RefundView(Integer refundId, String orderId, String paymentId, Integer buyerId, String buyerPhone,
			String buyerName, Integer shopId, String shopName, Double amount, String reason, String status,
			String evidence, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime processDate,
			String refundType, String notes, Double originalAmount, String history, String refundMethod,
			Integer processedBy, LocalDateTime adminApprovedAt, LocalDateTime adminRejectedAt, String adminNotes,
			Integer processedByAdmin, LocalDateTime shopRejectedAt, List<RefundItemView> refundItem) {
		super();
		this.refundId = refundId;
		this.orderId = orderId;
		this.paymentId = paymentId;
		this.buyerId = buyerId;
		this.buyerPhone = buyerPhone;
		this.buyerName = buyerName;
		this.shopId = shopId;
		this.shopName = shopName;
		this.amount = amount;
		this.reason = reason;
		this.status = status;
		this.evidence = evidence;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.processDate = processDate;
		this.refundType = refundType;
		this.notes = notes;
		this.originalAmount = originalAmount;
		this.history = history;
		this.refundMethod = refundMethod;
		this.processedBy = processedBy;
		this.adminApprovedAt = adminApprovedAt;
		this.adminRejectedAt = adminRejectedAt;
		this.adminNotes = adminNotes;
		this.processedByAdmin = processedByAdmin;
		this.shopRejectedAt = shopRejectedAt;
		this.refundItem = refundItem;
	}




	public LocalDateTime getAdminApprovedAt() {
		return adminApprovedAt;
	}




	public void setAdminApprovedAt(LocalDateTime adminApprovedAt) {
		this.adminApprovedAt = adminApprovedAt;
	}




	public LocalDateTime getAdminRejectedAt() {
		return adminRejectedAt;
	}




	public void setAdminRejectedAt(LocalDateTime adminRejectedAt) {
		this.adminRejectedAt = adminRejectedAt;
	}




	public String getAdminNotes() {
		return adminNotes;
	}




	public void setAdminNotes(String adminNotes) {
		this.adminNotes = adminNotes;
	}




	public Integer getProcessedByAdmin() {
		return processedByAdmin;
	}




	public void setProcessedByAdmin(Integer processedByAdmin) {
		this.processedByAdmin = processedByAdmin;
	}




	public LocalDateTime getShopRejectedAt() {
		return shopRejectedAt;
	}




	public void setShopRejectedAt(LocalDateTime shopRejectedAt) {
		this.shopRejectedAt = shopRejectedAt;
	}




	public String getShopName() {
		return shopName;
	}




	public void setShopName(String shopName) {
		this.shopName = shopName;
	}




	public List<RefundItemView> getRefundItem() {
		return refundItem;
	}

	public void setRefundItem(List<RefundItemView> refundItem) {
		this.refundItem = refundItem;
	}

	public Integer getRefundId() {
		return refundId;
	}



	public void setRefundId(Integer refundId) {
		this.refundId = refundId;
	}



	public String getOrderId() {
		return orderId;
	}



	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}



	public String getPaymentId() {
		return paymentId;
	}



	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}



	public Integer getBuyerId() {
		return buyerId;
	}



	public void setBuyerId(Integer buyerId) {
		this.buyerId = buyerId;
	}



	public String getBuyerPhone() {
		return buyerPhone;
	}



	public void setBuyerPhone(String buyerPhone) {
		this.buyerPhone = buyerPhone;
	}



	public String getBuyerName() {
		return buyerName;
	}



	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}



	public Integer getShopId() {
		return shopId;
	}



	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	public String getReason() {
		return reason;
	}



	public void setReason(String reason) {
		this.reason = reason;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getEvidence() {
		return evidence;
	}



	public void setEvidence(String evidence) {
		this.evidence = evidence;
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



	public LocalDateTime getProcessDate() {
		return processDate;
	}



	public void setProcessDate(LocalDateTime processDate) {
		this.processDate = processDate;
	}



	public String getRefundType() {
		return refundType;
	}



	public void setRefundType(String refundType) {
		this.refundType = refundType;
	}



	public String getNotes() {
		return notes;
	}



	public void setNotes(String notes) {
		this.notes = notes;
	}



	public Double getOriginalAmount() {
		return originalAmount;
	}



	public void setOriginalAmount(Double originalAmount) {
		this.originalAmount = originalAmount;
	}



	public String getHistory() {
		return history;
	}



	public void setHistory(String history) {
		this.history = history;
	}



	public String getRefundMethod() {
		return refundMethod;
	}



	public void setRefundMethod(String refundMethod) {
		this.refundMethod = refundMethod;
	}



	public Integer getProcessedBy() {
		return processedBy;
	}



	public void setProcessedBy(Integer processedBy) {
		this.processedBy = processedBy;
	}

	@Override
	public String toString() {
		return "RefundView [refundId=" + refundId + ", orderId=" + orderId + ", paymentId=" + paymentId + ", buyerId="
				+ buyerId + ", buyerPhone=" + buyerPhone + ", buyerName=" + buyerName + ", shopId=" + shopId
				+ ", amount=" + amount + ", reason=" + reason + ", status=" + status + ", evidence=" + evidence
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", processDate=" + processDate
				+ ", refundType=" + refundType + ", notes=" + notes + ", originalAmount=" + originalAmount
				+ ", history=" + history + ", refundMethod=" + refundMethod + ", processedBy=" + processedBy
				+ ", refundItem=" + refundItem + "]";
	}



    
}
