package projectSem4.com.controller.webController.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import projectSem4.com.model.utils.DeviceType;
import projectSem4.com.service.admin.FlashSaleService;
import projectSem4.com.service.admin.FlashSaleItemService;
import projectSem4.com.service.admin.FlashSaleService.FlashSaleCreateRequest;
import projectSem4.com.service.admin.FlashSaleService.FlashSaleUpdateRequest;
import projectSem4.com.service.admin.FlashSaleService.FlashSaleDetailResponse;
import projectSem4.com.service.admin.FlashSaleService.FlashSaleDTO;
import projectSem4.com.service.admin.FlashSaleService.PagedResult;
import projectSem4.com.service.admin.ProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seller/flashsale")
public class FlashSaleController {

	@Autowired
	private FlashSaleService flashSaleService;

	@Autowired
	private FlashSaleItemService flashSaleItemService;

	@Autowired
	private ProductService productService;

	// ---------------------------------------------------------
	// BƠM CỜ THIẾT BỊ TỪ INTERCEPTOR VÀO MODEL (CHO MỌI HANDLER)
	// ---------------------------------------------------------
	@ModelAttribute
	public void injectDeviceFlags(Model model, HttpServletRequest req) {
		Object dt = req.getAttribute("deviceType");
		Object m = req.getAttribute("isMobile");
		Object t = req.getAttribute("isTablet");
		Object d = req.getAttribute("isDesktop");

		DeviceType deviceType = (dt instanceof DeviceType) ? (DeviceType) dt : DeviceType.DESKTOP;
		boolean isMobile = (m instanceof Boolean) ? (Boolean) m : false;
		boolean isTablet = (t instanceof Boolean) ? (Boolean) t : false;
		boolean isDesktop = (d instanceof Boolean) ? (Boolean) d : !isMobile && !isTablet;

		model.addAttribute("deviceType", deviceType);
		model.addAttribute("isMobile", isMobile);
		model.addAttribute("isTablet", isTablet);
		model.addAttribute("isDesktop", isDesktop);
	}

	private boolean reqIsMobile(HttpServletRequest req) {
		Object m = req.getAttribute("isMobile");
		return (m instanceof Boolean) && ((Boolean) m);
	}

	private Integer getShopIdFromSession(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null)
			return null;
		Object v = session.getAttribute("shopId");
		if (v == null)
			v = session.getAttribute("SHOP_ID");
		if (v instanceof Integer)
			return (Integer) v;
		if (v instanceof Long)
			return ((Long) v).intValue();
		if (v instanceof String) {
			try {
				return Integer.parseInt((String) v);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	private LocalDateTime parseLdt(Object o) {
		if (o == null)
			return null;
		String s = String.valueOf(o).trim();
		if (s.isEmpty())
			return null;
		try {
			return LocalDateTime.parse(s);
		} catch (Exception e) {
			return null;
		}
	}

	// ============== LIST/INDEX ==============

	// JSON list toàn bộ (tuỳ chọn includeExpired)
	@GetMapping("/list")
	@ResponseBody
	public List<FlashSaleDTO> getAllFlashSales(
			@RequestParam(value = "includeExpired", defaultValue = "false") boolean includeExpired) {
		return flashSaleService.findAllWithShopName(includeExpired);
	}

	// Trang list (có includeExpired)
	@GetMapping("/")
	public String index(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "shopId", required = false) Integer shopId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "includeExpired", defaultValue = "false") boolean includeExpired, Model model,
			HttpServletRequest req) {

		flashSaleService.refreshExpiredStatusesNow();

		if (shopId == null) {
			Integer sid = getShopIdFromSession(req);
			if (sid != null && sid > 0)
				shopId = sid;
		}

		var result = flashSaleService.search(keyword, shopId, page, size, includeExpired);

		model.addAttribute("flashSales", result.data);
		model.addAttribute("currentPage", result.page);
		model.addAttribute("totalPages", (int) Math.ceil((double) result.total / result.size));
		model.addAttribute("keyword", keyword);
		model.addAttribute("shopId", shopId);
		model.addAttribute("sort", sort);
		model.addAttribute("includeExpired", includeExpired); // <- để bind ra UI (checkbox)
		model.addAttribute("shops", flashSaleService.getAllShops());
		return "admin/flashsale/listFlashSale";
	}

	// ============== SEARCH (JSON) ==============

