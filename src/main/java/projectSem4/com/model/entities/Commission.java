package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class Commission {
	private int commissionId ;
	private int  shopId ;
	private int  categoryId ;
	private Double minRevenue ;
	private String commissionType ;
	private Double rateValue ;
	private LocalDateTime startDate ;
	private LocalDateTime  endDate ;
	private int  status = 1;
	private String description ;
	
	public Commission() {}
	
	
	public Commission(int commissionId, int shopId, int categoryId, Double minRevenue, String commissionType,
			Double rateValue, LocalDateTime startDate, LocalDateTime endDate, int status, String description) {
		super();
		this.commissionId = commissionId;
		this.shopId = shopId;
		this.categoryId = categoryId;
		this.minRevenue = minRevenue;
		this.commissionType = commissionType;
		this.rateValue = rateValue;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.description = description;
	}


	public int getCommissionId() {
		return commissionId;
	}


	public void setCommissionId(int commissionId) {
		this.commissionId = commissionId;
	}


	public int getShopId() {
		return shopId;
	}


	public void setShopId(int shopId) {
		this.shopId = shopId;
	}


	public int getCategoryId() {
		return categoryId;
	}


	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}


	public Double getMinRevenue() {
		return minRevenue;
	}


	public void setMinRevenue(Double minRevenue) {
		this.minRevenue = minRevenue;
	}


	public String getCommissionType() {
		return commissionType;
	}


	public void setCommissionType(String commissionType) {
		this.commissionType = commissionType;
	}


	public Double getRateValue() {
		return rateValue;
	}


	public void setRateValue(Double rateValue) {
		this.rateValue = rateValue;
	}


	public LocalDateTime getStartDate() {
		return startDate;
	}


	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}


	public LocalDateTime getEndDate() {
		return endDate;
	}


	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public String toString() {
		return "Commission [commissionId=" + commissionId + ", shopId=" + shopId + ", categoryId=" + categoryId
				+ ", minRevenue=" + minRevenue + ", commissionType=" + commissionType + ", rateValue=" + rateValue
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", status=" + status + ", description="
				+ description + "]";
	}
	
	
}
