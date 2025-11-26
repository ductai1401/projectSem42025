package projectSem4.com.controller.webController.seller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller/chat")
public class ChatController {
	@GetMapping("")
	public String Index() {
		return "seller/chat";
	}
}
