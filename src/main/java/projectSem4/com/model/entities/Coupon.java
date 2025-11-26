package projectSem4.com.model.entities;

import projectSem4.com.model.enums.CouponEnums.CouponType;
import projectSem4.com.model.enums.CouponEnums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Coupon {
    private Integer couponId;
    private String code;
    private String couponType;       // PLATFORM / SHOP
    private String discountType;   // PERCENT / AMOUNT
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;      // optional (khi PERCENT)
    private BigDecimal minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer shopId;              // null nếu PLATFORM
    private Integer status;              // 0/1/2
    
    
    public Coupon() {}
    public Coupon(Integer couponId, String code, String couponType, String discountType, BigDecimal discountValue,
			BigDecimal maxDiscount, BigDecimal minOrderAmount, LocalDateTime startDate, LocalDateTime endDate,
			Integer usageLimit, Integer usedCount, Integer shopId, Integer status) {
		super();
		this.couponId = couponId;
		this.code = code;
		this.couponType = couponType;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.maxDiscount = maxDiscount;
		this.minOrderAmount = minOrderAmount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.usageLimit = usageLimit;
		this.usedCount = usedCount;
		this.shopId = shopId;
		this.status = status;
	}
	public Integer getCouponId() { return couponId; }
    public void setCouponId(Integer couponID) { this.couponId = couponID; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCouponType() { return couponType; }
    public void setCouponType(String couponType) { this.couponType = couponType; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopID) { this.shopId = shopID; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
