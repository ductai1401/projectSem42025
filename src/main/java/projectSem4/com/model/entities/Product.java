package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class Product {
    private Integer productId;
    private Integer shopId;
    private Integer categoryId;
    private String image; // nvarchar(200)
    private String productName; // nvarchar(255)
    private String description; // nvarchar(max)
    private String productOption; // nvarchar(max) - có thể null
    private Integer status; // int (0/1 tùy bạn quy ước)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Add categoryName and shopName fields
    private String categoryName;
    private String shopName;

    // Getters and Setters for new fields
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    // Existing getters and setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductOption() {
        return productOption;
    }

    public void setProductOption(String productOption) {
        this.productOption = productOption;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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

    public Product() {
        super();
    }

    public Product(Integer productId, Integer shopId, Integer categoryId, String image, String productName,
                   String description, String productOption, Integer status, LocalDateTime createdAt,
                   LocalDateTime updatedAt, String categoryName, String shopName) {
        this.productId = productId;
        this.shopId = shopId;
        this.categoryId = categoryId;
        this.image = image;
        this.productName = productName;
        this.description = description;
        this.productOption = productOption;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryName = categoryName;
        this.shopName = shopName;
    }

    @Override
    public String toString() {
        return "Product [productId=" + productId + ", shopId=" + shopId + ", categoryId=" + categoryId + ", image="
                + image + ", productName=" + productName + ", description=" + description + ", productOption="
                + productOption + ", status=" + status + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + ", categoryName=" + categoryName + ", shopName=" + shopName + "]";
    }
}
