package projectSem4.com.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSem4.com.model.entities.BuyerNotification;
import projectSem4.com.model.entities.NotificationTemplate;
import projectSem4.com.model.repositories.BuyerNotificationRepository;
import projectSem4.com.model.repositories.NotificationTemplateRepository;
import projectSem4.com.model.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BuyerNotificationService {

	@Autowired
	private BuyerNotificationRepository repo;

	@Autowired
	private NotificationTemplateRepository templateRepo;

	@Autowired(required = false)
	private UserRepository userRepository;

	/* ===================== CREATE ===================== */

	public Map<String, Object> createNotificationJson(CreateNotificationRequest req) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (req == null || req.userId == null || req.userId <= 0) {
				res.put("ok", false);
				res.put("message", "Thiếu hoặc sai userId.");
				return res;
			}
			if (req.content == null || req.content.isBlank()) {
				res.put("ok", false);
				res.put("message", "Nội dung trống.");
				return res;
			}

			BuyerNotification n = new BuyerNotification();
			n.setUserId(req.userId);
			n.setContent(req.content.trim());
			n.setRedirectUrl((req.redirectUrl == null || req.redirectUrl.isBlank()) ? null : req.redirectUrl.trim());
			n.setType(req.type == null ? 0 : req.type);
			n.setCreatedAt(LocalDateTime.now());
			n.setIsRead(false);
			n.setTemplateId(req.templateId);
			n.setStatus(1); // mặc định status = 1 (chưa click)

			Integer id = repo.createReturningId(n);
			res.put("ok", id != null);
			res.put("notificationId", id);
			return res;
		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi tạo thông báo: " + e.getMessage());
			return res;
		}
	}

	public Integer createSimple(Integer userId, String content, String url, Integer type) {
		if (userId == null || userId <= 0 || content == null || content.isBlank())
			return null;
		try {
			BuyerNotification n = new BuyerNotification();
			n.setUserId(userId);
			n.setContent(content.trim());
			n.setRedirectUrl((url == null || url.isBlank()) ? null : url.trim());
			n.setType(type == null ? 0 : type);
			n.setIsRead(false);
			n.setCreatedAt(LocalDateTime.now());
			n.setStatus(1);
			return repo.createReturningId(n);
		} catch (Exception e) {
			System.err.println("createSimple error: " + e.getMessage());
			return null;
		}
	}

	/* ===================== READ ===================== */

	public BuyerNotification findById(Integer id) {
		return (id == null || id <= 0) ? null : repo.findById(id);
	}

	public List<BuyerNotification> findAll() {
		return repo.findAll();
	}

	public List<BuyerNotification> findAllPaged(int page, int size) {
		return repo.findAllPaged(page, size);
	}

	public List<BuyerNotification> findByUser(Integer userId) {
		return (userId == null) ? List.of() : repo.findByUser(userId);
	}

	public List<BuyerNotification> findUnreadByUser(Integer userId) {
		return (userId == null) ? List.of() : repo.findUnreadByUser(userId);
	}

	public int countUnreadByUser(Integer userId) {
		return (userId == null) ? 0 : repo.countUnreadByUser(userId);
	}

	/* ===================== UPDATE ===================== */

	public Map<String, Object> markReadJson(Integer notificationId, Boolean read) {
		Map<String, Object> res = new HashMap<>();
		if (notificationId == null || notificationId <= 0) {
			res.put("ok", false);
			res.put("message", "Thiếu notificationId.");
			return res;
		}
		String msg = repo.markRead(notificationId, read != null ? read : true);
		res.put("ok", msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	@Transactional
	public Map<String, Object> markAllReadOfUserJson(Integer userId) {
		Map<String, Object> res = new HashMap<>();
		int rows = repo.markAllReadOfUser(userId);
		res.put("ok", true);
		res.put("updated", rows);
		res.put("message", "Đã đánh dấu đã đọc " + rows + " thông báo.");
		return res;
	}

	/** 🔔 Đổi status sang 2 khi user click */
	@Transactional
	public boolean markClicked(Integer notificationId) {
		if (notificationId == null || notificationId <= 0)
			return false;
		return repo.markClicked(notificationId) > 0;
	}

	/** 🔔 Đánh dấu đã đọc + đổi status=2 cùng lúc */
	@Transactional
	public boolean markReadAndClicked(Integer notificationId) {
		if (notificationId == null || notificationId <= 0)
			return false;
		return repo.markReadAndClicked(notificationId) > 0;
	}

	public Map<String, Object> updateContentJson(UpdateNotificationRequest req) {
		Map<String, Object> res = new HashMap<>();
		if (req == null || req.notificationId == null) {
			res.put("ok", false);
			res.put("message", "Thiếu notificationId.");
			return res;
		}
		String msg = repo.updateContent(req.notificationId, req.content, req.redirectUrl, req.type);
		res.put("ok", msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	/* ===================== DELETE ===================== */

	public Map<String, Object> deleteByIdJson(Integer notificationId) {
		Map<String, Object> res = new HashMap<>();
		String msg = repo.deleteById(notificationId);
		res.put("ok", msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	public Map<String, Object> deleteAllOfUserJson(Integer userId) {
		Map<String, Object> res = new HashMap<>();
		int rows = repo.deleteAllOfUser(userId);
		res.put("ok", true);
		res.put("deleted", rows);
		res.put("message", "Đã xóa " + rows + " thông báo của user.");
		return res;
	}

	/* ===================== TEMPLATE SUPPORT ===================== */

	private String render(String tpl, Map<String, Object> params) {
		if (tpl == null)
			return null;
		String out = tpl;
		if (params != null) {
			for (Map.Entry<String, Object> e : params.entrySet()) {
				out = out.replace("{{" + e.getKey() + "}}", String.valueOf(e.getValue()));
			}
		}
		return out;
	}

	public Map<String, Object> useTemplateForUser(Integer templateId, Integer userId, Map<String, Object> params) {
		Map<String, Object> res = new HashMap<>();
		NotificationTemplate tpl = templateRepo.findById(templateId);
		if (tpl == null) {
			res.put("ok", false);
			res.put("message", "Không tìm thấy template.");
			return res;
		}

		BuyerNotification n = new BuyerNotification();
		n.setUserId(userId);
		n.setContent(render(tpl.getContent(), params));
		n.setRedirectUrl(render(tpl.getRedirectUrl(), params));
		n.setType(tpl.getType());
		n.setIsRead(false);
		n.setCreatedAt(LocalDateTime.now());
		n.setTemplateId(tpl.getTemplateId());
		n.setStatus(1);

		Integer id = repo.createReturningId(n);
		res.put("ok", id != null);
		res.put("notificationId", id);
		return res;
	}

	public Map<String, Object> useTemplateOncePerUser(Integer templateId, Integer userId, Map<String, Object> params) {
		Map<String, Object> res = new HashMap<>();
		NotificationTemplate tpl = templateRepo.findById(templateId);
		if (tpl == null) {
			res.put("ok", false);
			res.put("message", "Template không tồn tại.");
			return res;
		}

		boolean exists = repo.existsByUserAndTemplate(userId, tpl.getTemplateId());
		if (exists) {
			res.put("ok", true);
			res.put("message", "Đã tồn tại, bỏ qua.");
			return res;
		}
		return useTemplateForUser(templateId, userId, params);
	}

	/* ===================== TEMPLATE: GỬI HÀNG LOẠT ===================== */

	/**
	 * 🔔 Gửi thông báo “sản phẩm mới từ cửa hàng yêu thích” cho danh sách user
	 */
	public void notifyNewProductFromShop(Integer shopId, String shopName, Integer productId, String productName,
			List<Integer> userIds) {
		if (userIds == null || userIds.isEmpty())
			return;

		NotificationTemplate tpl = templateRepo.findByCode("NEW_PRODUCT_FROM_FAVORITE_SHOP");
		if (tpl == null) {
			System.err.println("Không tìm thấy template NEW_PRODUCT_FROM_FAVORITE_SHOP");
			return;
		}

		String safeShopName = (shopName != null && !shopName.isBlank()) ? shopName : "một cửa hàng yêu thích";

		for (Integer uid : userIds) {
			Map<String, Object> params = new HashMap<>();
			params.put("shopName", safeShopName);
			params.put("productName", productName);
			params.put("productId", productId);
			params.put("shopId", shopId);
			String username = getUsernameByUserId(uid);
			params.put("username", (username != null && !username.isBlank()) ? username : "bạn");

			useTemplateForUser(tpl.getTemplateId(), uid, params);
		}
	}

	private String getUsernameByUserId(Integer userId) {
		try {
			return (userRepository != null) ? userRepository.findUsernameById(userId) : null;
		} catch (Exception e) {
			return null;
		}
	}

	/* ===================== DTO ===================== */

	public static class CreateNotificationRequest {
		public Integer userId;
		public String content;
		public String redirectUrl;
		public Integer type;
		public Integer templateId;
	}

	public static class UpdateNotificationRequest {
		public Integer notificationId;
		public String content;
		public String redirectUrl;
		public Integer type;
	}

	// BuyerNotificationService.java
	public int countStatus1ByUser(Integer userId) {
		return (userId == null) ? 0 : repo.countStatus1ByUser(userId);
	}

	/** (tuỳ chọn) top N cho dropdown */
	public List<BuyerNotification> getHeaderNotifications(Integer userId, int limit) {
		return (userId == null) ? List.of() : repo.findByUserTopN(userId, limit);
	}

}
