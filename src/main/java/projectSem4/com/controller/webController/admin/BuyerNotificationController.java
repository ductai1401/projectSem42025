package projectSem4.com.controller.webController.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.model.entities.BuyerNotification;
import projectSem4.com.service.admin.BuyerNotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/notifications")
public class BuyerNotificationController {

	@Autowired
	private BuyerNotificationService notificationService;

	/* ================== PAGE ================== */

	@GetMapping({ "", "/" })
	public String index(Model model, @RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "unreadOnly", required = false, defaultValue = "false") boolean unreadOnly,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "20") int size) {

		List<BuyerNotification> data;
		if (userId != null) {
			data = unreadOnly ? notificationService.findUnreadByUser(userId) : notificationService.findByUser(userId);
		} else {
			data = notificationService.findAllPaged(page, size);
		}

		model.addAttribute("items", data);
		model.addAttribute("userId", userId);
		model.addAttribute("unreadOnly", unreadOnly);
		model.addAttribute("page", page);
		model.addAttribute("size", size);

		return "admin/notifications/index";
	}

	/* ================== TEMPLATE USAGE (BẰNG TEMPLATE ID) ================== */

	/**
	 * Ví dụ gọi: POST
	 * /admin/notifications/use-template?templateId=5&userId=10&orderCode=ABC123&username=An
	 * Body form-data/x-www-form-urlencoded đều được.
	 */
	@PostMapping(value = "/use-template", produces = "application/json")
	@ResponseBody
	public Map<String, Object> useTemplate(@RequestParam("templateId") Integer templateId,
			@RequestParam("userId") Integer userId, @RequestParam Map<String, Object> params) {
		// Loại bỏ các tham số kỹ thuật để không bị thay thế vào template
		Map<String, Object> cleanParams = new HashMap<>(params);
		cleanParams.remove("templateId");
		cleanParams.remove("userId");

		return notificationService.useTemplateForUser(templateId, userId, cleanParams);
	}

	/**
	 * Gửi template 1 lần duy nhất cho mỗi user. Ví dụ: POST
	 * /admin/notifications/use-template-once?templateId=5&userId=10&username=An
	 */
	@PostMapping(value = "/use-template-once", produces = "application/json")
	@ResponseBody
	public Map<String, Object> useTemplateOnce(@RequestParam("templateId") Integer templateId,
			@RequestParam("userId") Integer userId, @RequestParam Map<String, Object> params) {
		Map<String, Object> cleanParams = new HashMap<>(params);
		cleanParams.remove("templateId");
		cleanParams.remove("userId");

		return notificationService.useTemplateOncePerUser(templateId, userId, cleanParams);
	}

	/* =============== QUICK APIs FOR UI BADGES =============== */

	@GetMapping(value = "/unread-count", produces = "application/json")
	@ResponseBody
	public Map<String, Object> unreadCount(@RequestParam("userId") Integer userId) {
		Map<String, Object> res = new HashMap<>();
		int count = notificationService.countUnreadByUser(userId);
		res.put("ok", true);
		res.put("userId", userId);
		res.put("unread", count);
		return res;
	}
}
