package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class WishlistItem {
    private Integer wishlistId;
    private Integer userId;
    private Integer productId;
    private LocalDateTime createdAt;
    private Integer status;

    // Constructors
    public WishlistItem() {
    }

    public WishlistItem(Integer wishlistId, Integer userId, Integer productId, LocalDateTime createdAt, Integer status) {
        this.wishlistId = wishlistId;
        this.userId = userId;
        this.productId = productId;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public Integer getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Integer wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    
}
