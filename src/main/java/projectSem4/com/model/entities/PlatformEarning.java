package projectSem4.com.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlatformEarning {
	private int earningId;
    private String orderId;
    private int shopId;
    private BigDecimal baseAmount;
    private BigDecimal discountPlatform;
    private BigDecimal discountShop;
    private BigDecimal shippingFee;
    private String voucherCode;

    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;

    private BigDecimal refundedAmount;
    private BigDecimal refundedCommission;
    private BigDecimal adjustedCommission;

    private BigDecimal platformGrossIncome;
    private BigDecimal platformNetIncome;
    private BigDecimal shopNetIncome;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public PlatformEarning() {}
	public PlatformEarning(int earningId, String orderId, int shopId, BigDecimal baseAmount,
			BigDecimal discountPlatform, BigDecimal discountShop, BigDecimal shippingFee, String voucherCode,
			BigDecimal commissionRate, BigDecimal commissionAmount, BigDecimal refundedAmount,
			BigDecimal refundedCommission, BigDecimal adjustedCommission, BigDecimal platformGrossIncome,
			BigDecimal platformNetIncome, BigDecimal shopNetIncome, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.earningId = earningId;
		this.orderId = orderId;
		this.shopId = shopId;
		this.baseAmount = baseAmount;
		this.discountPlatform = discountPlatform;
		this.discountShop = discountShop;
		this.shippingFee = shippingFee;
		this.voucherCode = voucherCode;
		this.commissionRate = commissionRate;
		this.commissionAmount = commissionAmount;
		this.refundedAmount = refundedAmount;
		this.refundedCommission = refundedCommission;
		this.adjustedCommission = adjustedCommission;
		this.platformGrossIncome = platformGrossIncome;
		this.platformNetIncome = platformNetIncome;
		this.shopNetIncome = shopNetIncome;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	
	public int getEarningId() {
		return earningId;
	}


	public void setEarningId(int earningId) {
		this.earningId = earningId;
	}


	public String getOrderId() {
		return orderId;
	}


	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}


	public int getShopId() {
		return shopId;
	}


	public void setShopId(int shopId) {
		this.shopId = shopId;
	}


	public BigDecimal getBaseAmount() {
		return baseAmount;
	}


	public void setBaseAmount(BigDecimal baseAmount) {
		this.baseAmount = baseAmount;
	}


	public BigDecimal getDiscountPlatform() {
		return discountPlatform;
	}


	public void setDiscountPlatform(BigDecimal discountPlatform) {
		this.discountPlatform = discountPlatform;
	}


	public BigDecimal getDiscountShop() {
		return discountShop;
	}


	public void setDiscountShop(BigDecimal discountShop) {
		this.discountShop = discountShop;
	}


	public BigDecimal getShippingFee() {
		return shippingFee;
	}


	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}


	public String getVoucherCode() {
		return voucherCode;
	}


	public void setVoucherCode(String voucherCode) {
		this.voucherCode = voucherCode;
	}


	public BigDecimal getCommissionRate() {
		return commissionRate;
	}


	public void setCommissionRate(BigDecimal commissionRate) {
		this.commissionRate = commissionRate;
	}


	public BigDecimal getCommissionAmount() {
		return commissionAmount;
	}


	public void setCommissionAmount(BigDecimal commissionAmount) {
		this.commissionAmount = commissionAmount;
	}


	public BigDecimal getRefundedAmount() {
		return refundedAmount;
	}


	public void setRefundedAmount(BigDecimal refundedAmount) {
		this.refundedAmount = refundedAmount;
	}


	public BigDecimal getRefundedCommission() {
		return refundedCommission;
	}


	public void setRefundedCommission(BigDecimal refundedCommission) {
		this.refundedCommission = refundedCommission;
	}


	public BigDecimal getAdjustedCommission() {
		return adjustedCommission;
	}


	public void setAdjustedCommission(BigDecimal adjustedCommission) {
		this.adjustedCommission = adjustedCommission;
	}


	public BigDecimal getPlatformGrossIncome() {
		return platformGrossIncome;
	}


	public void setPlatformGrossIncome(BigDecimal platformGrossIncome) {
		this.platformGrossIncome = platformGrossIncome;
	}


	public BigDecimal getPlatformNetIncome() {
		return platformNetIncome;
	}


	public void setPlatformNetIncome(BigDecimal platformNetIncome) {
		this.platformNetIncome = platformNetIncome;
	}


	public BigDecimal getShopNetIncome() {
		return shopNetIncome;
	}


	public void setShopNetIncome(BigDecimal shopNetIncome) {
		this.shopNetIncome = shopNetIncome;
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
		return "PlatformEarning [earningId=" + earningId + ", orderId=" + orderId + ", shopId=" + shopId
				+ ", baseAmount=" + baseAmount + ", discountPlatform=" + discountPlatform + ", discountShop="
				+ discountShop + ", shippingFee=" + shippingFee + ", voucherCode=" + voucherCode + ", commissionRate="
				+ commissionRate + ", commissionAmount=" + commissionAmount + ", refundedAmount=" + refundedAmount
				+ ", refundedCommission=" + refundedCommission + ", adjustedCommission=" + adjustedCommission
				+ ", platformGrossIncome=" + platformGrossIncome + ", platformNetIncome=" + platformNetIncome
				+ ", shopNetIncome=" + shopNetIncome + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
    
    
    
}
