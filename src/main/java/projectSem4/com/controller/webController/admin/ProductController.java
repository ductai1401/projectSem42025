package projectSem4.com.controller.webController.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.FlashSale;
import projectSem4.com.model.entities.FlashSaleItem;
import projectSem4.com.model.utils.DeviceType;
import projectSem4.com.service.admin.*;
import projectSem4.com.service.client.WishlistItemService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

	@Autowired
	private ProductService productService;
	@Autowired
	private ProductIamgeService productIamgeService;
	@Autowired
	private ShopService shopService;
	@Autowired
	private FlashSaleService flashSaleService;
	@Autowired
	private FlashSaleItemService flashSaleItemService;
	@Autowired
	private WishlistItemService wishlistItemService;
	@Autowired
	private BuyerNotificationService buyerNotificationService;

	// ---------- helpers ----------
	private Integer getSessionShopId(HttpServletRequest req) {
		return (Integer) (req.getSession(false) != null ? req.getSession(false).getAttribute("shopId") : null);
	}

	private String getSessionShopName(HttpServletRequest req) {
		Object o = (req.getSession(false) != null ? req.getSession(false).getAttribute("shopName") : null);
		return (o != null) ? String.valueOf(o) : null;
	}

	// ---------- device flags ----------
	@ModelAttribute
	public void injectDeviceFlags(Model model, HttpServletRequest req) {
		Object dt = req.getAttribute("deviceType");
		Object m = req.getAttribute("isMobile");
		Object t = req.getAttribute("isTablet");
		Object d = req.getAttribute("isDesktop");

		DeviceType deviceType = (dt instanceof DeviceType) ? (DeviceType) dt : DeviceType.DESKTOP;
		boolean isMobile = (m instanceof Boolean) && ((Boolean) m);
		boolean isTablet = (t instanceof Boolean) && ((Boolean) t);
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

	// ============== LIST/INDEX ==============
	@GetMapping("/list")
	@ResponseBody
	public List<Product> getAllProducts(HttpServletRequest req) {
		Integer shopId = getSessionShopId(req);

		if (isSeller(req) && shopId != null && shopId > 0) {
			return productService.findProductsByShopId(shopId);
		} else if (isAdmin(req)) {
			return productService.findAllProductsWithNames().stream().filter(p -> p.getShopId() == null)
					.collect(Collectors.toList());
		} else {
			return productService.findAllProductsWithNames();
		}
	}

	@GetMapping("/")
	public String index(Model model, HttpServletRequest req) {
		Integer shopId = getSessionShopId(req);
		List<Product> products;

		if (isSeller(req) && shopId != null && shopId > 0) {
			products = productService.findProductsByShopId(shopId);
		} else if (isAdmin(req)) {
			products = productService.findAllProductsWithNames().stream().filter(p -> p.getShopId() == null)
					.collect(Collectors.toList());
		} else {
			products = productService.findAllProductsWithNames();
		}

		// ✅ Chỉ đánh dấu “Flash Sale” nếu có ít nhất 1 FlashSaleItem status = 1 (hoặc
		// null)
		Set<Integer> activeFs = new HashSet<>();
		for (Product p : products) {
			try {
				List<FlashSaleItem> items = flashSaleService.findItemsByProductId(p.getProductId());
				boolean hasActiveItem = items != null
						&& items.stream().anyMatch(it -> it.getStatus() == null || it.getStatus() == 1);
				if (hasActiveItem) {
					activeFs.add(p.getProductId());
				}
			} catch (Exception ignore) {
			}
		}

		model.addAttribute("products", products);
		model.addAttribute("categories", productService.getAllCategories());
		model.addAttribute("activeFs", activeFs);
		return "admin/product/listProduct";
	}

	// ============== CREATE (FORM) ==============
	@GetMapping("/create")
	public String showCreateForm(Model model, HttpServletRequest req) {
		Integer sid = getSessionShopId(req);
		Product p = model.containsAttribute("product") ? (Product) model.getAttribute("product") : new Product();
		p.setShopId(sid);
		model.addAttribute("product", p);

		String sname = (sid != null && sid > 0) ? shopService.getShopNameById(sid) : null;
		model.addAttribute("shopName", (sname != null && !sname.isBlank()) ? sname : "sàn");
		model.addAttribute("shopIdFromSession", sid);
		model.addAttribute("categories", productService.getAllCategories());
		model.addAttribute("readOnly", reqIsMobile(req));
		return "admin/product/createProduct";
	}

	// ============== CREATE JSON ==============
	@PostMapping(value = "/create-json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> createProductJson(@RequestBody ProductService.ProductCreateRequest reqBody,
			HttpServletRequest httpReq) {
		if (reqIsMobile(httpReq))
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được thêm/sửa.");

		if (reqBody.shopId == null) {
			Integer sid = getSessionShopId(httpReq);
			if (sid != null && sid > 0)
				reqBody.shopId = sid;
		}

		Map<String, Object> res = productService.createProductJson(reqBody);

		if (Boolean.TRUE.equals(res.get("ok"))) {
			try {
				Integer productId = (res.get("productId") instanceof Number)
						? ((Number) res.get("productId")).intValue()
						: null;

				Integer shopId2 = reqBody.shopId;
				String shopName = getSessionShopName(httpReq);
				if ((shopName == null || shopName.isBlank()) && shopId2 != null) {
					try {
						shopName = shopService.getShopNameById(shopId2);
					} catch (Exception ignored) {
					}
				}
				if (shopName == null || shopName.isBlank())
					shopName = "một cửa hàng yêu thích";

				String productName = reqBody.productName;
				List<Integer> followerIds = wishlistItemService.getFollowerUserIds(shopId2);
				if (!followerIds.isEmpty()) {
					buyerNotificationService.notifyNewProductFromShop(shopId2, shopName, productId, productName,
							followerIds);
				}
			} catch (Exception e) {
				System.err.println("Gửi thông báo tạo sản phẩm mới lỗi: " + e.getMessage());
			}
		}

		return res;
	}

	// ============== UPLOAD COVER ==============
	@PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> uploadProductImage(@PathVariable("productId") Integer productId,
			@RequestParam("file") MultipartFile file, HttpServletRequest httpReq) {
		if (reqIsMobile(httpReq))
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được thêm ảnh.");
		return productService.uploadProductImage(productId, file);
	}

	// ============== SINGLE VARIANT ==============
	@PostMapping(value = "/{productId}/variant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> createSingleVariantForProduct(@PathVariable("productId") Integer productId,
			@RequestParam(value = "variantName", required = false) String variantName,
			@RequestParam(value = "sku", required = false) String sku,
			@RequestParam(value = "price", required = false) String priceStr,
			@RequestParam(value = "quantity", required = false) String qtyStr,
			@RequestParam(value = "file", required = false) MultipartFile variantImage,
			@RequestParam(value = "logType", required = false, defaultValue = "1") Integer logType,
			@RequestParam(value = "unitCost", required = false) String unitCostStr,
			@RequestParam(value = "refOrderId", required = false) Integer ignoredRefOrderId,
			HttpServletRequest httpReq) {
		if (reqIsMobile(httpReq))
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem, không được thêm biến thể.");
		return productService.createSingleVariantForProduct(productId, variantName, sku, priceStr, qtyStr, variantImage,
				logType, unitCostStr, null);
	}

	// ============== DETAIL PAGE ==============
	@GetMapping("/{productId}")
	@ResponseBody
	public ResponseEntity<?> getProductDetail(@PathVariable("productId") Integer productId) {
		ProductService.ProductDetailResponse payload = productService.getProductDetail(productId);
		if (!payload.ok)
			return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Không tìm thấy sản phẩm"));
		return ResponseEntity.ok(payload);
	}

	@GetMapping("/detail/{productId}")
	public String viewDetail(@PathVariable("productId") Integer productId, Model model, HttpServletRequest req) {
		if (forbidIfNotBelong(req, productId)) {
			model.addAttribute("errorMessage", "Bạn không có quyền xem sản phẩm này.");
			return "admin/product/detailProduct";
		}
		ProductService.ProductDetailResponse payload = productService.getProductDetail(productId);
		if (!payload.ok) {
			model.addAttribute("errorMessage", "Không tìm thấy sản phẩm");
			return "admin/product/detailProduct";
		}

		FlashSale currentFs = null;
		List<FlashSaleItem> fsItems = List.of();
		try {
			currentFs = flashSaleService.findCurrentByProductId(productId);
		} catch (Exception ignored) {
		}
		try {
			List<FlashSaleItem> tmp = flashSaleService.findItemsByProductId(productId);
			if (tmp != null)
				fsItems = tmp;
		} catch (Exception ignored) {
		}

		model.addAttribute("currentFlashSale", currentFs);
		model.addAttribute("flashSaleItems", fsItems);
		model.addAttribute("currentFlashSaleId", currentFs != null ? currentFs.getFlashSaleId() : null);
		model.addAttribute("product", payload.product);
		model.addAttribute("variants", payload.variants);
		model.addAttribute("productOptionPairs", payload.productOptionPairs);
		model.addAttribute("productImages", productIamgeService.findImagesForProduct(productId));
		return "admin/product/ProductDetail";
	}

	// ============== FLASH SALE ==============
	@PostMapping(value = "/{productId}/flashsale/attach", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> attachToFlashSale(@PathVariable("productId") Integer productId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "start", required = false) String startStr,
			@RequestParam(value = "end", required = false) String endStr,
			@RequestParam(value = "quantity", required = false) Integer quantity,
			@RequestParam(value = "percent", required = false) Integer percent, HttpServletRequest req) {
		if (reqIsMobile(req))
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem.");
		if (forbidIfNotBelong(req, productId))
			return Map.of("ok", false, "message", "Bạn không có quyền với sản phẩm này.");

		if (quantity == null || quantity <= 0)
			return Map.of("ok", false, "message", "Số lượng khuyến mãi phải > 0.");
		if (percent == null || percent <= 0 || percent >= 100)
			return Map.of("ok", false, "message", "Phần trăm giảm giá phải trong (0, 100).");

		Integer shopId = getSessionShopId(req);
		String safeName = (name != null) ? name.trim() : null;
		return flashSaleService.attachProductToFlashSale(productId, shopId, safeName, startStr, endStr, quantity,
				percent);
	}

	// ✅ HỦY FLASH SALE
	@DeleteMapping(value = "/{productId}/flashsale", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> deleteFlashSaleByProduct(@PathVariable("productId") Integer productId,
			@RequestParam(value = "onlyActive", defaultValue = "false") boolean onlyActive, HttpServletRequest req) {
		if (reqIsMobile(req))
			return ResponseEntity.badRequest()
					.body(Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem."));
		if (forbidIfNotBelong(req, productId))
			return ResponseEntity.status(403)
					.body(Map.of("ok", false, "message", "Bạn không có quyền với sản phẩm này."));

		var res = flashSaleItemService.cancelAllByProductId(productId, onlyActive);
		if (Boolean.TRUE.equals(res.get("ok")))
			return ResponseEntity.ok(res);
		return ResponseEntity.badRequest().body(res);
	}

	@PostMapping(value = "/{productId}/flashsale", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> deleteFlashSaleByProductFallback(@PathVariable("productId") Integer productId,
			@RequestParam(value = "_method", required = false) String methodOverride,
			@RequestParam(value = "onlyActive", defaultValue = "false") boolean onlyActive, HttpServletRequest req) {
		if (!"delete".equalsIgnoreCase(methodOverride))
			return ResponseEntity.status(405).body(Map.of("ok", false, "message", "Method Not Allowed"));
		if (reqIsMobile(req))
			return ResponseEntity.badRequest()
					.body(Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem."));
		if (forbidIfNotBelong(req, productId))
			return ResponseEntity.status(403)
					.body(Map.of("ok", false, "message", "Bạn không có quyền với sản phẩm này."));

		var res = flashSaleItemService.cancelAllByProductId(productId, onlyActive);
		if (Boolean.TRUE.equals(res.get("ok")))
			return ResponseEntity.ok(res);
		return ResponseEntity.badRequest().body(res);
	}

	// Alias upload cho gallery (script gọi)
	// ProductController

	// Upload 1 ảnh gallery (multipart)
	@PostMapping(value = "/{productId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> uploadGalleryImage(@PathVariable("productId") Integer productId,
			@RequestParam("file") MultipartFile file, HttpServletRequest req) {
		if (reqIsMobile(req))
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem.");
		return productIamgeService.uploadGalleryImage(productId, file);
	}

	// Xoá 1 ảnh trong gallery
	@DeleteMapping(value = "/{productId}/gallery/{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> deleteGalleryImage(@PathVariable("productId") Integer productId, @PathVariable("imageId") Integer imageId,
			HttpServletRequest req) {
		if (reqIsMobile(req))
			return Map.of("ok", false, "message", "Bạn đang dùng giao diện mobile: chỉ xem.");
		return productIamgeService.deleteGalleryImage(productId, imageId);
	}

	// ============== GALLERY ==============
	@GetMapping("/{productId}/gallery")
	@ResponseBody
	public Map<String, Object> listGallery(@PathVariable("productId") Integer productId) {
		return Map.of("ok", true, "items", productIamgeService.findImagesForProduct(productId));
	}

	// ============== UTILS ==============
	private boolean forbidIfNotBelong(HttpServletRequest req, Integer productId) {
		Integer sid = getSessionShopId(req);
		return (sid != null && sid > 0) && !productService.belongsToShop(productId, sid);
	}

	@SuppressWarnings("unchecked")
	private boolean hasRole(HttpServletRequest req, String r) {
		Object o = (req.getSession(false) != null) ? req.getSession(false).getAttribute("roles") : null;
		if (o instanceof List<?>) {
			for (Object it : (List<?>) o) {
				if (it != null && String.valueOf(it).toUpperCase().contains(r))
					return true;
			}
		}
		Object one = (req.getSession(false) != null) ? req.getSession(false).getAttribute("role") : null;
		return (one != null && String.valueOf(one).toUpperCase().contains(r));
	}

	private boolean isAdmin(HttpServletRequest req) {
		return hasRole(req, "ADMIN");
	}

	private boolean isSeller(HttpServletRequest req) {
		return hasRole(req, "SELLER");
	}
}
