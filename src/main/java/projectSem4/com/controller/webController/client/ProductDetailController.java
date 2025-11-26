package projectSem4.com.controller.webController.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import projectSem4.com.dto.FlashSaleForProductDTO;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.ProductImage;
import projectSem4.com.model.entities.ProductVariant;
import projectSem4.com.model.modelViews.ShopView;
import projectSem4.com.model.repositories.ProductImageRepository;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.service.admin.FlashSaleItemService;
import projectSem4.com.service.admin.ShopService;
import projectSem4.com.service.client.ReviewService;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/productdetails")
public class ProductDetailController {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductVariantRepository productVariantRepository;
	@Autowired
	private ProductImageRepository productImageRepository;
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private FlashSaleItemService flashSaleItemService;
	@Autowired
	private ShopService shopService;

	private static final Set<String> COLOR_KEYS_NORM = Set.of("color", "mau", "mausac");
	private static final Set<String> SIZE_KEYS_NORM = Set.of("size", "kichthuoc", "kichthuoc:");

	// =================== MAIN ===================

	@GetMapping("/{productId}")
	public String view(@PathVariable("productId") Integer productId, Model model) {

		// --- Lấy sản phẩm
		Product p = productRepository.findById(productId);
		if (p == null) return "redirect:/";

		// --- Biến thể & ảnh
		List<ProductVariant> variants = productVariantRepository.findByProductId(productId.longValue());
		List<ProductImage> images = productImageRepository.findByProductId(productId);

		// --- Flash sale (có thể null)
		FlashSaleForProductDTO flashSale = null;
		try {
			flashSale = flashSaleItemService.getActiveFlashSaleByProductId(productId);
		} catch (Exception ignored) {}

		// --- Rút Color/Size & bảng thuộc tính
		LinkedHashSet<String> colorValues = new LinkedHashSet<>();
		LinkedHashSet<String> sizeValues = new LinkedHashSet<>();
		Map<Long, List<Map.Entry<String, String>>> variantAttrs = new LinkedHashMap<>();

		for (ProductVariant v : variants) {
			String raw = v.getVarianName();
			if (raw == null || raw.isBlank()) continue;

			Map<String, String> attrs = safeParseVariantName(raw);
			if (attrs.isEmpty()) continue;

			List<Map.Entry<String, String>> pairs = attrs.entrySet().stream()
					.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()))
					.collect(Collectors.toList());
			variantAttrs.put(v.getVariantId(), pairs);

			for (Map.Entry<String, String> e : attrs.entrySet()) {
				String keyNorm = normalizeKey(e.getKey());
				String val = e.getValue() == null ? "" : e.getValue().trim();
				if (val.isEmpty()) continue;

				if (COLOR_KEYS_NORM.contains(keyNorm)) colorValues.add(val);
				else if (SIZE_KEYS_NORM.contains(keyNorm)) sizeValues.add(val);
			}
		}

		// --- Reviews
		Map<String, Object> rv = reviewService.getByProduct(productId, 1, 6);

		// --- Shop (không parse JSON địa chỉ)
		ShopView shop = null;
		Integer productCount = null;
		try {
			shop = shopService.getShopById(p.getShopId());
			if (shop != null) {
				productCount = productRepository.countByShopId(shop.getShopId());
			}
		} catch (Exception ignored) {}

		// --- Bind model
		model.addAttribute("product", p);
		model.addAttribute("variants", variants);
		model.addAttribute("productImages", images);
		model.addAttribute("colorValues", colorValues);
		model.addAttribute("sizeValues", sizeValues);
		model.addAttribute("variantAttrs", variantAttrs);
		model.addAttribute("flashSale", flashSale);
		model.addAttribute("rvData", rv);
		model.addAttribute("shop", shop);
		model.addAttribute("shopProductCount", productCount);

		return "client/product/details";
	}

	@GetMapping("")
	public String indexFallback() {
		return "redirect:/";
	}

	// =================== API: Reviews ===================
	@GetMapping("/{productId}/reviews")
	@ResponseBody
	public ResponseEntity<?> reviewsByProduct(
			@PathVariable("productId") Integer productId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "6") int size) {

		Map<String, Object> rv = reviewService.getByProduct(productId, page, size);
		return ResponseEntity.ok(rv);
	}

	// =================== Helpers ===================
	private static String normalizeKey(String s) {
		if (s == null) return "";
		String t = s.trim().toLowerCase(Locale.ROOT);
		t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		t = t.replaceAll("\\s+", "");
		return t;
	}

	/** Parse varianName: JSON hoặc "Color: Red | Size: M" */
	private Map<String, String> safeParseVariantName(String raw) {
		LinkedHashMap<String, String> manual = new LinkedHashMap<>();
		String[] parts = raw.split("\\s*[|;/,]\\s*");
		for (String part : parts) {
			if (part.isBlank()) continue;
			String[] kv = part.split("\\s*[:=]\\s*", 2);
			String k = kv[0].trim();
			String v = kv.length > 1 ? kv[1].trim() : "";
			if (!k.isEmpty()) manual.put(k, v);
		}
		return manual;
	}
}
