package projectSem4.com.service.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.dto.ShopCouponDto;
import projectSem4.com.dto.ValiOrderResult;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.model.modelViews.ShopView;
import projectSem4.com.model.repositories.CartItemRepository;
import projectSem4.com.model.repositories.CartRepository;
import projectSem4.com.service.CouponService;
import projectSem4.com.service.GHNService;
import projectSem4.com.service.admin.ShopService;
import projectSem4.com.service.admin.UserService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

	@Autowired
	private UserService userService;
	@Autowired
	private CouponService couponService;
	@Autowired
	private ShopService shopService;
	@Autowired
	private GHNService ghnService;
	@Autowired
	private CartItemRepository cartIRepo;

	@Autowired
	CartRepository cartRepo;

	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	/** Chuẩn bị toàn bộ dữ liệu checkout */
	public Map<String, Object> prepareCheckoutData(Integer userId, String cartDataJson) {
		final Map<String, Object> data = new HashMap<>();

		// --- User + Address ---
		User user = userService.getUserById(userId);
		List<Address> addresses = extractAddresses(user);
		data.put("user", user);
		data.put("addresses", addresses);

		// --- Giỏ hàng + coupon từ JSON ---
		List<CartItemView> cartView = Collections.emptyList();
		List<Map<Integer, Coupon>> couponShopSelected = Collections.emptyList();

		if (cartDataJson != null && !cartDataJson.isBlank()) {
			try {
				Map<String, Object> req = mapper.readValue(cartDataJson, new TypeReference<>() {
				});
				cartView = Optional
						.ofNullable(mapper.convertValue(req.get("items"), new TypeReference<List<CartItemView>>() {
						})).orElse(Collections.emptyList());
				couponShopSelected = Optional.ofNullable(
						mapper.convertValue(req.get("couponShop"), new TypeReference<List<Map<Integer, Coupon>>>() {
						})).orElse(Collections.emptyList());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// --- Nhóm giỏ hàng theo shop ---
		Map<Integer, List<CartItemView>> groupedCart = cartView.stream()
				.collect(Collectors.groupingBy(CartItemView::getShopId));
		data.put("groupedCart", groupedCart);

		// --- Coupon khả dụng ---
		data.put("shopCoupons", getShopCoupons(groupedCart));
		data.put("platformCoupons", getPlatformCoupons(cartView));
		data.put("freeshipCoupons", getFreeshipCoupons(cartView));

		// --- Coupon đã chọn ---
		data.putAll(extractSelectedCoupons(couponShopSelected));

		// --- Shipping fees ---
		if(addresses != null) {
			Address defaultAddress = addresses.stream().filter(Address::isDefault).findFirst().orElse(null);
			Map<Integer, Double> shippingFees = groupedCart.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
					e -> getShippingFeeForShop(e.getKey(), defaultAddress, e.getValue())));
			data.put("shippingFees", shippingFees);
		}
		

		return data;
	}

	/** Trích xuất địa chỉ từ JSON trong User */
	public List<Address> extractAddresses(User user) {
		if (user.getAddresses() == null)
			return Collections.emptyList();
		try {
			return mapper.readValue(user.getAddresses(), Addresses.class).getCustomerAddresses();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	// ========== Coupon ==========

	private Map<Integer, List<Coupon>> getShopCoupons(Map<Integer, List<CartItemView>> groupedCart) {
		Map<Integer, List<Coupon>> shopCoupons = new HashMap<>();
		for (var entry : groupedCart.entrySet()) {
			int shopId = entry.getKey();
			double subtotal = calcSubtotal(entry.getValue());
			List<Coupon> validCoupons = couponService.getByType("SHOP", shopId).stream()
					.filter(c -> subtotal >= c.getMinOrderAmount().doubleValue()).toList();
			shopCoupons.put(shopId, validCoupons);
		}
		return shopCoupons;
	}

	private List<Coupon> getPlatformCoupons(List<CartItemView> cartView) {
		double cartTotal = calcSubtotal(cartView);
		return couponService.getByType("PLATFORM", null).stream().filter(c -> cartTotal >= c.getMinOrderAmount().doubleValue()).toList();
	}

	private List<Coupon> getFreeshipCoupons(List<CartItemView> cartView) {
		double cartTotal = calcSubtotal(cartView);
		return couponService.getByType("FREESHIP", null).stream().filter(c -> cartTotal >= c.getMinOrderAmount().doubleValue()).toList();
	}

	/** Tách coupon đã chọn */
	private Map<String, Object> extractSelectedCoupons(List<Map<Integer, Coupon>> couponShopSelected) {
		Map<Integer, Integer> shopCoupons = new HashMap<>();
		Map<Integer, Integer> platformCoupons = new HashMap<>();
		Map<Integer, Integer> freeshipCoupons = new HashMap<>();

		couponShopSelected.forEach(c -> c.forEach((shopId, coupon) -> {
			if (coupon == null)
				return;
			switch (coupon.getCouponType()) {
			case "SHOP" -> shopCoupons.put(shopId, coupon.getCouponId());
			case "PLATFORM" -> platformCoupons.put(shopId, coupon.getCouponId());
			case "FREESHIP" -> freeshipCoupons.put(shopId, coupon.getCouponId());
			}
		}));

		return Map.of("selectedShopCoupons", shopCoupons, "selectedPlatformCoupons", platformCoupons,
				"selectedFreeshipCoupons", freeshipCoupons);
	}

	// ========== Validate Order ==========

	public ValiOrderResult validateOrder(List<Long> cartItemIds, List<ShopCouponDto> shopCoupons, Integer userId,
			String idAddress) {
		List<String> errors = new ArrayList<>();
		ValiOrderResult result = new ValiOrderResult();

// 1) Lấy cart items từ DB
		List<CartItemView> cartItems = cartIRepo.findAllByIdAndUser(userId, cartItemIds);
		if (cartItems.size() != cartItemIds.size()) {
			errors.add("Some cart items are invalid or removed.");
		}

// 2) Kiểm tra tồn kho
		for (CartItemView item : cartItems) {
			if (item.getQuantity() > item.getAvailableQty()) {
				errors.add("Insufficient stock for product: " + item.getProductName());
			}
		}

// 3) Nhóm cart theo shop
		Map<Integer, List<CartItemView>> groupedCart = cartItems.stream()
				.collect(Collectors.groupingBy(CartItemView::getShopId));

// 4) Lấy user + address đã chọn
		User user = userService.getUserById(userId);
		var addresses = extractAddresses(user);
		Address selectedAddress = null;
		if(addresses != null) {
			selectedAddress = addresses.stream()
					.filter(a -> a.getId().equals(idAddress)).findFirst().orElse(null);	
			result.setBuyerAddress(selectedAddress);
		} 
		if (selectedAddress == null) {
			errors.add("Address not found, please add an address to proceed with the order.");
			result.setErrors(errors);
			result.setValid(false);
			return result;
		}
		

// 5) Chuẩn bị maps lưu coupon / shipping
		Map<Long, Coupon> shopCouponMap = new HashMap<>();
		Map<Long, Coupon> platformCouponMap = new HashMap<>();
		Map<Long, Coupon> freeshipCouponMap = new HashMap<>();
// **Khai báo shippingFeeMap ở đây trước khi sử dụng**
		Map<Long, Double> shippingFeeMap = new HashMap<>();

		Set<Long> usedPlatformIds = new HashSet<>();
		Set<Long> usedFreeshipIds = new HashSet<>();

// 6) Xử lý shopCoupons (nếu có)
		if (shopCoupons != null) {
			Map<Long, List<ShopCouponDto>> groupedDtos = shopCoupons.stream()
					.collect(Collectors.groupingBy(ShopCouponDto::getShopId));

			for (Map.Entry<Long, List<ShopCouponDto>> entry : groupedDtos.entrySet()) {
				Long shopId = entry.getKey();
				List<ShopCouponDto> dtos = entry.getValue();

// subtotal của shop (lấy từ groupedCart - chú ý chuyển Long -> int)
				double subtotalShop = groupedCart.getOrDefault(shopId.intValue(), Collections.emptyList()).stream()
						.mapToDouble(ci -> (ci.getFlashSalePrice() != null ? ci.getFlashSalePrice() : ci.getPrice())
								* ci.getQuantity())
						.sum();

				Long shopCouponId = null, platformCouponId = null, freeshipCouponId = null;
				for (ShopCouponDto dto : dtos) {
					if (dto.getShopCouponId() != null) {
						if (shopCouponId != null)
							errors.add("Multiple shop coupons for shop " + shopId);
						else
							shopCouponId = dto.getShopCouponId();
					}
					if (dto.getPlatformCouponId() != null) {
						if (platformCouponId != null)
							errors.add("Multiple platform coupons for shop " + shopId);
						else
							platformCouponId = dto.getPlatformCouponId();
					}
					if (dto.getFreeshipCouponId() != null) {
						if (freeshipCouponId != null)
							errors.add("Multiple freeship coupons for shop " + shopId);
						else
							freeshipCouponId = dto.getFreeshipCouponId();
					}
				}

// validate shop coupon
				if (shopCouponId != null) {
					Coupon c = couponService.getById(shopCouponId);
					if (c == null) {
						errors.add("Shop coupon not found: " + shopCouponId);
					} else if (subtotalShop >= c.getMinOrderAmount().doubleValue()) {
						shopCouponMap.put(shopId, c);
					} else {
						errors.add("Shop voucher not valid for shop " + shopId);
					}
				}

// validate platform coupon (unique across shops)
				if (platformCouponId != null) {
					if (usedPlatformIds.contains(platformCouponId)) {
						errors.add("Platform voucher " + platformCouponId + " already used for another shop.");
					} else {
						Coupon c = couponService.getById(platformCouponId);
						if (c == null) {
							errors.add("Platform coupon not found: " + platformCouponId);
						} else if (subtotalShop >= c.getMinOrderAmount().doubleValue()) {
							platformCouponMap.put(shopId, c);
							usedPlatformIds.add(platformCouponId);
						} else {
							errors.add("Platform voucher not valid for shop " + shopId);
						}
					}
				}

// validate freeship coupon
				if (freeshipCouponId != null) {
					if (usedFreeshipIds.contains(freeshipCouponId)) {
						errors.add("Freeship voucher " + freeshipCouponId + " already used for another shop.");
					} else {
						Coupon c = couponService.getById(freeshipCouponId);
						if (c == null) {
							errors.add("Freeship coupon not found: " + freeshipCouponId);
						} else if (subtotalShop >= c.getMinOrderAmount().doubleValue()) {
							freeshipCouponMap.put(shopId, c);
							usedFreeshipIds.add(freeshipCouponId);
						} else {
							errors.add("Freeship voucher not valid for shop " + shopId);
						}
					}
				}

// 7) Tính phí ship cho shop này và cho vào shippingFeeMap (key là Long shopId)
				double shippingFee = getShippingFeeForShop(shopId.intValue(), selectedAddress,
						groupedCart.getOrDefault(shopId.intValue(), Collections.emptyList()));
				shippingFeeMap.put(shopId, shippingFee);
			}
		}

// Nếu có lỗi thì trả về ngay
		if (!errors.isEmpty()) {
			result.setErrors(errors);
			result.setValid(false);
			return result;
		}

// 8) Gán kết quả (chú ý setCartItems yêu cầu List<CartItemView>)
		result.setValid(true);
		result.setErrors(errors);
// flatten groupedCart -> List<CartItemView>
		result.setCartItems(groupedCart.values().stream().flatMap(List::stream).collect(Collectors.toList()));

		result.setShopCouponMap(shopCouponMap);
		result.setPlatformCouponMap(platformCouponMap);
		result.setFreeshipCouponMap(freeshipCouponMap);
		result.setShippingFeeMap(shippingFeeMap);

		return result;
	}

	private ValiOrderResult buildInvalidResult(List<String> errors, ValiOrderResult result) {
		result.setErrors(errors);
		result.setValid(false);
		return result;
	}

	// ========== Utils ==========

	private double calcSubtotal(List<CartItemView> items) {
		return items.stream()
				.mapToDouble(
						i -> (i.getFlashSalePrice() != null ? i.getFlashSalePrice() : i.getPrice()) * i.getQuantity())
				.sum();
	}

	private double getShippingFeeForShop(Integer shopId, Address address, List<CartItemView> items) {
		if (address == null)
			return 0;
		try {
			int defaultWeight = 500; // gram
			int toDistrict = Integer.parseInt(address.getDistrictId());
			String toWard = address.getWardCode();

			ShopView shop = shopService.getShopById(shopId);
			Address shopAd = Optional.ofNullable(shop.getAddress()).filter(a -> !a.trim().isEmpty()).map(a -> {
				try {
					return mapper.readValue(a, Addresses.class).getShopAddresses().getWarehouseAddress();
				} catch (Exception e) {
					return null;
				}
			}).orElse(null);

			if (shopAd == null)
				return 0;
			int fromDistrict = Integer.parseInt(shopAd.getDistrictId());
			int totalValue = (int) calcSubtotal(items);

			String res = ghnService.calculateFee(fromDistrict, toDistrict, toWard, defaultWeight, totalValue);
			JsonNode node = mapper.readTree(res);
			if (node.has("data") && node.get("data").has("total")) {
				return node.get("data").get("total").asDouble();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}