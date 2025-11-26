package projectSem4.com.model.entities;

public class Complaint {
	 private int complaintId;
	 private String orderId;
	 private int userId;
	 private String ComplaintType;
	 private String  Description;
	 private int Status;
	 private Double CreatedAt;
	 private Double ResolvedAt;
	 
	 public Complaint() {}
	 
	public Complaint(int complaintId, String orderId, int userId, String complaintType, String description, int status,
			Double createdAt, Double resolvedAt) {
		super();
		this.complaintId = complaintId;
		this.orderId = orderId;
		this.userId = userId;
		ComplaintType = complaintType;
		Description = description;
		Status = status;
		CreatedAt = createdAt;
		ResolvedAt = resolvedAt;
	}

	public int getComplaintId() {
		return complaintId;
	}

	public void setComplaintId(int complaintId) {
		this.complaintId = complaintId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getComplaintType() {
		return ComplaintType;
	}

	public void setComplaintType(String complaintType) {
		ComplaintType = complaintType;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public int getStatus() {
		return Status;
	}

	public void setStatus(int status) {
		Status = status;
	}

	public Double getCreatedAt() {
		return CreatedAt;
	}

	public void setCreatedAt(Double createdAt) {
		CreatedAt = createdAt;
	}

	public Double getResolvedAt() {
		return ResolvedAt;
	}

	public void setResolvedAt(Double resolvedAt) {
		ResolvedAt = resolvedAt;
	}

	@Override
	public String toString() {
		return "Complaint [complaintId=" + complaintId + ", orderId=" + orderId + ", userId=" + userId
				+ ", ComplaintType=" + ComplaintType + ", Description=" + Description + ", Status=" + Status
				+ ", CreatedAt=" + CreatedAt + ", ResolvedAt=" + ResolvedAt + "]";
	}
	 
	 
}
