package projectSem4.com.controller.webController.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import projectSem4.com.dto.CouponForm;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.service.CouponService;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final CouponService service;

    public AdminCouponController(CouponService service) {
        this.service = service;
    }

    // ===== LIST =====
    @GetMapping("/index")
    public String index(@RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size,
                        @RequestParam(name = "q", required = false) String q,
                        Model model) {

        model.addAttribute("coupons", service.adminList(page, size, q));
        model.addAttribute("total", service.adminCount(q));
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("q", q == null ? "" : q);
        return "admin/coupon/index";
    }

    // ===== CREATE =====
    @GetMapping("/create")
    public String create(Model model) {
        CouponForm f = new CouponForm();
        f.setCouponType("PLATFORM");
        f.setStatus(1);
        model.addAttribute("form", f);
        return "admin/coupon/create"; // ✅ view riêng
    }

    @PostMapping("/create")
    public String doCreate(@Valid @ModelAttribute("form") CouponForm f,
                           BindingResult br,
                           Model model) {
        if (br.hasErrors()) {
            return "admin/coupon/create"; // ✅ render lại đúng view
        }
        try {
            service.adminCreate(f);
            return "redirect:/admin/coupons";
        } catch (Exception ex) {
            br.rejectValue("code", "err", ex.getMessage());
            return "admin/coupon/index";
        }
    }

    // ===== EDIT =====
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") int id, Model model) {
        Coupon c = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        CouponForm f = new CouponForm();
        f.setCouponID(c.getCouponId());
        f.setCode(c.getCode());
        f.setCouponType("PLATFORM");
        f.setDiscountType(c.getDiscountType());
        f.setDiscountValue(c.getDiscountValue());
        f.setMaxDiscount(c.getMaxDiscount());
        f.setMinOrderAmount(c.getMinOrderAmount());
        f.setStartDate(c.getStartDate());
        f.setEndDate(c.getEndDate());
        f.setUsageLimit(c.getUsageLimit());
        f.setStatus(c.getStatus());

        model.addAttribute("form", f);
        return "admin/coupon/edit"; // ✅ view riêng
    }

    @PostMapping("/{id}/edit")
    public String doEdit(@PathVariable("id") int id,
                         @Valid @ModelAttribute("form") CouponForm f,
                         BindingResult br,
                         Model model) {
        if (br.hasErrors()) {
            return "admin/coupon/edit"; // ✅ render lại đúng view
        }
        try {
            service.adminUpdate(id, f);
            return "redirect:/admin/coupons";
        } catch (Exception ex) {
            br.rejectValue("code", "err", ex.getMessage());
            return "admin/coupon/edit";
        }
    }

    // ===== DELETE =====
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") int id) {
        service.adminDelete(id);
        return "redirect:/admin/coupons";
    }
}
