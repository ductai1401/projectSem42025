package projectSem4.com.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projectSem4.com.model.entities.ReviewUrl;
import projectSem4.com.model.repositories.ReviewUrlRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ReviewUrlService {

	private static final String UPLOAD_DIR = "D:/uploads/reviews/";

	private static final long MAX_MEDIA_BYTES = 20L * 1024 * 1024; // 20MB

	@Autowired
	private ReviewUrlRepository mediaRepo;

	private String validateFile(MultipartFile file) {
		if (file == null || file.isEmpty())
			return "File rỗng.";
		if (file.getSize() > MAX_MEDIA_BYTES)
			return "File quá lớn (tối đa 20MB).";
		return null;
	}

	private String saveFile(MultipartFile file) throws IOException {
		File dir = new File(UPLOAD_DIR);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Không tạo được thư mục: " + dir.getAbsolutePath());
		}
		String original = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
		String fileName = UUID.randomUUID() + "_" + original.replaceAll("[\\s]+", "_");
		file.transferTo(new File(dir, fileName));
		return fileName; // chỉ trả về tên file
	}
	/* ========== CRUD ========== */

	/** Tạo media (dùng URL có sẵn, không upload file) */
	@Transactional
	public Map<String, Object> create(ReviewUrl m) {
		Map<String, Object> res = new HashMap<>();
		try {
			Integer id = mediaRepo.createReturningId(m);
			if (id != null) {
				res.put("success", true);
				res.put("mediaId", id);
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

	/** Upload file trực tiếp và lưu vào DB */
	@Transactional
	public Map<String, Object> uploadAndCreate(Integer reviewId, MultipartFile file, Integer displayOrder) {
		Map<String, Object> res = new HashMap<>();
		try {
			String err = validateFile(file);
			if (err != null) {
				res.put("success", false);
				res.put("message", err);
				return res;
			}

			String savedName = saveFile(file);
			ReviewUrl m = new ReviewUrl();
			m.setReviewId(reviewId);
			m.setMediaUrl(savedName);
			m.setType(file.getContentType() != null && file.getContentType().startsWith("video") ? 1 : 0);
			m.setDisplayOrder(displayOrder != null ? displayOrder : 1);

			Integer id = mediaRepo.createReturningId(m);
			if (id != null) {
				res.put("success", true);
				res.put("mediaId", id);
				res.put("fileName", savedName);
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

	@Transactional
	public Map<String, Object> update(ReviewUrl m) {
		Map<String, Object> res = new HashMap<>();
		String msg = mediaRepo.update(m);
		res.put("success", msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	@Transactional
	public Map<String, Object> delete(int mediaId) {
		Map<String, Object> res = new HashMap<>();
		String msg = mediaRepo.delete(mediaId);
		res.put("success", msg.contains("thành công"));
		res.put("message", msg);
		return res;
	}

	public ReviewUrl getById(int mediaId) {
		return mediaRepo.findById(mediaId);
	}

	public List<ReviewUrl> getByReviewId(int reviewId) {
		return mediaRepo.findByReviewId(reviewId);
	}

	public Map<String, Object> getByReviewPaged(int reviewId, Integer type, int page, int size) {
		Map<String, Object> res = new HashMap<>();
		List<ReviewUrl> data = mediaRepo.findByReviewPaged(reviewId, type, page, size);
		int total = mediaRepo.countByReview(reviewId, type);
		res.put("success", true);
		res.put("data", data);
		res.put("total", total);
		return res;
	}

	@Transactional
	public int reorder(int mediaId, int newOrder) {
		return mediaRepo.updateDisplayOrder(mediaId, newOrder);
	}
}
