package projectSem4.com.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.ProductImage;
import projectSem4.com.model.repositories.ProductImageRepository;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.utils.MyValidate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductIamgeService { // (giữ đúng tên bạn đã gõ, nếu muốn chuẩn hãy đổi thành ProductImageService)

	private static final String UPLOAD_DIR = "D:/uploads/";
	private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductImageRepository productImageRepository;

	/*
	 * ===================== Public APIs dùng cho Controller =====================
	 */

	/** Trả danh sách ảnh của 1 sản phẩm để hiển thị trong Thymeleaf. */
	public List<ProductImageDTO> findImagesForProduct(Integer productId) {
		try {
			if (productId == null)
				return List.of();
			List<ProductImage> list = productImageRepository.findByProductId(productId);
			if (list == null || list.isEmpty())
				return List.of();
			return list.stream().map(img -> new ProductImageDTO(img.getImageId(), img.getImageUrl(), // chỉ tên file; FE
																										// đang tự
																										// prepend
																										// /uploads/
					img.getAltText(), img.getDisplayOrder() == null ? 0 : img.getDisplayOrder()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			System.err.println("❌ Lỗi findImagesForProduct: " + e.getMessage());
			return List.of();
		}
	}

	/**
	 * Upload 1 ảnh gallery cho sản phẩm. Trả về: { ok:true, imageId, url } — url là
	 * tên file (JS tự thêm /uploads/).
	 */
	public Map<String, Object> uploadGalleryImage(Integer productId, MultipartFile file) {
		Map<String, Object> res = new HashMap<>();
		try {
			Product p = (productId == null) ? null : productRepository.findById(productId);
			if (p == null)
				return Map.of("ok", false, "message", "Không tìm thấy sản phẩm.");

			// ✅ Giới hạn tối đa 6 ảnh
			int currentCount = 0;
			try {
				currentCount = productImageRepository.countByProductId(productId);
			} catch (Exception ignored) {
			}
			if (currentCount >= 6) {
				return Map.of("ok", false, "message", "Thư viện đã đạt tối đa 6 ảnh.");
			}

			String err = MyValidate.validateImageFile(file, MAX_IMAGE_BYTES);
			if (err != null)
				return Map.of("ok", false, "message", err);

			String savedName = saveFile(file);

			int nextOrder = currentCount + 1; // sau khi đếm
			ProductImage img = new ProductImage();
			img.setProductId(productId);
			img.setImageUrl(savedName);
			img.setAltText(null);
			img.setDisplayOrder(nextOrder);

			Integer newId = productImageRepository.create(img);
			if (newId == null) {
				safeDeleteFile(savedName);
				return Map.of("ok", false, "message", "Không lưu được ảnh vào cơ sở dữ liệu.");
			}

			return Map.of("ok", true, "imageId", newId, "url", savedName);

		} catch (Exception e) {
			return Map.of("ok", false, "message", "Upload ảnh thất bại: " + e.getMessage());
		}
	}

	/**
	 * Xoá 1 ảnh gallery của sản phẩm. Trả về: { ok:true } hoặc { ok:false, message
	 * }
	 */
	public Map<String, Object> deleteGalleryImage(Integer productId, Integer imageId) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (productId == null || imageId == null) {
				res.put("ok", false);
				res.put("message", "Thiếu productId hoặc imageId.");
				return res;
			}

			// Lấy ảnh
			ProductImage img = productImageRepository.findById(imageId);
			if (img == null) {
				res.put("ok", false);
				res.put("message", "Không tìm thấy ảnh.");
				return res;
			}
			if (!Objects.equals(img.getProductId(), productId)) {
				res.put("ok", false);
				res.put("message", "Ảnh không thuộc sản phẩm này.");
				return res;
			}

			String fileName = img.getImageUrl();

			// Xoá DB
			int rows = productImageRepository.deleteById(imageId);
			if (rows <= 0) {
				res.put("ok", false);
				res.put("message", "Xoá ảnh thất bại.");
				return res;
			}

			// Tuỳ chọn: xoá file vật lý (không bắt buộc, nhưng nên)
			safeDeleteFile(fileName);

			res.put("ok", true);
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi xoá ảnh: " + e.getMessage());
			return res;
		}
	}

	/** Đổi thứ tự hiển thị (tuỳ chọn nếu cần). */
	public Map<String, Object> swapOrder(Integer productId, Integer imageIdA, Integer imageIdB) {
		Map<String, Object> res = new HashMap<>();
		try {
			if (productId == null || imageIdA == null || imageIdB == null) {
				res.put("ok", false);
				res.put("message", "Thiếu tham số.");
				return res;
			}
			int n = productImageRepository.swapDisplayOrder(productId, imageIdA, imageIdB);
			if (n <= 0) {
				res.put("ok", false);
				res.put("message", "Không đổi được thứ tự.");
				return res;
			}
			res.put("ok", true);
			return res;
		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi đổi thứ tự: " + e.getMessage());
			return res;
		}
	}

	/* ===================== Helpers ===================== */

	private String saveFile(MultipartFile file) throws IOException {
		File dir = new File(UPLOAD_DIR);
		if (!dir.exists())
			dir.mkdirs();
		String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
		String fileName = UUID.randomUUID() + "_" + original.replaceAll("[\\s]+", "_");
		file.transferTo(new File(dir, fileName));
		return fileName;
	}

	/** Xoá file vật lý an toàn, nuốt lỗi. */
	private void safeDeleteFile(String fileName) {
		if (fileName == null || fileName.isBlank())
			return;
		try {
			File f = new File(UPLOAD_DIR, fileName);
			if (f.exists())
				f.delete();
		} catch (Exception ignored) {
		}
	}

	/* ===================== DTO ===================== */

	public static class ProductImageDTO {
		public Integer id;
		public String url; // tên file (FE sẽ prepand /uploads/)
		public String altText;
		public Integer displayOrder;

		public ProductImageDTO() {
		}

		public ProductImageDTO(Integer id, String url, String altText, Integer displayOrder) {
			this.id = id;
			this.url = url;
			this.altText = altText;
			this.displayOrder = displayOrder;
		}
	}
}
