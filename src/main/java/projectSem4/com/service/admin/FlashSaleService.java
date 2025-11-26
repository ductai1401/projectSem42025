package projectSem4.com.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectSem4.com.model.entities.FlashSale;
import projectSem4.com.model.entities.FlashSaleItem;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.repositories.FlashSaleRepository;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.model.utils.MyValidate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service cho FlashSale – dùng StartDate/EndDate. - CRUD + search phân trang -
 * Lấy FlashSale/Items theo productId (gọi trực tiếp FlashSaleRepository) -
 * attach/detach product vào Flash Sale
 */
@Service
public class FlashSaleService {

	@Autowired
	private FlashSaleRepository flashSaleRepository;
	@Autowired
	private ShopRepository shopRepository;

	// =================== READ ===================

	/** Lấy danh sách FlashSale + kèm shopName trong DTO */
	public List<FlashSaleDTO> findAllWithShopName(boolean includeExpired) {
		List<FlashSale> list = includeExpired ? flashSaleRepository.findAllIncludingExpired()
				: flashSaleRepository.findAllVisible();

		Map<Integer, String> shopNameCache = buildShopNameCache();
		List<FlashSaleDTO> out = new ArrayList<>(list.size());
		for (FlashSale fs : list) {
			out.add(FlashSaleDTO.fromEntity(fs, shopNameCache.get(fs.getShopId())));
		}
		return out;
	}

	/** Giữ tương thích: nếu controller cũ gọi không tham số, mặc định ẩn expired */
	public List<FlashSaleDTO> findAllWithShopName() {
		return findAllWithShopName(false);
	}

	public List<Shop> getAllShops() {
		return shopRepository.findAllShops();
	}

	// =================== CREATE ===================

	public Map<String, Object> createFlashSaleJson(FlashSaleCreateRequest req) {
		Map<String, Object> res = new HashMap<>();

		MyValidate.SimpleErrors errs = new MyValidate.SimpleErrors();
		if (req.shopId == null || req.shopId <= 0)
			errs.add("shopId", "Shop không hợp lệ.");
		if (MyValidate.isBlank(req.name))
			errs.add("name", "Tên Flash Sale không được để trống.");
		else if (req.name.trim().length() > 255)
			errs.add("name", "Tên Flash Sale tối đa 255 ký tự.");
		if (req.startDate == null)
			errs.add("startDate", "Vui lòng chọn ngày bắt đầu.");
		if (req.endDate == null)
			errs.add("endDate", "Vui lòng chọn ngày kết thúc.");
		if (req.startDate != null && req.endDate != null && !req.endDate.isAfter(req.startDate)) {
			errs.add("dateRange", "EndDate phải sau StartDate.");
		}

		if (errs.hasErrors()) {
			res.put("ok", false);
			res.put("message", errs.firstMessage());
			res.put("errors", errs.all());
			return res;
		}

		try {
			FlashSale fs = new FlashSale();
			fs.setShopId(req.shopId);
			fs.setName(req.name != null ? req.name.trim() : null);
			fs.setStartDate(req.startDate);
			fs.setEndDate(req.endDate);

			Integer id = flashSaleRepository.createReturningId(fs);
			if (id == null) {
				res.put("ok", false);
				res.put("message", "Không lấy được FlashSaleID sau khi tạo.");
				return res;
			}

			res.put("ok", true);
			res.put("flashSaleId", id);
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi tạo Flash Sale: " + e.getMessage());
			return res;
		}
	}

	// =================== UPDATE ===================

