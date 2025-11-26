package projectSem4.com.controller.webController.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("report")
public class ReportController {
	@GetMapping("/shop")
	public String showReportShop() {
		return "admin/report/shop_report";
	}
	@GetMapping("/buyer")
	public String showReportbuyer() {
		return "admin/report/buyer_report";
	}
	@GetMapping("/product")
	public String showReportProduct() {
		return "admin/report/product_report";
	}
}
