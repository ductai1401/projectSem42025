package projectSem4.com.model.entities;

public class FlashSaleItem {
    private Integer flashSaleItemId;
    private Integer flashSaleId;
    private Integer productId;   // thay variantId
    private Integer quantity;    // thay quantityLimit
    private Integer percern;     // thêm cột percern (% giảm giá)
    private Float totalAmount;   // thêm cột số tiền đã bán
    private Integer status;
    // Getter & Setter
    public Integer getFlashSaleItemId() {
        return flashSaleItemId;
    }

    public void setFlashSaleItemId(Integer flashSaleItemId) {
        this.flashSaleItemId = flashSaleItemId;
    }

    public Integer getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(Integer flashSaleId) {
        this.flashSaleId = flashSaleId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPercern() {
        return percern;
    }

    public void setPercern(Integer percern) {
        this.percern = percern;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Constructors
   
    public FlashSaleItem() {
        super();
    }

	public FlashSaleItem(Integer flashSaleItemId, Integer flashSaleId, Integer productId, Integer quantity,
			Integer percern, Float totalAmount, Integer status) {
		super();
		this.flashSaleItemId = flashSaleItemId;
		this.flashSaleId = flashSaleId;
		this.productId = productId;
		this.quantity = quantity;
		this.percern = percern;
		this.totalAmount = totalAmount;
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
