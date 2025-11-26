package projectSem4.com.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectSem4.com.dto.RefundOrderDTO;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Order;
import projectSem4.com.model.entities.OrderDiscount;
import projectSem4.com.model.entities.OrderItem;
import projectSem4.com.model.entities.Payment;
import projectSem4.com.model.entities.RefundItem;
import projectSem4.com.model.entities.RefundRequest;
import projectSem4.com.model.entities.Shipment;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.enums.OrderStatus;
import projectSem4.com.model.enums.RefundStatus;
import projectSem4.com.model.modelViews.RefundItemView;
import projectSem4.com.model.modelViews.RefundView;
import projectSem4.com.model.modelViews.ShopView;
import projectSem4.com.model.repositories.CouponRepository;
import projectSem4.com.model.repositories.OrderDiscountRepository;
import projectSem4.com.model.repositories.OrderItemRepository;
import projectSem4.com.model.repositories.OrderRepository;
import projectSem4.com.model.repositories.PaymentRepository;
import projectSem4.com.model.repositories.RefundItemRepository;
import projectSem4.com.model.repositories.RefundRequestRepository;
import projectSem4.com.model.repositories.ShipmentRepository;
import projectSem4.com.model.repositories.ShopRepository;

@Service
public class RefundService {

	private static final String BASE_UPLOAD_DIR = "C:/data/uploads/refund";
	
	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private RefundRequestRepository rRRepo;

	@Autowired
	private OrderItemRepository oItemRepo;

	@Autowired
	private OrderDiscountRepository oDRepo;

	@Autowired
	private RefundItemRepository rIRepo;

	@Autowired
	private CouponRepository cRepo;

	@Autowired
	private ShopRepository shopRepo;

	@Autowired
	private VNPayService vnpayService;

	@Autowired
	private PaymentRepository payRepo;
	
	@Autowired
	private ShipmentRepository shipmentRepo;

	public List<RefundOrderDTO> getRefunByUser(int userID, int pageSize, int pageNumber, String status) {

		return rRRepo.fillterRefundByBuyerId(userID ,status,pageNumber, pageSize );
	}

	public Map<String, Object> getRefunByShop(int shopID, int pageSize, int pageNumber, String status, String keyword,
			Date date) {
		if ("ALL".equals(status)) {
			status = null;
		}
		return rRRepo.filterRefundByShopId(pageNumber, pageSize, status, shopID, keyword, date);
	}
	public Map<String, Object> getRefunByAdmin(int pageSize, int pageNumber, String status, String keyword,
			Date date) {
		if ("ALL".equals(status)) {
			status = null;
		}
		return rRRepo.filterRefundByAdmin(pageNumber, pageSize, status, keyword, date);
	}

	public RefundRequest getRefunByID(int id) {

		return rRRepo.findById(id);
	}
	public RefundRequest getRefundByOrderID(String id) {
		
		return rRRepo.findByOrder(id);
	}

