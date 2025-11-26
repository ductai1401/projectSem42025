package projectSem4.com.model.modelViews;

public class OrderItemView {
	private int orderItemId ;
	private String orderId ;	
	private int  productVariantId ;
	private String productVariantName;
	private String productVariantImage;
	private int  Quantity ;
	private double  unitPrice ;
	private int  flashSaleId ;
	private String flashSaleName;
	private double subtotal ;
	private double discountAllocated;
	private double finalPrice;
	private double unitFinalPrice;
	
	public OrderItemView() {}
	
	
	


	public OrderItemView(int orderItemId, String orderId, int productVariantId, String productVariantName,
			String productVariantImage, int quantity, double unitPrice, int flashSaleId, String flashSaleName,
			double subtotal, double discountAllocated, double finalPrice, double unitFinalPrice) {
		super();
		this.orderItemId = orderItemId;
		this.orderId = orderId;
		this.productVariantId = productVariantId;
		this.productVariantName = productVariantName;
		this.productVariantImage = productVariantImage;
		Quantity = quantity;
		this.unitPrice = unitPrice;
		this.flashSaleId = flashSaleId;
		this.flashSaleName = flashSaleName;
		this.subtotal = subtotal;
		this.discountAllocated = discountAllocated;
		this.finalPrice = finalPrice;
		this.unitFinalPrice = unitFinalPrice;
	}





	public String getProductVariantImage() {
		return productVariantImage;
	}


	public void setProductVariantImage(String productVariantImage) {
		this.productVariantImage = productVariantImage;
	}


	public int getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(int orderItemId) {
		this.orderItemId = orderItemId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
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
	public void setProductVariantName(String productVariantName) {
		this.productVariantName = productVariantName;
	}
	public int getQuantity() {
		return Quantity;
	}
	public void setQuantity(int quantity) {
		Quantity = quantity;
	}
	public double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public int getFlashSaleId() {
		return flashSaleId;
	}
	public void setFlashSaleId(int flashSaleId) {
		this.flashSaleId = flashSaleId;
	}
	public String getFlashSaleName() {
		return flashSaleName;
	}
	public void setFlashSaleName(String flashSaleName) {
		this.flashSaleName = flashSaleName;
	}
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}





	public double getDiscountAllocated() {
		return discountAllocated;
	}





	public void setDiscountAllocated(double discountAllocated) {
		this.discountAllocated = discountAllocated;
	}





	public double getFinalPrice() {
		return finalPrice;
	}





	public void setFinalPrice(double finalPrice) {
		this.finalPrice = finalPrice;
	}





	public double getUnitFinalPrice() {
		return unitFinalPrice;
	}





	public void setUnitFinalPrice(double unitFinalPrice) {
		this.unitFinalPrice = unitFinalPrice;
	}





	@Override
	public String toString() {
		return "OrderItemView [orderItemId=" + orderItemId + ", orderId=" + orderId + ", productVariantId="
				+ productVariantId + ", productVariantName=" + productVariantName + ", productVariantImage="
				+ productVariantImage + ", Quantity=" + Quantity + ", unitPrice=" + unitPrice + ", flashSaleId="
				+ flashSaleId + ", flashSaleName=" + flashSaleName + ", subtotal=" + subtotal + ", discountAllocated="
				+ discountAllocated + ", finalPrice=" + finalPrice + ", unitFinalPrice=" + unitFinalPrice + "]";
	}
	
	
	
}
