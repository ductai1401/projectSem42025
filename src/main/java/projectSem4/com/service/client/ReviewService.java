package projectSem4.com.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSem4.com.model.entities.Review;
import projectSem4.com.model.entities.ReviewUrl;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.repositories.ReviewRepository;
import projectSem4.com.service.admin.UserService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ReviewService: - CRUD review - Tìm kiếm / phân trang - Lấy theo productId
 * (kèm userName + medias) - (Tuỳ chọn) Lấy theo variantId (kèm userName +
 * medias) - Kiểm tra tồn tại review theo order
 */
@Service
public class ReviewService {

	@Autowired
	private ReviewRepository reviewRepo;

	@Autowired
	private UserService userService; // để lấy FullName theo userId

	@Autowired
	private ReviewUrlService reviewUrlService; // để lấy media theo reviewId

	/*
	 * ============================ CREATE / UPDATE / DELETE
	 * ============================
	 */

	/** Tạo review mới */
	@Transactional
	public Map<String, Object> createReview(Review r) {
		Map<String, Object> res = new HashMap<>();
		try {
			r.setCreatedAt(LocalDateTime.now());
			r.setStatus(r.getStatus() == null ? 1 : r.getStatus());
			Integer id = reviewRepo.createReviewReturningId(r);
			if (id != null) {
				res.put("success", true);
				res.put("reviewId", id);
			} else {
				res.put("success", false);
				res.put("message", "Insert failed");
			}
		} catch (Exception e) {
			res.put("success", false);
			res.put("message", e.getMessage());
		}
		return res;
	}

	/** Update review (nội dung, rating…) */
	@Transactional
	public Map<String, Object> updateReview(Review r) {
		Map<String, Object> res = new HashMap<>();
		String msg = reviewRepo.updateReview(r);
		res.put("success", msg != null && msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	/** Xoá review */
	@Transactional
	public Map<String, Object> deleteReview(int id) {
		Map<String, Object> res = new HashMap<>();
		String msg = reviewRepo.deleteReview(id);
		res.put("success", msg != null && msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	/*
	 * ===================================== READ
	 * =====================================
	 */

	/** Lấy review theo ID (không enrich) */
	public Review getById(int id) {
		return reviewRepo.findById(id);
	}

	/** Lấy tất cả review (phân trang) – không enrich để tránh cost lớn */
	public List<Review> getAll(int page, int size) {
		return reviewRepo.findAllPaged(page, size);
	}

	/*
	 * ================================== SEARCH/FILTER
	 * ==================================
	 */

	/** Tìm kiếm review theo từ khoá + filter (không enrich mặc định) */
	public Map<String, Object> search(String keyword, Integer productId, Integer userId, Integer rating, Integer status,
			int page, int size) {
		Map<String, Object> res = new HashMap<>();
		List<Review> list = reviewRepo.searchReviews(keyword, productId, userId, rating, status, page, size);
		int total = reviewRepo.countSearchReviews(keyword, productId, userId, rating, status);

		res.put("success", true);
		res.put("data", list);
		res.put("total", total);
		return res;
	}

	/*
	 * ============================ BY PRODUCT (WITH ENRICH)
	 * ============================
	 */

	/**
	 * Lấy review theo productId (phân trang) + tính average + ENRICH (userName,
	 * medias)
	 */
	public Map<String, Object> getByProduct(int productId, int page, int size) {
		Map<String, Object> res = new HashMap<>();

		List<Review> list = reviewRepo.findByProductIdPaged(productId, page, size);
		// enrich userName + medias
		enrichReviews(list);

		int total = reviewRepo.countByProductId(productId);
		Double avg = reviewRepo.getAverageRatingByProduct(productId);

		res.put("success", true);
		res.put("data", list);
		res.put("total", total);
		res.put("averageRating", avg);
		return res;
	}

	/*
	 * ============================= BY VARIANT (OPTIONAL)
	 * =============================
	 */

	/**
	 * Tuỳ chọn: Lấy review theo variantId (phân trang) + ENRICH (userName, medias).
	 * Cần các method tương ứng trong ReviewRepository: - findByVariantIdPaged(int
	 * variantId, int page, int size) - countByVariantId(int variantId) -
	 * getAverageRatingByVariant(int variantId)
	 */
	public Map<String, Object> getByVariant(int variantId, int page, int size) {
		Map<String, Object> res = new HashMap<>();

		// Nếu chưa có các query theo variant trong repo, hãy bổ sung hoặc tạm bỏ block
		// này.
		List<Review> list = reviewRepo.findByVariantIdPaged(variantId, page, size);
		enrichReviews(list);

		int total = reviewRepo.countByVariantId(variantId);
		Double avg = reviewRepo.getAverageRatingByVariant(variantId);

		res.put("success", true);
		res.put("data", list);
		res.put("total", total);
		res.put("averageRating", avg);
		return res;
	}

	/*
	 * ================================ RECENT BY USER
	 * =================================
	 */

	/** Lấy review gần đây theo user (không enrich mặc định để nhẹ) */
//	public List<Review> getRecentByUser(int userId, int topN) {
//		return reviewRepo.findRecentByUser(userId, topN);
//	}

	/*
	 * ================================ EXISTS CHECKS
	 * ==================================
	 */

	/** Kiểm tra user đã review cho 1 sản phẩm trong 1 đơn chưa */
	public boolean existsUserReviewForOrder(int userId, String orderId, int productId) {
		return reviewRepo.existsUserReviewForOrder(userId, orderId, productId);
	}

	/** Kiểm tra user đã review cho 1 đơn chưa (không phân biệt productId) */
	public boolean existsUserReviewForOrder(int userId, String orderId) {
		return reviewRepo.existsUserReviewForOrder(userId, orderId);
	}

	/*
	 * ================================== HELPERS ==================================
	 */

	/**
	 * Gắn userName + danh sách medias cho từng review trong list. - userName: lấy
	 * từ Users.FullName (fallback: "Người dùng") - medias: lấy từ ReviewUrl theo
	 * reviewId
	 */
	private void enrichReviews(List<Review> list) {
		if (list == null || list.isEmpty())
			return;

		for (Review r : list) {
			// userName: đã có sẵn từ JOIN trong ReviewRepository
			if (r.getUserName() == null || r.getUserName().isBlank()) {
				r.setUserName("Người dùng");
			}

			// medias: lấy từ ReviewUrlService
			try {
				List<ReviewUrl> medias = reviewUrlService.getByReviewId(r.getReviewId());
				r.setMedias(medias != null ? medias : Collections.emptyList());
			} catch (Exception ignore) {
				r.setMedias(Collections.emptyList());
			}
		}
	}

}
