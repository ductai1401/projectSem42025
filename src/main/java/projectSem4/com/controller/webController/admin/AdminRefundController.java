package projectSem4.com.controller.webController.admin;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.service.RefundService;

@Controller
@RequestMapping("admin/refunds")
public class AdminRefundController {
	@Autowired
	private RefundService refundService;
	
	@GetMapping("")
	public String index(@RequestParam(name = "page", defaultValue = "1") int page,
	        @RequestParam(name = "size", defaultValue = "10") int size,
	        @RequestParam(name = "keyword", required = false) String keyword,
	        @RequestParam(name = "date", required = false) String date,
	        @RequestParam(name = "status", required = false) String status, HttpSession session,
			Model model) {
		java.sql.Date parsedDate = null;
	    try {
	        if (date != null && !date.isEmpty()) {
	            java.util.Date utilDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
	            parsedDate = new java.sql.Date(utilDate.getTime());
	        }
	    } catch (Exception e) {
	        parsedDate = null;
	    }
	    
	    var data = refundService.getRefunByAdmin(size ,page , status, keyword, parsedDate);
	    model.addAllAttributes(data);
		
	    return "admin/refund/index";
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
	
	@PostMapping("/{id}/refund")
	@ResponseBody
	public ResponseEntity<?> refund(@PathVariable("id") Integer refundId,
	        @RequestBody Map<String, Object> body) {
		if(refundId == null || refundId <= 0) {
			return ResponseEntity.status(500).body("Id not fund.");
		}
		if(body.isEmpty()) {
			return ResponseEntity.badRequest().body("Id not fund.");
		}
		var reason = (String) body.get("reason");
		var rs = refundService.updateStatusByAdmin(refundId, null, 0, null);
		
		//TODO: process POST request
		
		return ResponseEntity.ok(Map.of("success", true, "meassage", "Refund success."));
	}
	@PostMapping("/{id}/reject")
	@ResponseBody
	public ResponseEntity<?> reject(@PathVariable("id") Integer refundId,
	        @RequestBody Map<String, Object> body, HttpServletRequest req) {
		//TODO: process POST request
		var reason = (String) body.get("reason");
		var userId = (Integer) req.getAttribute("userId");
		var rs = refundService.updateStatusByAdmin(refundId, "REJECTED", userId, reason);
		return  ResponseEntity.ok(rs);
	}
	@PostMapping("/{id}/approve")
	@ResponseBody
	public ResponseEntity<?> approve(@PathVariable("id") Integer refundId,
	        @RequestBody Map<String, Object> body,  HttpServletRequest req) {
		var reason = (String) body.get("reason");
		var userId = (Integer) req.getAttribute("userId");
		var rs = refundService.updateStatusByAdmin(refundId, "APPROVED", userId, reason);
		//TODO: process POST request
		
		return  ResponseEntity.ok(rs);
	}
	

}
