package projectSem4.com.model.enums;

public enum ShipmentStatus {
	NOT_READY(0, "Not Ready", "Chưa sẵn sàng - shop đang chuẩn bị"),
    PENDING(1, "Pending", "Sẵn sàng - chờ shipper nhận"),
    ASSIGNED(2, "Assigned", "Shipper đã nhận đơn"),
    PICKED(3, "Picked", "Shipper Đã lấy hàng từ shop va dang tren duong giao den tay ban"),
    DELIVERED(4, "Delivered", "Đã giao thành công"),
    FAILED(5, "Failed", "Giao thất bại"),
    RETURNED(6, "Returned", "Đã trả về shop");

    private final int value;
    private final String label;
    private final String description;

    ShipmentStatus(int value, String label, String description) {
        this.value = value;
        this.label = label;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Lấy enum từ giá trị int (ví dụ từ DB)
     */
    public static ShipmentStatus fromValue(int value) {
        for (ShipmentStatus status : ShipmentStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ShipmentStatus value: " + value);
    }

    @Override
    public String toString() {
        return label;
    }
}
