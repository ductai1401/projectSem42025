package projectSem4.com.controller.apiController.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.model.entities.WishlistItem;
import projectSem4.com.service.client.WishlistItemService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/wishlist")
public class WishlistItemApiController {

    @Autowired
    private WishlistItemService wishlistItemService;

    /* =============== READ =============== */

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getWishlist(@PathVariable Integer userId) {
        List<WishlistItem> list = wishlistItemService.getUserWishlist(userId);
        return ResponseEntity.ok(Map.of("ok", true, "data", list));
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<?> count(@PathVariable Integer userId) {
        int total = wishlistItemService.countByUser(userId);
        return ResponseEntity.ok(Map.of("ok", true, "total", total));
    }

    /* =============== CREATE =============== */

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody WishlistItemService.AddWishlistRequest req) {
        Map<String, Object> res = wishlistItemService.addItemJson(req);
        return ResponseEntity.status(Boolean.TRUE.equals(res.get("ok")) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(res);
    }

    /* =============== UPDATE =============== */

    @PatchMapping("/{wishlistId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer wishlistId,
                                          @RequestParam("status") Integer status) {
        String msg = wishlistItemService.updateStatus(wishlistId, status);
        boolean ok = msg.toLowerCase().contains("thành công");
        return ResponseEntity.status(ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", ok, "message", msg));
    }

    /* =============== DELETE =============== */

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> delete(@PathVariable Integer wishlistId) {
        Map<String, Object> res = wishlistItemService.removeById(wishlistId);
        return ResponseEntity.status(Boolean.TRUE.equals(res.get("ok")) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(res);
    }

    @DeleteMapping("/by-user-product")
    public ResponseEntity<?> deleteByUserAndProduct(@RequestParam("userId") Integer userId,
                                                    @RequestParam("productId") Integer productId) {
        Map<String, Object> res = wishlistItemService.removeByUserAndProduct(userId, productId);
        return ResponseEntity.status(Boolean.TRUE.equals(res.get("ok")) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(res);
    }
}
