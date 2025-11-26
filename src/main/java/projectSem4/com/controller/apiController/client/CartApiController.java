
package projectSem4.com.controller.apiController.client;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import projectSem4.com.dto.CartItemDto;
import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.service.JwtProvider;
import projectSem4.com.service.client.CartService;


@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;
    
    @Autowired 
    private JwtProvider jwtService;


    // ✅ Load giỏ hàng (guest bằng json, user thì lấy theo userId)
    @GetMapping("/load")
    public List<CartItemView> loadCart(
            @RequestParam(required = false, defaultValue = "0") int userId,
            @RequestParam(required = false) String json
    ) {
        return cartService.loadData(userId, json);
    }

    // ✅ Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public Map<String, Object> addToCart(
            @RequestParam int quantity,
            @RequestParam int variantId,
            @RequestParam int userId
    ) {
        return cartService.addToCart(quantity, variantId, userId);
    }

    // ✅ Merge giỏ hàng local (guest) vào giỏ hàng user
    @PostMapping("/merge")
    public List<CartItemView> mergeCart(
            @RequestParam int userId,
            @RequestBody List<CartItemDto> localCart
    ) {
        return cartService.mergeCart(userId, localCart);
    }

    // ✅ Update số lượng sản phẩm
    @PutMapping("/update")
    public Map<String, Object> updateItem(
            @RequestParam int userId,
            @RequestParam int variantId,
            @RequestParam int quantity
    ) {
        return cartService.updateItem(userId, variantId, quantity);
    }

    // ✅ Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/delete")
    public Map<String, Object> deleteItem(
            @RequestParam int userId,
            @RequestParam int variantId
    ) {
        return cartService.deleteItem(userId, variantId);
    }

    // ✅ Checkout
    @PostMapping("/checkout/{userId}")
    public Map<String, Object> checkout(
    		@PathVariable(name = "userId") Integer userId,
            @RequestBody CheckCartItem req
    ) {
    	System.out.println("vao");
    	var a = cartService.checkOut(userId, req);
    	System.out.println(a);
        return a;
    }
}
