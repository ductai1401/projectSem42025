package projectSem4.com.service.client;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.catalina.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import projectSem4.com.dto.CheckCartItem;
import projectSem4.com.dto.ShopCouponDto;
import projectSem4.com.dto.ValiOrderResult;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Coupon;

import projectSem4.com.model.entities.Order;
import projectSem4.com.model.entities.OrderDiscount;
import projectSem4.com.model.entities.OrderItem;
import projectSem4.com.model.entities.Payment;
import projectSem4.com.model.entities.PaymentOrder;
import projectSem4.com.model.entities.PlatformEarning;
import projectSem4.com.model.entities.Shipment;
import projectSem4.com.model.enums.OrderStatus;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.model.modelViews.OrderItemView;
import projectSem4.com.model.modelViews.OrderView;
import projectSem4.com.model.repositories.CartItemRepository;
import projectSem4.com.model.repositories.OrderDiscountRepository;
import projectSem4.com.model.repositories.OrderItemRepository;
import projectSem4.com.model.repositories.OrderRepository;
import projectSem4.com.model.repositories.PaymentOrderRepository;
import projectSem4.com.model.repositories.PaymentRepository;
import projectSem4.com.model.repositories.PlatformEarningRepository;
import projectSem4.com.model.repositories.ShipmentRepository;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.model.repositories.WalletRepository;
import projectSem4.com.service.VNPayService;

@Service
public class OrderService {
	@Autowired
    private OrderRepository orderRepo;

    @Autowired 
    private OrderItemRepository oIRepository;
    
    @Autowired
    private CheckoutService checkService;
    
    @Autowired
    private CartItemRepository cartIRepo;
    
    @Autowired
    private OrderDiscountRepository oDrepo;
    
    @Autowired 
    private ShopRepository shopRepo;
    
    @Autowired 
    private PaymentOrderRepository pORepo;
    
    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private VNPayService vnpayService;
    
    @Autowired
    private WalletRepository walletRepo;
    
    @Autowired
    private PlatformEarningRepository pERepo; 
    
    @Autowired
	private ShipmentRepository shipmentRepo;
    
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public Map<String, Object> placeOrderCOD(CheckCartItem checkCartItem, Integer userId) {
        // Extract từ class CheckCartItem
        List<Long> cartItemIds = checkCartItem.getCartItemIds();
        List<ShopCouponDto> shopCoupons = checkCartItem.getShopCoupons();
        String idAddress = checkCartItem.getIdAddress();
        String methodPayment = checkCartItem.getMethodPayment(); // Giả sử "COD"

        // Gọi hàm kiểm tra
        ValiOrderResult validationResult = checkService.validateOrder(cartItemIds, shopCoupons, userId, idAddress);
        
        if (!validationResult.isValid()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("errors", validationResult.getErrors());
            return result;
        }
        var result = insertOrder(validationResult, userId, checkCartItem.getMethodPayment());
        var isSuccess =  (Boolean) result.get("success");
        
        if (isSuccess) {
            if (!methodPayment.equals("COD")) {
                Map<String, Object> res = new HashMap<>();
                var orderId = (String) result.get("oredrId");
                try {
                    var total = (Double) result.get("total");
                    var transacCode = (String) result.get("transacCode");
                   

                    String vnpUrl = vnpayService.createPaymentUrl(total, "Thanh toán đơn hàng #" + orderId, transacCode);
                    var payment = new Payment();
                    payment.setTransactionCode(transacCode);
                    payment.setPaymentUrl(vnpUrl);
                    payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
                    var a = paymentRepo.updateUrl(payment);
                    res.put("success", true);
                    res.put("payment", true);  // ✅ thêm cờ cho biết đã có link thanh toán
                    res.put("url", vnpUrl);
                    res.put("orderId", orderId);
                    return res;
                } catch (Exception e) {
                    e.printStackTrace();
                    res.put("success", true);  // ✅ vẫn coi là đặt hàng thành công
                    res.put("payment", false); // nhưng chưa thanh toán
                    res.put("orderId", orderId);
                    res.put("message", "Đơn hàng đã được tạo nhưng lỗi khi khởi tạo thanh toán VNPAY. Vui lòng thử lại.");
                    return res;
                }
            }
        }
        return result;
        // Nếu kiểm tra thành công, gọi hàm insert
     
    }

