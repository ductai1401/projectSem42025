package projectSem4.com.controller.webController.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import projectSem4.com.service.AuthService;
import projectSem4.com.service.OtpService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
public class ForgotPasswordController {

    private final AuthService authService;
    private final OtpService otpService;

    public ForgotPasswordController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    // B1: Trang nhập email
    @GetMapping("/forgot-password")
    public String forgotPage() {
        return "client/common/forgot-password";
    }

    // B1: Gửi OTP -> PRG sang verify-otp
    @PostMapping("/forgot-password")
    public String handleForgot(@RequestParam("email") String email,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        String ua = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        authService.requestPasswordReset(email, ua, ip);

        ra.addFlashAttribute("message", "An OTP has been sent to the email.");
        ra.addFlashAttribute("email", email);
        return "redirect:/verify-otp?email=" + UriUtils.encode(email, StandardCharsets.UTF_8);
    }

    // B2: Trang nhập OTP
    @GetMapping("/verify-otp")
    public String verifyPage(@RequestParam(value = "email", required = false) String email,
                             Model model) {
        model.addAttribute("email", email);
        return "client/common/verify-otp";
    }

    // B2: Submit OTP -> nếu đúng, cấp ticket và chuyển sang reset-password
    @PostMapping("/verify-otp")
    public String handleVerify(@RequestParam("email") String email,
                               @RequestParam("otp") String otp,
                               HttpSession session,
                               RedirectAttributes ra,
                               Model model) {

        boolean ok = otpService.verify(email, otp); // hoặc dùng authService.verifyOtp(email, otp) nếu bạn đã có
        if (!ok) {
            model.addAttribute("error", "Invalid or expired OTP.");
            model.addAttribute("email", email);
            return "client/common/verify-otp";
        }

        String ticket = UUID.randomUUID().toString();
        session.setAttribute("OTP_TICKET_EMAIL", email.toLowerCase());
        session.setAttribute("OTP_TICKET", ticket);

        ra.addFlashAttribute("message", "OTP verified successfully. Please set your new password.");
        return "redirect:/reset-password?email=" + UriUtils.encode(email, StandardCharsets.UTF_8)
                + "&ticket=" + UriUtils.encode(ticket, StandardCharsets.UTF_8);
    }

    // B3: Trang đặt lại mật khẩu – chỉ hiển thị form nếu ticket hợp lệ
    @GetMapping("/reset-password")
    public String resetPage(@RequestParam(value = "email", required = false) String email,
                            @RequestParam(value = "ticket", required = false) String ticket,
                            HttpSession session,
                            Model model) {

        boolean otpVerified = false;
        if (email != null && ticket != null) {
            String se = (String) session.getAttribute("OTP_TICKET_EMAIL");
            String st = (String) session.getAttribute("OTP_TICKET");
            otpVerified = email.equalsIgnoreCase(String.valueOf(se)) && ticket.equals(String.valueOf(st));
            model.addAttribute("ticket", ticket);
        }
        model.addAttribute("email", email);
        model.addAttribute("otpVerified", otpVerified); // null-safe cho template
        return "client/common/reset-password";
    }

    // B3: Submit đặt lại mật khẩu – ticket hợp lệ mới cho đổi
    @PostMapping("/reset-password")
    public String handleReset(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam("ticket") String ticket,
                              HttpSession session,
                              RedirectAttributes ra,
                              Model model) {

        String se = (String) session.getAttribute("OTP_TICKET_EMAIL");
        String st = (String) session.getAttribute("OTP_TICKET");
        boolean otpVerified = email.equalsIgnoreCase(String.valueOf(se)) && ticket.equals(String.valueOf(st));

        if (!otpVerified) {
            ra.addFlashAttribute("error", "You need to verify OTP first.");
            return "redirect:/verify-otp?email=" + UriUtils.encode(email, StandardCharsets.UTF_8);
        }

        try {
            // Ở flow này, OTP đã verify ở bước trước, nên đổi mật khẩu trực tiếp:
            // Hãy tạo 1 hàm trong AuthService để cập nhật mật khẩu (hash) không cần OTP nữa.
            authService.updatePasswordAfterOtpVerified(email, password, confirmPassword);

            // dọn ticket và otp
            session.removeAttribute("OTP_TICKET_EMAIL");
            session.removeAttribute("OTP_TICKET");

            return "redirect:/auth?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("otpVerified", true);
            model.addAttribute("ticket", ticket);
            return "client/common/reset-password";
        }
    }
}