	public Map<String, Object> updateFlashSaleJson(FlashSaleUpdateRequest req) {
		Map<String, Object> res = new HashMap<>();

		if (req.flashSaleId == null || req.flashSaleId <= 0) {
			res.put("ok", false);
			res.put("message", "Thiếu hoặc sai FlashSaleID.");
			return res;
		}

		FlashSale exist = flashSaleRepository.findById(req.flashSaleId);
		if (exist == null) {
			res.put("ok", false);
			res.put("message", "Không tìm thấy Flash Sale.");
			return res;
		}

		MyValidate.SimpleErrors errs = new MyValidate.SimpleErrors();
		if (req.shopId != null && req.shopId <= 0)
			errs.add("shopId", "Shop không hợp lệ.");
		if (req.name != null && req.name.trim().length() > 255)
			errs.add("name", "Tên Flash Sale tối đa 255 ký tự.");

		LocalDateTime newStart = (req.startDate != null) ? req.startDate : exist.getStartDate();
		LocalDateTime newEnd = (req.endDate != null) ? req.endDate : exist.getEndDate();
		if (newStart == null)
			errs.add("startDate", "Vui lòng chọn ngày bắt đầu.");
		if (newEnd == null)
			errs.add("endDate", "Vui lòng chọn ngày kết thúc.");
		if (newStart != null && newEnd != null && !newEnd.isAfter(newStart)) {
			errs.add("dateRange", "EndDate phải sau StartDate.");
		}

		if (errs.hasErrors()) {
			res.put("ok", false);
			res.put("message", errs.firstMessage());
			res.put("errors", errs.all());
			return res;
		}

		try {
			if (req.shopId != null)
				exist.setShopId(req.shopId);
			if (req.name != null)
				exist.setName(req.name.trim());
			if (req.startDate != null)
				exist.setStartDate(req.startDate);
			if (req.endDate != null)
				exist.setEndDate(req.endDate);

			String msg = flashSaleRepository.update(exist);
			boolean ok = msg != null && msg.toLowerCase(Locale.ROOT).contains("thành công");
			res.put("ok", ok);
			res.put("message", msg);
			res.put("flashSaleId", exist.getFlashSaleId());
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi cập nhật Flash Sale: " + e.getMessage());
			return res;
		}
	}

	// =================== DELETE ===================

	public Map<String, Object> deleteFlashSale(Integer flashSaleId) {
		Map<String, Object> res = new HashMap<>();
		if (flashSaleId == null || flashSaleId <= 0) {
			res.put("ok", false);
			res.put("message", "Thiếu hoặc sai FlashSaleID.");
			return res;
		}
		try {
			String msg = flashSaleRepository.delete(flashSaleId);
			boolean ok = msg != null && msg.toLowerCase(Locale.ROOT).contains("thành công");
			res.put("ok", ok);
			res.put("message", msg);
			res.put("flashSaleId", flashSaleId);
			return res;
		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi xóa Flash Sale: " + e.getMessage());
			return res;
		}
	}

	// =================== DETAIL ===================

	public FlashSaleDetailResponse getFlashSaleDetail(Integer flashSaleId) {
		if (flashSaleId == null || flashSaleId <= 0)
			return new FlashSaleDetailResponse(false, null, null);
		FlashSale fs = flashSaleRepository.findById(flashSaleId);
		if (fs == null)
			return new FlashSaleDetailResponse(false, null, null);
		String shopName = shopRepository.getShopNameById(fs.getShopId());
		return new FlashSaleDetailResponse(true, fs, shopName);
	}

	// =================== SEARCH / PAGINATION ===================

	// NEW: search có cờ includeExpired
	public PagedResult<FlashSaleDTO> search(String keyword, Integer shopId, int page, int size,
			boolean includeExpired) {
		int p = Math.max(1, page);
		int s = Math.max(1, size);

		List<FlashSale> rows = includeExpired ? flashSaleRepository.searchIncludingExpired(keyword, shopId, p, s)
				: flashSaleRepository.searchVisible(keyword, shopId, p, s);

		int total = includeExpired ? flashSaleRepository.countSearchIncludingExpired(keyword, shopId)
				: flashSaleRepository.countSearchVisible(keyword, shopId);

		Map<Integer, String> shopNameCache = buildShopNameCache();
		List<FlashSaleDTO> dtos = new ArrayList<>(rows.size());
		for (FlashSale fs : rows) {
			dtos.add(FlashSaleDTO.fromEntity(fs, shopNameCache.get(fs.getShopId())));
		}
		return new PagedResult<>(dtos, total, p, s);
	}

	/** Giữ tương thích: nếu code cũ gọi search không có cờ, mặc định ẩn expired */
	public PagedResult<FlashSaleDTO> search(String keyword, Integer shopId, int page, int size) {
		return search(keyword, shopId, page, size, false);
	}

	public PagedResult<FlashSaleDTO> findAllPaged(int page, int size) {
		int p = Math.max(1, page);
		int s = Math.max(1, size);

		List<FlashSale> rows = flashSaleRepository.findAllPaged(p, s);
		int total = flashSaleRepository.countAll();

		Map<Integer, String> shopNameCache = buildShopNameCache();
		List<FlashSaleDTO> dtos = new ArrayList<>(rows.size());
		for (FlashSale fs : rows) {
			dtos.add(FlashSaleDTO.fromEntity(fs, shopNameCache.get(fs.getShopId())));
		}

		return new PagedResult<>(dtos, total, p, s);
	}

