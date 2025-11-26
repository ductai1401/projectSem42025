package projectSem4.com.model.enums;

public enum ShopStatus {
	PENDING(1, "Pending Approval"),   // Chờ duyệt
    ACTIVE(2, "Active"),              // Đang hoạt động
    BLOCKED(3, "Blocked"),            // Bị khóa (vi phạm)
    INACTIVE(4, "Inactive"),          // Tạm ngưng bán
    CLOSED(5, "Closed");              // Đã đóng vĩnh viễn

    private final int code;
    private final String description;

    ShopStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ShopStatus fromCode(int code) {
        for (ShopStatus status : ShopStatus.values()) {
            if (status.code == code) return status;
        }
        return null;
    }
}
