package projectSem4.com.controller.webController.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.dto.WishlistItemDTO;
import projectSem4.com.service.client.WishlistItemService;

import java.util.List;

@Controller
@RequestMapping("/client/wishlist")
public class WishlistItemController {

    @Autowired
    private WishlistItemService wishlistItemService;

    /** Chỉ đọc session hiện có (KHÔNG tạo mới). Trả về userId nếu tìm thấy. */
    private Integer currentUserId(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        if (ss == null) return null;

        Object uid = ss.getAttribute("userId");
        if (uid instanceof Integer) return (Integer) uid;
        if (uid instanceof Number)   return ((Number) uid).intValue();

        Object user = ss.getAttribute("currentUser");
        if (user != null) {
            try {
                Object val = user.getClass().getMethod("getUserId").invoke(user);
                if (val instanceof Integer) return (Integer) val;
                if (val instanceof Number)  return ((Number) val).intValue();
            } catch (Exception ignore) { /* no-op */ }
        }
        return null;
    }

    /** Trang danh sách wishlist (dùng DTO để có ảnh & giá). */
    @GetMapping("")
    public String page(HttpServletRequest req, Model model) {
        Integer userId = currentUserId(req);
        if (userId == null) return "redirect:/client/login";

        List<WishlistItemDTO> items = wishlistItemService.getUserWishlistDTO(userId);
        model.addAttribute("wishlistItems", items);
        model.addAttribute("totalWishlist", items != null ? items.size() : 0);
        return "client/wishlist/index";
    }

    /** Thêm sản phẩm vào wishlist. */
    @PostMapping("/add")
    public String add(@RequestParam(name = "productId", required = true) Integer productId,
                      HttpServletRequest req) {
        Integer userId = currentUserId(req);
        if (userId == null) return "redirect:/client/login";

        wishlistItemService.addItemJson(
                new WishlistItemService.AddWishlistRequest(userId, productId, 1)
        );
        return "redirect:/client/wishlist";
    }

    /** Xóa theo wishlistId. */
    @GetMapping("/delete/{wishlistId}")
    public String delete(@PathVariable("wishlistId") Integer wishlistId,
                         HttpServletRequest req) {
        Integer userId = currentUserId(req);
        if (userId == null) return "redirect:/client/login";

        wishlistItemService.removeById(wishlistId);
        return "redirect:/client/wishlist";
    }

    /** Xóa theo productId (cặp user+product). */
    @GetMapping("/delete-product/{productId}")
    public String deleteByProduct(@PathVariable("productId") Integer productId,
                                  HttpServletRequest req) {
        Integer userId = currentUserId(req);
        if (userId == null) return "redirect:/client/login";

        wishlistItemService.removeByUserAndProduct(userId, productId);
        return "redirect:/client/wishlist";
    }

    /** Cập nhật status wishlist item. */
    @PostMapping("/{wishlistId}/status")
    public String updateStatus(@PathVariable("wishlistId") Integer wishlistId,
                               @RequestParam(name = "status", required = true) Integer status,
                               HttpServletRequest req) {
        Integer userId = currentUserId(req);
        if (userId == null) return "redirect:/client/login";

        wishlistItemService.updateStatus(wishlistId, status);
        return "redirect:/client/wishlist";
    }
}
