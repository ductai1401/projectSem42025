package projectSem4.com.controller.apiController.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.service.admin.BuyerNotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/notifications")
public class ClientNotificationController {

	@Autowired
	private BuyerNotificationService notificationService;

	/* --------------------- Helpers --------------------- */
	private Integer getSessionUserId(HttpServletRequest request) {
		Object id = (request.getSession(false) != null) ? request.getSession(false).getAttribute("userId") : null;
		return (id instanceof Integer) ? (Integer) id : null;
	}

	/* --------------------- API Lấy danh sách --------------------- */
	@GetMapping("/list")
	public Map<String, Object> getNotifications(HttpServletRequest request) {
		Integer userId = getSessionUserId(request);
		Map<String, Object> res = new HashMap<>();

		if (userId == null) {
			res.put("ok", false);
			res.put("message", "User not logged in");
			return res;
		}

		List<?> list = notificationService.findByUser(userId);
		res.put("ok", true);
		res.put("data", list);
		return res;
	}

	/* --------------------- API Đếm chưa đọc --------------------- */
	@GetMapping("/unread-count")
	public Map<String, Object> getUnreadCount(HttpServletRequest request) {
		Integer userId = getSessionUserId(request);
		int count = (userId == null) ? 0 : notificationService.countUnreadByUser(userId);

		Map<String, Object> res = new HashMap<>();
		res.put("ok", true);
		res.put("userId", userId);
		res.put("unread", count);
		return res;
	}

	/* --------------------- API Đánh dấu 1 thông báo --------------------- */
	@PostMapping("/{id}/read")
	public Map<String, Object> markAsRead(@PathVariable("id") Integer notificationId, HttpServletRequest request) {
		Integer userId = getSessionUserId(request);
		if (userId == null) {
			return Map.of("ok", false, "message", "User not logged in");
		}

		// Optionally: kiểm tra xem thông báo này có thuộc về user đó không (nếu cần)
		return notificationService.markReadJson(notificationId, true);
	}

	/* --------------------- API Đánh dấu tất cả đã đọc --------------------- */
	@PostMapping("/read-all")
	public Map<String, Object> markAllAsRead(HttpServletRequest request) {
		Integer userId = getSessionUserId(request);
		if (userId == null) {
			return Map.of("ok", false, "message", "User not logged in");
		}

		return notificationService.markAllReadOfUserJson(userId);
	}
}