    // Hàm kiểm tra (validation) - Chỉnh sửa để xử lý coupons per shop từ List<ShopCouponDto>
    

    // Hàm insert (tạo đơn hàng) - Tính discount per shop, không cần chia global
    public Map<String, Object> insertOrder(ValiOrderResult validationResult, Integer userId, String methodPayment) {
        Map<String, Object> result = new HashMap<>();
        List<CartItemView> cartItems = validationResult.getCartItems();
        Map<Long, Coupon> shopCouponMap = validationResult.getShopCouponMap();
        Map<Long, Coupon> platformCouponMap = validationResult.getPlatformCouponMap();
        Map<Long, Coupon> freeshipCouponMap = validationResult.getFreeshipCouponMap();
        Map<Long, Double> ShippingFees = validationResult.getShippingFeeMap();
        Address buyerAddress = validationResult.getBuyerAddress();

        // Group cart theo shop
        Map<Integer, List<CartItemView>> groupedCart = cartItems.stream()
                .collect(Collectors.groupingBy(CartItemView::getShopId));

        List<Map<String, Object>> ordersResult = new ArrayList<>();
        
        List<PaymentOrder> paymentOrder = new ArrayList<>();
     
        double totalFull = 0.0; 
        // Tạo order cho mỗi shop
        for (var entry : groupedCart.entrySet()) {
            int shopIdInt = entry.getKey();
            
            var shop = shopRepo.findById(shopIdInt);
            
            if(shop == null) {
            	result.put("success", false);
        	    throw new RuntimeException("Shop not found " + shopIdInt);
            }
            
            Long shopId = (long) shopIdInt;
            List<CartItemView> items = entry.getValue();

         // Xác định order có flash sale hay không
            boolean hasFlashSale = items.stream().anyMatch(ci -> ci.getFlashSaleId() != null);
            
            // subtotal shop
            double subtotalShop = items.stream()
                    .mapToDouble(ci -> {
                        double price = (ci.getFlashSalePrice() != null ? ci.getFlashSalePrice() : ci.getPrice());
                        return price * ci.getQuantity();
                    })
                    .sum();
            double subtotalAfter = subtotalShop;
            // Giảm giá shop voucher
            double shopDiscount = 0;
            Coupon shopVoucher = shopCouponMap.get(shopId);
            if (shopVoucher != null) {
                if ("PERCENT".equals(shopVoucher.getDiscountType())) {
                	
                    shopDiscount = subtotalAfter * shopVoucher.getDiscountValue().doubleValue() / 100.0;
                } else {
                    shopDiscount = shopVoucher.getDiscountValue().doubleValue();
                }
                subtotalAfter -= shopDiscount;
            }

            // Giảm giá platform (global)
            double platformDiscount = 0;
            Coupon platformCoupon = platformCouponMap.get(shopId);
            if (platformCoupon != null) {
                if ("PERCENT".equals(platformCoupon.getDiscountType())) {
                    platformDiscount = subtotalAfter * platformCoupon.getDiscountValue().doubleValue() / 100.0;
                } else {
                    platformDiscount = platformCoupon.getDiscountValue().doubleValue();
                }
                subtotalAfter -= platformDiscount;
            }

            double freeshipDiscount = 0;

            Coupon freeshipCoupon = freeshipCouponMap.get(shopId);
            double shippingFee = ShippingFees.get(shopId);
            if (freeshipCoupon != null) {
                if ("PERCENT".equals(freeshipCoupon.getDiscountType())) {
                    freeshipDiscount = shippingFee * freeshipCoupon.getDiscountValue().doubleValue() / 100.0;
                } else {
                    freeshipDiscount = freeshipCoupon.getDiscountValue().doubleValue();
                }

                // Không cho freeship vượt quá tiền ship
                freeshipDiscount = Math.min(freeshipDiscount, shippingFee);
            }
            
            Map<Integer, Double> totalItem = new HashMap<>();
            
            for (CartItemView ci : items) {
                double price = (ci.getFlashSalePrice() != null ? ci.getFlashSalePrice() : ci.getPrice());
                double itemSubtotal = price * ci.getQuantity();

                // tỷ lệ của item trong tổng shop
                double ratio = itemSubtotal / subtotalShop;

                // phân bổ shop discount
                double itemShopDiscount = shopDiscount * ratio;

                // phân bổ platform discount
                double itemPlatformDiscount = platformDiscount * ratio;

                // tổng giảm giá cho item
                double totalItemDiscount = itemShopDiscount + itemPlatformDiscount;

                // gán vào model (bạn nên thêm các field này trong CartItemView)
               
                totalItem.put(ci.getVariantId(), totalItemDiscount);
            }

            double totalShop = subtotalAfter + shippingFee - freeshipDiscount;
            if (totalShop < 0) totalShop = 0;

            // Tạo order (phần còn lại giống gốc)
            Order order = new Order();
            var orderId = "ORDER" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
            
            
            
            order.setOrderId(orderId);
            order.setBuyerId(userId);
            order.setShopId(shopIdInt);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(totalShop);    
            order.setPaymentMethod(methodPayment);
            order.setShippingFee(shippingFee);
            order.setDeliveryAddressJson(buyerAddress);
            order.setStatus(methodPayment.equals("COD") ? 1 : 0); // PENDING
            order.setFlashSaleStatus(hasFlashSale ? 1 : 0);
            order.setRefund(0.0);
            var resultOrder = orderRepo.createOrder(order);
            var rows =  (int) resultOrder.get("rows");
            paymentOrder.add(new PaymentOrder(null,null,order.getOrderId()));
            if(rows > 0) {
            	// Tạo order items
                for (CartItemView item : items) {
                	try {
                		
                		
                        OrderItem oi = new OrderItem();
                        
                        
                        oi.setOrderId(order.getOrderId());
                        oi.setProductVariantId(item.getVariantId());
                        oi.setQuantity(item.getQuantity());

                        double unitPrice = (item.getFlashSalePrice() != null ? item.getFlashSalePrice() : item.getPrice());
                        oi.setUnitPrice(unitPrice);
                        oi.setFlashSaleId(item.getFlashSaleId() != null ? item.getFlashSaleId() : 0);
                        oi.setDiscountAllocated(totalItem.get(item.getVariantId()));	
                        var resultItem = oIRepository.createOrderItem(oi);
                        int rowsItem = (int) resultItem.get("rows");
                        if (rowsItem <= 0) {
                            throw new RuntimeException("Insert OrderItem failed for variantId=" + item.getVariantId());
                        }

                        
                    } catch (Exception e) {
                        // log lỗi chi tiết, rồi throw ra ngoài để rollback toàn bộ
                        throw new RuntimeException("Lỗi khi insert order item : ", e);
                    }
                    
                    
                }
                if("COD".equals(methodPayment)) {
                	var inven = orderRepo.updateStatusInven(order.getStatus(), order.getOrderId(), null);
                    if (inven <= 0) {
                        throw new RuntimeException("Insert inventory failed " );
                    }
                }
                
                
                for (CartItemView item : items) {
                    int deleted = cartIRepo.deleteCartItem(item.getCartId(), item.getVariantId());
                    if (deleted < 0) {
                        throw new RuntimeException("Delete CartItem failed for cartItemId=" + item.getCartId());
                    }
                } 
                
                try {
                	
                     if (shopVoucher != null) {
                        var discount = new OrderDiscount();
                        discount.setCouponId(shopVoucher.getCouponId());
                        discount.setDiscountType(shopVoucher.getDiscountType());
                        discount.setDiscountValue(shopDiscount);
                        discount.setOrderId(order.getOrderId());
                        discount.setCreatedAt(order.getOrderDate());
                         
                        oDrepo.createOrderDiscount(discount);
                         
                     }
                     if (platformCoupon != null) {
                     	 var discount = new OrderDiscount();
                          discount.setCouponId(platformCoupon.getCouponId());
                          discount.setDiscountType(platformCoupon.getDiscountType());
                          discount.setDiscountValue(platformDiscount);
                          discount.setOrderId(order.getOrderId());
                          discount.setCreatedAt(order.getOrderDate());
                          oDrepo.createOrderDiscount(discount);
                     }
                     if (freeshipCoupon != null) {
                     	var discount = new OrderDiscount();
                         discount.setCouponId(freeshipCoupon.getCouponId());
                         discount.setDiscountType(freeshipCoupon.getDiscountType());
                         discount.setDiscountValue(freeshipDiscount);
                         discount.setOrderId(order.getOrderId());
                         discount.setCreatedAt(order.getOrderDate());
                         oDrepo.createOrderDiscount(discount);
                     }
				} catch (Exception e) {
					throw new RuntimeException("Lỗi khi insert order discoun : ", e);
				}
                
               
                
                
                Map<String, Object> singleOrder = new HashMap<>();
                singleOrder.put("orderId", order.getOrderId());
                singleOrder.put("shopId", shopIdInt);
                singleOrder.put("totalAmount", totalShop);
                ordersResult.add(singleOrder);
                result.put("success", true);
            } else {
            	result.put("success", false);
            	    throw new RuntimeException("Insert order failed for shopId " + shopIdInt);
            	
            } 
            totalFull += totalShop;
            result.put("orderId", orderId);
        }
        try {
        	 if("VNPAY".equals(methodPayment) ) {
             	Payment payment = new Payment();
                 payment.setCreatedAt(LocalDateTime.now());
                 payment.setUpdatedAt(LocalDateTime.now());
                 payment.setPaymentAmount(totalFull);
                 payment.setPaymentMethod("VNPAY");
                 payment.setStatus(-1);
                 payment.setPaymentMethod(methodPayment);
                 var transac = "PAY" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
                 payment.setTransactionCode(transac);
                 
                 var paymentId = paymentRepo.create(payment);
                 if(paymentId != null) {
                	 paymentOrder.stream().forEach(a -> {
                		 a.setPaymentId(paymentId);
                		 pORepo.save(a);
                	 });
                	 result.put("transacCode", transac);
                 }
                 else {
                	 result.put("success", false);
             	    throw new RuntimeException("Insert payments failed");
             	
                 }
             }
             
		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi insert payment : ", e);
		}
        
        result.put("total", totalFull);
        result.put("orders", ordersResult);
        return result;
    }
    
