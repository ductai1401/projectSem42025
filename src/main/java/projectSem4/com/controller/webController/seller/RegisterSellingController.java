package projectSem4.com.controller.webController.seller;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.service.admin.UserService;



@Controller
@RequestMapping("registerSeller")
public class RegisterSellingController {
	
	@Autowired
	private UserService userService;
	
	@GetMapping("")
	public String newShop(HttpServletRequest request, HttpServletResponse response, Model model) {
	    HttpSession session = request.getSession(false);
	    if (session == null || session.getAttribute("userId") == null) {
	        return "redirect:/login";
	    }

	    Object userIdObj = session.getAttribute("userId");
	    int userId;
	    if (userIdObj instanceof Number n) {
	        userId = n.intValue();
	    } else {
	        return "redirect:/login";
	    }

	    var user = userService.getUserById(userId);
	    if (user == null) {
	        return "redirect:/login";
	    }

	    // Chia roles
	    String rolesStr = user.getRoleId(); // "BUYER,SELLER"
	    List<String> roles = new ArrayList<>(Arrays.asList(rolesStr.split(",")));

	    // Nếu đã là SELLER → redirect thẳng
	    if (roles.contains("SELLER")) {
	        return "redirect:/seller/profile";
	    }

	    model.addAttribute("user", user);
	    return "seller/registerForSelling";
	}
}
