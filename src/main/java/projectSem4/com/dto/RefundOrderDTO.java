package projectSem4.com.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundOrderDTO {
	 private String orderId;
	    private Long refundId;
	    private BigDecimal refundAmount;
	    private String refundStatus;
	    private String refundStatusText;
	    private String reason;
	    private LocalDateTime requestDate;
	    private LocalDateTime updatedDate;
	    private String refundType;
	    private String refundMethod;
	    private String notes;

	    // Pagination fields
	    private int totalRows;
	    private int totalPages;
	    private int currentPage;
	    private int pageSize;
		
	    public RefundOrderDTO() {}
	    public RefundOrderDTO(String orderId, Long refundId, BigDecimal refundAmount, String refundStatus,
				String refundStatusText, String reason, LocalDateTime requestDate, LocalDateTime updatedDate,
				String refundType, String refundMethod, String notes, int totalRows, int totalPages, int currentPage,
				int pageSize) {
			super();
			this.orderId = orderId;
			this.refundId = refundId;
			this.refundAmount = refundAmount;
			this.refundStatus = refundStatus;
			this.refundStatusText = refundStatusText;
			this.reason = reason;
			this.requestDate = requestDate;
			this.updatedDate = updatedDate;
			this.refundType = refundType;
			this.refundMethod = refundMethod;
			this.notes = notes;
			this.totalRows = totalRows;
			this.totalPages = totalPages;
			this.currentPage = currentPage;
			this.pageSize = pageSize;
		}


		public String getOrderId() {
			return orderId;
		}
		public void setOrderId(String orderId) {
			this.orderId = orderId;
		}
		public Long getRefundId() {
			return refundId;
		}
		public void setRefundId(Long refundId) {
			this.refundId = refundId;
		}
		public BigDecimal getRefundAmount() {
			return refundAmount;
		}
		public void setRefundAmount(BigDecimal refundAmount) {
			this.refundAmount = refundAmount;
		}
		public String getRefundStatus() {
			return refundStatus;
		}
		public void setRefundStatus(String refundStatus) {
			this.refundStatus = refundStatus;
		}
		public String getRefundStatusText() {
			return refundStatusText;
		}
		public void setRefundStatusText(String refundStatusText) {
			this.refundStatusText = refundStatusText;
		}
		public String getReason() {
			return reason;
		}
		public void setReason(String reason) {
			this.reason = reason;
		}
		public LocalDateTime getRequestDate() {
			return requestDate;
		}
		public void setRequestDate(LocalDateTime requestDate) {
			this.requestDate = requestDate;
		}
		public LocalDateTime getUpdatedDate() {
			return updatedDate;
		}
		public void setUpdatedDate(LocalDateTime updatedDate) {
			this.updatedDate = updatedDate;
		}
		public String getRefundType() {
			return refundType;
		}
		public void setRefundType(String refundType) {
			this.refundType = refundType;
		}
		public String getRefundMethod() {
			return refundMethod;
		}
		public void setRefundMethod(String refundMethod) {
			this.refundMethod = refundMethod;
		}
		public String getNotes() {
			return notes;
		}
		public void setNotes(String notes) {
			this.notes = notes;
		}
		public int getTotalRows() {
			return totalRows;
		}
		public void setTotalRows(int totalRows) {
			this.totalRows = totalRows;
		}
		public int getTotalPages() {
			return totalPages;
		}
		public void setTotalPages(int totalPages) {
			this.totalPages = totalPages;
		}
		public int getCurrentPage() {
			return currentPage;
		}
		public void setCurrentPage(int currentPage) {
			this.currentPage = currentPage;
		}
		public int getPageSize() {
			return pageSize;
		}
		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}
		@Override
		public String toString() {
			return "RefundOrderDTO [orderId=" + orderId + ", refundId=" + refundId + ", refundStatus=" + refundStatus
					+ ", refundStatusText=" + refundStatusText + ", reason=" + reason + ", refundType=" + refundType
					+ ", refundMethod=" + refundMethod + ", notes=" + notes + ", totalRows=" + totalRows
					+ ", totalPages=" + totalPages + ", currentPage=" + currentPage + ", pageSize=" + pageSize + "]";
		}
	    
	    
	    
}
