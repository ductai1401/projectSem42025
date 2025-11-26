package projectSem4.com.model.modelViews;

public class CartItemView {
	private int cartItemId;
	 private int cartId ;
	 private int quantity ;
	 private Integer variantId; // ID của biến thể sản phẩm
	 private Integer categoryId;
	 private Integer productId;
	 private String productName;
	 private String variantName; // Tên biến thể sản phẩm (ví dụ: Màu sắc, Kích cỡ)
	 private String SKU; // Mã sản phẩm (Stock Keeping Unit)
	 private String imageVariant; // Hình ảnh của biến thể sản phẩm
	 private Double price; // Giá của biến thể sản phẩm
	 private int shopId;
	 private String shopName;
	 private Integer remaining;
	 private Integer flashSaleItemId;
	 private Integer flashSaleId;
	 private Double flashSalePrice;
	 private Integer discountPercent;
	 private Integer availableQty;
	 
	 public CartItemView() {}

	

	public CartItemView(int cartItemId, int cartId, int quantity, Integer variantId, Integer categoryId,
			Integer productId, String productName, String variantName, String sKU, String imageVariant, Double price,
			int shopId, String shopName, Integer remaining, Integer flashSaleItemId, Integer flashSaleId,
			Double flashSalePrice, Integer discountPercent, Integer availableQty) {
		super();
		this.cartItemId = cartItemId;
		this.cartId = cartId;
		this.quantity = quantity;
		this.variantId = variantId;
		this.categoryId = categoryId;
		this.productId = productId;
		this.productName = productName;
		this.variantName = variantName;
		SKU = sKU;
		this.imageVariant = imageVariant;
		this.price = price;
		this.shopId = shopId;
		this.shopName = shopName;
		this.remaining = remaining;
		this.flashSaleItemId = flashSaleItemId;
		this.flashSaleId = flashSaleId;
		this.flashSalePrice = flashSalePrice;
		this.discountPercent = discountPercent;
		this.availableQty = availableQty;
	}



	public int getCartItemId() {
		return cartItemId;
	}

	public void setCartItemId(int cartItemId) {
		this.cartItemId = cartItemId;
	}

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Integer getVariantId() {
		return variantId;
	}

	public void setVariantId(Integer variantId) {
		this.variantId = variantId;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getVariantName() {
		return variantName;
	}

	public void setVariantName(String variantName) {
		this.variantName = variantName;
	}

	public String getSKU() {
		return SKU;
	}

	public void setSKU(String sKU) {
		SKU = sKU;
	}

	public String getImageVariant() {
		return imageVariant;
	}

	public void setImageVariant(String imageVariant) {
		this.imageVariant = imageVariant;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public Integer getRemaining() {
		return remaining;
	}

	public void setRemaining(Integer remaining) {
		this.remaining = remaining;
	}

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

	public Double getFlashSalePrice() {
		return flashSalePrice;
	}

	public void setFlashSalePrice(Double flashSalePrice) {
		this.flashSalePrice = flashSalePrice;
	}


	public Integer getAvailableQty() {
		return availableQty;
	}

	public void setAvailableQty(Integer availableQty) {
		this.availableQty = availableQty;
	}



	public Integer getDiscountPercent() {
		return discountPercent;
	}



	public void setDiscountPercent(Integer discountPercent) {
		this.discountPercent = discountPercent;
	}



	@Override
	public String toString() {
		return "CartItemView [cartItemId=" + cartItemId + ", cartId=" + cartId + ", quantity=" + quantity
				+ ", variantId=" + variantId + ", categoryId=" + categoryId + ", productId=" + productId
				+ ", productName=" + productName + ", variantName=" + variantName + ", SKU=" + SKU + ", imageVariant="
				+ imageVariant + ", price=" + price + ", shopId=" + shopId + ", shopName=" + shopName + ", remaining="
				+ remaining + ", flashSaleItemId=" + flashSaleItemId + ", flashSaleId=" + flashSaleId
				+ ", flashSalePrice=" + flashSalePrice + ", discountPercent=" + discountPercent + ", availableQty="
				+ availableQty + "]";
	}

	
	 
}