	public RefundView getRefundViewByID(int id) {
		if(id <= 0) {
			return null;
		}
		return rRRepo.getRefundById(id);
	}
	public RefundView getRefundViewByIdOrder(String idOrder) {
		if(idOrder.isEmpty()) {
			return null;
		}
		return rRRepo.findViewByOrder(idOrder);
	}
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> createRefund(String orderId, String reason, List<MultipartFile> files, int userId,
			String itemsJson) throws IOException {
		Map<String, Object> res = new HashMap<>();
		Map<String, String> errors = new HashMap<>();

// ========== VALIDATE INPUT ==========
		if (reason == null || reason.trim().isEmpty()) {
			errors.put("Reason", "Vui lòng nhập lý do hoàn tiền");
		}
		if (files == null || files.isEmpty()) {
			errors.put("Files", "Vui lòng đính kèm bằng chứng (ảnh hoặc video).");
		}
		if (itemsJson == null || itemsJson.trim().isEmpty()) {
			errors.put("Items", "Vui lòng chọn sản phẩm hoàn trả.");
		}
		if (!errors.isEmpty()) {
			res.put("success", false);
			res.put("errors", errors);
			return res;
		}

// ========== KIỂM TRA ORDER ==========
		Order order = orderRepository.getOrderById(orderId);
		if (order == null) {
			res.put("message", "Đơn hàng không tồn tại");
			res.put("success", false);
			return res;
		}
		if (order.getBuyerId() != userId) {
			res.put("message", "Bạn không có quyền với đơn hàng này");
			res.put("success", false);
			return res;
		}
		if (order.getStatus() != 5) { // 5 = completed
			res.put("message", "Đơn hàng chưa được phép hoàn tiền");
			res.put("success", false);
			return res;
		}

		// ========== KIỂM TRA REFUND CŨ ==========
		RefundRequest oldRefund = rRRepo.findByOrder(orderId);
		if (oldRefund != null) {
			switch (oldRefund.getStatus()) {
			case "PENDING", "SHOP_APPROVED", "ESCALATED", "PROCESSING_REFUND" -> {
				res.put("message", "Đơn hàng này đang có yêu cầu hoàn tiền, vui lòng chờ xử lý.");
				res.put("success", false);
				return res;
			}
			case "SHOP_REJECTED" -> {
				boolean updated = rRRepo.updateStatusByBuyer(oldRefund.getRefundId(),
						RefundStatus.ESCALATED.toString());
				res.put("message", updated ? "Đơn hàng này đang được escalated lên Admin."
						: "Yêu cầu escalated thất bại, vui lòng thử lại.");
				res.put("success", updated);
				return res;
			}
			case "ADMIN_APPROVED", "ADMIN_REJECTED", "REFUNDED" -> {
				res.put("message", "Đơn hàng này đã kết thúc quy trình hoàn tiền.");
				res.put("success", false);
				return res;
			}
			default -> {
				res.put("message", "Trạng thái refund không hợp lệ.");
				res.put("success", false);
				return res;
			}
			}
		}

// ========== UPLOAD FILE ==========
		List<String> urls;
		try {
			urls = saveFiles(files);
		} catch (Exception e) {
			res.put("message", "Upload file thất bại: " + e.getMessage());
			res.put("errors", "File");
			res.put("success", false);
			return res;
		}

// ========== PARSE ITEMS ==========
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> itemList = mapper.readValue(itemsJson, List.class);

		List<RefundItem> refundItems = new ArrayList<>();
		double rawRefundTotal = 0.0;
		double remainingTotalAmount2 = 0.0;
		double remainingTotalAmount1 = 0.0;
		
		var refundType = "FULL";
		for (Map<String, Object> item : itemList) {
			long itemId = Long.parseLong(item.get("itemId").toString());
			int qtyRefund = Integer.parseInt(item.get("quantity").toString());

			OrderItem orderItem = oItemRepo.getOrderItemById((int) itemId);
			if (orderItem == null) {
				res.put("message", "Không tìm thấy sản phẩm trong đơn hàng này.");
				res.put("success", false);
				return res;
			}

			if (qtyRefund < 1 || qtyRefund > orderItem.getQuantity()) {
				res.put("message", "Số lượng hoàn trả không hợp lệ.");
				res.put("success", false);
				return res;
			}

			double UnitTotalItem2 = orderItem.getUnitPrice() * qtyRefund;
			
			remainingTotalAmount2 += UnitTotalItem2;
			
			RefundItem rItem = new RefundItem();
			rItem.setOrderItemId(orderItem.getOrderItemId());
			rItem.setQuantity(qtyRefund);
			refundItems.add(rItem);
		}
		
		var oldOrderItem = oItemRepo.getByOrderID(orderId);
		if(oldOrderItem != null) {
			for (var oI : oldOrderItem) {
				double UnitTotalItem1 = oI.getUnitPrice() * oI.getQuantity() ;
				remainingTotalAmount1 += UnitTotalItem1;
			}
			
		}

// ========== CHECK VOUCHER ==========
		double lostVoucherShop = 0.0;
		double lostVoucherPlatform = 0.0;
		double lostFreeShip = order.getShippingFee();

		var discountShop = oDRepo.getOrderDiscountByTypeCoupon(orderId, "SHOP");
		var discountPlatform = oDRepo.getOrderDiscountByTypeCoupon(orderId, "PLATFORM");
		var discountFreeship = oDRepo.getOrderDiscountByTypeCoupon(orderId, "FREESHIP");

		if (discountFreeship != null) {
			lostFreeShip -= discountFreeship.getDiscountValue();
		}
		double remainingAmount = remainingTotalAmount1 - remainingTotalAmount2;
		
		double finalRefundTotal = 0.0;
		double rowRefundTotal = remainingAmount;
		if (remainingAmount > 0.0) {
			refundType = "PARTIAL";
			if (discountShop != null) {
				var co = cRepo.findById(discountShop.getCouponId());
				if(!co.isEmpty()) {
					var coupon = co.get();
					if (rowRefundTotal > coupon.getMinOrderAmount().doubleValue()) {
						if("FIXED".equals(coupon.getDiscountType())) {
							
							remainingAmount = remainingAmount - coupon.getDiscountValue().doubleValue();
						} else {
							remainingAmount = remainingAmount - (remainingAmount * coupon.getDiscountValue().doubleValue() / 100);
						}

						
					} else {
						lostVoucherShop = discountShop.getDiscountValue();
					}
				} else {
					lostVoucherShop = discountShop.getDiscountValue();
				}
				
			}
			if (discountPlatform != null) {
				var co = cRepo.findById(discountPlatform.getCouponId());
				if(!co.isEmpty()) {
					var coupon = co.get();
					if (rowRefundTotal >= coupon.getMinOrderAmount().doubleValue()) {
						if("FIXED".equals(coupon.getDiscountType())) {
							remainingAmount = remainingAmount - coupon.getDiscountValue().doubleValue();
						} else {
							remainingAmount = remainingAmount - (remainingAmount * coupon.getDiscountValue().doubleValue() / 100);
						}

						
					} else {
						lostVoucherPlatform = discountPlatform.getDiscountValue();
					}
				} else {
					lostVoucherPlatform = discountPlatform.getDiscountValue();
				}
				
			}

			// ========== PHÂN BỔ THEO TỶ LỆ ==========

//			
			finalRefundTotal = order.getTotalAmount() - lostFreeShip - remainingAmount;
			if (finalRefundTotal > 0) {
				for (RefundItem rItem : refundItems) {
		        // tính tổng tiền gốc của item (chưa trừ voucher)
		        OrderItem orderItem = oItemRepo.getOrderItemById(rItem.getOrderItemId());
		        double raw = orderItem.getUnitPrice() * rItem.getQuantity();
		        double ratio = raw / remainingTotalAmount2; // tỷ lệ đóng góp của item trong tổng refund

		        // số tiền refund thực tế cho item
		        double refundPerItem = finalRefundTotal * ratio;

		        // đảm bảo không âm
		        if (refundPerItem < 0) refundPerItem = 0.0;
		        
		        rItem.setRefundAmount(refundPerItem);
		    }
			}
		} else {
			finalRefundTotal = order.getTotalAmount() - lostFreeShip;
		}

// ========== LƯU REFUND REQUEST ==========
		
		RefundRequest refund = new RefundRequest();
		refund.setOrderId(orderId);
		refund.setReason(reason);
		refund.setAmount(finalRefundTotal);
		refund.setBuyerId(order.getBuyerId());
		refund.setShopId(order.getShopId());
		refund.setStatus(RefundStatus.PENDING.toString());
		refund.setEvidence(String.join(",", urls));
		refund.setCreatedAt(LocalDateTime.now());
		refund.setUpdatedAt(LocalDateTime.now());
		refund.setOriginalAmount(order.getTotalAmount());
		refund.setRefundMethod(order.getPaymentMethod());
		if(!"COD".equals(order.getPaymentMethod())) {
			var payment = payRepo.findByOrderIdAndPayment(orderId);
			if(payment != null) {
				refund.setPaymentId(payment.getPaymentId());
			}
		}
		refund.setRefundType(refundType);

		var created = rRRepo.create(refund);
		try {
			if (created != null) {
				if("FULL".equals(refund.getRefundType())) {
					for (RefundItem rItem : refundItems) {
						var oI = oItemRepo.getOrderItemById(rItem.getOrderItemId());
						rItem.setRefundAmount(oI.getFinalPrice());
						rItem.setRefundId(created);
						var a = rIRepo.create(rItem);
						if (!a) {
							throw new RuntimeException("Không thể lưu RefundItem cho OrderItemID = " + rItem.getOrderItemId());
						}
					}
				} else {
					for (RefundItem rItem : refundItems) {
						rItem.setRefundId(created);
						var a = rIRepo.create(rItem);
						if (!a) {
							throw new RuntimeException("Không thể lưu RefundItem cho OrderItemID = " + rItem.getOrderItemId());
						}
					}
				}

				
				res.put("message", "Yêu cầu hoàn tiền thành công.");
				res.put("success", true);
				res.put("refundAmount", finalRefundTotal);
			} else {
				deleteFiles(urls);
				res.put("message", "Yêu cầu hoàn tiền thất bại, vui lòng thử lại sau.");
				res.put("success", false);
			}
		} catch (Exception e) {
			deleteFiles(urls);
			res.put("message", "Yêu cầu hoàn tiền thất bại, vui lòng thử lại sau.");
			res.put("success", false);
		}

		return res;
	}

