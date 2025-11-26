package projectSem4.com.controller.webController.seller;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import projectSem4.com.model.entities.Order;
import projectSem4.com.model.entities.OrderItem;
import projectSem4.com.model.modelViews.OrderItemView;
import projectSem4.com.model.modelViews.OrderView;
import projectSem4.com.model.modelViews.RefundView;
import projectSem4.com.service.RefundService;
import projectSem4.com.service.client.OrderService;

@Controller
@RequestMapping("/seller")
public class OrderController {
	@Autowired
	private OrderService oService;
	
	@Autowired
	private RefundService refundService;
	
	@GetMapping("/order")
	public String Index(Model model) {
		// Dummy data
		return "seller/order/order";
	}
	
	@GetMapping("/refund")
	public String refund(Model model, HttpSession session) {
		
		return "seller/order/refund";
	}
	
	@GetMapping("/loadData")
	@ResponseBody
	public Map<String, Object> fillterOrder(
	        @RequestParam(name = "page", defaultValue = "1") int page,
	        @RequestParam(name = "size", defaultValue = "10") int size,
	        @RequestParam(name = "keyword", required = false) String keyword,
	        @RequestParam(name = "date", required = false) String date,
	        @RequestParam(name = "status", required = false) String status,
	        @RequestParam(name = "payment", required = false) String payment, HttpSession session) {
	    
	    java.sql.Date parsedDate = null;
	    try {
	        if (date != null && !date.isEmpty()) {
	            java.util.Date utilDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
	            parsedDate = new java.sql.Date(utilDate.getTime());
	        }
	    } catch (Exception e) {
	        parsedDate = null;
	    }
	    var shopId = (Integer) session.getAttribute("shopId");
	   
	    return oService.fillterOrderByShopId(page, size, keyword, parsedDate, status, payment, shopId.intValue());
	}
	
	@GetMapping("/loadDataRefund")
	@ResponseBody
	public Map<String, Object> fillterRefund(
	        @RequestParam(name = "page", defaultValue = "1") int page,
	        @RequestParam(name = "size", defaultValue = "10") int size,
	        @RequestParam(name = "keyword", required = false) String keyword,
	        @RequestParam(name = "date", required = false) String date,
	        @RequestParam(name = "status", required = false) String status, HttpSession session) {
	    
	    java.sql.Date parsedDate = null;
	    try {
	        if (date != null && !date.isEmpty()) {
	            java.util.Date utilDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
	            parsedDate = new java.sql.Date(utilDate.getTime());
	        }
	    } catch (Exception e) {
	        parsedDate = null;
	    }
	    var shopId = (Integer) session.getAttribute("shopId");
	   
	    return refundService.getRefunByShop(shopId, size,page , status, keyword, parsedDate);
	}

	
	@GetMapping("/orders/{id}")
	public String orderDetailFragment(@PathVariable("id") String id, Model model) {
	    // Lấy đơn hàng
	  
	   var order = oService.getOrderItemByOrderId(id);
	    // Đưa vào model
	    model.addAttribute("order", order);

	    return "seller/order/detail :: detail";
	}
	@GetMapping("/refund/{id}")
	public String refundDetailFragment(@PathVariable("id") Integer id, Model model) {
	    RefundView refund = refundService.getRefundViewByID(id);
	    List<String> mediaList = new ArrayList<>();
	    if(refund.getEvidence() != null && !refund.getEvidence().isEmpty()) {
	        // Nếu JSON dạng ["url1","url2"] hoặc CSV "url1,url2"
	        try {
	            mediaList = new ObjectMapper().readValue(refund.getEvidence(), new TypeReference<List<String>>(){});
	        } catch (Exception e) {
	            // fallback CSV
	            mediaList = Arrays.asList(refund.getEvidence().split(","));
	        }
	    }


	    model.addAttribute("mediaList", mediaList); // biến này dùng trong fragment
	    model.addAttribute("refund", refund);
	    return "seller/order/refundDetail :: refundDetailFragment"; // Trả về fragment
	}
	
	@PutMapping("refund/{id}/status")
	public ResponseEntity<?> updateStatusRefundByShop(
	        @PathVariable("id") Integer refundId,
	        @RequestBody Map<String, Object> body,
	        HttpSession session) {

	    String newStatus = (String) body.get("status");
	    String note = (String) body.get("note");
	    Boolean requestReturn = (Boolean) body.get("requestReturn"); // lấy từ body

	    try {
	        Integer userId = (Integer) session.getAttribute("userId");
	        if (userId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
	                "success", false,
	                "message", "Bạn chưa đăng nhập"
	            ));
	        }

	        // gọi service để xử lý
	        var updated = refundService.updateRefundStatusByShop(refundId, newStatus, userId ,note , requestReturn);
	        var sucess = (Boolean) updated.get("success");
	        return ResponseEntity.ok(Map.of(
	            "success", sucess,
	            "message", updated.get("message"),
	            "data", updated
	        ));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
	                "success", false,
	                "message", "Failed to update order status"
	        ));
	    }
	}
	
	@PutMapping("/order/{id}/status")
	public ResponseEntity<?> updateStatusOrderByShop(
	        @PathVariable("id") String orderId,
	        @RequestBody Map<String, Object> body,
	        HttpSession session) {

	    String newStatus = (String) body.get("status");
	    String node = (String) body.get("note");
	    try {
	        Integer userId = (Integer) session.getAttribute("userId");
	        if (userId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
	                "success", false,
	                "message", "Bạn chưa đăng nhập"
	            ));
	        }

	        // gọi service để xử lý
	        var updated = oService.updateOrderStatusByShop(orderId, newStatus, userId, node);
	        var success = (Boolean) updated.get("success");
	        return ResponseEntity.ok(Map.of(
	            "success", success,
	            "message", updated.get("message"),
	            "data", updated
	        ));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
	                "success", false,
	                "message", "Failed to update order status"
	        ));
	    }
	}
	
}
