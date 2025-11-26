package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class Payment {
	private int paymentId; 
	private double paymentAmount;
	private LocalDateTime paymentDate;
	private String paymentMethod;
	private int status;
	private Double refundAmount;
	private String transactionCode;
    private String gatewayTransactionNo; 
    private String paymentUrl;
    private LocalDateTime ExpiredAt;
	private LocalDateTime createdAt;
	private LocalDateTime UpdatedAt;
	
	public Payment() {}

	


	public Payment(int paymentId, double paymentAmount, LocalDateTime paymentDate, String paymentMethod, int status,
			Double refundAmount, String transactionCode, String gatewayTransactionNo, String paymentUrl,
			LocalDateTime expiredAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.paymentId = paymentId;
		this.paymentAmount = paymentAmount;
		this.paymentDate = paymentDate;
		this.paymentMethod = paymentMethod;
		this.status = status;
		this.refundAmount = refundAmount;
		this.transactionCode = transactionCode;
		this.gatewayTransactionNo = gatewayTransactionNo;
		this.paymentUrl = paymentUrl;
		ExpiredAt = expiredAt;
		this.createdAt = createdAt;
		UpdatedAt = updatedAt;
	}




	public String getPaymentUrl() {
		return paymentUrl;
	}




	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}




	public LocalDateTime getExpiredAt() {
		return ExpiredAt;
	}




	public void setExpiredAt(LocalDateTime expiredAt) {
		ExpiredAt = expiredAt;
	}




	public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public double getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(double paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return UpdatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		UpdatedAt = updatedAt;
	}




	public String getGatewayTransactionNo() {
		return gatewayTransactionNo;
	}




	public void setGatewayTransactionNo(String gatewayTransactionNo) {
		this.gatewayTransactionNo = gatewayTransactionNo;
	}




	@Override
	public String toString() {
		return "Payment [paymentId=" + paymentId + ", paymentAmount=" + paymentAmount + ", paymentDate=" + paymentDate
				+ ", paymentMethod=" + paymentMethod + ", status=" + status + ", refundAmount=" + refundAmount
				+ ", transactionCode=" + transactionCode + ", gatewayTransactionNo=" + gatewayTransactionNo
				+ ", paymentUrl=" + paymentUrl + ", ExpiredAt=" + ExpiredAt + ", createdAt=" + createdAt
				+ ", UpdatedAt=" + UpdatedAt + "]";
	}


	
	
}