	public Map<String, Object> updateRefundStatusByBuyer(int refundId, String newStatus, int userId) {
		Map<String, Object> res = new HashMap<>();

		var refund = rRRepo.findByIdAndBuyer(refundId, userId);
		if (refund == null) {
			res.put("success", false);
			res.put("message", "Update status failed, The refund does not exist. ");
			return res;
		}

		var statusCode = RefundStatus.fromName(newStatus);

		if (statusCode == null) {
			res.put("success", false);
			res.put("message", "Update Status Failed");
			return res;
		}
		var rs = false;
		if ("WAITING_FOR_RETURN".equals(refund.getStatus())) {

			if ("RETURNING".equals(newStatus)) {
				rs = rRRepo.updateStatusByBuyer(refund.getRefundId(), newStatus);
				if (rs) {
					res.put("success", true);
					res.put("message", "Update susscess " + newStatus + " to " + refund.getStatus());
				} else {
					res.put("success", false);
					res.put("message", "Cann't update " + newStatus + " to " + refund.getStatus());
				}
			}

			return res;
		}
		if ("PENDING".equals(refund.getStatus())) {

			if ("CANCEL".equals(newStatus)) {
				rs = rRRepo.updateStatusByBuyer(refund.getRefundId(), newStatus);
				if (rs) {
					res.put("success", true);
					res.put("message", "Update susscess " + newStatus + " to " + refund.getStatus());
				} else {
					res.put("success", false);
					res.put("message", "Cann't update " + newStatus + " to " + refund.getStatus());
				}
			}

			return res;
		}
		if ("SHOP_REJECTED".equals(refund.getStatus())) {
			
			if ("CANCEL".equals(newStatus)) {
				rs = rRRepo.updateStatusByBuyer(refund.getRefundId(), newStatus);
				if (rs) {
					res.put("success", true);
					res.put("message", "Update susscess " + newStatus + " to " + refund.getStatus());
				} else {
					res.put("success", false);
					res.put("message", "Cann't update " + newStatus + " to " + refund.getStatus());
				}
			}
			if ("ESCALATED".equals(newStatus)) {
				rs = rRRepo.updateStatusByBuyer(refund.getRefundId(), newStatus);
				if (rs) {
					res.put("success", true);
					res.put("message", "Update susscess " + newStatus + " to " + refund.getStatus());
				} else {
					res.put("success", false);
					res.put("message", "Cann't update " + newStatus + " to " + refund.getStatus());
				}
			}
			
			return res;
		}
		

		res.put("success", false);
		res.put("message", "Cann't update " + newStatus + " to " + refund.getStatus());
		return res;
	}

	
//	public Map<String, Object> updateRefundStatusByShop1(
//	        int refundId, String newStatus, int userId, String note, Boolean isReturn) {
//
//	    Map<String, Object> res = new HashMap<>();
//
//	    // ✅ Kiểm tra trạng thái hợp lệ
//	    if (!List.of("APPROVED", "REJECTED", "RETURNED").contains(newStatus)) {
//	        res.put("success", false);
//	        res.put("message", "Invalid refund status: " + newStatus);
//	        return res;
//	    }
//
//	    // ✅ Tìm shop
//	    var shop = shopRepo.findByUserId(userId);
//	    if (shop == null) {
//	        res.put("success", false);
//	        res.put("message", "Shop not found for user " + userId);
//	        return res;
//	    }
//
//	    // ✅ Tìm refund
//	    var refund = rRRepo.findByIdAndShop(refundId, shop.getShopId());
//	    if (refund == null) {
//	        res.put("success", false);
//	        res.put("message", "Refund request not found.");
//	        return res;
//	    }
//
//	    // ✅ Lấy thông tin thanh toán nếu không phải COD
//	    Payment payment = null;
//	    boolean isCOD = "COD".equalsIgnoreCase(refund.getRefundMethod());
//	    if (!isCOD) {
//	        Long paymentId = (long) refund.getPaymentId();
//	        if (paymentId != null) {
//	            payment = payRepo.findById(paymentId);
//	        }
//	        if (payment == null) {
//	            res.put("success", false);
//	            res.put("message", "Payment record not found for refund " + refundId);
//	            return res;
//	        }
//	    }
//
//	    boolean rs = false;
//
//	    try {
//	        // =============================
//	        // 🔹 1. Trạng thái PENDING
//	        // =============================
//	        if ("PENDING".equals(refund.getStatus())) {
//	            switch (newStatus) {
//	                case "APPROVED" -> {
//	                    refund.setProcessedBy(userId);
//	                    refund.setProcessDate(LocalDateTime.now());
//	                    refund.setStatus("SHOP_APPROVED");
//	                    rs = rRRepo.updateStatusByShop(refund);
//	                    
//	                    if(rs) {
//	                    	if (Boolean.TRUE.equals(isReturn)) {
//		                        // Chờ người mua trả hàng
//		                        refund.setStatus("WAITING_FOR_RETURN");
//		                        rs = rRRepo.updateStatusByShop(refund);
//		                        
//		                        if(rs) {
//		                        	 try {
//		 	                        	var order = orderRepository.getOrderById(refund.getOrderId());
//		 	                        	if(order == null) {
//		 	                        		rs = false;
//		 	                        	}
//		 	    	                   	var shipment = new Shipment();
//		 	    	                   	Address shopAd = Optional.ofNullable(shop.getAddress()).filter(a -> !a.trim().isEmpty()).map(a -> {
//		 	    	        				try {
//		 	    	        					return mapper.readValue(a, Addresses.class).getShopAddresses().getWarehouseAddress();
//		 	    	        				} catch (Exception e) {
//		 	    	        					return null;
//		 	    	        				}
//		 	    	        			}).orElse(null);
//		 	    	                   	var add = order.getDeliveryAddressJson();
//		 	    	                   	shipment.setPickupAddress(add.getStreet() + ", " + add.getWardName() + ", " +
//		 	    	                   			add.getDistrictName() + ", " + add.getProvinceName()); // addressShop
//		 	    	                   	shipment.setDeliveryAddress(shopAd.getStreet() + ", " + shopAd.getWardName() + ", " +
//		 	    	                   			shopAd.getDistrictName() + ", " + shopAd.getProvinceName()
//		 	    	                   			
//		 	    	                   			); // addressBuyer
//		 	    	                   	
//		 	    	                   	var shipmentId = "SHP" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
//		 	    	                   	shipment.setShipmentID(shipmentId);
//		 	    	   					var c = shipmentRepo.insertByRefund(shipment,order.getBuyerId(), refundId);
//		 	    	   					if(!c) {
//		 	    	   						rs = !c;
//		 	    	   					}
//		 	    	   				} catch (Exception e) {
//		 	    	   					rs = false;
//		 	    	   				} 
//		                        } 
//		                       
//		                        
//		                    } else {
//		                        // Không cần trả hàng, hoàn tiền ngay
//		                        refund.setStatus("PROCESSING_REFUND");
//		                        rs = rRRepo.updateStatusByShop(refund);
//
//		                        var o = orderRepository.getOrderById(refund.getOrderId());
//		                        if (rs && !isCOD) {
//		                            refund.setStatus("REFUNDED");
//
//	                                rs = rRRepo.updateStatusByShop(refund); 
//	                                
//		                        } else if (rs && isCOD) {
//		                            refund.setStatus("REFUNDED");
//		                            rs = rRRepo.updateStatusByShop(refund);
//		                        }
//		                        if(rs) {
//		                        	 if (rs) {
//		                        	        var check = handleAfterRefunded(refund);
//		                        	        if(!check) {
//		                        	        	res.put("success", false);
//		            	         	            res.put("message", "Cannot update " + refund.getStatus() + " to " + newStatus);
//		            	         	            return res;
//		                        	        }
//		                        	    }
//		                        }
//		                        
//		                        
//		                    }
//	                    } else {
//	                    	res.put("success", false);
//	         	            res.put("message", "Cannot update " + refund.getStatus() + " to " + newStatus);
//	         	            return res;
//	                    }
//	                    
//	                }
//	                case "REJECTED" -> {
//	                    refund.setNotes(note);
//	                    refund.setStatus("SHOP_REJECTED");
//	                    rs = rRRepo.updateStatusByShop(refund);        
//	                }
//	            }
//	        }
//
//	        // =============================
//	        // 🔹 3. Các trạng thái không hợp lệ
//	        // =============================
//	        else {
//	            res.put("success", false);
//	            res.put("message", "Cannot update " + refund.getStatus() + " to " + newStatus);
//	            return res;
//	        }
//
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        res.put("success", false);
//	        res.put("message", "Exception: " + e.getMessage());
//	        return res;
//	    }
//
//	    // ✅ Kết quả cuối cùng
//	    if (rs) {
//	        res.put("success", true);
//	        res.put("message", "Updated refund status to " + refund.getStatus() + " successfully.");
//	    } else {
//	        res.put("success", false);
//	        res.put("message", "Failed to update refund status to " + newStatus);
//	    }
//
//	    return res;
//	}
	
