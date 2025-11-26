package projectSem4.com.model.enums;

public enum RefundStatus {
    PENDING(1),
    SHOP_APPROVED(2),
    SHOP_REJECTED(3),
    ESCALATED(4),
    ADMIN_APPROVED(5),
    ADMIN_REJECTED(6),
    WAITING_FOR_RETURN(7),
    RETURNING(8),
    RETURNED(9),
    PROCESSING_REFUND(10),
    REFUNDED(11),
    CANCEL(12);

    private final int code;

    RefundStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // map int → enum
    public static RefundStatus fromCode(int code) {
        for (RefundStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Invalid RefundStatus code: " + code);
    }

    // map String → enum
    public static RefundStatus fromName(String name) {
        if (name == null) return null;
        try {
            return RefundStatus.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
        	return null;
        }
    }

    // map String → int
    public static Integer codeFromName(String name) {
        RefundStatus status = fromName(name);
        return status != null ? status.getCode() : null;
    }
}