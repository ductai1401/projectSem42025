package projectSem4.com.model.entities;

public class RefundItem {
	private Integer id;             
    private Integer refundId;
    private Integer orderItemId;   
    private Integer Quantity;      
    private Double refundAmount;
    
    public RefundItem() {}
	public RefundItem(Integer id, Integer refundId, Integer orderItemId, Integer quantity, Double refundAmount) {

		this.id = id;
		this.refundId = refundId;
		this.orderItemId = orderItemId;
		Quantity = quantity;
		this.refundAmount = refundAmount;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getRefundId() {
		return refundId;
	}
	public void setRefundId(Integer refundId) {
		this.refundId = refundId;
	}
	public Integer getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(Integer orderItemId) {
		this.orderItemId = orderItemId;
	}
	public Integer getQuantity() {
		return Quantity;
	}
	public void setQuantity(Integer quantity) {
		Quantity = quantity;
	}
	public Double getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}
	@Override
	public String toString() {
		return "RefundItems [id=" + id + ", refundId=" + refundId + ", orderItemId=" + orderItemId + ", Quantity="
				+ Quantity + ", refundAmount=" + refundAmount + "]";
	}
    
}
