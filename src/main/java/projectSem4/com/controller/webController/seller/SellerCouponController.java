package projectSem4.com.controller.webController.seller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import projectSem4.com.dto.CouponForm;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.utils.JwtUtil;
import projectSem4.com.service.CouponService;
import projectSem4.com.service.admin.ShopService;

@Controller
@RequestMapping("/seller/coupons")
public class SellerCouponController {

    private final CouponService service;
    private final ShopService shopService;

    public SellerCouponController(CouponService service, ShopService shopService) {
        this.service = service;
        this.shopService = shopService;
    }

    /** Lấy shopId hiện tại của seller theo thứ tự:
     *  1) request attribute (được JwtAuthInterceptor set)
     *  2) session
     *  3) decode ACCESS_TOKEN từ header/cookie
     *  Sau khi có userId -> lấy shopId từ session, nếu chưa có thì hỏi DB và cache.
     */
    private int currentShopId(HttpServletRequest req) {
        // ---- Lấy userId
        Integer userId = (Integer) req.getAttribute("userId");              // 1) từ request
        if (userId == null) userId = (Integer) req.getSession().getAttribute("userId"); // 2) từ session

        if (userId == null) { // 3) decode token
            String token = null;

            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);

            if (token == null) {
                Cookie[] cookies = req.getCookies();
                if (cookies != null) {
                    for (Cookie c : cookies) {
                        if ("ACCESS_TOKEN".equals(c.getName())) {
                            token = c.getValue();
                            break;
                        }
                    }
                }
            }

            if (token != null && JwtUtil.validateToken(token)) {
                try {
                    userId = Integer.parseInt(JwtUtil.getClaims(token).getSubject());
                    req.getSession().setAttribute("userId", userId); // cache
                } catch (Exception ignored) {}
            }
        }

        if (userId == null) {
            throw new RuntimeException("Không lấy được userId. Vui lòng đăng nhập lại.");
        }

        // ---- Lấy shopId
        Integer shopId = (Integer) req.getSession().getAttribute("SHOP_ID");
        if (shopId == null || shopId <= 0) {
            shopId = shopService.getShopIdByUser(userId);
            if (shopId == null || shopId <= 0) {
                throw new RuntimeException("Tài khoản SELLER chưa có Shop. Vui lòng tạo Shop trước.");
            }
            req.getSession().setAttribute("SHOP_ID", shopId); // cache
        }
        return shopId;
    }

    // ===== LIST =====
    // vào được bằng: GET /seller/coupons
    @GetMapping({"", "/"})
    public String index(@RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size,
                        @RequestParam(name = "q", required = false) String q,
                        HttpServletRequest request,
                        Model model) {

        int shopId = currentShopId(request);
        model.addAttribute("coupons", service.shopList(shopId, page, size, q));
        model.addAttribute("total", service.shopCount(shopId, q));
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("q", q == null ? "" : q);
        return "seller/coupon/index";
    }

    // ===== CREATE =====
    @GetMapping("/create")
    public String create(Model model) {
        CouponForm f = new CouponForm();
        f.setCouponType("SHOP");  // seller luôn là SHOP
        f.setStatus(1);
        model.addAttribute("form", f);
        return "seller/coupon/create";
    }

    @PostMapping("/create")
    public String doCreate(@Valid @ModelAttribute("form") CouponForm f,
                           BindingResult br,
                           HttpServletRequest request,
                           Model model) {
        if (br.hasErrors()) {
            // Optional debug
            br.getFieldErrors().forEach(err ->
                System.out.println("[Seller/Coupon/Create] " + err.getField() + " -> " + err.getDefaultMessage())
            );
            return "seller/coupon/create";
        }
        int shopId = currentShopId(request);
        try {
            service.shopCreate(shopId, f);
            return "redirect:/seller/coupons";
        } catch (Exception ex) {
            br.rejectValue("code", "err", ex.getMessage());
            return "seller/coupon/create"; // giữ lại form để hiển thị lỗi
        }
    }

    // ===== EDIT =====
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") int id,
                       HttpServletRequest request,
                       Model model) {
        int shopId = currentShopId(request);
        Coupon c = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (c.getShopId() == null || !c.getShopId().equals(shopId)) {
            throw new RuntimeException("Coupon không thuộc shop của bạn");
        }

        CouponForm f = new CouponForm();
        f.setCouponID(c.getCouponId());
        f.setCode(c.getCode());
        f.setCouponType("SHOP");
        f.setDiscountType(c.getDiscountType());
        f.setDiscountValue(c.getDiscountValue());
        f.setMaxDiscount(c.getMaxDiscount());
        f.setMinOrderAmount(c.getMinOrderAmount());
        f.setStartDate(c.getStartDate());
        f.setEndDate(c.getEndDate());
        f.setUsageLimit(c.getUsageLimit());
        f.setStatus(c.getStatus());

        model.addAttribute("form", f);
        return "seller/coupon/edit";
    }

    @PostMapping("/{id}/edit")
    public String doEdit(@PathVariable("id") int id,
                         @Valid @ModelAttribute("form") CouponForm f,
                         BindingResult br,
                         HttpServletRequest request,
                         Model model) {
        if (br.hasErrors()) {
            return "seller/coupon/edit";
        }
        int shopId = currentShopId(request);
        try {
            service.shopUpdate(shopId, id, f);
            return "redirect:/seller/coupons";
        } catch (Exception ex) {
            br.rejectValue("code", "err", ex.getMessage());
            return "seller/coupon/edit";
        }
    }

    // ===== DELETE =====
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") int id,
                         HttpServletRequest request) {
        int shopId = currentShopId(request);
        service.shopDelete(shopId, id);
        return "redirect:/seller/coupons";
    }
}
