package projectSem4.com.model.entities;

public class ProductImage {
	private Integer imageId;
	private Integer productId;
	private String imageUrl;
	private String altText;
	private Integer displayOrder;

	public Integer getImageId() {
		return imageId;
	}

	public void setImageId(Integer imageId) {
		this.imageId = imageId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public ProductImage(Integer imageId, Integer productId, String imageUrl, String altText, Integer displayOrder) {
		super();
		this.imageId = imageId;
		this.productId = productId;
		this.imageUrl = imageUrl;
		this.altText = altText;
		this.displayOrder = displayOrder;
	}

	public ProductImage() {
		super();
		// TODO Auto-generated constructor stub
	}

	// Constructor rỗng

}
