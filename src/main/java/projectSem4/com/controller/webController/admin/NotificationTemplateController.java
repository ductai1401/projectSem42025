package projectSem4.com.controller.webController.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.dto.NotificationTemplateForm;
import projectSem4.com.model.entities.NotificationTemplate;
import projectSem4.com.service.admin.NotificationTemplateService;

import java.util.List;

@Controller
@RequestMapping("/admin/notification-templates")
public class NotificationTemplateController {

	@Autowired
	private NotificationTemplateService templateService;

	/* ================== PAGE LIST ================== */
	@GetMapping({ "", "/" })
	public String index(Model model) {
		List<NotificationTemplate> templates = templateService.findAll();
		model.addAttribute("templates", templates);
		return "admin/notificationTemplates/index";
	}

	/* ================== CREATE PAGE ================== */
	@GetMapping("/create")
	public String createPage(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new NotificationTemplateForm());
		}
		return "admin/notificationTemplates/create";
	}

	@PostMapping("/create")
	public String createSubmit(@ModelAttribute("form") NotificationTemplateForm form, BindingResult binding,
			Model model) {

		// ===== VALIDATE =====
		if (form.getCode() == null || form.getCode().isBlank()) {
			binding.rejectValue("code", "required", "Vui lòng nhập code");
		}
		if (form.getContent() == null || form.getContent().isBlank()) {
			binding.rejectValue("content", "required", "Vui lòng nhập nội dung");
		}

		if (binding.hasErrors()) {
			return "admin/notificationTemplates/create";
		}

		// ===== MAP FORM → ENTITY =====
		NotificationTemplate tpl = new NotificationTemplate();
		tpl.setCode(form.getCode().trim());
		tpl.setTitle(form.getTitle());
		tpl.setContent(form.getContent());
		tpl.setRedirectUrl(form.getRedirectUrl());
		tpl.setType(form.getType());
		tpl.setActive(Boolean.TRUE.equals(form.getActive()));

		// ===== SAVE =====
		boolean ok = templateService.createTemplate(tpl);
		model.addAttribute("ok", ok);
		model.addAttribute("message", ok ? "Tạo template thành công!" : "Tạo template thất bại!");

		// Reset form sau khi tạo thành công
		if (ok)
			model.addAttribute("form", new NotificationTemplateForm());

		return "admin/notificationTemplates/create";
	}

	/* ================== DELETE ================== */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id) {
		templateService.deleteByIdJson(id);
		return "redirect:/admin/notification-templates";
	}
}
