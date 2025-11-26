package projectSem4.com.model.entities;

import java.time.LocalDateTime;

public class Category {
    private int categoryId;
    private Integer parentCategory; // Cho phép null
    private String categoryName;
    private String description;
    private int sortOrder;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Thêm 2 cột mới
    private String image;           // VARCHAR(255)
    private String categoryOption;  // NVARCHAR(MAX)

    public Category() {}

    public Category(int categoryId, Integer parentCategory, String categoryName, String description, int sortOrder,
                    int status, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String image, String categoryOption) {
        super();
        this.categoryId = categoryId;
        this.parentCategory = parentCategory;
        this.categoryName = categoryName;
        this.description = description;
        this.sortOrder = sortOrder;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.image = image;
        this.categoryOption = categoryOption;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Integer parentCategory) {
        this.parentCategory = parentCategory;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
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

    // ✅ Getter & Setter cho cột mới
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategoryOption() {
        return categoryOption;
    }

    public void setCategoryOption(String categoryOption) {
        this.categoryOption = categoryOption;
    }

    @Override
    public String toString() {
        return "Category [categoryId=" + categoryId + ", parentCategory=" + parentCategory + ", categoryName="
                + categoryName + ", description=" + description + ", sortOrder=" + sortOrder + ", status=" + status
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + ", image=" + image + ", categoryOption=" + categoryOption + "]";
    }
}
