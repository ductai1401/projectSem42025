package projectSem4.com.controller.webController.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projectSem4.com.dto.ReviewCreateRequest;
import projectSem4.com.model.entities.Review;
import projectSem4.com.model.entities.ReviewUrl;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.service.client.ReviewService;
import projectSem4.com.service.client.ReviewUrlService;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("review")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;
	@Autowired
	private ReviewUrlService reviewUrlService;
	@Autowired
	private ProductVariantRepository productVariantRepo;

	// Thư mục lưu file thật trên server (nằm trong static -> có thể truy cập
	// public)
	@Value("${app.upload.review.dir:src/main/resources/static/uploads/reviews/}")
	private String uploadDir;

	// Prefix public để trả về cho FE
	@Value("${app.upload.review.public-prefix:/uploads/reviews/}")
	private String publicPrefix;

	/* ----------------------------- PAGE ----------------------------- */

	@GetMapping("/create")
	public String reviewPage(@RequestParam("orderId") String orderId, @RequestParam("variantId") Integer variantId,
			Model model, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null)
			return "redirect:/login";

		Integer productId = productVariantRepo.getProductIdByVariantId(variantId);

		model.addAttribute("orderId", orderId);
		model.addAttribute("variantId", variantId);
		model.addAttribute("productId", productId);
		return "client/order/Review :: riviewFragment";
	}

	/* ----------------------------- CREATE (JSON) ----------------------------- */
	/** Tạo review + media khi FE đã có sẵn URL (hoặc gọi /media/upload trước) */
	@PostMapping("/create")
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody ReviewCreateRequest payload, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		if (payload == null || payload.getReview() == null) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu dữ liệu review"));
		}

		Review rv = payload.getReview();
		rv.setUserId(userId);

		Map<String, Object> res = reviewService.createReview(rv);
		if (!Boolean.TRUE.equals(res.get("success"))) {
			return ResponseEntity.badRequest().body(res);
		}

		Integer reviewId = (Integer) res.get("reviewId");
		List<ReviewCreateRequest.MediaItem> mediaList = payload.getMedia();
		if (mediaList != null && !mediaList.isEmpty()) {
			Map<Integer, String> mediaErrors = new HashMap<>();
			int index = 0;
			for (ReviewCreateRequest.MediaItem mi : mediaList) {
				index++;
				if (mi == null || mi.getMediaUrl() == null || mi.getMediaUrl().isBlank())
					continue;

				ReviewUrl m = new ReviewUrl();
				m.setReviewId(reviewId);
				m.setMediaUrl(mi.getMediaUrl());
				m.setType(mi.getType() == null ? 0 : mi.getType()); // 0=image,1=video
				m.setDisplayOrder(mi.getDisplayOrder() == null ? index : mi.getDisplayOrder());

				var mediaRes = reviewUrlService.create(m);
				if (!Boolean.TRUE.equals(mediaRes.get("success"))) {
					mediaErrors.put(index, String.valueOf(mediaRes.get("message")));
				}
			}
			if (!mediaErrors.isEmpty())
				res.put("mediaErrors", mediaErrors);
		}
		return ResponseEntity.ok(res);
	}

	/*
	 * ----------------------------- CREATE (MULTIPART)
	 * -----------------------------
	 */
	/**
	 * Nhận review dạng multipart: part "review" là JSON
	 * (ReviewCreateRequest.review), part "files" là danh sách ảnh/video. Server sẽ
	 * upload file → tạo record ReviewUrl.
	 */
	// ... giữ nguyên import & @Controller/@RequestMapping

	@PostMapping(value = "/create-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public ResponseEntity<?> createMultipart(@RequestPart("review") String reviewJson,
			@RequestPart(value = "files", required = false) MultipartFile[] files, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			ReviewCreateRequest req = mapper.readValue(reviewJson, ReviewCreateRequest.class);
			if (req == null || req.getReview() == null) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu dữ liệu review"));
			}

			Review rv = req.getReview();
			rv.setUserId(userId);

			// 1) Tạo review
			Map<String, Object> res = reviewService.createReview(rv);
			if (!Boolean.TRUE.equals(res.get("success"))) {
				return ResponseEntity.badRequest().body(res);
			}
			Integer reviewId = (Integer) res.get("reviewId");

			// 2) Upload & insert media qua service (lưu vào D:/uploads/)
			int display = 0;
			if (files != null && files.length > 0) {
				for (MultipartFile f : files) {
					if (f == null || f.isEmpty())
						continue;
					display++;
					reviewUrlService.uploadAndCreate(reviewId, f, display);
					// uploadAndCreate sẽ set mediaUrl = fileName (VD: 123abc.jpg)
					// FE sẽ hiển thị bằng /uploads/{fileName}
				}
			}

			return ResponseEntity.ok(res);
		} catch (IOException e) {
			return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi parse JSON / upload file"));
		} catch (Exception ex) {
			return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống khi tạo review"));
		}
	}

	/*
	 * ----------------------------- MEDIA UPLOAD (độc lập)
	 * -----------------------------
	 */
	/**
	 * Dùng khi bạn muốn upload từng file trước, FE nhận về URL rồi đưa vào
	 * /review/create (JSON).
	 */
	@PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File trống"));
		}
		try {
			Files.createDirectories(Path.of(uploadDir));
			String url = saveFileAndReturnPublicUrl(file);
			return ResponseEntity
					.ok(Map.of("success", true, "url", url, "type", isVideoContentType(file.getContentType()) ? 1 : 0));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("success", false, "message", "Upload thất bại"));
		}
	}

	/*
	 * ----------------------------- UPDATE / DELETE REVIEW
	 * -----------------------------
	 */

	@PostMapping("/update")
	@ResponseBody
	public ResponseEntity<?> update(@RequestBody Review payload, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		if (payload == null || payload.getReviewId() == null) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ReviewID"));
		}
		payload.setUserId(userId);
		Map<String, Object> res = reviewService.updateReview(payload);
		return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
	}

	@PostMapping("/delete")
	@ResponseBody
	public ResponseEntity<?> delete(@RequestBody Map<String, Integer> body, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		Integer reviewId = body.get("reviewId");
		if (reviewId == null) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "reviewId is required"));
		}
		Map<String, Object> res = reviewService.deleteReview(reviewId);
		return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
	}

	/* ----------------------------- QUERY ----------------------------- */

	@GetMapping("/detail")
	@ResponseBody
	public ResponseEntity<?> detail(@RequestParam("reviewId") int reviewId) {
		Review r = reviewService.getById(reviewId);
		return (r != null) ? ResponseEntity.ok(Map.of("success", true, "data", r))
				: ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("success", false, "message", "Không tìm thấy review"));
	}

	@GetMapping("/all")
	@ResponseBody
	public ResponseEntity<?> all(@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(Map.of("success", true, "data", reviewService.getAll(page, size)));
	}

	@GetMapping("/by-product")
	@ResponseBody
	public ResponseEntity<?> getByProduct(@RequestParam("productId") int productId,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(reviewService.getByProduct(productId, page, size));
	}

	@GetMapping("/search")
	@ResponseBody
	public ResponseEntity<?> search(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "productId", required = false) Integer productId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "rating", required = false) Integer rating,
			@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(reviewService.search(keyword, productId, userId, rating, status, page, size));
	}