	@Transactional
	public Map<String, Object> updateRefundStatusByShop(
	        int refundId, String newStatus, int userId, String note, Boolean isReturn) {

	    Map<String, Object> res = new HashMap<>();

	    try {
	        // =============================
	        // VALIDATION
	        // =============================
	        if (!List.of("APPROVED", "REJECTED", "RETURNED").contains(newStatus)) {
	            throw new IllegalArgumentException("Invalid refund status: " + newStatus);
	        }

	        // Tìm shop
	        var shop = shopRepo.findByUserId(userId);
	        if (shop == null) {
	            throw new IllegalArgumentException("Shop not found");
	        }

	        // Tìm refund
	        var refund = rRRepo.findByIdAndShop(refundId, shop.getShopId());
	        if (refund == null) {
	            throw new IllegalArgumentException("Refund request not found.");
	        }

	        // Kiểm tra trạng thái hiện tại
	        if (!"PENDING".equals(refund.getStatus())) {
	            throw new IllegalStateException("Cannot update " + refund.getStatus() + " to " + newStatus);
	        }

	        // Lấy thông tin thanh toán nếu không phải COD
	        Payment payment = null;
	        boolean isCOD = "COD".equalsIgnoreCase(refund.getRefundMethod());
	        
	        if (!isCOD) {
	            Long paymentId = (long) refund.getPaymentId();
	            if (paymentId != null) {
	                payment = payRepo.findById(paymentId);
	            }
	            if (payment == null) {
	                throw new IllegalStateException("Payment record not found for refund " + refundId);
	            }
	        }

	        // =============================
	        // PROCESS BY STATUS
	        // =============================
	        switch (newStatus) {
	            case "APPROVED" -> handleApproved(refund, userId, shop, isReturn, isCOD);
	            case "REJECTED" -> handleRejected(refund, note);
	            default -> throw new IllegalArgumentException("Unsupported status: " + newStatus);
	        }

	        // Success response
	        res.put("success", true);
	        res.put("message", "Updated refund status to " + refund.getStatus() + " successfully.");
	        return res;

	    } catch (IllegalArgumentException | IllegalStateException e) {
	        // Validation/Business logic errors - rollback tự động
	        res.put("success", false);
	        res.put("message", e.getMessage());
	        return res;

	    } catch (Exception e) {
	        // System errors - rollback tự động
	        e.printStackTrace();
	        res.put("success", false);
	        res.put("message", "System error: " + e.getMessage());
	        return res;
	    }
	}

