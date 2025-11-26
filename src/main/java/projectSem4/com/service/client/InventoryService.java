package projectSem4.com.service.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectSem4.com.model.entities.InventoryLog;

import projectSem4.com.model.repositories.InventoryLogRepository;

@Service
public class InventoryService {

	@Autowired
	private InventoryLogRepository iLRepo;

	// ===== Messages (có thể chuyển sang i18n sau) =====
	private static final String MSG_NOT_SOLD = "The product is no longer sold in the store.";
	private static final String MSG_OUT_OF_STOCK = "The product is out of stock.";
	private static final String MSG_ONLY_LEFT = "This product is left with only %d. Do you want to buy this quantity?";
	private static final String MSG_AVAILABLE = "Còn hàng";

	/**
	 * Kiểm tra tồn kho của một productinven
	 *
	 * @param productinvenId id biến thể sản phẩm
	 * @param requiredQty    số lượng cần
	 * @return Map { success(bool), message(String), availableQty(int, optional) }
	 */
	public Map<String, Object> checkStock(int variantId, int requiredQty) {
		Map<String, Object> response = new HashMap<>();

		// Lấy tồn hiện tại (0 nếu chưa có log)
		int remaining = 0;
		try {
			remaining = iLRepo.getStock(variantId);
		} catch (Exception ignored) {
			remaining = 0;
		}

		// Nếu bạn muốn coi "chưa có log" = "không còn bán":
		// if (iLRepo.findLatestByVariantId(variantId) == null) {
		// response.put("success", false);
		// response.put("message", MSG_NOT_SOLD);
		// return response;
		// }

		if (remaining <= 0) {
			response.put("success", false);
			response.put("message", MSG_OUT_OF_STOCK);
			response.put("availableQty", 0);
			return response;
		}

		if (remaining < requiredQty) {
			response.put("success", false);
			response.put("message", String.format(MSG_ONLY_LEFT, remaining));
			response.put("availableQty", remaining);
			return response;
		}

		response.put("success", true);
		response.put("message", MSG_AVAILABLE);
		response.put("availableQty", remaining);
		return response;
	}

	public int getStock(int variantId) {
        try {
        	return iLRepo.getStock(variantId);
        } catch (Exception ignored) {
            return 0;
        }
	}
	public int addLog(InventoryLog inven) {
		return iLRepo.addLog(inven);
	}

	public InventoryLog getRemainning(int variantId) {

		return iLRepo.findLatestByVariantId(variantId);
	}

// ===== Optional: dùng DTO thay Map cho type-safe =====
	public StockCheckResult checkStockTyped(int variantId, int requiredQty) {
		Map<String, Object> m = checkStock(variantId, requiredQty);
		boolean success = Boolean.TRUE.equals(m.get("success"));
		int available = (m.get("availableQty") instanceof Number n) ? n.intValue() : 0;
		String message = String.valueOf(m.getOrDefault("message", ""));
		return new StockCheckResult(success, message, available);
	}

	public static class StockCheckResult {
		public final boolean success;
		public final String message;
		public final int availableQty;

		public StockCheckResult(boolean success, String message, int availableQty) {
			this.success = success;
			this.message = message;
			this.availableQty = availableQty;
		}
	}
}