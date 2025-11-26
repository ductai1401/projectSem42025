// HeaderAdvice.java
package projectSem4.com.controller.webController._advice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import projectSem4.com.service.admin.BuyerNotificationService;
import projectSem4.com.model.entities.BuyerNotification;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class HeaderAdvice {

	@Autowired
	private BuyerNotificationService notificationService;

	@ModelAttribute
	public void injectHeaderData(Model model, Principal principal) {
		// Lấy userId hiện tại theo cách của bạn
		Integer userId = getCurrentUserId(principal); // bạn tự implement
		if (userId == null) {
			model.addAttribute("myStatus1Count", 0);
			model.addAttribute("myNotifications", List.of());
			return;
		}

		int status1Count = notificationService.countStatus1ByUser(userId);
		List<BuyerNotification> all = notificationService.getHeaderNotifications(userId, 20);

		// Chỉ giữ status = 1
		List<BuyerNotification> unreadOnly = all.stream().filter(n -> n.getStatus() != null && n.getStatus() == 1)
				.toList();

		model.addAttribute("myNotifications", unreadOnly);
		model.addAttribute("myUnreadCount", unreadOnly.size());

	}

	private Integer getCurrentUserId(Principal principal) {
		// TODO: Lấy userId từ principal/session tuỳ hệ thống của bạn
		return null;
	}
}
