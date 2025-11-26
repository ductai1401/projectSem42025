package projectSem4.com.controller.apiController.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import projectSem4.com.model.entities.User;
import projectSem4.com.service.admin.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    // Lấy danh sách Buyers (CUSTOMER)
    @GetMapping("/buyers")
    public Map<String, Object> getBuyers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<User> data = userService.getBuyers(keyword, page, size);
        int total = userService.countBuyers(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("buyers", data);
        result.put("keyword", keyword);
        result.put("page", page);
        result.put("total", total);
        result.put("size", size);
        return result;
    }

    // Lấy danh sách Sellers
    @GetMapping("/sellers")
    public Map<String, Object> getSellers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<User> data = userService.getSellers(keyword, page, size);
        int total = userService.countSellers(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("sellers", data);
        result.put("keyword", keyword);
        result.put("page", page);
        result.put("total", total);
        result.put("size", size);
        return result;
    }

    // Xem thông tin user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // Toggle trạng thái (khóa/mở)
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleStatus(@PathVariable int id) {
        userService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}