//	@GetMapping("/recent")
//	@ResponseBody
//	public ResponseEntity<?> recent(@RequestParam(value = "top", defaultValue = "10") int top, HttpSession session) {
//		Integer userId = (Integer) session.getAttribute("userId");
//		if (userId == null) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
//		}
//		return ResponseEntity.ok(Map.of("success", true, "data", reviewService.getRecentByUser(userId, top)));
//	}

	@GetMapping("/exists")
	@ResponseBody
	public ResponseEntity<?> exists(@RequestParam("orderId") String orderId, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		boolean ok = reviewService.existsUserReviewForOrder(userId, orderId);
		return ResponseEntity.ok(Map.of("success", true, "exists", ok));
	}

	/* ----------------------------- MEDIA CRUD ----------------------------- */

	@GetMapping("/media/by-review")
	@ResponseBody
	public ResponseEntity<?> mediaByReview(@RequestParam("reviewId") int reviewId,
			@RequestParam(value = "type", required = false) Integer type,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		return ResponseEntity.ok(reviewUrlService.getByReviewPaged(reviewId, type, page, size));
	}

	@PostMapping("/media/create")
	@ResponseBody
	public ResponseEntity<?> mediaCreate(@RequestBody ReviewUrl payload, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		if (payload == null || payload.getReviewId() == null || payload.getMediaUrl() == null
				|| payload.getMediaUrl().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu dữ liệu media"));
		}
		var res = reviewUrlService.create(payload);
		return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
	}

	@PostMapping("/media/update")
	@ResponseBody
	public ResponseEntity<?> mediaUpdate(@RequestBody ReviewUrl payload, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		if (payload == null || payload.getMediaId() == null) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu MediaID"));
		}
		var res = reviewUrlService.update(payload);
		return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
	}

	@PostMapping("/media/delete")
	@ResponseBody
	public ResponseEntity<?> mediaDelete(@RequestBody Map<String, Integer> body, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		Integer mediaId = body.get("mediaId");
		if (mediaId == null) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "mediaId is required"));
		}
		var res = reviewUrlService.delete(mediaId);
		return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
	}

	@PostMapping("/media/reorder")
	@ResponseBody
	public ResponseEntity<?> mediaReorder(@RequestBody Map<String, Integer> body, HttpSession session) {
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
		}
		Integer mediaId = body.get("mediaId");
		Integer newOrder = body.get("displayOrder");
		if (mediaId == null || newOrder == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("success", false, "message", "mediaId & displayOrder are required"));
		}
		int rows = reviewUrlService.reorder(mediaId, newOrder);
		return ResponseEntity.ok(Map.of("success", rows > 0, "rows", rows));
	}

	/* ----------------------------- UTIL ----------------------------- */

	private boolean isVideoContentType(String contentType) {
		return contentType != null && contentType.toLowerCase().startsWith("video");
	}

	private String saveFileAndReturnPublicUrl(MultipartFile file) throws IOException {
		String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		String ext = "";
		int dot = original.lastIndexOf('.');
		if (dot >= 0)
			ext = original.substring(dot);
		String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

		Path abs = Path.of(uploadDir, fileName);
		Files.copy(file.getInputStream(), abs, StandardCopyOption.REPLACE_EXISTING);

		return publicPrefix + fileName;
	}
}
