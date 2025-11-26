package projectSem4.com.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponForm {

    private Integer couponID;

    @NotBlank(message = "Code is required")
    private String code;

    // "PLATFORM" hoặc "SHOP" – admin create luôn là PLATFORM
    private String couponType;

    @NotBlank(message = "Discount type is required")
    private String discountType; // PERCENT | AMOUNT

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.00", message = "Discount value must be >= 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Max discount must be >= 0")
    private BigDecimal maxDiscount;

    @NotNull(message = "Min order amount is required")
    @DecimalMin(value = "0.00", message = "Min order amount must be >= 0")
    private BigDecimal minOrderAmount;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @NotNull(message = "Usage limit is required")
    @Min(value = 0, message = "Usage limit must be >= 0")
    private Integer usageLimit;

    @NotNull
    private Integer status; // 1 active, 0 inactive

    // getters/setters ...
    public Integer getCouponID() { return couponID; }
    public void setCouponID(Integer couponID) { this.couponID = couponID; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
