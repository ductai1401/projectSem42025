package projectSem4.com.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import projectSem4.com.dto.WishlistItemDTO;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.ProductVariant;
import projectSem4.com.model.entities.WishlistItem;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.model.repositories.WishlistItemRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WishlistItemService {

	@Autowired
	private WishlistItemRepository wishlistItemRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductVariantRepository productVariantRepository;
	/* ===================== CREATE ===================== */

	/**
	 * Thêm vào wishlist (trả về map giống style ProductService, có try–catch đầy
	 * đủ)
	 */
	public Map<String, Object> addItemJson(AddWishlistRequest req) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (req == null || req.userId == null || req.productId == null) {
				res.put("ok", false);
				res.put("message", "Thiếu userId hoặc productId.");
				return res;
			}

			// Không thêm trùng (UserID + ProductID)
			if (wishlistItemRepository.existsByUserAndProduct(req.userId, req.productId)) {
				res.put("ok", true);
				res.put("message", "Sản phẩm đã có trong wishlist.");
				// Lấy id hiện có cho tiện FE
				Integer id = wishlistItemRepository.addIfNotExists(req.userId, req.productId);
				res.put("wishlistId", id);
				return res;
			}

			WishlistItem w = new WishlistItem();
			w.setUserId(req.userId);
			w.setProductId(req.productId);
			w.setStatus(req.status == null ? 1 : req.status);
			w.setCreatedAt(LocalDateTime.now());

			Integer id = wishlistItemRepository.addItemReturningId(w);
			if (id == null) {
				res.put("ok", false);
				res.put("message", "Không tạo được wishlist item.");
				return res;
			}

			res.put("ok", true);
			res.put("wishlistId", id);
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi khi thêm wishlist: " + e.getMessage());
			return res;
		}
	}

	/** Thêm nếu chưa có (trả về id); tiện cho API nhanh */
	public Integer addIfNotExists(Integer userId, Integer productId) {
		try {
			if (userId == null || productId == null)
				return null;
			return wishlistItemRepository.addIfNotExists(userId, productId);
		} catch (Exception e) {
			System.err.println("Lỗi addIfNotExists: " + e.getMessage());
			return null;
		}
	}

	/* ===================== READ ===================== */

	public List<WishlistItem> getUserWishlist(Integer userId) {
		try {
			if (userId == null)
				return List.of();
			return wishlistItemRepository.findByUserId(userId);
		} catch (Exception e) {
			System.err.println("Lỗi getUserWishlist: " + e.getMessage());
			return List.of();
		}
	}

	public List<WishlistItem> getUserWishlistPaged(Integer userId, int page, int size) {
		try {
			if (userId == null)
				return List.of();
			// Lấy rồi lọc nhanh phía Java (nếu muốn paginate ở DB thì có thể viết query
			// riêng)
			List<WishlistItem> all = wishlistItemRepository.findByUserId(userId);
			if (all.isEmpty())
				return all;
			int from = Math.max(0, (page - 1) * size);
			int to = Math.min(all.size(), from + size);
			return from >= to ? List.of() : all.subList(from, to);
		} catch (Exception e) {
			System.err.println("Lỗi getUserWishlistPaged: " + e.getMessage());
			return List.of();
		}
	}

	public int countByUser(Integer userId) {
		try {
			if (userId == null)
				return 0;
			return wishlistItemRepository.countByUserId(userId);
		} catch (Exception e) {
			System.err.println("Lỗi countByUser: " + e.getMessage());
			return 0;
		}
	}

	public boolean exists(Integer userId, Integer productId) {
		try {
			if (userId == null || productId == null)
				return false;
			return wishlistItemRepository.existsByUserAndProduct(userId, productId);
		} catch (Exception e) {
			System.err.println("Lỗi exists: " + e.getMessage());
			return false;
		}
	}

	/* ===================== UPDATE ===================== */

	public String updateStatus(Integer wishlistId, Integer status) {
		try {
			if (wishlistId == null || status == null)
				return "Thiếu tham số.";
			return wishlistItemRepository.updateStatus(wishlistId, status);
		} catch (Exception e) {
			System.err.println("Lỗi updateStatus: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật trạng thái!";
		}
	}

	/* ===================== DELETE ===================== */

	public Map<String, Object> removeById(Integer wishlistId) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (wishlistId == null) {
				res.put("ok", false);
				res.put("message", "Thiếu wishlistId.");
				return res;
			}
			String msg = wishlistItemRepository.deleteById(wishlistId);
			res.put("ok", msg.contains("thành công"));
			res.put("message", msg);
			return res;
		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi khi xóa wishlist item: " + e.getMessage());
			return res;
		}
	}

	@Transactional
	public Map<String, Object> removeByUserAndProduct(Integer userId, Integer productId) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (userId == null || productId == null) {
				res.put("ok", false);
				res.put("message", "Thiếu userId hoặc productId.");
				return res;
			}
			int rows = wishlistItemRepository.deleteByUserAndProduct(userId, productId);
			res.put("ok", rows > 0);
			res.put("deleted", rows);
			res.put("message", rows > 0 ? "Đã xóa khỏi wishlist." : "Không tìm thấy bản ghi để xóa.");
			return res;
		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi khi xóa wishlist theo user+product: " + e.getMessage());
			return res;
		}
	}

	/* ===================== DTO ===================== */
	public static class AddWishlistRequest {
		public Integer userId;
		public Integer productId;
		public Integer status; // 0/1 (mặc định 1)

		public AddWishlistRequest() {
		}

		public AddWishlistRequest(Integer userId, Integer productId, Integer status) {
			this.userId = userId;
			this.productId = productId;
			this.status = status;
		}
	}

	public List<WishlistItemDTO> getUserWishlistWithProduct(Integer userId) {
		List<WishlistItem> items = wishlistItemRepository.findByUserId(userId);
		List<WishlistItemDTO> result = new ArrayList<>();

		for (WishlistItem w : items) {
			Product p = productRepository.findById(w.getProductId());
			if (p != null) {
				// Lấy variant (ví dụ: giá thấp nhất)
				ProductVariant variant = productVariantRepository.findFirstByProductIdOrderByPriceAsc(p.getProductId());
				Double price = (variant != null) ? variant.getPrice() : null;
				String variantImage = (variant != null && variant.getImage() != null) ? variant.getImage()
						: p.getImage();

				result.add(new WishlistItemDTO(w.getWishlistId(), p.getProductId(), p.getProductName(), variantImage,
						price));
			}
		}
		return result;
	}

	public List<WishlistItemDTO> getUserWishlistDTO(Integer userId) {
		if (userId == null)
			return List.of();

		List<WishlistItem> items = wishlistItemRepository.findByUserId(userId);
		List<WishlistItemDTO> result = new ArrayList<>();

		for (WishlistItem w : items) {
			Product p = productRepository.findById(w.getProductId());
			if (p != null) {
				// Lấy variant rẻ nhất (giá thấp nhất) để hiển thị
				List<ProductVariant> variants = productVariantRepository.findByProductId(p.getProductId());
				ProductVariant cheapest = variants.stream().filter(v -> v.getPrice() != null)
						.min((a, b) -> Double.compare(a.getPrice(), b.getPrice())).orElse(null);

				Double price = (cheapest != null) ? cheapest.getPrice() : null;
				String image = (cheapest != null && cheapest.getImage() != null && !cheapest.getImage().isEmpty())
						? cheapest.getImage()
						: p.getImage();

				result.add(new WishlistItemDTO(w.getWishlistId(), p.getProductId(), p.getProductName(), image, price));
			}
		}
		return result;
	}

	public List<Integer> getFollowerUserIds(Integer shopId) {
		return wishlistItemRepository.findUserIdsByShop(shopId);
	}
}
