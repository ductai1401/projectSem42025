package projectSem4.com.dto;

import java.util.List;
import projectSem4.com.model.entities.Review;

public class ReviewCreateRequest {
    private Review review;
    private List<MediaItem> media; // có thể null/empty

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }

    public List<MediaItem> getMedia() { return media; }
    public void setMedia(List<MediaItem> media) { this.media = media; }

    public static class MediaItem {
        private String mediaUrl;
        private Integer type;          // 0=image, 1=video (tùy bạn quy ước)
        private Integer displayOrder;  // có thể null

        public String getMediaUrl() { return mediaUrl; }
        public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
}
