package projectSem4.com.controller.apiController.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import projectSem4.com.dto.RefundOrderDTO;
import projectSem4.com.service.RefundService;
import projectSem4.com.service.client.OrderService;

@RestController
@RequestMapping("/api/refund")
public class RefundApiController {

    @Autowired
    private RefundService refundService;

    @Autowired
    private OrderService orderService;

    // 🟢 Upload yêu cầu hoàn tiền
    @PostMapping("/upload")
    public ResponseEntity<?> uploadRefund(
            @RequestParam("orderId") String orderId,
            @RequestParam("items") String items,
            @RequestParam("reason") String reason,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId"); // ✅ Lấy từ JwtAuthInterceptor
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));

        try {
            Map<String, Object> result = refundService.createRefund(orderId, reason, files, userId, items);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Yêu cầu đang bị lỗi, vui lòng thử lại sau"
            ));
        }
    }

    // 🟡 Lấy danh sách refund theo user
    @GetMapping("/list")
    public ResponseEntity<?> listRefunds(
            @RequestParam(defaultValue = "1") int page,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));

        int pageSize = 10;
        List<RefundOrderDTO> data = refundService.getRefunByUser(userId, pageSize, page, null);
        return ResponseEntity.ok(Map.of("success", true, "refundOrders", data));
    }

    // 🔵 Chi tiết đơn refund theo orderId
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getRefundDetail(@PathVariable("orderId") String orderId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));

        var order = orderService.getOrderItemByOrderId(orderId);
        if (order == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy đơn hàng", "success", false));

        var refunds = refundService.getRefundByOrderID(orderId);
        return ResponseEntity.ok(Map.of(
                "order", order,
                "refunds", refunds,
                "success", true
        ));
    }

    // 🔴 Hủy yêu cầu refund
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelRefund(@RequestParam("refundId") Integer refundId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));

        if (refundId == null || refundId <= 0)
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid refund ID"));

        try {
            var updated = refundService.updateRefundStatusByBuyer(refundId, "CANCEL", userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to cancel refund request"
            ));
        }
    }

    // 🟢 Người mua xác nhận trả hàng
    @PostMapping("/confirm_return")
    public ResponseEntity<?> confirmReturn(@RequestParam("refundId") Integer refundId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));

        if (refundId == null || refundId <= 0)
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid refund ID"));

        try {
            var updated = refundService.updateRefundStatusByBuyer(refundId, "RETURNING", userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to confirm return"
            ));
        }
    }

    // 🟠 Shop cập nhật trạng thái refund
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatusByShop(
            @PathVariable("id") Integer refundId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));

        try {
            String newStatus = (String) body.get("status");
            String note = (String) body.get("note");
            boolean checkReturn = body.get("returnVarianat") != null && (Boolean) body.get("returnVarianat");

            var updated = refundService.updateRefundStatusByShop(refundId, newStatus, userId, note, checkReturn);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to update refund status"
            ));
        }
    }

    // 🧮 Helper (chỉ dùng nội bộ)
    private int getStatusPercent(String status) {
        return switch (status) {
            case "PENDING", "ESCALATED" -> 10;
            case "SHOP_APPROVED", "ADMIN_APPROVED" -> 40;
            case "SHOP_REJECTED", "ADMIN_REJECTED" -> 40;
            case "WAITING_FOR_RETURN" -> 60;
            case "RETURNING" -> 70;
            case "RETURNED" -> 80;
            case "PROCESSING_REFUND" -> 90;
            case "REFUNDED" -> 100;
            default -> 0;
        };
    }
}
