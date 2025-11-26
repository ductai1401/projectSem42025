package projectSem4.com.service.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import projectSem4.com.dto.CartItemDto;
import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.dto.ShopCouponDto;
import projectSem4.com.model.entities.Cart;
import projectSem4.com.model.entities.CartItem;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.model.repositories.CartItemRepository;
import projectSem4.com.model.repositories.CartRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.service.CouponService;


@Service
public class CartService {
	@Autowired
	private CartRepository cartRepo;
	
	@Autowired 
	private ProductVariantRepository pVRepo; 
	
	@Autowired 
	private CartItemRepository cartIRepo;
	
	@Autowired
	private CouponService couponRepo;
	
	@Autowired 
	private InventoryService inventoryService;
	
public List<CartItemView> loadData(int userId, String json){
		List<CartItemView> a = new ArrayList<>();
		if(userId <= 0) {
			a = cartIRepo.findByJsonVariant(json);
		} else {
			a = cartIRepo.findByUserId(userId);
		}
		return a;
	}
	

    public  Map<String, Object> addToCart(int quantity, int idVar ,int userId) {
    	Map<String, Object> response = new HashMap<>();
    	
    	// 1. Kiểm tra tồn kho trước
        Map<String, Object> stockCheck = inventoryService.checkStock(idVar, quantity);

        if (!(boolean) stockCheck.get("success")) {
            return stockCheck; // trả về luôn lỗi tồn kho
        }
       
        
        int inQtt = (Integer) stockCheck.get("availableQty");
        var cartItem = new CartItem();
        
        var cart = cartRepo.findByUserId(userId);
        if(cart != null) {
        	var item = cartIRepo.findByCartAndVariant(cart.getCartId(), idVar);
        	if(item != null) {
        		int currentQuantity = item.getQuantity();
        		int totalRequested = currentQuantity + quantity;
        		if(totalRequested > inQtt) {
        			item.setQuantity(inQtt);
        			cartIRepo.updateCartItem(item);
        			
        			 response.put("success", true);
        		     response.put("message", "The product is only left " + inQtt + " piece . The cart has been updated to the maximum quantity.");
        		     response.put("finalQuantity", inQtt);
        		     return response;
        		}
        		
        	}
        	
        }
        
      //add vao cart
        var a = cartRepo.AddCart(userId, idVar, quantity);
        
        if(a <= 0) {
        	 response.put("success", false);
             response.put("message", "There are no changes.");
             response.put("productVariantId", idVar);
        } else { 
            response.put("success", true);
            response.put("message", "Product has been added to the cart");
            response.put("productVariantId", idVar);
        }
        return response;
    }
	
    @Transactional
    public List<CartItemView> mergeCart(int userId, List<CartItemDto> localCart) {
        Cart cart = cartRepo.findByUserId(userId); // 1 lần thôi
        if (cart == null) {
            cart = cartRepo.createCart(userId);
        }

        for (CartItemDto item : localCart) {
            CartItem existing = cartIRepo.findByCartAndVariant(cart.getCartId(), item.getVariantId());
            int stock = inventoryService.getStock(item.getVariantId());

            if (existing != null) {
                int newQty = Math.min(existing.getQuantity() + item.getQuantity(), stock);
                existing.setQuantity(newQty);
                cartIRepo.updateCartItem(existing);
            } else {
                int newQty = Math.min(item.getQuantity(), stock);
                cartRepo.AddCart(userId, item.getVariantId(), newQty);
            }
        }

        return cartIRepo.findByUserId(userId);
    }
  
    public Map<String , Object> updateItem(int userId,int  idVar,int  quantity){
    	Map<String , Object> res = new HashMap<>();
    	var cartId = cartRepo.findByUserId(userId).getCartId();
    	Map<String, Object> stockCheck = inventoryService.checkStock(idVar, quantity);
    	if (!(boolean) stockCheck.get("success")) {
            return stockCheck; 
        }
       
    	var a  = cartIRepo.updateByCartAndVariant(cartId, idVar,quantity);
    	if(a > 0) {
    		res.put("success", true);
    		res.put("message", "Update success");
    	} else {
    		res.put("success", false);
    		res.put("message", "Update failed");
    	}
    	
    	return res;
    }
    
