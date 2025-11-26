package projectSem4.com.dto;

import java.math.BigDecimal;

public class WalletStatsDTO {
	private BigDecimal totalBaseAmount;      // Tổng doanh thu
    private BigDecimal totalCommission;      // Tổng hoa hồng
    private BigDecimal totalShopNetIncome;   // Tổng thu nhập ròng shop
    private BigDecimal totalDiscount;        // Tổng giảm giá
    private BigDecimal totalRefund;          // Tổng hoàn tiền
    private Integer totalOrders;             // Tổng số đơn hàng
	
    public WalletStatsDTO() {}
    public WalletStatsDTO(BigDecimal totalBaseAmount, BigDecimal totalCommission, BigDecimal totalShopNetIncome,
			BigDecimal totalDiscount, BigDecimal totalRefund, Integer totalOrders) {
		super();
		this.totalBaseAmount = totalBaseAmount;
		this.totalCommission = totalCommission;
		this.totalShopNetIncome = totalShopNetIncome;
		this.totalDiscount = totalDiscount;
		this.totalRefund = totalRefund;
		this.totalOrders = totalOrders;
	}
    
    
    
    public BigDecimal getTotalBaseAmount() {
		return totalBaseAmount;
	}
	public void setTotalBaseAmount(BigDecimal totalBaseAmount) {
		this.totalBaseAmount = totalBaseAmount;
	}
	public BigDecimal getTotalCommission() {
		return totalCommission;
	}
	public void setTotalCommission(BigDecimal totalCommission) {
		this.totalCommission = totalCommission;
	}
	public BigDecimal getTotalShopNetIncome() {
		return totalShopNetIncome;
	}
	public void setTotalShopNetIncome(BigDecimal totalShopNetIncome) {
		this.totalShopNetIncome = totalShopNetIncome;
	}
	public BigDecimal getTotalDiscount() {
		return totalDiscount;
	}
	public void setTotalDiscount(BigDecimal totalDiscount) {
		this.totalDiscount = totalDiscount;
	}
	public BigDecimal getTotalRefund() {
		return totalRefund;
	}
	public void setTotalRefund(BigDecimal totalRefund) {
		this.totalRefund = totalRefund;
	}
	public Integer getTotalOrders() {
		return totalOrders;
	}
	public void setTotalOrders(Integer totalOrders) {
		this.totalOrders = totalOrders;
	}
	@Override
	public String toString() {
		return "WalletStatsDTO [totalBaseAmount=" + totalBaseAmount + ", totalCommission=" + totalCommission
				+ ", totalShopNetIncome=" + totalShopNetIncome + ", totalDiscount=" + totalDiscount + ", totalRefund="
				+ totalRefund + ", totalOrders=" + totalOrders + "]";
	}


	
    
    
    
    
}
