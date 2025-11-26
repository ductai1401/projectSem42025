package projectSem4.com.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpSession session) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                Object roleObj = session.getAttribute("roles");
                if (roleObj != null) {
                    @SuppressWarnings("unchecked")
                    var roles = (java.util.List<String>) roleObj;
                    if (roles.contains("ADMIN")) return "redirect:/admin/product/";
                    if (roles.contains("SELLER")) return "redirect:/seller/profile";
                    if (roles.contains("BUYER")) return "redirect:/home";
                }
                return "redirect:/home";
            }
        }
        return "error"; // trang error mặc định
    }
}