    public Map<String , Object> deleteItem(int userId,int  idVar){
    	Map<String , Object> res = new HashMap<>();
    	var cartId = cartRepo.findByUserId(userId).getCartId();
    	var a  = cartIRepo.deleteCartItem(cartId, idVar);
    	if(a > 0) {
    		res.put("success", true);
    		res.put("message", "Delete success");
    	} else {
    		res.put("success", false);
    		res.put("message", "Delete failed");
    	}
    	
    	return res;
    }
    
//    public Map<String , Object> checkOut(Integer userId, CheckCartItem req) {
//    	Map<String , Object> res = new HashMap<>();
//    	
//    	if (userId == null || userId <= 0) {
//            res.put("success", false);
//            res.put("message", "Vui lòng đăng nhập");
//            res.put("err", 0);
//            return res;
//        }
//
//        // --- Lấy cart items ---
//        List<CartItemView> items = cartIRepo.findAllByIdAndUser(userId, req.getCartItemIds());
//        if (items == null || items.isEmpty()) {
//            res.put("success", false);
//            res.put("message", "Không tìm thấy sản phẩm nào trong giỏ hàng");
//            res.put("err", 1);
//            return res;
//        }
//
//        List<Map<String, Object>> invalids = new ArrayList<>();
//        List<CartItemView> validItems = new ArrayList<>();
//        List<Map<Integer, Coupon>> coup = new ArrayList<>();
//
//        // --- Validate sản phẩm & flash sale ---
//        for (CartItemView item : items) {
//            int qty = item.getQuantity();
//            int available = item.getAvailableQty();
//
//            if (item.getProductId() == null || item.getVariantId() == null) {
//                invalids.add(Map.of(
//                    "cartItemId", item.getCartItemId(),
//                    "cartItemName", item.getVariantName(),
//                    "message", "Sản phẩm hoặc biến thể không còn tồn tại"
//                ));
//                continue;
//            }
//            if (item.getFlashSaleId() != null && available > 0) {
//                if (qty > available) {
//                    invalids.add(Map.of(
//                        "cartItemId", item.getCartItemId(),
//                        "cartItemName", item.getVariantName(),
//                        "message", "Flash sale chỉ còn " + available
//                                + " sản phẩm, nếu bán muốn tiếp tuc mua sẽ tính theo giá gốc"
//                    ));
//                }
//            } else {
//            	if (qty > available) {
//                    invalids.add(Map.of(
//                        "cartItemId", item.getCartItemId(),
//                        "cartItemName", item.getVariantName(),
//                        "message", "Chỉ còn " + available + " sản phẩm trong kho"
//                    ));
//                    
//                }
//            }
//            
//            
//
//            validItems.add(item);
//        }
//
//        // --- Nhóm theo shop ---
//        Map<Integer, BigDecimal> shopTotals = validItems.stream()
//                .collect(Collectors.groupingBy(
//                    CartItemView::getShopId,
//                    Collectors.mapping(
//                        i -> BigDecimal.valueOf(i.getPrice())  // Convert Double -> BigDecimal
//                                .multiply(BigDecimal.valueOf(i.getQuantity())),
//                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
//                    )
//                ));
//     // --- Validate coupon theo shop ---
//        Map<Integer, Coupon> shopCouponMap = new HashMap<>();
//        Map<Integer, Coupon> platformCouponMap = new HashMap<>();
//        Map<Integer, Coupon> freeshipCouponMap = new HashMap<>();
//
//        for (ShopCouponDto shopCoupon : req.getShopCoupons()) {
//            Integer shopId = shopCoupon.getShopId().intValue();
//            BigDecimal shopTotal = shopTotals.getOrDefault(shopId, BigDecimal.ZERO);
//
//            // ===== Shop coupon =====
//            if (shopCoupon.getShopCouponId() != null) {
//                Coupon coupon = couponRepo.getById(shopCoupon.getShopCouponId());
//                if (coupon == null) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getShopCouponId(), "message", "Mã giảm giá shop không tồn tại"));
//                } else if (shopTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getShopCouponId(), "message", "Đơn hàng chưa đủ điều kiện sử dụng mã shop"));
//                } else if (shopCouponMap.containsKey(shopId)) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getShopCouponId(), "message", "Chỉ được sử dụng một mã giảm giá shop cho mỗi shop"));
//                } else {
//                    shopCouponMap.put(shopId, coupon);
//                    coup.add(Map.of(shopId, coupon));
//                }
//            }
//
//            // ===== Platform coupon (mỗi shop riêng) =====
//            if (shopCoupon.getPlatformCouponId() != null) {
//                Coupon coupon = couponRepo.getById(shopCoupon.getPlatformCouponId());
//                if (coupon == null) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getPlatformCouponId(), "message", "Mã giảm giá platform không tồn tại"));
//                } else if (shopTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getPlatformCouponId(), "message", "Đơn hàng chưa đủ điều kiện sử dụng mã platform"));
//                } else if (platformCouponMap.containsKey(shopId)) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getPlatformCouponId(), "message", "Chỉ được sử dụng một mã platform cho mỗi shop"));
//                } else {
//                    platformCouponMap.put(shopId, coupon);
//                    coup.add(Map.of(shopId, coupon));
//                }
//            }
//
//            // ===== Freeship coupon (mỗi shop riêng) =====
//            if (shopCoupon.getFreeshipCouponId() != null) {
//                Coupon coupon = couponRepo.getById(shopCoupon.getFreeshipCouponId());
//                if (coupon == null) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getFreeshipCouponId(), "message", "Mã freeship không tồn tại"));
//                } else if (shopTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getFreeshipCouponId(), "message", "Đơn hàng chưa đủ điều kiện sử dụng mã freeship"));
//                } else if (freeshipCouponMap.containsKey(shopId)) {
//                    invalids.add(Map.of("shopId", shopId, "couponId", shopCoupon.getFreeshipCouponId(), "message", "Chỉ được sử dụng một mã freeship cho mỗi shop"));
//                } else {
//                    freeshipCouponMap.put(shopId, coupon);
//                    coup.add(Map.of(shopId, coupon));
//                }
//            }
//        }
//        
//
//        // --- Kết quả ---
//        if (!invalids.isEmpty()) {
//            res.put("success", false);
//            res.put("message", "Có lỗi khi kiểm tra giỏ hàng");
//            res.put("err", 2);
//            res.put("invalidItems", invalids);
//            return res;
//        }
//        
//       
//        res.put("success", true);
//        res.put("message", "Giỏ hàng hợp lệ, chuyển sang checkout");
//        res.put("dataOrder", req);
//        if(!coup.isEmpty()) {
//        	res.put("couponShop", coup); 
//        }
//        res.put("items", validItems);
//        return res;
//    	
//    }
    
