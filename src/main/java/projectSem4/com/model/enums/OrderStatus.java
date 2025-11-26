package projectSem4.com.model.enums;

public enum OrderStatus {
	UNPAID(0),
    PENDING(1),
    PROCESSING(2),
    SHIPPED(3),
    CANCELLED(4),
    DELIVERED(5),
    DELIVER_FAILED(6),    // Buyer yêu cầu hoàn tiền
    REFUNDED(8);             // Hoàn tiền toàn bộ

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OrderStatus fromString(String status) {
        if (status == null) return null;
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.code == code) return s;
        }
        return null;
    }
    
    public static int safeGetCode(String status) {
        if (status == null || status.isEmpty()) {
            return -1;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase()).getCode();
        } catch (IllegalArgumentException e) {
            return -1; // nếu không tồn tại trong enum
        }
    }
}
