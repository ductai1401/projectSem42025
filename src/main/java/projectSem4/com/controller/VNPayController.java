package projectSem4.com.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import projectSem4.com.controller.apiController.admin.CategoryApiController;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.dto.ValiOrderResult;
import projectSem4.com.service.JwtProvider;
import projectSem4.com.service.VNPayService;
import projectSem4.com.service.client.OrderService;

@Controller
public class VNPayController {

	private final CategoryApiController categoryApiController;

	private final VNPayService vnPayService = new VNPayService();

	@Autowired
	private OrderService orderService;

	@Autowired
	private JwtProvider jwtProvider;

	VNPayController(CategoryApiController categoryApiController) {
		this.categoryApiController = categoryApiController;
	}

//    @GetMapping("/pay-vnpay")
//    public RedirectView payVNPay(@RequestParam(name = "amount") long amount,
//                                 @RequestParam(name = "orderInfo") String orderInfo,
//                                 @RequestParam(name = "orderId") String orderId) {
//        try {
//            String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, orderId);
//            return new RedirectView(paymentUrl);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new RedirectView("/error");
//        }
//    }

	@GetMapping("/vnpay-return")
	public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, String> fields = new HashMap<>();
		for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
			String fieldName = params.nextElement();
			String fieldValue = request.getParameter(fieldName);
			if (fieldValue != null && fieldValue.length() > 0) {
				fields.put(fieldName, fieldValue);
			}
		}

		String responseCode = fields.get("vnp_ResponseCode");
		String transactionStatus = fields.get("vnp_TransactionStatus");
		String paymentId = fields.get("vnp_TxnRef");

		if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
			orderService.updateStatusPaymentSuccess(paymentId, 1);

			// redirect về domain của bạn kèm status
			response.sendRedirect("http://localhost:8383/payment/result?status=success&orderId=" + paymentId);
		} else {
			orderService.updateStatusPaymentSuccess(paymentId, 4);
			response.sendRedirect("http://localhost:8383/payment/result?status=fail&orderId=" + paymentId);
		}
	}

	@GetMapping("/payment/result")
	public String paymentResult(@RequestParam("status") String status, @RequestParam(value = "orderId",required = false) String orderId,
			Model model) {
		model.addAttribute("orderId", orderId);

		if ("success".equals(status)) {
			model.addAttribute("message", "Thanh toán thành công!");
			return "client/checkoutSuccess";
		} else {
			model.addAttribute("message", "Thanh toán thất bại hoặc bị hủy.");
			return "client/checkoutFail";
		}
	}

	@RequestMapping(value = "/vnpay-ipn", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public Map<String, String> vnpayIpn(HttpServletRequest request) {
		Map<String, String> fields = new HashMap<>();
		for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
			String fieldName = params.nextElement();
			String fieldValue = request.getParameter(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				fields.put(fieldName, fieldValue);
			}
		}

		Map<String, String> response;
		try {
			// 1. Xác thực chữ ký
			response = vnPayService.verifyIpn(new HashMap<>(fields));
			if (!"00".equals(response.get("RspCode"))) {
				return response; // Sai chữ ký thì dừng luôn
			}

			// 2. Lấy dữ liệu từ IPN
			String responseCode = fields.get("vnp_ResponseCode");
			String transactionStatus = fields.get("vnp_TransactionStatus");
			String paymentId = fields.get("vnp_TxnRef");

			// 3. Xử lý nghiệp vụ
			if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
				int updated = orderService.updateStatusPaymentSuccess(paymentId, 1);
				if (updated != 0) {
					response.put("RspCode", "00");
					response.put("Message", "Confirm Success");
				} else {
					response.put("RspCode", "02");
					response.put("Message", "Order Not Found or Already Processed");
				}
			} else {
				orderService.updateStatusPaymentSuccess(paymentId, 4);
				response.put("RspCode", "01");
				response.put("Message", "Transaction Failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = new HashMap<>();
			response.put("RspCode", "99");
			response.put("Message", "Unknown Error");
		}

		return response;
	}

}