    public Map<String, Object> checkOut(Integer userId, CheckCartItem req) {
        Map<String, Object> res = new HashMap<>();
        
        // Validate user
        if (userId == null || userId <= 0) {
            return buildErrorResponse("Vui lòng đăng nhập", 0);
        }


        
        List<CartItemView> items = cartIRepo.findAllByIdAndUser(userId, req.getCartItemIds());
        if (items == null || items.isEmpty()) {
            return buildErrorResponse("Không tìm thấy sản phẩm nào trong giỏ hàng", 1);
        }

        // Validate items and collect errors
        List<Map<String, Object>> invalids = new ArrayList<>();
        List<CartItemView> validItems = new ArrayList<>();
        
        for (CartItemView item : items) {
            String error = validateCartItem(item);
            if (error != null) {
                invalids.add(Map.of(
                    "cartItemId", item.getCartItemId(),
                    "cartItemName", item.getVariantName(),
                    "message", error
                ));
            }
            validItems.add(item);
        }

        // Calculate shop totals
        Map<Integer, BigDecimal> shopTotals = calculateShopTotals(validItems);

        // Validate coupons
        CouponValidationResult couponResult = validateCoupons(req.getShopCoupons(), shopTotals, invalids);

        // Return errors if any
        if (!invalids.isEmpty()) {
            res.put("success", false);
            res.put("message", "Có lỗi khi kiểm tra giỏ hàng");
            res.put("err", 2);
            res.put("invalidItems", invalids);
            return res;
        }

        // Build success response
        return buildSuccessResponse(req, validItems, couponResult.getAllCoupons());
    }

