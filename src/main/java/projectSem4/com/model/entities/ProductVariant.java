package projectSem4.com.model.entities;

import java.time.LocalDateTime;

import io.micrometer.observation.transport.Propagator.Setter;

public class ProductVariant {
	private Long variantId; // ID của biến thể sản phẩm
	private Integer productId;
	private String varianName; // Tên biến thể sản phẩm (ví dụ: Màu sắc, Kích cỡ)
	private String SKU; // Mã sản phẩm (Stock Keeping Unit)
	private String image; // Hình ảnh của biến thể sản phẩm
	private Double price; // Giá của biến thể sản phẩm
	private Integer stockQuantity; // Số lượng tồn kho
	private Boolean status; // Trạng thái (Active/Inactive)
	private LocalDateTime createdAt; // Thời gian tạo
	private LocalDateTime updatedAt; // Thời gian cập nhật

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	// Getter và Setter
	public Long getVariantId() {
		return variantId;
	}

	public void setVariantId(Long variantId) {
		this.variantId = variantId;
	}

	public String getVarianName() {
		return varianName;
	}

	public void setVarianName(String varianName) {
		this.varianName = varianName;
	}

	public String getSKU() {
		return SKU;
	}

	public void setSKU(String SKU) {
		this.SKU = SKU;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	// Constructor không tham số
	public ProductVariant() {
	}

	public ProductVariant(Long variantId, Integer productId, String varianName, String sKU, String image, Double price,
			Integer stockQuantity, Boolean status, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.variantId = variantId;
		this.productId = productId;
		this.varianName = varianName;
		SKU = sKU;
		this.image = image;
		this.price = price;
		this.stockQuantity = stockQuantity;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	// Constructor với các tham số
	
}
