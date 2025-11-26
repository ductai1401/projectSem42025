package projectSem4.com.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectSem4.com.model.entities.NotificationTemplate;
import projectSem4.com.model.repositories.NotificationTemplateRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationTemplateService {

	@Autowired
	private NotificationTemplateRepository templateRepo;

	/* ================== CREATE ================== */
	public Map<String, Object> createTemplateJson(NotificationTemplate tpl) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (tpl == null) {
				res.put("ok", false);
				res.put("message", "Thiếu dữ liệu template.");
				return res;
			}
			if (tpl.getTitle() == null || tpl.getTitle().isBlank()) {
				res.put("ok", false);
				res.put("message", "Vui lòng nhập tiêu đề (Title).");
				return res;
			}
			if (tpl.getContent() == null || tpl.getContent().isBlank()) {
				res.put("ok", false);
				res.put("message", "Vui lòng nhập nội dung (Content).");
				return res;
			}

			Integer id = templateRepo.createReturningId(tpl);
			if (id == null) {
				res.put("ok", false);
				res.put("message", "Không tạo được template.");
			} else {
				res.put("ok", true);
				res.put("templateId", id);
			}
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi khi tạo template: " + e.getMessage());
			return res;
		}
	}

	public boolean createTemplate(NotificationTemplate tpl) {
		Object ok = createTemplateJson(tpl).get("ok");
		return ok instanceof Boolean && (Boolean) ok;
	}

	/* ================== READ ================== */
	public NotificationTemplate findById(Integer id) {
		return (id == null) ? null : templateRepo.findById(id);
	}

	public List<NotificationTemplate> findAll() {
		return templateRepo.findAll();
	}

	public List<NotificationTemplate> findAllPaged(int page, int size) {
		return templateRepo.findAllPaged(page, size);
	}

	/* ================== UPDATE ================== */
	public Map<String, Object> updateTemplateJson(NotificationTemplate tpl) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (tpl == null || tpl.getTemplateId() == null) {
				res.put("ok", false);
				res.put("message", "Thiếu TemplateID.");
				return res;
			}
			if (tpl.getTitle() == null || tpl.getTitle().isBlank()) {
				res.put("ok", false);
				res.put("message", "Vui lòng nhập tiêu đề (Title).");
				return res;
			}
			if (tpl.getContent() == null || tpl.getContent().isBlank()) {
				res.put("ok", false);
				res.put("message", "Vui lòng nhập nội dung (Content).");
				return res;
			}

			String msg = templateRepo.updateTemplate(tpl);
			res.put("ok", msg.toLowerCase().contains("thành công"));
			res.put("message", msg);
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi cập nhật template: " + e.getMessage());
			return res;
		}
	}

	/* ================== DELETE ================== */
	public Map<String, Object> deleteByIdJson(Integer id) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (id == null) {
				res.put("ok", false);
				res.put("message", "Thiếu TemplateID.");
				return res;
			}
			String msg = templateRepo.deleteById(id);
			res.put("ok", msg.toLowerCase().contains("thành công"));
			res.put("message", msg);
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi khi xóa template: " + e.getMessage());
			return res;
		}
	}
}
