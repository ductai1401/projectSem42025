package projectSem4.com.controller.webController.client;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.dto.CartItemDto;
import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.model.entities.CartItem;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.service.CouponService;
import projectSem4.com.service.client.CartService;



@Controller
@RequestMapping("cart")
public class CartController {
	 @Autowired
	    private CartService cartService;
	 
	 @Autowired 
	 private CouponService couponService;

	    // Trang giỏ hàng
	    @GetMapping("")
	    public String index(Model model) {
	        // Lấy userId từ principal (tạm thời mock userId=2 nếu bạn chưa làm login)
//	        int userId = 2;
//
//	        Map<String, Object> dataCart = cartService.loadData(); 
//	        model.addAttribute("cartData", dataCart);
	    	var vSan = couponService.getByType("PLATFORM", null);
	    	var freeship = couponService.getByType("FREESHIP", null);
	    	
	    	ObjectMapper mapper = new ObjectMapper();
	    	try {
	    		model.addAttribute("voucherSan", vSan);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	

	    	model.addAttribute("freeShip", freeship);
	    	

	        return "client/cart"; // => /WEB-INF/views/client/cart.jsp
	    }
	    
	    @PostMapping("/add")
	    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpSession session) {
	        int userId = (Integer) session.getAttribute("userId"); // TODO: lấy từ principal hoặc token
 	        Integer variantId = ((Number) body.get("variantId")).intValue();
	        Integer quantity = ((Number) body.get("quantity")).intValue();

	        Map<String, Object> response = cartService.addToCart(quantity, variantId, userId);
	        return ResponseEntity.ok(response);
	    }
	    
	    @GetMapping("loadData")
	    public ResponseEntity<?> loadData(Model model,  HttpServletRequest request, HttpSession session) {
	       
	        Integer userId = 0;
	        try {
				userId = (Integer) session.getAttribute("userId");
			} catch (Exception e) {
				e.printStackTrace();
				
			}   
	        System.out.println(userId);

	        return ResponseEntity.ok(cartService.loadData(userId, ""));
	    }
	    
	    @PostMapping("local")
	    public ResponseEntity<?> loadDataByJson(@RequestBody String cartJson, Model model,  HttpServletRequest request, HttpSession session) {
	        return ResponseEntity.ok(cartService.loadData(0, cartJson));
	    }
	    
	    @PostMapping("/updateQuantity")
	    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpSession session) {
	        int userId = (Integer) session.getAttribute("userId"); 
 	        Integer variantId = ((Number) body.get("variantId")).intValue();
	        Integer quantity = ((Number) body.get("quantity")).intValue();

	        Map<String, Object> response = cartService.updateItem(userId , variantId ,quantity);
	        return ResponseEntity.ok(response);
	    }
	    
	    @PostMapping("/delete")
	    public ResponseEntity<?> delete(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpSession session) {
	        int userId = (Integer) session.getAttribute("userId"); // TODO: lấy từ principal hoặc token
 	        Integer variantId = ((Number) body.get("variantId")).intValue(); 

	        Map<String, Object> response = cartService.deleteItem(userId , variantId);
	        return ResponseEntity.ok(response);
	    }
	    
	    @PostMapping("/voucherShop")
	    public ResponseEntity<?> loadVoucherShop(@RequestBody Map<String, Integer> payload) {
	        Integer idShop = payload.get("idShop");
	        if (idShop == null) {
	            return ResponseEntity.badRequest().body("Shop ID is required");
	        }
	        return ResponseEntity.ok(couponService.getByShop(idShop));
	    }
	    
	 // Gọi từ Ajax JS
	    @PostMapping("/checkCart")
	    @ResponseBody
	    public ResponseEntity<?> startCheckout(@RequestBody CheckCartItem request,
	                                           HttpSession session) {
	        // ✅ Lấy userId từ session
	        Integer userId = (Integer) session.getAttribute("userId");

	        // ✅ Gọi service
	        Map<String, Object> result = cartService.checkOut(userId, request);

	        // ✅ Nếu có lỗi thì trả 400, ngược lại 200
	        if (!(Boolean) result.get("success")) {
	            return ResponseEntity.badRequest().body(result);
	        }
	        var items  = result.get("items"); 
	        try {
	        	if(items != null) {
		        	ObjectMapper mapper = new ObjectMapper();
		        	mapper.registerModule(new JavaTimeModule());
		        	mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		        	String json = mapper.writeValueAsString(result);
		        	session.setAttribute("req", json);
		        	String checkoutToken = UUID.randomUUID().toString();
		        	session.setAttribute("checkoutToken", checkoutToken);
		        	
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        
	        return ResponseEntity.ok(result);
	    }
	    
	    
	    @PostMapping("/merge")
	    @ResponseBody
	    public ResponseEntity<?> mergeCart(@RequestBody List<CartItemDto> localCart, HttpSession session) {
	        Integer userId = (Integer) session.getAttribute("userId");
	        if (userId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                                 .body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
	        }

	        List<CartItemView> merged = cartService.mergeCart(userId, localCart);
	        return ResponseEntity.ok(Map.of("success", true, "items", merged));
	    }
    
}
