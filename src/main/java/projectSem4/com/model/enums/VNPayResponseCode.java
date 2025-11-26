package projectSem4.com.model.enums;

import java.util.HashMap;
import java.util.Map;

public class VNPayResponseCode {
	private static final Map<String, String> codeMap = new HashMap<>();
    static {
        codeMap.put("00", "Thanh toán thành công");
        codeMap.put("01", "Đơn hàng không tồn tại");
        codeMap.put("02", "Đơn hàng đã được xác nhận");
        codeMap.put("04", "Số tiền không hợp lệ");
        codeMap.put("07", "Giao dịch bị nghi ngờ gian lận (Fraud)");
        codeMap.put("09", "Thẻ/Tài khoản chưa đăng ký InternetBanking");
        codeMap.put("10", "Xác thực thông tin thẻ/tài khoản thất bại");
        codeMap.put("11", "Thẻ/Tài khoản đã hết hạn");
        codeMap.put("12", "Thẻ/Tài khoản bị khóa");
        codeMap.put("24", "Khách hàng hủy giao dịch");
        codeMap.put("51", "Tài khoản không đủ số dư");
        codeMap.put("65", "Vượt quá hạn mức giao dịch");
        codeMap.put("75", "Ngân hàng đang bảo trì");
        codeMap.put("79", "Khách hàng nhập sai OTP quá số lần quy định");
        codeMap.put("99", "Lỗi không xác định");
        // có thể bổ sung thêm dựa trên tài liệu VNPay
    }

    public static String getMessage(String code) {
        return codeMap.getOrDefault(code, "Lỗi không xác định");
    }
}