	// =============================
	// HELPER METHODS
	// =============================

	private void handleApproved(RefundRequest refund, int userId, ShopView shop, Boolean isReturn, boolean isCOD) {
	    // Update to SHOP_APPROVED
	    refund.setProcessedBy(userId);
	    refund.setProcessDate(LocalDateTime.now());
	    refund.setStatus("SHOP_APPROVED");
	    
	    boolean rs = rRRepo.updateStatusByShop(refund);
	    if (!rs) {
	        throw new RuntimeException("Failed to update status to SHOP_APPROVED");
	    }

	    if (Boolean.TRUE.equals(isReturn)) {
	        handleReturnFlow(refund, shop);
	    } else {
	        handleDirectRefundFlow(refund, isCOD);
	    }
	}

	private void handleReturnFlow(RefundRequest refund, ShopView shop) {
	    // Chờ người mua trả hàng
	    refund.setStatus("WAITING_FOR_RETURN");
	    boolean rs = rRRepo.updateStatusByShop(refund);
	    if (!rs) {
	        throw new RuntimeException("Failed to update status to WAITING_FOR_RETURN");
	    }

	    // Tạo shipment cho việc trả hàng
	    var order = orderRepository.getOrderById(refund.getOrderId());
	    if (order == null) {
	        throw new RuntimeException("Order not found for refund " + refund.getRefundId());
	    }

	    // Lấy địa chỉ kho của shop
	    Address shopAddress = getShopWarehouseAddress(shop);
	    if (shopAddress == null) {
	        throw new RuntimeException("Shop warehouse address not found");
	    }

	    // Tạo shipment
	    var shipment = createReturnShipment(order, shopAddress);
	    boolean created = shipmentRepo.insertByRefund(shipment, order.getBuyerId(), refund.getRefundId());
	    
	    if (!created) {
	        throw new RuntimeException("Failed to create return shipment");
	    }
	}

	private void handleDirectRefundFlow(RefundRequest refund, boolean isCOD) {
	    // Không cần trả hàng, hoàn tiền ngay
	    refund.setStatus("PROCESSING_REFUND");
	    boolean rs = rRRepo.updateStatusByShop(refund);
	    if (!rs) {
	        throw new RuntimeException("Failed to update status to PROCESSING_REFUND");
	    }

	    var order = orderRepository.getOrderById(refund.getOrderId());
	    if (order == null) {
	        throw new RuntimeException("Order not found for refund " + refund.getRefundId());
	    }

	    // Update to REFUNDED
	    refund.setStatus("REFUNDED");
	    rs = rRRepo.updateStatusByShop(refund);
	    if (!rs) {
	        throw new RuntimeException("Failed to update status to REFUNDED");
	    }

	    // Xử lý sau khi hoàn tiền
	    handleAfterRefunded(refund);
	}

