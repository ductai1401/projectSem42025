package projectSem4.com.controller.webController.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.dto.FlashSaleForProductDTO;
import projectSem4.com.model.entities.Category;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.BuyerNotification;
import projectSem4.com.service.admin.CategoryService;
import projectSem4.com.service.admin.FlashSaleItemService;
import projectSem4.com.service.client.HomeService;
import projectSem4.com.service.client.SearchKeyService;
import projectSem4.com.service.admin.BuyerNotificationService; // 👈 thêm

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("home")
public class HomeControler {

	private final SearchKeyService searchKeyService_1;

	@Autowired
	private HomeService homeService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private SearchKeyService searchKeyService;
	@Autowired
	private FlashSaleItemService flashSaleItemService;

	// 👇 Thêm service thông báo
	@Autowired
	private BuyerNotificationService buyerNotificationService;

	HomeControler(SearchKeyService searchKeyService_1) {
		this.searchKeyService_1 = searchKeyService_1;
	}

	// Helper: lấy userId từ session (bạn điều chỉnh key theo chỗ login của bạn)
	private Integer getSessionUserId(HttpServletRequest req) {
		return (Integer) (req.getSession(false) != null ? req.getSession(false).getAttribute("userId") : null);
	}

	@GetMapping("")
	public String index(Model model, HttpServletRequest req) {
		// ==== dữ liệu sản phẩm như hiện tại ====
		List<Product> arrivals = homeService.getLatest(12);
		List<Product> bestSellers = homeService.getBestSellers(12);
		List<Product> onSale = homeService.getOnSale(12);
		List<Product> suggestions = homeService.getLatest(24);

		Map<Integer, Double> priceMap = homeService.buildMinPriceMap(arrivals, bestSellers, onSale, suggestions);

		Set<Integer> allProductIds = new HashSet<>();
		addAllIds(allProductIds, arrivals);
		addAllIds(allProductIds, bestSellers);
		addAllIds(allProductIds, onSale);
		addAllIds(allProductIds, suggestions);

		Map<Integer, FlashSaleForProductDTO> flashSaleMap = new HashMap<>();
		for (Integer pid : allProductIds) {
			FlashSaleForProductDTO dto = flashSaleItemService.getActiveFlashSaleByProductId(pid);
			if (dto != null) {
				flashSaleMap.put(pid, dto);
				if (dto.getSalePrice() != null) {
					priceMap.put(pid, dto.getSalePrice());
				}
			}
		}

		List<Category> rootCategories = categoryService.getAll().stream().filter(c -> c.getParentCategory() == null)
				.collect(Collectors.toList());

		List<Object[]> topKeywordsRaw = searchKeyService.getTopKeywords(5);
		List<Map<String, Object>> topKeywords = new ArrayList<>();
		for (Object[] row : topKeywordsRaw) {
			Map<String, Object> map = new HashMap<>();
			map.put("keyword", row[0]);
			map.put("count", row[1]);
			topKeywords.add(map);
		}

		model.addAttribute("arrivals", arrivals);
		model.addAttribute("bestSellers", bestSellers);
		model.addAttribute("onSale", onSale);
		model.addAttribute("suggestions", suggestions);

		model.addAttribute("priceMap", priceMap);
		model.addAttribute("flashSaleMap", flashSaleMap);
		model.addAttribute("rootCategories", rootCategories);
		model.addAttribute("topKeywords", topKeywords);

		// ==== 👇 Phần THÔNG BÁO: đổ vào model ====
		Integer userId = getSessionUserId(req);
		if (userId != null) {
			// lấy 10 thông báo mới nhất của user
			List<BuyerNotification> allOfUser = buyerNotificationService.findByUser(userId);
			List<BuyerNotification> latest10 = (allOfUser == null) ? List.of() : allOfUser.stream().limit(10).toList();

			int unread = buyerNotificationService.countUnreadByUser(userId);

			model.addAttribute("myNotifications", latest10);
			model.addAttribute("myUnreadCount", unread);
		} else {
			model.addAttribute("myNotifications", List.of());
			model.addAttribute("myUnreadCount", 0);
		}

		return "client/index";
	}

	private void addAllIds(Set<Integer> set, List<Product> list) {
		if (list == null)
			return;
		for (Product p : list) {
			if (p != null && p.getProductId() != null)
				set.add(p.getProductId());
		}
	}
}
