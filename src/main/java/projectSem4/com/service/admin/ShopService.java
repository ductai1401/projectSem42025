package projectSem4.com.service.admin;

import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.modelViews.ShopView;
import projectSem4.com.model.repositories.OrderRepository;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ShopRepository;

@Service
public class ShopService {
	@Autowired
	private ShopRepository shopRepo;

	@Autowired
	private ProductRepository proRepo;

	private ObjectMapper mapper;

	@Autowired
	private OrderRepository orderRepo;

	public ShopService() {
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public Map<String, Object> searchAndPagi(int page, int size, String keyword, Double minRating, Integer status,
			String sortBy) {
		return shopRepo.searchAndPagi(page, size, keyword, minRating, status, sortBy);
	}

	public int getRepoHashCode() {
		return System.identityHashCode(shopRepo);
	}

	public List<Shop> getAllShopPage(int pageSize, int pageNumber, String keyword) {
		return shopRepo.findAllPaged(pageSize, pageNumber);
	}

	public ShopView getShopByIdUsr(int userId) {
		return shopRepo.findByUserId(userId);
	}

	public int getShopIdByUser(int userId) {
		return shopRepo.getShopIdByUserId(userId);
	}

	public String getShopNameById(int shopId) {
		return shopRepo.getShopNameById(shopId);
	}

	public ShopView getShopById(int shopId) {
		return shopRepo.findById(shopId);
	}

	public Map<String, Object> updateStatus(int shopId, int status) {
		try {
			var shop = shopRepo.findById(shopId);
			if (shop == null) {
				return Map.of("success", false, "message", "Shop not found");
			}

			var rs = shopRepo.updateStatusShop(shopId, status);
			if (rs) {
				return Map.of("success", rs, "message", "upadte status success");
			} else {
				return Map.of("success", rs, "message", "upadte status failed");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Map.of("success", false, "message", "upadte status failed");
		}
	}

	public Map<String, Object> getShopDetail(int shopId) {
		try {
			Map<String, Object> res = new HashMap<>();
			Map<String, Object> stats = new HashMap<>();
			var shop = shopRepo.findById(shopId);
			if (shop == null) {
				return Map.of("error", "Shop not found");
			}
			res.put("shop", shop);

			var totalOrder = orderRepo.totalOrderByShop(shopId);
			stats.put("totalOrder", totalOrder);

			Address shopAd = null;
			String strAddress = "";
			if (shop.getAddress() != null) {
				Addresses addresses = Optional.ofNullable(shop.getAddress()).filter(s -> !s.trim().isEmpty()).map(s -> {
					try {
						return mapper.readValue(s, Addresses.class);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).orElse(new Addresses());

				shopAd = addresses.getShopAddresses().getWarehouseAddress();

				strAddress = shopAd.getProvinceName() + ", " + shopAd.getDistrictName() + ", " + shopAd.getWardName()
						+ ", " + shopAd.getStreet();
				stats.put("strAddress", strAddress);
			}

			res.put("stats", stats);
			return res;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Map.of("error", "upadte status failed");
		}

	}
}
