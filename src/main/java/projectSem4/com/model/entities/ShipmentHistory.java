package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class ShipmentHistory {
	private int historyID;
    private String shipmentID;
    private int status;
    private String description;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    
    public ShipmentHistory() {}
    public ShipmentHistory(int historyID, String shipmentID, int status, String description, Integer updatedBy,
			LocalDateTime createdAt) {
		super();
		this.historyID = historyID;
		this.shipmentID = shipmentID;
		this.status = status;
		this.description = description;
		this.updatedBy = updatedBy;
		this.createdAt = createdAt;
	}
	public int getHistoryID() {
		return historyID;
	}
	public void setHistoryID(int historyID) {
		this.historyID = historyID;
	}
	public String getShipmentID() {
		return shipmentID;
	}
	public void setShipmentID(String shipmentID) {
		this.shipmentID = shipmentID;
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
	public Integer getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	@Override
	public String toString() {
		return "ShipmentHistory [historyID=" + historyID + ", shipmentID=" + shipmentID + ", status=" + status
				+ ", description=" + description + ", updatedBy=" + updatedBy + ", createdAt=" + createdAt + "]";
	}
    
    
}