	private void handleRejected(RefundRequest refund, String note) {
	    refund.setNotes(note);
	    refund.setStatus("SHOP_REJECTED");
	    
	    boolean rs = rRRepo.updateStatusByShop(refund);
	    if (!rs) {
	        throw new RuntimeException("Failed to update status to SHOP_REJECTED");
	    }
	}

	private Address getShopWarehouseAddress(ShopView shop) {
	    return Optional.ofNullable(shop.getAddress())
	        .filter(a -> !a.trim().isEmpty())
	        .map(a -> {
	            try {
	                return mapper.readValue(a, Addresses.class)
	                    .getShopAddresses()
	                    .getWarehouseAddress();
	            } catch (Exception e) {
	                throw new RuntimeException("Failed to parse shop address", e);
	            }
	        })
	        .orElse(null);
	}

	private Shipment createReturnShipment(Order order, Address shopAddress) {
	    var shipment = new Shipment();
	    var deliveryAddress = order.getDeliveryAddressJson();
	    
	    // Địa chỉ lấy hàng: nhà người mua
	    shipment.setPickupAddress(
	        deliveryAddress.getStreet() + ", " + 
	        deliveryAddress.getWardName() + ", " +
	        deliveryAddress.getDistrictName() + ", " + 
	        deliveryAddress.getProvinceName()
	    );
	    
	    // Địa chỉ giao: kho của shop
	    shipment.setDeliveryAddress(
	        shopAddress.getStreet() + ", " + 
	        shopAddress.getWardName() + ", " +
	        shopAddress.getDistrictName() + ", " + 
	        shopAddress.getProvinceName()
	    );
	    
	    String shipmentId = "SHP" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
	    shipment.setShipmentID(shipmentId);
	    
	    return shipment;
	}
	
	@Transactional
	public Map<String, Object> updateStatusByAdmin(int refundId, String newStatus, int userId, String note) {
	    Map<String, Object> res = new HashMap<>();
	    
	    try {
	        // Validation
	        if(note == null) {
	            throw new IllegalArgumentException("Please enter reason.");
	        }
	        
	        if (!List.of("APPROVED", "REJECTED", "RETURNED").contains(newStatus)) {
	            throw new IllegalArgumentException("Invalid refund status: " + newStatus);
	        }
	        
	        var refund = rRRepo.findById(refundId);
	        if(refund == null) {
	            throw new IllegalArgumentException("Refund Request not found");
	        }
	        
	        // Process APPROVED status
	        if("APPROVED".equals(newStatus)) {
	            // Update to ADMIN_APPROVED
	            refund.setProcessedByAdmin(userId);
	            refund.setStatus("ADMIN_APPROVED");
	            refund.setAdminApprovedAt(LocalDateTime.now());
	            refund.setAdminNotes(note);
	            
	            boolean rs = rRRepo.updateStatusByAdmin(refund);
	            if(!rs) {
	                throw new RuntimeException("Cannot update status to ADMIN_APPROVED");
	            }
	            
	            // Get order
	            var order = orderRepository.getOrderById(refund.getOrderId());
	            if(order == null) {
	                throw new RuntimeException("Order not found");
	            }
	            
	            // Handle after refunded
	            handleAfterRefunded(refund);
	            if(!rs) {
	                throw new RuntimeException("Failed to handle after refunded");
	            }
	            
	            // Update to REFUNDED
	            refund.setStatus("REFUNDED");
	            rs = rRRepo.updateStatusByAdmin(refund);
	            if(!rs) {
	                throw new RuntimeException("Cannot update status to REFUNDED");
	            }
	        }
	        if("REJECTED".equals(newStatus)) {
	        	refund.setProcessedByAdmin(userId);
	            refund.setStatus("ADMIN_REJECTED");
	            refund.setAdminRejectedAt(LocalDateTime.now());
	            refund.setAdminNotes(note);
	        	 boolean rs = rRRepo.updateStatusByAdmin(refund);
		            if(!rs) {
		                throw new RuntimeException("Cannot update status to REFUNDED");
		            }
	        }
	        
	        // TODO: Handle REJECTED and RETURNED status if needed
	        
	        res.put("success", true);
	        res.put("message", newStatus + " successful.");
	        return res;
	        
	    } catch (IllegalArgumentException e) {
	        // Validation errors - sẽ rollback tự động
	        res.put("success", false);
	        res.put("message", e.getMessage());
	        return res;
	        
	    } catch (RuntimeException e) {
	        // Business logic errors - sẽ rollback tự động
	        e.printStackTrace();
	        res.put("success", false);
	        res.put("message", e.getMessage());
	        return res;
	        
	    } catch (Exception e) {
	        // System errors - sẽ rollback tự động
	        e.printStackTrace();
	        res.put("success", false);
	        res.put("message", "System error");
	        return res;
	    }
	}
	
