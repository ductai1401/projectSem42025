package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class ShopStat {
	private int shopStatId ;
	private int shopId ;
	private int year ;
	private int week ;
	private int month ;
	private String periodType ;
	private Double totalRevenue ;
	private Double flashSaleRevenue ;
	private int orderCount ;
	private LocalDateTime updatedAt ;
	
	public ShopStat() {}
	
	public ShopStat(int shopStatId, int shopId, int year, int week, int month, String periodType, Double totalRevenue,
			Double flashSaleRevenue, int orderCount, LocalDateTime updatedAt) {
		super();
		this.shopStatId = shopStatId;
		this.shopId = shopId;
		this.year = year;
		this.week = week;
		this.month = month;
		this.periodType = periodType;
		this.totalRevenue = totalRevenue;
		this.flashSaleRevenue = flashSaleRevenue;
		this.orderCount = orderCount;
		this.updatedAt = updatedAt;
	}

	public int getShopStatId() {
		return shopStatId;
	}

	public void setShopStatId(int shopStatId) {
		this.shopStatId = shopStatId;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getPeriodType() {
		return periodType;
	}

	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}

	public Double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(Double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public Double getFlashSaleRevenue() {
		return flashSaleRevenue;
	}

	public void setFlashSaleRevenue(Double flashSaleRevenue) {
		this.flashSaleRevenue = flashSaleRevenue;
	}

	public int getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "ShopStat [shopStatId=" + shopStatId + ", shopId=" + shopId + ", year=" + year + ", week=" + week
				+ ", month=" + month + ", periodType=" + periodType + ", totalRevenue=" + totalRevenue
				+ ", flashSaleRevenue=" + flashSaleRevenue + ", orderCount=" + orderCount + ", updatedAt=" + updatedAt
				+ "]";
	}
	
	
	
}
