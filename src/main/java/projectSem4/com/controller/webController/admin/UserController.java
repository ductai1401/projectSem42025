// projectSem4.com.controller.admin.UserController
package projectSem4.com.controller.webController.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.model.entities.User;

import projectSem4.com.service.admin.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

	private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // BUYERS
    @GetMapping({"", "/", "/buyers"})
    public String buyers(Model model,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {

        List<User> data = userService.getBuyers(keyword, page, size);
        int total = userService.countBuyers(keyword);

        model.addAttribute("buyers", data);
        model.addAttribute("buyerKeyword", keyword);
        model.addAttribute("buyerPage", page);
        model.addAttribute("buyerTotal", total);
        model.addAttribute("size", size);
        return "admin/user/userList";
    }

    // SELLERS
    @GetMapping("/sellers")
    public String sellers(Model model,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {

        List<User> data = userService.getSellers(keyword, page, size);
        int total = userService.countSellers(keyword);

        model.addAttribute("sellers", data);
        model.addAttribute("sellerKeyword", keyword);
        model.addAttribute("sellerPage", page);
        model.addAttribute("sellerTotal", total);
        model.addAttribute("size", size);
        return "admin/user/userList";
    }

    // Khóa/Mở
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable int id,
                         @RequestParam(defaultValue = "buyers") String tab) {
        userService.toggleStatus(id);
        return "redirect:/admin/users" + ("sellers".equals(tab) ? "/sellers" : "");
    }

    // Xóa user
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id,
                         @RequestParam(defaultValue = "buyers") String tab,
                         @RequestParam(defaultValue = "1") int page) {
        userService.deleteUser(id);
        return "redirect:/admin/users/" + ("sellers".equals(tab) ? "sellers" : "buyers") + "?page=" + page;
    }

    // Trang duyệt seller (nếu bạn muốn để riêng)
    @GetMapping("/approved")
    public String approvedSeller() {
        return "admin/user/seller_approvals";
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable int id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user/editUser";
    }

}