    // ============ Helper Methods ============

    private Map<String, Object> buildErrorResponse(String message, int errorCode) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", message);
        res.put("err", errorCode);
        return res;
    }

    private Map<String, Object> buildSuccessResponse(CheckCartItem req, List<CartItemView> items, List<Map<Integer, Coupon>> coupons) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Giỏ hàng hợp lệ, chuyển sang checkout");
        res.put("dataOrder", req);
        res.put("items", items);
        if (!coupons.isEmpty()) {
            res.put("couponShop", coupons);
        }
        return res;
    }

    private String validateCartItem(CartItemView item) {
        if (item.getProductId() == null || item.getVariantId() == null) {
            return "Sản phẩm hoặc biến thể không còn tồn tại";
        }

        int qty = item.getQuantity();
        int available = item.getAvailableQty();

        if (qty > available) {
            if (item.getFlashSaleId() != null && available > 0) {
                return "Flash sale chỉ còn " + available + " sản phẩm, nếu bạn muốn tiếp tục mua sẽ tính theo giá gốc";
            }
            return "Chỉ còn " + available + " sản phẩm trong kho";
        }

        return null; // No error
    }

    private Map<Integer, BigDecimal> calculateShopTotals(List<CartItemView> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                    CartItemView::getShopId,
                    Collectors.mapping(
                        i -> BigDecimal.valueOf(i.getPrice()).multiply(BigDecimal.valueOf(i.getQuantity())),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
    }

    private CouponValidationResult validateCoupons(List<ShopCouponDto> shopCoupons, 
                                                    Map<Integer, BigDecimal> shopTotals,
                                                    List<Map<String, Object>> invalids) {
        CouponValidationResult result = new CouponValidationResult();

        for (ShopCouponDto shopCoupon : shopCoupons) {
            Integer shopId = shopCoupon.getShopId().intValue();
            BigDecimal shopTotal = shopTotals.getOrDefault(shopId, BigDecimal.ZERO);

            validateShopCoupon(shopCoupon.getShopCouponId(), shopId, shopTotal, result.shopCoupons, invalids, "shop");
            validateShopCoupon(shopCoupon.getPlatformCouponId(), shopId, shopTotal, result.platformCoupons, invalids, "platform");
            validateShopCoupon(shopCoupon.getFreeshipCouponId(), shopId, shopTotal, result.freeshipCoupons, invalids, "freeship");
        }

        return result;
    }

    private void validateShopCoupon(Long couponId, Integer shopId, BigDecimal shopTotal,
                                    Map<Integer, Coupon> couponMap, List<Map<String, Object>> invalids,
                                    String couponType) {
        if (couponId == null) return;

        Coupon coupon = couponRepo.getById(couponId);
        
        if (coupon == null) {
            invalids.add(Map.of(
                "shopId", shopId,
                "couponId", couponId,
                "message", "Mã giảm giá " + couponType + " không tồn tại"
            ));
            return;
        }

        if (shopTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount().doubleValue())) < 0) {
            invalids.add(Map.of(
                "shopId", shopId,
                "couponId", couponId,
                "message", "Đơn hàng chưa đủ điều kiện sử dụng mã " + couponType
            ));
            return;
        }

        if (couponMap.containsKey(shopId)) {
            invalids.add(Map.of(
                "shopId", shopId,
                "couponId", couponId,
                "message", "Chỉ được sử dụng một mã " + couponType + " cho mỗi shop"
            ));
            return;
        }

        couponMap.put(shopId, coupon);
    }

    // ============ Inner Class ============

    private class CouponValidationResult {
        Map<Integer, Coupon> shopCoupons = new HashMap<>();
        Map<Integer, Coupon> platformCoupons = new HashMap<>();
        Map<Integer, Coupon> freeshipCoupons = new HashMap<>();

        List<Map<Integer, Coupon>> getAllCoupons() {
            List<Map<Integer, Coupon>> all = new ArrayList<>();
            shopCoupons.forEach((k, v) -> all.add(Map.of(k, v)));
            platformCoupons.forEach((k, v) -> all.add(Map.of(k, v)));
            freeshipCoupons.forEach((k, v) -> all.add(Map.of(k, v)));
            return all;
        }
    }
}