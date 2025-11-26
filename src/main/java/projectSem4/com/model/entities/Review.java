package projectSem4.com.model.entities;

import java.time.LocalDateTime;
import java.util.List;

public class Review {
    private Integer reviewId;       // int
    private Integer productId;      // int
    private Integer userId;         // int
    private String orderId;         // nvarchar(200)
    private Integer rating;         // int
    private Integer typeRv;         // int
    private String reviewText;      // nvarchar(MAX)
    private LocalDateTime createdAt; // datetime2(7)
    private Integer status;         // int

    // Thêm các field phụ (không có trong DB) để hiển thị kèm
    private String productName;
    private String userName;

    // Thêm danh sách media (ảnh/video) từ bảng ReviewUrl
    private List<ReviewUrl> medias;

    // ===== Getter & Setter cho medias =====
    public List<ReviewUrl> getMedias() {
        return medias;
    }

    public void setMedias(List<ReviewUrl> medias) {
        this.medias = medias;
    }

    // ===== Getter & Setter cho các field phụ =====
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // ===== Getter & Setter cho các field trong DB =====
    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getTypeRv() {
        return typeRv;
    }

    public void setTypeRv(Integer typeRv) {
        this.typeRv = typeRv;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
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

    // ===== Constructors =====
    public Review() {
        super();
    }

    public Review(Integer reviewId, Integer productId, Integer userId, String orderId, Integer rating,
                  Integer typeRv, String reviewText, LocalDateTime createdAt, Integer status,
                  String productName, String userName, List<ReviewUrl> medias) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.orderId = orderId;
        this.rating = rating;
        this.typeRv = typeRv;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.status = status;
        this.productName = productName;
        this.userName = userName;
        this.medias = medias;
    }

    @Override
    public String toString() {
        return "Review [reviewId=" + reviewId + ", productId=" + productId + ", userId=" + userId
                + ", orderId=" + orderId + ", rating=" + rating + ", typeRv=" + typeRv
                + ", reviewText=" + reviewText + ", createdAt=" + createdAt + ", status=" + status
                + ", productName=" + productName + ", userName=" + userName
                + ", medias=" + medias + "]";
    }
}
