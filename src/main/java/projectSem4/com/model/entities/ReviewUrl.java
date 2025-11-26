package projectSem4.com.model.entities;

public class ReviewUrl {
    private Integer mediaId;       // PK
    private Integer reviewId;      // FK -> Reviews.ReviewID
    private String mediaUrl;       // nvarchar(255)
    private Integer type;          // 0=image,1=video (tuỳ quy ước)
    private Integer displayOrder;  // int

    // --- Optional display fields (không có trong DB), phục vụ join nếu cần
    private String reviewUserName;
    private String productName;

    public ReviewUrl() {}

    public ReviewUrl(Integer mediaId, Integer reviewId, String mediaUrl,
                     Integer type, Integer displayOrder,
                     String reviewUserName, String productName) {
        this.mediaId = mediaId;
        this.reviewId = reviewId;
        this.mediaUrl = mediaUrl;
        this.type = type;
        this.displayOrder = displayOrder;
        this.reviewUserName = reviewUserName;
        this.productName = productName;
    }

    public Integer getMediaId() { return mediaId; }
    public void setMediaId(Integer mediaId) { this.mediaId = mediaId; }

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getReviewUserName() { return reviewUserName; }
    public void setReviewUserName(String reviewUserName) { this.reviewUserName = reviewUserName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    @Override
    public String toString() {
        return "ReviewUrl [mediaId=" + mediaId + ", reviewId=" + reviewId + ", mediaUrl=" + mediaUrl
                + ", type=" + type + ", displayOrder=" + displayOrder
                + ", reviewUserName=" + reviewUserName + ", productName=" + productName + "]";
    }
}