	// =================== PRODUCT HELPERS (GỌI REPOSITORY) ===================

	public FlashSale findCurrentByProductId(Integer productId) {
		if (productId == null || productId <= 0)
			return null;
		return flashSaleRepository.findCurrentByProductId(productId);
	}

	public List<FlashSaleItem> findItemsByProductId(Integer productId) {
		if (productId == null || productId <= 0)
			return List.of();
		List<FlashSaleItem> items = flashSaleRepository.findItemsByProductId(productId);
		return (items != null) ? items : List.of();
	}

	// =================== ATTACH / DETACH ===================

	/**
	 * Gắn product vào Flash Sale: - Có đủ name/start/end -> tạo FlashSale mới (theo
	 * shopId) rồi upsert FlashSaleItem. - Không có name/start/end -> gắn vào
	 * FlashSale đang active của product (nếu có). Trả về: { ok, message,
	 * flashSaleId, flashSaleItemId? }
	 */
	public Map<String, Object> attachProductToFlashSale(Integer productId, Integer shopId, String name, String startStr,
			String endStr, Integer quantity, Integer percent) {
		Map<String, Object> res = new HashMap<>();

		// validate
		if (productId == null || productId <= 0) {
			res.put("ok", false);
			res.put("message", "Thiếu hoặc sai ProductID.");
			return res;
		}
		if (quantity == null || quantity <= 0) {
			res.put("ok", false);
			res.put("message", "Số lượng khuyến mãi phải > 0.");
			return res;
		}
		if (percent == null || percent <= 0 || percent >= 100) {
			res.put("ok", false);
			res.put("message", "Phần trăm giảm giá phải trong (0, 100).");
			return res;
		}

		boolean hasCreateInputs = (name != null && !name.trim().isEmpty()) && (startStr != null && !startStr.isBlank())
				&& (endStr != null && !endStr.isBlank());

		try {
			Integer flashSaleId;

			if (hasCreateInputs) {
				if (shopId == null || shopId <= 0) {
					res.put("ok", false);
					res.put("message", "ShopID không hợp lệ khi tạo Flash Sale mới.");
					return res;
				}
				LocalDateTime start = parseDateTimeFlexible(startStr);
				LocalDateTime end = parseDateTimeFlexible(endStr);
				if (!end.isAfter(start)) {
					res.put("ok", false);
					res.put("message", "EndDate phải sau StartDate.");
					return res;
				}

				FlashSale fs = new FlashSale();
				fs.setShopId(shopId);
				fs.setName(name.trim());
				fs.setStartDate(start);
				fs.setEndDate(end);

				flashSaleId = flashSaleRepository.createReturningId(fs);
				if (flashSaleId == null) {
					res.put("ok", false);
					res.put("message", "Không tạo được Flash Sale mới.");
					return res;
				}
			} else {
				FlashSale current = flashSaleRepository.findCurrentByProductId(productId);
				if (current == null) {
					res.put("ok", false);
					res.put("message", "Không có Flash Sale hiện tại. Hãy cung cấp name/start/end để tạo mới.");
					return res;
				}
				flashSaleId = current.getFlashSaleId();
			}

			Integer flashSaleItemId = flashSaleRepository.upsertFlashSaleItem(flashSaleId, productId, quantity,
					percent);

			boolean ok = (flashSaleItemId != null && flashSaleItemId > 0);
			res.put("ok", ok);
			res.put("flashSaleId", flashSaleId);
			if (ok) {
				res.put("flashSaleItemId", flashSaleItemId);
				res.put("message", "Đã gắn sản phẩm vào Flash Sale thành công.");
			} else {
				res.put("message", "Không thể tạo/cập nhật FlashSaleItem.");
			}
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi attach: " + e.getMessage());
			return res;
		}
	}

