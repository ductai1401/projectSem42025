package projectSem4.com.controller.webController.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.model.entities.BuyerNotification;
import projectSem4.com.service.admin.BuyerNotificationService;

@Controller
@RequestMapping("/client/notifications")
public class NotificationController {

    @Autowired
    private BuyerNotificationService notificationService;

    /** Khi user click vào 1 thông báo trong dropdown */
    @GetMapping("/open/{id}")
    public String openNotification(@PathVariable("id") Integer id) {
        BuyerNotification notif = notificationService.findById(id);

        if (notif != null) {
            // ✅ Cập nhật trạng thái (đã đọc + đã click)
            notificationService.markReadAndClicked(id);

            // ✅ Redirect sang URL gốc
            String redirect = notif.getRedirectUrl();
            if (redirect != null && !redirect.isBlank()) {
                // Nếu link trong DB bắt đầu bằng "/", giữ nguyên
                if (redirect.startsWith("/")) return "redirect:" + redirect;
                // Nếu là link đầy đủ (http...), redirect trực tiếp
                if (redirect.startsWith("http")) return "redirect:" + redirect;
            }
        }
        // fallback nếu không có redirect URL
        return "redirect:/";
    }
}