	private Map<String, Object> callVNPayRefund(RefundRequest refund, Payment payment, Integer shopId, Map<String, Object> res, String transactionType) {
	    Map<String, Object> refundResp = new HashMap<>();
	    try {
	        String refundRespStr = vnpayService.refund(
	                payment.getTransactionCode(),
	                payment.getPaymentDate().toString(),
	                payment.getGatewayTransactionNo(),
	                refund.getAmount(),
	                "Refund by shop",
	                "shop_" + shopId,
	                transactionType
	        );

	        refundResp = mapper.readValue(refundRespStr, Map.class);

	        if (!"00".equals(refundResp.get("RspCode"))) {
	            res.put("vnpay_error", refundResp.get("Message"));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        res.put("vnpay_error", e.getMessage());
	    }
	    return refundResp;
	}

	// Danh sách extension cho ảnh & video

	// Giới hạn dung lượng (bytes)
	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
	private static final long MAX_VIDEO_SIZE = 20 * 1024 * 1024; // 20 MB
	private static final long MAX_OTHER_SIZE = 10 * 1024 * 1024; // 10 MB

	private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
	private static final Set<String> VIDEO_EXT = Set.of("mp4", "avi", "mov", "mkv", "flv", "wmv");

	// Lưu file và trả về list path tương đối (để lưu DB)
	public List<String> saveFiles(List<MultipartFile> files) throws IOException {
		List<String> urls = new ArrayList<>();

		if (files == null || files.isEmpty())
			return urls;

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {

				// ✅ Kiểm tra dung lượng
				validateFileSize(file);

				// Xác định thư mục con (images/videos/others)
				String folder = detectFolder(file);

				// Đường dẫn tuyệt đối để lưu
				Path dirPath = Paths.get(BASE_UPLOAD_DIR, folder);
				Files.createDirectories(dirPath);

				// Tạo tên file duy nhất
				String fileName = UUID.randomUUID() + "_" + Objects.requireNonNull(file.getOriginalFilename());
				Path path = dirPath.resolve(fileName);

				// Lưu file vật lý
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				// ✅ DB chỉ lưu đường dẫn tương đối
				urls.add("/" + folder + "/" + fileName);
			}
		}
		return urls;
	}

	// Xác định thư mục lưu ảnh/video/others
	private String detectFolder(MultipartFile file) {
		String contentType = file.getContentType();
		String originalName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();

		boolean isImage = (contentType != null && contentType.startsWith("image/"))
				|| IMAGE_EXT.stream().anyMatch(originalName::endsWith);

		boolean isVideo = (contentType != null && contentType.startsWith("video/"))
				|| VIDEO_EXT.stream().anyMatch(originalName::endsWith);

		if (isImage) {
			return "images";
		} else if (isVideo) {
			return "videos";
		} else {
			return "others";
		}
	}

	// Kiểm tra dung lượng file
	private void validateFileSize(MultipartFile file) {
		String contentType = file.getContentType();
		String originalName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
		long size = file.getSize();

		boolean isImage = (contentType != null && contentType.startsWith("image/"))
				|| IMAGE_EXT.stream().anyMatch(originalName::endsWith);

		boolean isVideo = (contentType != null && contentType.startsWith("video/"))
				|| VIDEO_EXT.stream().anyMatch(originalName::endsWith);

		if (isImage && size > MAX_IMAGE_SIZE) {
			throw new IllegalArgumentException("Ảnh " + originalName + " vượt quá dung lượng tối đa (5MB).");
		}
		if (isVideo && size > MAX_VIDEO_SIZE) {
			throw new IllegalArgumentException("Video " + originalName + " vượt quá dung lượng tối đa (50MB).");
		}
		if (!isImage && !isVideo && size > MAX_OTHER_SIZE) {
			throw new IllegalArgumentException("File " + originalName + " vượt quá dung lượng tối đa (10MB).");
		}
	}

	// Xóa file dựa trên đường dẫn tương đối (đọc từ DB)
	public void deleteFiles(List<String> urls) {
		if (urls == null || urls.isEmpty())
			return;

		for (String url : urls) {
			try {
				// url trong DB: /images/xxx.png → bỏ dấu "/"
				String relativePath = url.startsWith("/") ? url.substring(1) : url;

				// Ghép với BASE_UPLOAD_DIR để ra đường dẫn thật
				Path path = Paths.get(BASE_UPLOAD_DIR, relativePath);

				if (Files.exists(path)) {
					Files.delete(path);
					System.out.println("🗑️ Đã xoá file: " + path);
				}
			} catch (Exception e) {
				System.err.println("❌ Không thể xoá file: " + url + " - " + e.getMessage());
			}
		}
	}
	
	@Transactional
	public void handleAfterRefunded(RefundRequest refund) {
	    // Lấy thông tin order
	    var order = orderRepository.getOrderById(refund.getOrderId());
	    if (order == null) {
	        throw new IllegalStateException("Order not found: " + refund.getOrderId());
	    }

	    // Lấy danh sách items
	    var orderItems = oItemRepo.getAllByOrderId(order.getOrderId());
	    if (orderItems == null || orderItems.isEmpty()) {
	        throw new IllegalStateException("Order items not found for order: " + order.getOrderId());
	    }

	    // Cập nhật trạng thái inventory
	    var check = orderRepository.updateStatusInven(8, refund.getOrderId(), null);
	    if (check <= 0) {
	        throw new RuntimeException("Failed to update inventory status for order: " + refund.getOrderId());
	    }

	    // Nếu là hoàn tiền toàn bộ -> Không cần tính lại discount
	    if ("FULL".equals(refund.getRefundType())) {
	        return;
	    }

	    // Xử lý hoàn tiền một phần
	    recalculatePartialRefund(refund, orderItems);
	}