	// API search (JSON) có includeExpired
	@GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public PagedResult<FlashSaleDTO> search(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "shopId", required = false) Integer shopId,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "20") int size,
			@RequestParam(value = "includeExpired", defaultValue = "false") boolean includeExpired,
			HttpServletRequest req) {

		flashSaleService.refreshExpiredStatusesNow();

		if (shopId == null) {
			Integer sid = getShopIdFromSession(req);
			if (sid != null && sid > 0)
				shopId = sid;
		}
		return flashSaleService.search(keyword, shopId, page, size, includeExpired);
	}

	// ============== CREATE (FORM) ==============

	@GetMapping("/create")
	public String showCreateForm(Model model, HttpServletRequest req,
			@RequestParam(value = "productId", required = false) Integer productId) {

		HttpSession session = req.getSession(false);
		Integer shopId = session != null ? (Integer) session.getAttribute("shopId") : null;
		String shopName = session != null ? (String) session.getAttribute("shopName") : null;

		model.addAttribute("sessionShopId", shopId);
		model.addAttribute("sessionShopName", shopName);
		model.addAttribute("readOnly", reqIsMobile(req));
		model.addAttribute("shops", flashSaleService.getAllShops());

		if (productId != null && productId > 0) {
			model.addAttribute("prefillProductId", productId);
			try {
				var product = productService.findById(productId);
				if (product != null) {
					if (shopId != null && !shopId.equals(product.getShopId())) {
						model.addAttribute("prefillWarning",
								"Sản phẩm không thuộc cửa hàng hiện tại, không thể thêm vào Flash Sale.");
					} else {
						model.addAttribute("prefillProductName", product.getProductName());
					}
				}
			} catch (Exception ignored) {
			}
		}

		return "admin/flashsale/createFlashSale";
	}

	// ============== CREATE (JSON) – shopId lấy từ session ==============

	@PostMapping(value = "/create-json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> createFlashSaleJson(@RequestBody Map<String, Object> body, HttpServletRequest httpReq) {
		if (reqIsMobile(httpReq)) {
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được thêm/sửa.");
		}

		Integer shopId = getShopIdFromSession(httpReq);
		if (shopId == null || shopId <= 0) {
			return Map.of("ok", false, "message", "Không tìm thấy shopId trong session.");
		}

		FlashSaleCreateRequest req = new FlashSaleCreateRequest();
		req.shopId = shopId;
		req.name = body.get("name") != null ? String.valueOf(body.get("name")) : null;
		req.startDate = parseLdt(body.get("startDate"));
		req.endDate = parseLdt(body.get("endDate"));

		Map<String, Object> fsResult = flashSaleService.createFlashSaleJson(req);
		if (!Boolean.TRUE.equals(fsResult.get("ok"))) {
			return fsResult;
		}

		Integer flashSaleId = null;
		Object fsIdObj = fsResult.get("flashSaleId");
		if (fsIdObj instanceof Integer)
			flashSaleId = (Integer) fsIdObj;
		else if (fsIdObj instanceof Long)
			flashSaleId = ((Long) fsIdObj).intValue();
		else if (fsIdObj != null) {
			try {
				flashSaleId = Integer.parseInt(String.valueOf(fsIdObj));
			} catch (Exception ignored) {
			}
		}

		Map<String, Object> itemResult = null;
		Object firstItem = body.get("firstItem");
		if (flashSaleId != null && firstItem instanceof Map) {
			Map<?, ?> item = (Map<?, ?>) firstItem;

			Integer productId = tryParseInt(item.get("productId"));
			Integer quantity = tryParseInt(item.get("quantity"));
			Integer percern = tryParseInt(item.get("percern"));
			Double totalAmount = tryParseDouble(item.get("totalAmount"));

			if (quantity == null || quantity < 0)
				quantity = 0;
			if (percern == null || percern < 0)
				percern = 0;

			if (productId != null && productId > 0) {
				try {
					var p = productService.findById(productId);
					if (p == null) {
						itemResult = Map.of("ok", false, "message", "Sản phẩm không tồn tại.");
					} else if (p.getShopId() != null && !p.getShopId().equals(shopId)) {
						itemResult = Map.of("ok", false, "message", "Sản phẩm không thuộc cửa hàng hiện tại.");
					} else {
						itemResult = flashSaleItemService.createItem(flashSaleId, productId, quantity, percern);

						if (Boolean.TRUE.equals(itemResult.get("ok")) && totalAmount != null && totalAmount > 0) {
							try {
								Object itemIdObj = itemResult.get("flashSaleItemId");
								Integer itemId = itemIdObj instanceof Integer ? (Integer) itemIdObj
										: (itemIdObj instanceof Long ? ((Long) itemIdObj).intValue()
												: (itemIdObj != null ? Integer.parseInt(String.valueOf(itemIdObj))
														: null));
								if (itemId != null) {
									flashSaleItemService.addRevenue(itemId, totalAmount);
								}
							} catch (Exception ignored) {
							}
						}
					}
				} catch (Exception ex) {
					itemResult = Map.of("ok", false, "message", "Không thể thêm item đầu tiên: " + ex.getMessage());
				}
			} else {
				itemResult = Map.of("ok", false, "message", "firstItem.productId không hợp lệ.");
			}
		}

		if (itemResult != null) {
			return Map.of("ok", true, "flashSaleId", flashSaleId, "firstItemResult", itemResult);
		}
		return Map.of("ok", true, "flashSaleId", flashSaleId);
	}

	// ============== DETAIL ==============

	@GetMapping("/{flashSaleId}")
	@ResponseBody
	public ResponseEntity<?> getFlashSaleDetail(@PathVariable("flashSaleId") Integer flashSaleId) {
		FlashSaleDetailResponse payload = flashSaleService.getFlashSaleDetail(flashSaleId);
		if (payload == null || !payload.ok) {
			return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Không tìm thấy Flash Sale"));
		}
		return ResponseEntity.ok(payload);
	}

	@GetMapping("/detail/{flashSaleId}")
	public String viewDetail(@PathVariable("flashSaleId") Integer flashSaleId, Model model) {
		FlashSaleDetailResponse payload = flashSaleService.getFlashSaleDetail(flashSaleId);
		if (payload == null || !payload.ok) {
			model.addAttribute("errorMessage", "Không tìm thấy Flash Sale");
			return "admin/flashsale/detailFlashSale";
		}
		model.addAttribute("flashSale", payload.flashSale);
		model.addAttribute("shopName", payload.shopName);
		return "admin/flashsale/detailFlashSale";
	}

	// ============== UPDATE (JSON) ==============

	@PostMapping(value = "/{flashSaleId}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> updateFlashSale(@PathVariable("flashSaleId") Integer flashSaleId,
			@RequestBody Map<String, Object> body, HttpServletRequest httpReq) {
		if (reqIsMobile(httpReq)) {
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được chỉnh sửa.");
		}

		FlashSaleUpdateRequest req = new FlashSaleUpdateRequest();
		req.flashSaleId = flashSaleId;

		if (body.get("name") != null)
			req.name = String.valueOf(body.get("name"));
		if (body.containsKey("startDate"))
			req.startDate = parseLdt(body.get("startDate"));
		if (body.containsKey("endDate"))
			req.endDate = parseLdt(body.get("endDate"));

		Integer sessionShopId = getShopIdFromSession(httpReq);
		if (sessionShopId != null && sessionShopId > 0)
			req.shopId = sessionShopId;

		return flashSaleService.updateFlashSaleJson(req);
	}

	// ============== DELETE (JSON) ==============

	@DeleteMapping(value = "/{flashSaleId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> deleteFlashSale(@PathVariable("flashSaleId") Integer flashSaleId,
			HttpServletRequest httpReq) {
		if (reqIsMobile(httpReq)) {
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được xoá.");
		}
		return flashSaleService.deleteFlashSale(flashSaleId);
	}

	// =====================================================================
	// ======================== FLASH SALE ITEMS ============================
	// =====================================================================

	/**
	 * Hiển thị form thêm 1 sản phẩm (productId) vào Flash Sale (flashSaleId). GET
	 * /seller/flashsale/{flashSaleId}/items/add?productId=123
	 */
	@GetMapping("/{flashSaleId}/items/add")
	public String showAddItemForm(@PathVariable("flashSaleId") Integer flashSaleId,
			@RequestParam("productId") Integer productId, HttpServletRequest req, Model model) {

		var fsDetail = flashSaleService.getFlashSaleDetail(flashSaleId);
		Integer sessionShopId = getShopIdFromSession(req);
		if (fsDetail == null || !fsDetail.ok || sessionShopId == null
				|| !sessionShopId.equals(fsDetail.flashSale.getShopId())) {
			model.addAttribute("errorMessage", "Flash Sale không hợp lệ hoặc không thuộc cửa hàng của bạn.");
			return "admin/flashsale/addFlashSaleItem";
		}

		model.addAttribute("flashSaleId", flashSaleId);
		model.addAttribute("productId", productId);
		return "admin/flashsale/addFlashSaleItem";
	}

	/**
	 * Tạo FlashSaleItem (JSON). Body ví dụ: { "productId": 123, "quantity": 10,
	 * "percern": 15, "totalAmount": 0 }
	 */
	@PostMapping(value = "/{flashSaleId}/items", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> createFlashSaleItem(@PathVariable("flashSaleId") Integer flashSaleId,
			@RequestBody Map<String, Object> body, HttpServletRequest req) {
		if (reqIsMobile(req)) {
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được thao tác.");
		}

		var fsDetail = flashSaleService.getFlashSaleDetail(flashSaleId);
		Integer sessionShopId = getShopIdFromSession(req);
		if (fsDetail == null || !fsDetail.ok || sessionShopId == null
				|| !sessionShopId.equals(fsDetail.flashSale.getShopId())) {
			return Map.of("ok", false, "message", "Flash Sale không hợp lệ hoặc không thuộc cửa hàng của bạn.");
		}

		Integer productId = tryParseInt(body.get("productId"));
		Integer quantity = tryParseInt(body.get("quantity"));
		Integer percern = tryParseInt(body.get("percern"));
		Double totalAmount = tryParseDouble(body.get("totalAmount"));

		if (productId == null || productId <= 0) {
			return Map.of("ok", false, "message", "Thiếu hoặc sai productId.");
		}
		if (quantity == null || quantity < 0) {
			return Map.of("ok", false, "message", "Quantity phải >= 0.");
		}
		if (percern != null && percern < 0) {
			return Map.of("ok", false, "message", "Percern phải >= 0.");
		}
		if (totalAmount != null && totalAmount < 0) {
			return Map.of("ok", false, "message", "TotalAmount phải >= 0.");
		}

		var result = flashSaleItemService.createItem(flashSaleId, productId, quantity, percern);

		if (Boolean.TRUE.equals(result.get("ok")) && totalAmount != null && totalAmount > 0) {
			try {
				Integer newId = (Integer) result.get("flashSaleItemId");
				if (newId != null) {
					flashSaleItemService.addRevenue(newId, totalAmount);
				}
			} catch (Exception ignored) {
			}
		}
		return result;
	}

	// ========= Helpers =========
	private Integer tryParseInt(Object v) {
		if (v == null)
			return null;
		try {
			return Integer.valueOf(String.valueOf(v));
		} catch (Exception e) {
			return null;
		}
	}

	private Double tryParseDouble(Object v) {
		if (v == null)
			return null;
		try {
			return Double.valueOf(String.valueOf(v));
		} catch (Exception e) {
			return null;
		}
	}

	@GetMapping("/{flashSaleId}/edit")
	public String showEditForm(@PathVariable("flashSaleId") Integer flashSaleId, Model model, HttpServletRequest req) {
		FlashSaleDetailResponse payload = flashSaleService.getFlashSaleDetail(flashSaleId);
		if (payload == null || !payload.ok) {
			model.addAttribute("errorMessage", "Không tìm thấy Flash Sale");
			return "admin/flashsale/editFlashSale";
		}

		// Shop info để hiển thị
		HttpSession session = req.getSession(false);
		Integer shopId = session != null ? (Integer) session.getAttribute("shopId") : null;
		String shopName = session != null ? (String) session.getAttribute("shopName") : null;

		model.addAttribute("sessionShopId", shopId);
		model.addAttribute("sessionShopName", shopName);
		model.addAttribute("flashSale", payload.flashSale);
		model.addAttribute("shopName", payload.shopName);

		// ✅ Nạp danh sách item của flash sale
		var items = flashSaleItemService.findByFlashSaleId(flashSaleId);
		model.addAttribute("items", items);

		// (tuỳ chọn) prefill 1 item lên form nếu bạn muốn:
		if (items != null && !items.isEmpty()) {
			model.addAttribute("firstItem", items.get(0));
		}
		return "admin/flashsale/editFlashSale";
	}

	@PostMapping("/refresh-status")
	@ResponseBody
	public Map<String, Object> refreshStatusNow() {
		return flashSaleService.refreshExpiredStatusesNow();
	}

}