    public Map<String, Object> calculateTotal(ValiOrderResult validationResult) {
        Map<String, Object> result = new HashMap<>();
        List<CartItemView> cartItems = validationResult.getCartItems();
        Map<Long, Coupon> shopCouponMap = validationResult.getShopCouponMap();
        Map<Long, Coupon> platformCouponMap = validationResult.getPlatformCouponMap();
        Map<Long, Coupon> freeshipCouponMap = validationResult.getFreeshipCouponMap();
        Map<Long, Double> shippingFees = validationResult.getShippingFeeMap();

        // Group cart theo shop
        Map<Integer, List<CartItemView>> groupedCart = cartItems.stream()
                .collect(Collectors.groupingBy(CartItemView::getShopId));

        double grandTotal = 0.0;
        List<Map<String, Object>> shopTotals = new ArrayList<>();

        for (var entry : groupedCart.entrySet()) {
            int shopIdInt = entry.getKey();
            Long shopId = (long) shopIdInt;
            List<CartItemView> items = entry.getValue();

            // subtotal shop
            double subtotalShop = items.stream()
                    .mapToDouble(ci -> {
                        double price = (ci.getFlashSalePrice() != null ? ci.getFlashSalePrice() : ci.getPrice());
                        return price * ci.getQuantity();
                    })
                    .sum();

            // Shop voucher
            double shopDiscount = 0;
            Coupon shopVoucher = shopCouponMap.get(shopId);
            if (shopVoucher != null) {
                if ("PERCENT".equals(shopVoucher.getDiscountType())) {
                    shopDiscount = subtotalShop * shopVoucher.getDiscountValue().doubleValue() / 100.0;
                } else {
                    shopDiscount = shopVoucher.getDiscountValue().doubleValue();
                }
            }

            // Platform voucher
            double platformDiscount = 0;
            Coupon platformCoupon = platformCouponMap.get(shopId);
            if (platformCoupon != null) {
                if ("PERCENT".equals(platformCoupon.getDiscountType())) {
                    platformDiscount = subtotalShop * platformCoupon.getDiscountValue().doubleValue() / 100.0;
                } else {
                    platformDiscount = platformCoupon.getDiscountValue().doubleValue();
                }
            }

            // Shipping fee + freeship
            double shippingFee = shippingFees.get(shopId);
            double freeshipDiscount = 0;
            Coupon freeshipCoupon = freeshipCouponMap.get(shopId);
            if (freeshipCoupon != null) {
                if ("PERCENT".equals(freeshipCoupon.getDiscountType())) {
                    freeshipDiscount = shippingFee * freeshipCoupon.getDiscountValue().doubleValue() / 100.0;
                } else {
                    freeshipDiscount = freeshipCoupon.getDiscountValue().doubleValue();
                }
                freeshipDiscount = Math.min(freeshipDiscount, shippingFee); // không vượt quá tiền ship
            }
            
            

            // Tổng cho shop
            double totalShop = subtotalShop - shopDiscount - platformDiscount + shippingFee - freeshipDiscount;
            if (totalShop < 0) totalShop = 0;

            grandTotal += totalShop;

            Map<String, Object> shopResult = new HashMap<>();
            shopResult.put("shopId", shopIdInt);
            shopResult.put("subtotal", subtotalShop);
            shopResult.put("shopDiscount", shopDiscount);
            shopResult.put("platformDiscount", platformDiscount);
            shopResult.put("shippingFee", shippingFee);
            shopResult.put("freeshipDiscount", freeshipDiscount);
            shopResult.put("totalShop", totalShop);

            shopTotals.add(shopResult);
        }

        result.put("success", true);
        result.put("grandTotal", grandTotal);
        result.put("shops", shopTotals);
        return result;
    }
    