	/**
	 * Tính lại discount và phân bổ lại cho hoàn tiền một phần
	 */
	private void recalculatePartialRefund(RefundRequest refund, List<OrderItem> orderItems) {
	    // Lấy các discount
	    var discountShop = oDRepo.getOrderDiscountByTypeCoupon(refund.getOrderId(), "SHOP");
	    var discountPlatform = oDRepo.getOrderDiscountByTypeCoupon(refund.getOrderId(), "PLATFORM");
	    
	    if(discountShop == null && discountPlatform == null) {
	    	return;
	    }

	    // Tính tổng tiền của các item còn lại (chưa refund hoàn toàn)
	    double newSubtotal = orderItems.stream()
	            .filter(i -> i.getItemRefundStatus() != 2) // Không phải item đã refund
	            .mapToDouble(i -> (i.getQuantity() - i.getQtyRefunded()) * i.getUnitPrice())
	            .sum();

	    
	    // Nếu không còn item nào -> Reset discount và return
	    if (newSubtotal <= 0) {
	        resetAllItemDiscounts(orderItems);
	        removeInvalidDiscounts(discountShop, discountPlatform);
	        return;
	    }

	    double newDiscountTotal = 0.0;

	    // Tính lại discount cho shop
	    if (discountShop != null) {
	        double shopDiscount = recalcDiscount(discountShop, newSubtotal);
	        newDiscountTotal += shopDiscount;
	    }

	    // Tính lại discount cho platform
	    if (discountPlatform != null) {
	        double platformDiscount = recalcDiscount(discountPlatform, newSubtotal);
	        newDiscountTotal += platformDiscount;
	    }
	    
	    

	    // Phân bổ lại discount cho các item còn lại
	    if (newDiscountTotal > 0) {
	        redistributeDiscounts(orderItems, newSubtotal, newDiscountTotal);
	    } else {
	        resetAllItemDiscounts(orderItems);
	    }
	}

	/**
	 * Tính lại discount dựa trên subtotal mới
	 */
	private double recalcDiscount(OrderDiscount discount, double newSubtotal) {
	    if (discount == null) {
	        return 0.0;
	    }

	    // Lấy thông tin coupon
	    var couponOpt = cRepo.findById(discount.getCouponId());
	    if (couponOpt.isEmpty()) {
	        // Coupon không tồn tại -> Xóa discount
	        oDRepo.deleteDisCount(discount);
	        return 0.0;
	    }

	    var coupon = couponOpt.get();

	    // Kiểm tra điều kiện tối thiểu
	    if (newSubtotal < coupon.getMinOrderAmount().doubleValue()) {
	        // Không đủ điều kiện -> Xóa discount
	        oDRepo.deleteDisCount(discount);
	        return 0.0;
	    }

	    double discountValue;

	    if ("FIXED".equals(coupon.getDiscountType())) {
	        // Giảm giá cố định
	        discountValue = coupon.getDiscountValue().doubleValue();
	        
	        // Không cho phép discount lớn hơn subtotal
	        if (discountValue >= newSubtotal) {
	            // Xóa discount không hợp lệ
	            oDRepo.deleteDisCount(discount);
	            return 0.0;
	        }
	    } else {
	        // Giảm giá theo phần trăm
	        discountValue = newSubtotal * coupon.getDiscountValue().doubleValue() / 100.0;
	        
	        // Cập nhật giá trị discount mới
	        discount.setDiscountValue(discountValue);
	        boolean updated = oDRepo.updateDisCountValue(discount);
	        
	        if (!updated) {
	            throw new RuntimeException("Failed to update discount value for discount: " + discount.getOrderDiscountId());
	        }
	    }

	    return discountValue;
	}

	/**
	 * Phân bổ lại discount cho các item còn lại theo tỷ lệ
	 */
	private void redistributeDiscounts(List<OrderItem> orderItems, double newSubtotal, double totalDiscount) {
	    for (var item : orderItems) {
	        // Chỉ xử lý item chưa refund hoàn toàn
	        if (item.getItemRefundStatus() == 2) {
	            continue;
	        }

	        // Tính tỷ lệ của item trong tổng giá trị
	        double itemValue = (item.getQuantity() - item.getQtyRefunded()) * item.getUnitPrice();
	        double itemRatio = itemValue / newSubtotal;

	        // Phân bổ discount theo tỷ lệ
	        double itemDiscount = totalDiscount * itemRatio;
	        item.setDiscountAllocated(itemDiscount);

	        // Cập nhật vào database
	        boolean updated = oItemRepo.updateDiscountAllocated(item);
	        if (!updated) {
	            throw new RuntimeException("Failed to update discount allocated for item: " + item.getOrderItemId());
	        }
	    }
	}

	/**
	 * Reset discount = 0 cho tất cả items
	 */
	private void resetAllItemDiscounts(List<OrderItem> orderItems) {
	    for (var item : orderItems) {
	        item.setDiscountAllocated(0.0);
	        boolean updated = oItemRepo.updateDiscountAllocated(item);
	        
	        if (!updated) {
	            throw new RuntimeException("Failed to reset discount for item: " + item.getOrderItemId());
	        }
	    }
	}

	/**
	 * Xóa các discount không hợp lệ
	 */
	private void removeInvalidDiscounts(OrderDiscount... discounts) {
	    for (var discount : discounts) {
	        if (discount != null) {
	            boolean removed = oDRepo.deleteDisCount(discount);
	            if (!removed) {
	                throw new RuntimeException("Failed to remove invalid discount: " + discount.getOrderDiscountId());
	            }
	        }
	    }
	}
	
	public List<RefundItemView> getRefundItemViewByRefundId(int id){
		
		return rIRepo.getViewAllByRefundId(id);
	}
}
