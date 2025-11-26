package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class InventoryLog {
	private Integer logId;
	private Long variantId; // bạn nói VariantID là long
	private String logType; // <— GIỮ LÀ STRING
	private Integer quantity;
	private double unitCost;
	private Integer remaining;
	private String refOrderId;
	private LocalDateTime createdAt;
	private Integer refImportId; 

	public Integer getLogId() {
		return logId;
	}

	public void setLogId(Integer logId) {
		this.logId = logId;
	}

	public Long getVariantId() {
		return variantId;
	}

	public void setVariantId(Long variantId) {
		this.variantId = variantId;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(double unitCost) {
		this.unitCost = unitCost;
	}

	public Integer getRemaining() {
		return remaining;
	}

	public void setRemaining(Integer remaining) {
		this.remaining = remaining;
	}

	public String getRefOrderId() {
		return refOrderId;
	}

	public void setRefOrderId(String refOrderId) {
		this.refOrderId = refOrderId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	

	public Integer getRefImportId() {
		return refImportId;
	}

	public void setRefImportId(Integer refImportId) {
		this.refImportId = refImportId;
	}

	public InventoryLog(Integer logId, Long variantId, String logType, Integer quantity, double unitCost,
			Integer remaining, String refOrderId, LocalDateTime createdAt, Integer refImportId) {
		super();
		this.logId = logId;
		this.variantId = variantId;
		this.logType = logType;
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.remaining = remaining;
		this.refOrderId = refOrderId;
		this.createdAt = createdAt;
		this.refImportId = refImportId;
	}

	public InventoryLog() {
		super();
		// TODO Auto-generated constructor stub
	}

	// getters/setters...
}
