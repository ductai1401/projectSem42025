package projectSem4.com.model.modelViews;

public class RefundItemView {
	private Integer id;             
    private Integer refundId;
    private Integer orderItemId;
    private Integer Quantity;
    private Double refundAmount;
	private int  productVariantId ;
	private String productVariantName;
	private String productName;
	private String productVariantImage;
	
	public RefundItemView() {}
	
	

	public RefundItemView(Integer id, Integer refundId, Integer orderItemId, Integer quantity, Double refundAmount,
			int productVariantId, String productVariantName, String productName, String productVariantImage) {
		super();
		this.id = id;
		this.refundId = refundId;
		this.orderItemId = orderItemId;
		Quantity = quantity;
		this.refundAmount = refundAmount;
		this.productVariantId = productVariantId;
		this.productVariantName = productVariantName;
		this.productName = productName;
		this.productVariantImage = productVariantImage;
	}



	public String getProductVariantImage() {
		return productVariantImage;
	}



	public void setProductVariantImage(String productVariantImage) {
		this.productVariantImage = productVariantImage;
	}



	public Double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Double refundAmount) {
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
	public int getProductVariantId() {
		return productVariantId;
	}
	public void setProductVariantId(int productVariantId) {
		this.productVariantId = productVariantId;
	}
	public String getProductVariantName() {
		return productVariantName;
	}
	public void setProductVariantName(String productVarianName) {
		this.productVariantName = productVarianName;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}



	@Override
	public String toString() {
		return "RefundItemView [id=" + id + ", refundId=" + refundId + ", orderItemId=" + orderItemId + ", Quantity="
				+ Quantity + ", refundAmount=" + refundAmount + ", productVariantId=" + productVariantId
				+ ", productVariantName=" + productVariantName + ", productName=" + productName
				+ ", productVariantImage=" + productVariantImage + "]";
	}




	

}
