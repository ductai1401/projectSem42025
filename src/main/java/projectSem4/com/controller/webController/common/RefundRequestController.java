package projectSem4.com.controller.webController.common;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import projectSem4.com.dto.RefundOrderDTO;
import projectSem4.com.model.entities.RefundRequest;
import projectSem4.com.model.enums.RefundStatus;
import projectSem4.com.model.modelViews.OrderView;
import projectSem4.com.service.RefundService;
import projectSem4.com.service.client.OrderService;
import projectSem4.com.service.seller.ShipmentService;

@Controller
@RequestMapping("/refund")
public class RefundRequestController {

	@Autowired
	private RefundService refundService;

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private ShipmentService shipmentService;
	@PostMapping("/create")
	@ResponseBody
	public Map<String, Object> uploadRefund(@RequestParam("orderId") String orderId,
			@RequestParam("items") String items, @RequestParam("reason") String reason,
			@RequestParam(value = "files", required = false) List<MultipartFile> files, HttpSession session) {
		var userId = (Integer) session.getAttribute("userId");

		Map<String, Object> result = new HashMap<>();
		try {
			result = refundService.createRefund(orderId, reason, files, userId, items);
		} catch (IOException e) {
			result.put("success", false);
			result.put("message", "yêu cầu đang bị lỗi, vui lòng thử lại sau");
			e.printStackTrace();
		}

		return result;
	}

	@GetMapping("/list")
	public String listRefunds(@RequestParam("page") int page, Model model, HttpSession session) {
		var userId = (Integer) session.getAttribute("userId");
		int pageSize = 10;

		List<RefundOrderDTO> data = refundService.getRefunByUser(userId, pageSize, page, null);

		if (data != null) {
		
			model.addAttribute("refundOrders", data);
		}
		// lấy danh sách refund theo refundStatus + page

	        return "client/order/refundOrderList :: refundOrderListFragment";
	}

//	@GetMapping("/{refundId}")
//	public String refundsDetail(@PathVariable("refundId") int refundId, Model model, HttpSession session) {
//		var userId = (Integer) session.getAttribute("userId");
//		int pageSize = 10;
//
//		var data = refundService.getRefunByID(refundId);
//
//		if (data != null) {
//
//			model.addAttribute("refund", data);
//			var test = getStatusPercent(data.getStatus());
//			model.addAttribute("statusPercent", test);
//			ObjectMapper mapper = new ObjectMapper();
//			List<Map<String, Object>> historyList = new ArrayList<>();
//
//			try {
//			    if (data.getHistory() != null && !data.getHistory().isBlank()) {
//			        historyList = mapper.readValue(data.getHistory(), new TypeReference<>() {});
//			    }
//			    System.out.println("JSON history raw = " + data.getHistory());
//			    historyList = mapper.readValue(data.getHistory(), new TypeReference<>() {});
//			    System.out.println("Parsed historyList = " + historyList);
//			} catch (Exception e) {
//			    e.printStackTrace();
//			}
//			
//			model.addAttribute("historyList", historyList);
//		}
//		// lấy danh sách refund theo refundStatus + page
//
//		return "client/order/refundDetail"; // trả về fragment
//	}
	
	@GetMapping("/{orderId}")
    public String orderDetail(@PathVariable("orderId") String orderId,
                              Model model) {
        
        // 1. Lấy thông tin đơn hàng
       var order = orderService.getOrderItemByOrderId(orderId);
        if (order == null) {
            return "redirect:/orders?error=notfound";
        }

        // 3. Lấy danh sách refund/return liên quan (nếu có)
        var refund = refundService.getRefundViewByIdOrder(orderId);
     // Tính xem buyer còn quyền escalate
        boolean canEscalate = false;
        if(refund.getStatus().equals("SHOP_REJECTED")) {
            LocalDateTime rejectTime = refund.getShopRejectedAt();
            canEscalate = rejectTime.plusHours(48).isAfter(LocalDateTime.now());
        }
        model.addAttribute("canEscalate", canEscalate);
        // 4. Đẩy sang model
        model.addAttribute("order", order);
       
        model.addAttribute("refund", refund);
        
        var refundItems = refundService.getRefundItemViewByRefundId(refund.getRefundId());
        model.addAttribute("refundItems", refundItems);
        var shipment = shipmentService.getByOrderId(orderId);   
        model.addAttribute("shipment", shipment);
        
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> historyList;
        try {
        	historyList  = mapper.readValue(
                     refund.getHistory(),
                     new TypeReference<List<Map<String, Object>>>() {}
             );
		} catch (Exception e) {
			e.printStackTrace();
			historyList = null;
		}
       

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if(historyList != null) {
        	for (Map<String, Object> h : historyList) {
                String dateStr = (String) h.get("date");
                LocalDateTime dt = LocalDateTime.parse(dateStr, fmt);
                h.put("parsedDate", dt);
            }

            model.addAttribute("historyList", historyList);
        }
        

        return "client/order/refundDetail";
    }

	@PutMapping("/{id}/status")
	public ResponseEntity<?> updateStatusByShop(@PathVariable("id") Integer refundId,
			@RequestBody Map<String, Object> body, HttpSession session) {

		String newStatus = (String) body.get("status");
		String note = (String) body.get("note");
		boolean checkReturn = (Boolean) body.get("returnVarianat");

		try {
			var userId = (Integer) session.getAttribute("userId");
			var updated = refundService.updateRefundStatusByShop(refundId, newStatus, userId, note, checkReturn);

			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Failed to update order status"));
		}
	}

	@PostMapping("/confirm_return")
	public ResponseEntity<?> comfirmReturn(@RequestParam("refundId") Integer refundId, HttpSession session) {
		if(refundId == null || refundId <= 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Failed to update status"));
		}
		 try {
	        	var userId = (Integer) session.getAttribute("userId"); 
	            var updated = refundService.updateRefundStatusByBuyer(refundId, "RETURNING", userId);
	            return ResponseEntity.ok(updated);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
	                    "success", false,
	                    "message", "Failed to update status"
	            ));
	        }
		
	}
	@PostMapping("/escalate")
	public ResponseEntity<?> escalateAdmin(@RequestParam("refundId") Integer refundId, HttpSession session) {
		if(refundId == null || refundId <= 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Failed to update order status"));
		}
		try {
			var userId = (Integer) session.getAttribute("userId"); 
			var updated = refundService.updateRefundStatusByBuyer(refundId, "ESCALATED", userId);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
					"success", false,
					"message", "Failed to update status"
					));
		}
		
	}
	
	@PostMapping("/cancel")
	public ResponseEntity<?> cancel(@RequestParam("refundId") Integer refundId, HttpSession session) {
		if(refundId == null || refundId <= 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Failed to update order status"));
		}
		 try {
	        	var userId = (Integer) session.getAttribute("userId"); 
	            var updated = refundService.updateRefundStatusByBuyer(refundId, "CANCEL", userId);
	            return ResponseEntity.ok(updated);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
	                    "success", false,
	                    "message", "Failed to update order status"
	            ));
	        }
		
	}

	public int getStatusPercent(String status) {
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
