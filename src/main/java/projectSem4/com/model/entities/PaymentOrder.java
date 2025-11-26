package projectSem4.com.model.entities;

public class PaymentOrder {
	private Integer id;
    private Integer paymentId;
    private String orderId;
    
    public PaymentOrder() {}
    
	public PaymentOrder(Integer id, Integer paymentId, String orderId) {
		super();
		this.id = id;
		this.paymentId = paymentId;
		this.orderId = orderId;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(Integer paymentId) {
		this.paymentId = paymentId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public String toString() {
		return "PaymentOrder [id=" + id + ", paymentId=" + paymentId + ", orderId=" + orderId + "]";
	}
    
    
}