    public Map<String, Object> fillterOrderByShopId(int page, int size, String keyword,  Date date,String status, String payment, int shopId){
    	return orderRepo.fillterOrderByShopId(page, size, keyword, date, status, payment, shopId);
    }
   
    
    public int updateStatusOrder(String orderId, int status) {
    	
    	return orderRepo.updateStatusInven(status, orderId, null);
    }
    public int updateStatusPaymentSuccess(String paymentId, int status) {
    	return orderRepo.updateStatusPaymentSuccess(paymentId, status);
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateOrderStatusByShop(String orderId, String newStatus, int userId, String note) {

        Map<String, Object> res = new HashMap<>();

        try {

            // 1) Validate status
            if (!"PROCESSING".equals(newStatus) && 
                !"SHIPPED".equals(newStatus) && 
                !"CANCELLED".equals(newStatus)) {

                throw new RuntimeException("Update status failed: invalid status");
            }

            // 2) Validate shop
            var shop = shopRepo.findByUserId(userId);
            if (shop == null)
                throw new RuntimeException("Update failed: shop not found");

            // 3) Validate order
            var order = orderRepo.getOrderByShop(orderId, shop.getShopId());
            if (order == null)
                throw new RuntimeException("Update failed: order not found");

            int statusCode = OrderStatus.safeGetCode(newStatus);
            if (statusCode == -1)
                throw new RuntimeException("Update failed: unknown status code");

            // Note required when cancel
            if (statusCode == 4 && (note == null || note.isBlank()))
                throw new RuntimeException("Please enter cancel reason");


            var currentStatus = order.getStatus();
            var currentStr = OrderStatus.fromCode(currentStatus);

            // -------------------------
            // PROCESSING
            // -------------------------
            if ("PROCESSING".equals(newStatus)) {
                if (currentStatus != 1)
                    throw new RuntimeException("Cannot update " + newStatus + " from " + currentStr);

                int result = orderRepo.updateStatusInven(statusCode, orderId, null);

                if (result <= 0)
                    throw new RuntimeException("Update PROCESSING failed");

                res.put("success", true);
                res.put("message", "Order updated successfully");
                return res;
            }

            // -------------------------
            // SHIPPED
            // -------------------------
            if ("SHIPPED".equals(newStatus)) {
                if (currentStatus != 2)
                    throw new RuntimeException("Cannot update " + newStatus + " from " + currentStr);

                int result = orderRepo.updateStatusInven(statusCode, orderId, null);
                if (result <= 0)
                    throw new RuntimeException("Update SHIPPED failed (DB)");

                // Insert shipment
                var shipment = new Shipment();

                Address shopAd = Optional.ofNullable(shop.getAddress()).filter(a -> !a.trim().isEmpty()).map(a -> {
                    try { return mapper.readValue(a, Addresses.class).getShopAddresses().getWarehouseAddress(); }
                    catch (Exception e) { return null; }
                }).orElse(null);

                var add = order.getDeliveryAddressJson();

                shipment.setPickupAddress(shopAd.getStreet() + ", " + shopAd.getWardName() + ", " +
                        shopAd.getDistrictName() + ", " + shopAd.getProvinceName());

                shipment.setDeliveryAddress(add.getStreet() + ", " + add.getWardName() + ", " +
                        add.getDistrictName() + ", " + add.getProvinceName());

                shipment.setShipmentID("SHP" + UUID.randomUUID().toString().replace("-", "").toUpperCase());

                boolean inserted = shipmentRepo.insertByOrder(shipment, order.getBuyerId(), orderId);
                if (!inserted)
                    throw new RuntimeException("Shipment insert failed");

                res.put("success", true);
                res.put("message", "Order updated successfully");
                return res;
            }

            // -------------------------
            // CANCELLED
            // -------------------------
            if ("CANCELLED".equals(newStatus)) {
                if (currentStatus != 1)
                    throw new RuntimeException("Cannot update CANCELLED from " + currentStr);

                int result = orderRepo.updateStatusInven(statusCode, orderId, note);
                if (result <= 0)
                    throw new RuntimeException("Cancel failed");

                res.put("success", true);
                res.put("message", "Order updated successfully");
                return res;
            }

            throw new RuntimeException("Invalid transition");

        } catch (Exception ex) {

            // FORCE rollback
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            res.put("success", false);
            res.put("message", ex.getMessage());
            return res;
        }
    }
    
    public Map<String, Object> updateOrderStatusByBuyer(String orderId, int status, String node) {
    	Map<String, Object> res = new HashMap<>();
    	if(status != 4 && status != 5) {
    		res.put("success", false);
    		res.put("message", "Update Status Failed");
    		 return res;
    	} 
    	
    	
       var order = orderRepo.getOrderById(orderId);
       if(order == null) {
    	   res.put("success", false);
   		res.put("message", "Update status failed, The order does not exist. ");
   		 return res;
       }
       var statusNew = OrderStatus.fromCode(status);
       var statusStr = OrderStatus.fromCode(order.getStatus());
       var statusOld = order.getStatus();
       if(statusOld == 3) { 
    	   if(status == 5) {
    		   
    		   var result = orderRepo.updateStatusInven(status, order.getOrderId(), null);
    		   if(result > 0) {
    			   // tinh tien hoa hong va cap nhat wallet
    			   
    			   
    			   res.put("success", true);
    	    		res.put("message", "Update status "+ statusNew + " success.");
    	    		
    		   }else {
    			   res.put("success", false);
    				res.put("message", "Update status "+ statusNew + " failed.");
    		   }
    		   return res;
    	   } 
    	   
       } 
       if(statusOld == 1 || statusOld == 2 || statusOld == 0) {
    	   if(status == 4) {
    		   if(node == null) {
    			   res.put("success", false);
   				res.put("message", "Please enter reason for cancel order");
   				return res;
    		   }
    		   
    		   var result = orderRepo.updateStatusInven(status, order.getOrderId(), node);
    		   if(result > 0) {
    			   res.put("success", true);
    	    		res.put("message", "Update status "+ statusNew + " success.");
    	    		
    		   }else {
    			   res.put("success", false);
    				res.put("message", "Update status "+ statusNew + " failed.");
    		   }
    		   return res;
    	   } 
       }
       
       res.put("success", false);
		res.put("message", "Cann't update " + statusNew  + " to " + statusStr );
       return res;
    }

    
    
    public OrderView getOrderItemByOrderId(String orderId){
    	return orderRepo.getOrderViewByOrderId(orderId);
    }
    public OrderView getOrderItemByShipmentId(String shipmentId){
    	return orderRepo.getOrderViewByShipmentId(shipmentId);
    }

    public Map<String, Object> getAllByStatusUserId(int userId, String status, int pageIndex, int pageSize) {
    	Integer statusInt = STATUS_MAP.getOrDefault(status, null);
    	return orderRepo.getOrderViewByUserId(userId, statusInt, pageIndex, pageSize);
    }
    
    private static final Map<String, Integer> STATUS_MAP = Map.of(
    	    "UNPAID", 0,
    	    "PENDING", 1,
    	    "PROCESSING", 2,
    	    "SHIPPING", 3,
    	    "CANCELLED", 4,
    	    "COMPLETED", 5
    	);


}
