package projectSem4.com.controller.webController.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import projectSem4.com.controller.apiController.admin.UserApiController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpSession;
import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.dto.ValiOrderResult;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.model.modelViews.ShopView;
import projectSem4.com.service.GHNService;
import projectSem4.com.service.PaymentAttemptService;
import projectSem4.com.service.PaymentService;
import projectSem4.com.service.VNPayService;
import projectSem4.com.service.admin.ShopService;
import projectSem4.com.service.admin.UserService;
import projectSem4.com.service.client.CheckoutService;

import projectSem4.com.service.client.OrderService;

@Controller
@RequestMapping("checkout")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private VNPayService vnpayService;
    
    @Autowired PaymentService paymentService;
    
    @Autowired
    private PaymentAttemptService paymentAttemptService;
    
    
    @GetMapping("")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) userId = 0;

        String cartDataJson = (String) session.getAttribute("req");
        if (cartDataJson == null || cartDataJson.isEmpty()) {
            redirectAttrs.addFlashAttribute("message", "Giỏ hàng của bạn trống hoặc đơn đã thanh toán");
            return "redirect:/cart"; // quay về trang giỏ hàng
        }
        

        Map<String, Object> checkoutData = checkoutService.prepareCheckoutData(userId, cartDataJson);
        
        var checkoutToken = (String) session.getAttribute("checkoutToken");
        if(checkoutToken == null) {
        	 return "redirect:/cart"; // quay về cart
        }
        model.addAttribute("checkoutToken", checkoutToken);
        model.addAllAttributes(checkoutData); // map -> model
        return "client/checkout";
    }

    @PostMapping("/place-order")
    @ResponseBody
    public Map<String, Object> createOrder(@RequestBody CheckCartItem data, HttpSession session){
    	var token = data.getToken();
    	var methodPayment = data.getMethodPayment();
    	String tokenInSession = (String) session.getAttribute("checkoutToken");
    	if(tokenInSession == null || !tokenInSession.equals(token)) {
    	    Map<String, Object> res = new HashMap<>();
    	    res.put("success", false);
    	    res.put("message", "Cart đã hết hạn hoặc đơn đã được submit");
    	    res.put("redirectUrl", "/cart");
    	    return res;
    	}
    	
    	
        Integer userId = (Integer) session.getAttribute("userId");
        var result = orderService.placeOrderCOD(data, userId);  
    
        var success = (Boolean) result.get("success");
        if(success) {
        	session.removeAttribute("checkoutToken");
        }
        return result;
    }
    
    @PostMapping("/continue")
    @ResponseBody
    public ResponseEntity<?> continuePayment(@RequestParam(name = "orderId") String orderId) {
        // Check chống spam (nếu bật Redis thì nên để lại)
        // if (!paymentAttemptService.allowPayment(orderId)) {
        //     return ResponseEntity.status(429).body("Bạn đã thử thanh toán lại quá nhiều lần. Vui lòng thử sau 30 phút.");
        // }

        try {
            String paymentUrl = paymentService.createOrReusePaymentUrl(orderId);
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Có lỗi xảy ra, vui lòng thử lại sau"));
        }
    }
    
    @PostMapping("/cancel")
    @ResponseBody
    public ResponseEntity<?> cencalOrder(@RequestParam(name = "orderId") String orderId,
    		@RequestParam(name = "reason") String reason
    		) {
       
        try {
            
            var a = orderService.updateOrderStatusByBuyer(orderId, 4, reason);
            
          return ResponseEntity.ok(a);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success" , false));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Có lỗi xảy ra, vui lòng thử lại sau", "success" , false));
        }
    }
    @PostMapping("/confirm")
    @ResponseBody 
    public ResponseEntity<?> confirmOrder(@RequestParam(name = "orderId") String order){
    	try {
			var a = orderService.updateOrderStatusByBuyer(order, 5, null);
			return ResponseEntity.ok(a);
		} catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success" , false));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Có lỗi xảy ra, vui lòng thử lại sau", "success" , false));
        }
    }
    
}
