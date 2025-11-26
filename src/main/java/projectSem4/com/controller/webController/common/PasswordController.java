package projectSem4.com.controller.webController.common;

//import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;


import projectSem4.com.service.client.PasswordResetService;

//import projectSem4.com.model.dto.ForgotPasswordRequest;
//import projectSem4.com.model.dto.ResetPasswordRequest;


@Controller
public class PasswordController {

    private final PasswordResetService resetService;

    @Autowired
    public PasswordController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

//    @GetMapping("/forgot-password")
//    public String forgotPage(Model model) {
//        if (!model.containsAttribute("forgotReq")) {
//            model.addAttribute("forgotReq", new ForgotPasswordRequest());
//        }
//        return "client/common/forgot-password";
//    }
//
//    @PostMapping("/forgot-password")
//    public String handleForgot(@Valid @ModelAttribute("forgotReq") ForgotPasswordRequest req,
//                               BindingResult binding,
//                               RedirectAttributes ra) {
//        if (binding.hasErrors()) {
//            ra.addFlashAttribute("org.springframework.validation.BindingResult.forgotReq", binding);
//            ra.addFlashAttribute("forgotReq", req);
//            ra.addFlashAttribute("forgotError", "Please check your email");
//            return "redirect:/forgot-password";
//        }
//
//        String link = resetService.requestReset(req);
//        ra.addFlashAttribute("forgotSuccess", "If the email exists, a reset link has been generated.");
//        if (link != null) ra.addFlashAttribute("devResetLink", link); // DEV tiện test
//        return "redirect:/forgot-password";
//    }
//
//    @GetMapping("/reset-password")
//    public String resetPage(@RequestParam("token") String token, Model model) {
//        ResetPasswordRequest req = new ResetPasswordRequest();
//        req.setToken(token);
//        model.addAttribute("resetReq", req);
//        return "client/common/reset-password";
//    }
//
//    @PostMapping("/reset-password")
//    public String handleReset(@ModelAttribute("resetReq") ResetPasswordRequest req,
//                              RedirectAttributes ra) {
//        String result = resetService.resetPassword(req);
//        if ("OK".equals(result)) {
//            ra.addFlashAttribute("registerSuccess", "Password changed. Please log in.");
//            return "redirect:/login";
//        } else {
//            ra.addFlashAttribute("resetError", result);
//            ra.addFlashAttribute("resetReq", req);
//            return "redirect:/reset-password?token=" + req.getToken();
//        }
//    }
}
