package projectSem4.com.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("Dashboard")
public class DashboardControler {
	@GetMapping("/")
	public String Index() {
		return "admin/index";
	}
	
}
