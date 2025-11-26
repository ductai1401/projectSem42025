package projectSem4.com.model.entities;

public class OrderItem {
	private int orderItemId ;
	private String orderId ;
	private int  productVariantId ;
	private int  quantity ;
	private double  unitPrice ;
	private int  flashSaleId ;
	private double subtotal ;
	private double discountAllocated;
	private double finalPrice;
	private double unitFinalPrice;
	private Integer qtyRefunded;
	private double amountRefunded;
	private int itemRefundStatus;
	
	public OrderItem() {}

	



	public OrderItem(int orderItemId, String orderId, int productVariantId, int quantity, double unitPrice,
			int flashSaleId, double subtotal, double discountAllocated, double finalPrice, double unitFinalPrice,
			Integer qtyRefunded, double amountRefunded, int itemRefundStatus) {
		super();
		this.orderItemId = orderItemId;
		this.orderId = orderId;
		this.productVariantId = productVariantId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.flashSaleId = flashSaleId;
		this.subtotal = subtotal;
		this.discountAllocated = discountAllocated;
		this.finalPrice = finalPrice;
		this.unitFinalPrice = unitFinalPrice;
		this.qtyRefunded = qtyRefunded;
		this.amountRefunded = amountRefunded;
		this.itemRefundStatus = itemRefundStatus;
	}





	public Integer getQtyRefunded() {
		return qtyRefunded;
	}





	public void setQtyRefunded(Integer qtyRefunded) {
		this.qtyRefunded = qtyRefunded;
	}





	public double getAmountRefunded() {
		return amountRefunded;
	}





	public void setAmountRefunded(double amountRefunded) {
		this.amountRefunded = amountRefunded;
	}





	public int getItemRefundStatus() {
		return itemRefundStatus;
	}





	public void setItemRefundStatus(int itemRefundStatus) {
		this.itemRefundStatus = itemRefundStatus;
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

	public double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}





	public int getQuantity() {
		return quantity;
	}





	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}





	@Override
	public String toString() {
		return "OrderItem [orderItemId=" + orderItemId + ", orderId=" + orderId + ", productVariantId="
				+ productVariantId + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", flashSaleId="
				+ flashSaleId + ", subtotal=" + subtotal + ", discountAllocated=" + discountAllocated + ", finalPrice="
				+ finalPrice + ", unitFinalPrice=" + unitFinalPrice + ", qtyRefunded=" + qtyRefunded
				+ ", amountRefunded=" + amountRefunded + ", itemRefundStatus=" + itemRefundStatus + "]";
	}


	

	
	
	
}
