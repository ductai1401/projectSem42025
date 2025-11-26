package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class FlashSale {
	private Integer flashSaleId;
	private Integer shopId;
	private String name;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Integer status;
	public Integer getFlashSaleId() {
		return flashSaleId;
	}

	public void setFlashSaleId(Integer flashSaleId) {
		this.flashSaleId = flashSaleId;
	}

	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	

	public FlashSale(Integer flashSaleId, Integer shopId, String name, LocalDateTime startDate, LocalDateTime endDate,
			Integer status) {
		super();
		this.flashSaleId = flashSaleId;
		this.shopId = shopId;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public FlashSale() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