	/**
	 * Gỡ sản phẩm khỏi Flash Sale hiện tại (xóa các FlashSaleItem theo productId).
	 */
	public Map<String, Object> detachProductFromFlashSale(Integer productId) {
		Map<String, Object> res = new HashMap<>();

		if (productId == null || productId <= 0) {
			res.put("ok", false);
			res.put("message", "Thiếu hoặc sai ProductID.");
			return res;
		}

		try {
			List<FlashSaleItem> items = findItemsByProductId(productId);
			if (items.isEmpty()) {
				res.put("ok", false);
				res.put("message", "Sản phẩm này chưa được gắn vào Flash Sale nào.");
				return res;
			}

			int count = 0;
			for (FlashSaleItem item : items) {
				boolean deleted = flashSaleRepository.deleteFlashSaleItemById(item.getFlashSaleItemId());
				if (deleted)
					count++;
			}

			res.put("ok", true);
			res.put("deleted", count);
			res.put("message", "Đã gỡ sản phẩm khỏi Flash Sale thành công (" + count + " bản ghi).");
			return res;

		} catch (Exception e) {
			res.put("ok", false);
			res.put("message", "Lỗi khi gỡ sản phẩm khỏi Flash Sale: " + e.getMessage());
			return res;
		}
	}

	// =================== HELPERS ===================

	private Map<Integer, String> buildShopNameCache() {
		List<Shop> shops = shopRepository.findAllShops();
		Map<Integer, String> map = new HashMap<>();
		for (Shop s : shops) {
			map.put(s.getShopId(), s.getShopName());
		}
		return map;
	}

	/** Parse linh hoạt các format thường gặp và fallback ISO. */
	private LocalDateTime parseDateTimeFlexible(String raw) {
		if (raw == null)
			throw new DateTimeParseException("null", "", 0);

		// yyyy-MM-dd -> 00:00
		try {
			LocalDate d = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			return d.atStartOfDay();
		} catch (DateTimeParseException ignore) {
		}

		// các format HTML datetime-local & biến thể
		List<DateTimeFormatter> fmts = List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		for (DateTimeFormatter f : fmts) {
			try {
				return LocalDateTime.parse(raw, f);
			} catch (DateTimeParseException ignore) {
			}
		}
		// fallback ISO_LOCAL_DATE_TIME
		return LocalDateTime.parse(raw);
	}

	// =================== DTOs ===================

	public static class FlashSaleCreateRequest {
		public Integer shopId;
		public String name;
		public LocalDateTime startDate;
		public LocalDateTime endDate;
	}

	public static class FlashSaleUpdateRequest {
		public Integer flashSaleId;
		public Integer shopId; // optional
		public String name; // optional
		public LocalDateTime startDate; // optional
		public LocalDateTime endDate; // optional
	}

	public static class FlashSaleDetailResponse {
		public boolean ok;
		public FlashSale flashSale;
		public String shopName;

		public FlashSaleDetailResponse() {
		}

		public FlashSaleDetailResponse(boolean ok, FlashSale flashSale, String shopName) {
			this.ok = ok;
			this.flashSale = flashSale;
			this.shopName = shopName;
		}
	}

	/** DTO hiển thị list (kèm shopName) */
	public static class FlashSaleDTO {
		public Integer flashSaleId;
		public Integer shopId;
		public String name;
		public LocalDateTime startDate;
		public LocalDateTime endDate;
		public String shopName;

		public Integer status; // <-- thêm

		public static FlashSaleDTO fromEntity(FlashSale fs, String shopName) {
			FlashSaleDTO d = new FlashSaleDTO();
			d.flashSaleId = fs.getFlashSaleId();
			d.shopId = fs.getShopId();
			d.name = fs.getName();
			d.startDate = fs.getStartDate();
			d.endDate = fs.getEndDate();
			d.shopName = shopName;
			d.status = fs.getStatus(); // <-- thêm
			return d;
		}
	}

	/** Kết quả phân trang chuẩn */
	public static class PagedResult<T> {
		public List<T> data;
		public int total;
		public int page;
		public int size;

		public PagedResult() {
		}

		public PagedResult(List<T> data, int total, int page, int size) {
			this.data = data;
			this.total = total;
			this.page = page;
			this.size = size;
		}
	}

	public Map<String, Object> refreshExpiredStatusesNow() {
		Map<String, Object> r = new HashMap<>();
		try {
			int a = flashSaleRepository.markExpiredFlashSales();
			int b = flashSaleRepository.markExpiredItemsByParent();
			r.put("ok", true);
			r.put("expiredSalesUpdated", a);
			r.put("expiredItemsUpdated", b);
		} catch (Exception e) {
			r.put("ok", false);
			r.put("message", e.getMessage());
		}
		return r;
	}

}
