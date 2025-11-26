package projectSem4.com.controller.apiController.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.service.JwtProvider;
import projectSem4.com.service.PaymentService;
import projectSem4.com.service.client.CheckoutService;
import projectSem4.com.service.client.OrderService;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutApiController {
	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private JwtProvider jwtProvider;

	// 🟢 Lấy dữ liệu checkout
	@GetMapping("")
	public ResponseEntity<?> getCheckoutData(HttpServletRequest request, @RequestParam("cartData") String cartDataJson,
			@RequestParam(value = "checkoutToken", required = false) String checkoutToken) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
		}

		if (cartDataJson == null || cartDataJson.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(Map.of("success", false, "message", "Giỏ hàng của bạn trống hoặc đơn đã thanh toán"));
		}

		Map<String, Object> checkoutData = checkoutService.prepareCheckoutData(userId, cartDataJson);

		if (checkoutToken == null) {
			checkoutToken = UUID.randomUUID().toString();
		}

		checkoutData.put("checkoutToken", checkoutToken);
		checkoutData.put("success", true);
		return ResponseEntity.ok(checkoutData);
	}

	// 🟡 Đặt hàng COD
	@PostMapping("/place-order")
	public ResponseEntity<?> createOrder(@RequestBody CheckCartItem data, HttpServletRequest request) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
		}

		Map<String, Object> result = orderService.placeOrderCOD(data, userId);
		return ResponseEntity.ok(result);
	}

	// 🔵 Tiếp tục thanh toán (VNPay / Momo / v.v)
	@PostMapping("/continue")
	public ResponseEntity<?> continuePayment(HttpServletRequest request, @RequestParam("orderId") String orderId) {
		try {
			String paymentUrl = paymentService.createOrReusePaymentUrl(orderId);
			return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl, "success", true));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "success", false));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("error", "Có lỗi xảy ra, vui lòng thử lại sau", "success", false));
		}
	}

	// 🔴 Hủy đơn hàng
	@PostMapping("/cancel")
	public ResponseEntity<?> cancelOrder(HttpServletRequest request, @RequestParam("orderId") String orderId) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
		}

		try {
			var result = orderService.updateOrderStatusByBuyer(orderId, 4, null);
			return ResponseEntity.ok(result);
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Có lỗi xảy ra, vui lòng thử lại sau", "success", false));
		}
	}

	// 🟢 Xác nhận đơn hàng
	@PostMapping("/confirm")
	public ResponseEntity<?> confirmOrder(HttpServletRequest request, @RequestParam("orderId") String orderId) {
		Integer userId = (Integer) request.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
		}

		try {
			var result = orderService.updateOrderStatusByBuyer(orderId, 5, null);
			return ResponseEntity.ok(result);
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Có lỗi xảy ra, vui lòng thử lại sau", "success", false));
		}
	}

}
