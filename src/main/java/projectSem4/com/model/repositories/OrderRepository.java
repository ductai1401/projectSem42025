package projectSem4.com.model.repositories;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Order;
import projectSem4.com.model.entities.RefundRequest;
import projectSem4.com.model.modelViews.OrderDiscountView;
import projectSem4.com.model.modelViews.OrderItemView;
import projectSem4.com.model.modelViews.OrderView;


@Repository
public class OrderRepository {
	private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private RefundRequestRepository rRRepo;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ Order initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho Order
	private RowMapper<Order> rowMapperForOrder = new RowMapper<Order>() {
		@Override
		public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer BuyerID = rs.getInt("BuyerID");
			Integer ShopID = rs.getInt("ShopID");
			String orderId = rs.getString("OrderID");
			Double totalAmount = rs.getDouble("TotalAmount");
			Integer FlashSaleStatus = rs.getInt("FlashSaleStatus");
			String PaymentMethod = rs.getString("PaymentMethod");
			Integer status = rs.getInt("Status");
			LocalDateTime orderDate = rs.getTimestamp("OrderDate").toLocalDateTime();
			Double Refund = rs.getDouble("Refund");
			Double ShippingFee = rs.getDouble("ShippingFee");
			var addressJson = rs.getString("DeliveryAddressJson");
			Address addressbuyer = null;
			if(addressJson != null) {
				try {
   					addressbuyer  = mapper.readValue(addressJson, Address.class);
   				} catch (Exception e) {
   					addressbuyer = null;
   				}
			}
			String orderNote = rs.getString("OrderNote");
			String cancelReason = rs.getString("CancelReason");
			Timestamp cancelledAt = rs.getTimestamp("CancelledAt");
			Timestamp confirmedAt = rs.getTimestamp("ConfirmedAt") ;
			Timestamp completedAt = rs.getTimestamp("CompletedAt");
   				
   			

			// Map dữ liệu và tạo đối tượng Order
			Order Order = new Order(orderId,BuyerID,ShopID,orderDate,totalAmount,status,
					PaymentMethod,FlashSaleStatus,Refund,ShippingFee, addressbuyer,
					orderNote,cancelReason,cancelledAt == null ? null : cancelledAt.toLocalDateTime(),
							confirmedAt == null ? null : confirmedAt.toLocalDateTime(),
									completedAt == null ? null : completedAt.toLocalDateTime()		
					);
			
			return Order;
		}
	};

	// Tạo một Order mới
	public Map<String, Object> createOrder(Order Order) {
		Map<String, Object> a = new HashMap<>();
		
		try {
            
            var addressJson = mapper.writeValueAsString(Order.getDeliveryAddressJson());
            
			
			String sql = "INSERT INTO Orders (OrderID ,BuyerID ,ShopID , OrderDate, TotalAmount, Status, "
					+ "PaymentMethod ,FlashSaleStatus ,Refund ,ShippingFee, DeliveryAddressJson) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			int rows = jdbcTemplate.update(sql,
					Order.getOrderId(),
					Order.getBuyerId(), Order.getShopId(),
					Order.getOrderDate(), Order.getTotalAmount(), Order.getStatus(), 
					Order.getPaymentMethod(),Order.getFlashSaleStatus(), Order.getRefund(), Order.getShippingFee(),
					addressJson
					);
			if(rows > 0) {
				a.put("message", "Order registration successful!");
				a.put("rows", rows);
			} else {
				a.put("message", "Order registration failed!");
				a.put("rows", rows);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			a.put("message", "Order registration failed!");
			a.put("rows", 0);
		}
		return a;
	}

	// Xóa Order theo ID
	public String deleteOrder(int OrderId) {
		try {
			String sql = "Update FROM Orders Set Status = ? WHERE OrderID = ?";
			int rows = jdbcTemplate.update(sql, 4, OrderId);
			return rows > 0 ? "Order deleted successfully!" : "Deleting the Order failed!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error when deleting Order : " + e.getMessage();
		}
	}

	
	
	public List<Order> findAllOrders() {
        try {
            String sql = "SELECT * FROM Orders ORDER BY OrderID DESC";
            return jdbcTemplate.query(sql, rowMapperForOrder);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả cửa hàng: " + e.getMessage());
            return List.of();
        }
    }

	// Tìm Order theo trang và số lượng
	public List<Order> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = "SELECT * FROM Orders ORDER BY OrderID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
			return jdbcTemplate.query(sql, rowMapperForOrder, offset, size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

//	 Tìm kiếm Order theo tên
	public Map<String, Object> fillterOrderByShopId(int page, int size, String keyword, Date date, String status, String payment, int shopId) {
	    Map<String, Object> res = new HashMap<>();
	    try {
	        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
	            .withProcedureName("FilterOrdersByShopId")
	            .declareParameters(
	                new SqlParameter("PageIndex", Types.INTEGER),
	                new SqlParameter("PageSize", Types.INTEGER),
	                new SqlParameter("ShopId", Types.INTEGER),
	                new SqlParameter("Keyword", Types.NVARCHAR),
	                new SqlParameter("PaymentMethod", Types.NVARCHAR),
	                new SqlParameter("Status", Types.NVARCHAR),
	                new SqlParameter("OrderDate", Types.DATE)
	            )
	            .returningResultSet("orders", new BeanPropertyRowMapper<>(OrderView.class))
	            .returningResultSet("meta", (rs, rowNum) -> {
	                Map<String, Object> meta = new HashMap<>();
	                meta.put("TotalRows", rs.getInt("TotalRows"));
	                meta.put("TotalPages", rs.getInt("TotalPages"));
	                return meta;
	            });

	        MapSqlParameterSource params = new MapSqlParameterSource()
	            .addValue("PageIndex", page)
	            .addValue("PageSize", size)
	            .addValue("Keyword", keyword == "" ? null : keyword)
	            .addValue("PaymentMethod", payment == "" ? null : payment)
	            .addValue("Status", status)
	            .addValue("ShopId", shopId)
	            .addValue("OrderDate", date != null ? new java.sql.Date(date.getTime()) : null);

	        Map<String, Object> result = call.execute(params);

	        @SuppressWarnings("unchecked")
	        List<OrderView> orders = (List<OrderView>) result.get("orders");

	        @SuppressWarnings("unchecked")
	        List<Map<String, Object>> metaList = (List<Map<String, Object>>) result.get("meta");

	        Map<String, Object> meta = metaList.isEmpty() ? new HashMap<>() : metaList.get(0);

	        res.put("orders", orders);
	        res.put("totalRows", meta.get("TotalRows"));
	        res.put("totalPages", meta.get("TotalPages"));
	        res.put("page", page);

	    } catch (Exception e) {
	        e.printStackTrace();
	        res = null;
	    }
	    return res;
	}


    // Lấy tên cửa hàng theo OrderId
    public String getOrderNameById(int OrderId) {
        try {
            String sql = "SELECT OrderName FROM Orders WHERE OrderID=?";
            return jdbcTemplate.queryForObject(sql, String.class, OrderId);
        } catch (Exception e) {
            System.err.println("Error when retrieving store name ID=" + OrderId + ": " + e.getMessage());
            return null;
        }
    }
    
    public int updateStatusInven(int status, String orderId, String reson) {
    	try {
    		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
			        .withProcedureName("UpdateOrderAndInventory")
			        .declareParameters(
			                new SqlParameter("OrderID", Types.NVARCHAR),
			                new SqlParameter("Action", Types.INTEGER),
			                new SqlParameter("CancelReson", Types.NVARCHAR),
			                new SqlOutParameter("ResultCode", Types.INTEGER)
			            );
			        

			    MapSqlParameterSource params = new MapSqlParameterSource()
			    		.addValue("OrderID", orderId)
			    		.addValue("CancelReson", reson)
			    		.addValue("Action", status);
			    Map<String, Object> result = call.execute(params);
			    
			    var a = (Integer) result.get("ResultCode");
			    if(a == null) {
			    	a = 0;
			    }
			    return a;
		} catch (Exception e) {
			 System.err.println("Failed to execute UpdateOrderAndInventory");
			    System.err.println("OrderID=" + orderId + ", Action=" + status);
			    e.printStackTrace();
			return 0;
		}
    }
    
    public int updateStatusPaymentSuccess(String paymentId, int Status) {
    	try {
    		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
			        .withProcedureName("SetOrderStatusByPaymentID")
			        .declareParameters(
			                new SqlParameter("PaymentID", Types.NVARCHAR),
			                new SqlParameter("Action", Types.INTEGER),
			                new SqlOutParameter("Result", Types.INTEGER)
			            );
			        

			    MapSqlParameterSource params = new MapSqlParameterSource()
			    		.addValue("PaymentID", paymentId)
			    		.addValue("Action", Status);
			    Map<String, Object> result = call.execute(params);
			    
			    var a = (Integer) result.get("Result");
			    if(a == null) {
			    	a = 0;
			    }
			    return a;
		} catch (Exception e) {
			 System.err.println("Failed to execute SetPendingAndReserveByPaymentID");
			    System.err.println("paymentId=" + paymentId);
			    e.printStackTrace();
			return 0;
		}
    }
    
    public Order getOrderByShop(String orderId ,int shopId) {
    	try {
            String sql = "SELECT * FROM Orders WHERE OrderID=? And ShopID = ?";
            var result = jdbcTemplate.query(sql, rowMapperForOrder, orderId, shopId);
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            System.err.println("Error when select order " + orderId + ": " + e.getMessage());
            return null;
        }
    	
    }
    
    public OrderView getOrderViewByOrderId(String orderId) {
        String sql = "SELECT " +
                "o.OrderID AS orderId, " +
                "o.BuyerID AS buyerId, " +
                "u.FullName AS buyerName, " +
                "o.ShopID AS shopId, " +
                "s.ShopName AS shopName, " +
                "o.OrderDate AS orderDate, " +
                "o.TotalAmount AS totalAmount, " +
                "o.Status AS status, " +
                "o.PaymentMethod AS paymentMethod, " +
                "sh.ShipmentID AS shipmentId, " +
                "sh.Status AS shipmentStatus, " +
                "o.FlashSaleStatus AS flashSaleStatus, " +
                "o.Refund AS refund, " +
                "o.ShippingFee AS shippingFee, " +
                "o.OrderNote AS orderNote, " +
                "o.CancelReason AS cancelReason, " +
                "o.CancelledAt AS cancelledAt, " +
                "o.ConfirmedAt AS confirmedAt, " +
                "o.CompletedAt AS completedAt, " +
                "o.DeliveryAddressJson AS deliveryAddressJson, " +

                "i.OrderItemID AS orderItemId, " +
                "i.OrderID AS itemOrderId, " +
                "i.ProductVariantID AS productVariantId, " +
                "pv.VariantName AS productVariantName, " +
                "i.Quantity AS quantity, " +
                "i.UnitPrice AS unitPrice, " +
                "i.FlashSaleID AS itemFlashSaleId, " +
                "f.Name AS itemFlashSaleName, " +
                "i.Subtotal AS subtotal, " +
                "pv.Image AS productVariantImage, " +

                "od.OrderDiscountID AS orderDiscountId, " +
                "od.CouponID, " +
                "od.DiscountType, " +
                "od.DiscountValue, " +
                "od.Description, " +
                "od.CreatedAt, " +
                "c.Code, " +
                "c.CouponType, " +
                "i.DiscountAllocated, " +
                "i.FinalPrice, " +
                "i.UnitFinalPrice " +

                "FROM Orders o " +
                "LEFT JOIN OrderItems i ON o.OrderID = i.OrderID " +
                "LEFT JOIN Users u ON o.BuyerID = u.UserID " +
                "LEFT JOIN Shops s ON o.ShopID = s.ShopID " +
                "LEFT JOIN FlashSales f ON i.FlashSaleID = f.FlashSaleID " +
                "LEFT JOIN ProductVariants pv ON i.ProductVariantID = pv.VariantID " +
                "LEFT JOIN OrderDiscounts od ON od.OrderID = o.OrderID " +
                "LEFT JOIN Coupons c ON c.CouponID = od.CouponID " +
                "LEFT JOIN Shipments sh ON sh.OrderID = o.OrderID " +
                "WHERE o.OrderID = ?";

        try {
            return jdbcTemplate.query(sql, rs -> {
                OrderView order = null;
                Map<Integer, OrderItemView> itemMap = new LinkedHashMap<>();
                Map<Integer, OrderDiscountView> discountMap = new LinkedHashMap<>();

                while (rs.next()) {
                    if (order == null) {
                        order = new OrderView();
                        order.setOrderId(rs.getString("orderId"));
                        order.setBuyerId(rs.getInt("buyerId"));
                        order.setBuyerName(rs.getString("buyerName"));
                        order.setShopId(rs.getInt("shopId"));
                        order.setShopName(rs.getString("shopName"));
                        order.setOrderDate(rs.getTimestamp("orderDate") != null
                                ? rs.getTimestamp("orderDate").toLocalDateTime()
                                : null);
                        order.setTotalAmount(rs.getDouble("totalAmount"));
                        order.setStatus(rs.getInt("status"));
                        order.setPaymentMethod(rs.getString("paymentMethod"));
                        order.setShipmentId(rs.getString("shipmentId"));
                        order.setFlashSaleStatus(rs.getInt("flashSaleStatus"));
                        order.setRefund(rs.getDouble("refund"));
                        order.setShippingFee(rs.getDouble("shippingFee"));
                        order.setShipmentStatus(rs.getInt("shipmentStatus"));
                        order.setCancelReason(rs.getString("cancelReason"));
                        order.setOrderNote(rs.getString("orderNote"));
                        order.setConfirmedAt(rs.getTimestamp("confirmedAt") != null
                                ? rs.getTimestamp("confirmedAt").toLocalDateTime()
                                : null);
                        order.setCancelledAt(rs.getTimestamp("cancelledAt") != null
                                ? rs.getTimestamp("cancelledAt").toLocalDateTime()
                                : null);
                        order.setCompletedAt(rs.getTimestamp("completedAt") != null
                                ? rs.getTimestamp("completedAt").toLocalDateTime()
                                : null);
                        var addressJson = rs.getString("deliveryAddressJson");
            			Address addressbuyer = null;
            			if(addressJson != null) {
            				try {
               					addressbuyer  = mapper.readValue(addressJson, Address.class);
               				} catch (Exception e) {
               					addressbuyer = null;
               				}
            			}
            			if(addressbuyer != null) {
            				order.setDeliveryAddressJson(addressbuyer);
            			}
                        
                    }

                    // ✅ Xử lý items (tránh duplicate)
                    int itemId = rs.getInt("orderItemId");
                    if (itemId > 0 && !itemMap.containsKey(itemId)) {
                        OrderItemView item = new OrderItemView();
                        item.setOrderItemId(itemId);
                        item.setOrderId(rs.getString("itemOrderId"));
                        item.setProductVariantId(rs.getInt("productVariantId"));
                        item.setProductVariantName(rs.getString("productVariantName"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getDouble("unitPrice"));
                        item.setFlashSaleId(rs.getInt("itemFlashSaleId"));
                        item.setFlashSaleName(rs.getString("itemFlashSaleName"));
                        item.setSubtotal(rs.getDouble("subtotal"));
                        item.setProductVariantImage(rs.getString("productVariantImage"));
                        item.setDiscountAllocated(rs.getDouble("discountAllocated"));
                        item.setFinalPrice(rs.getDouble("finalPrice"));
                        item.setUnitFinalPrice(rs.getDouble("unitFinalPrice"));

                        itemMap.put(itemId, item);
                    }

                    // ✅ Xử lý discounts (tránh duplicate)
                    Integer discountId = rs.getObject("orderDiscountId", Integer.class);
                    if (discountId != null && !discountMap.containsKey(discountId)) {
                        OrderDiscountView od = new OrderDiscountView();
                        od.setOrderDiscountId(discountId);
                        od.setCouponId(rs.getInt("CouponID"));
                        od.setDiscountType(rs.getString("DiscountType"));
                        od.setDiscountValue(rs.getDouble("DiscountValue"));
                        od.setDescription(rs.getString("Description"));
                        od.setCreatedAt(rs.getTimestamp("CreatedAt") != null
                                ? rs.getTimestamp("CreatedAt").toLocalDateTime()
                                : null);
                        od.setCouponCode(rs.getString("Code"));
                        od.setCouponType(rs.getString("CouponType"));

                        discountMap.put(discountId, od);
                    }
                }

                if (order != null) {
                    order.setItems(new ArrayList<>(itemMap.values()));
                    order.setDiscounts(new ArrayList<>(discountMap.values()));
                }

                return order;
            }, orderId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Map<String, Object> getOrderViewByUserId(int userId, Integer status, int pageIndex, int pageSize) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("GetOrdersWithItemsPagedByBuyerId")
                    .declareParameters(
                            new SqlParameter("BuyerId", Types.INTEGER),
                            new SqlParameter("Status", Types.INTEGER),
                            new SqlParameter("PageIndex", Types.INTEGER),
                            new SqlParameter("PageSize", Types.INTEGER),
                            new SqlOutParameter("TotalPages", Types.INTEGER),
                            new SqlOutParameter("TotalRows", Types.INTEGER)
                    );

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("BuyerId", userId)
                    .addValue("Status", status)
                    .addValue("PageIndex", pageIndex)
                    .addValue("PageSize", pageSize);

            Map<String, Object> out = jdbcCall.execute(params);

            List<Map<String, Object>> rows = (List<Map<String, Object>>) out.get("#result-set-1");
            Map<String, OrderView> orderMap = new LinkedHashMap<>();

            for (Map<String, Object> row : rows) {
                String orderId = String.valueOf(row.get("orderId")); // alias đã là orderId
                OrderView order = orderMap.get(orderId);

                if (order == null) {
                    order = new OrderView();
                    order.setOrderId(orderId);
                    order.setBuyerId((Integer) row.get("buyerId"));
                    order.setBuyerName((String) row.get("buyerName"));
                    order.setShopId((Integer) row.get("shopId"));
                    order.setShopName((String) row.get("shopName"));
                    order.setStatus((Integer) row.get("status"));
                    order.setPaymentMethod((String) row.get("paymentMethod"));
                    order.setOrderDate(row.get("orderDate") != null
                            ? ((Timestamp) row.get("orderDate")).toLocalDateTime()
                            : null);
                    order.setTotalAmount(row.get("totalAmount") != null
                            ? ((Number) row.get("totalAmount")).doubleValue()
                            : 0.0);
                    order.setRefund(row.get("refund") != null
                            ? ((Number) row.get("refund")).doubleValue()
                            : 0.0);
                    order.setShippingFee(row.get("shippingFee") != null
                            ? ((Number) row.get("shippingFee")).doubleValue()
                            : 0.0);
                    var shipmentId =  row.get("shipmentId");
                    if(shipmentId != null) {
                    	order.setShipmentId((String) shipmentId);
                    }
                    var shipmentStatus = row.get("shipmentStatus");
                    if(shipmentStatus != null) {
                    	order.setShipmentStatus((Integer) shipmentStatus);
                    }
                    var rR = rRRepo.findByOrder(orderId);
                    if(rR == null) {
                    	order.setCanRequestRefund(true);
                    }
                    
                    
                   

                 // ✅ lấy danh sách discount cho order
                    String sqlDiscount =
                            "SELECT od.OrderDiscountID, od.CouponID, od.DiscountType, od.DiscountValue, " +
                            "od.Description, od.CreatedAt, c.Code, c.CouponType " +   // thêm khoảng trắng sau CouponType
                            "FROM OrderDiscounts od " +
                            "JOIN Coupons c ON c.CouponID = od.CouponID " +
                            "WHERE od.OrderID = ?";

                    List<OrderDiscountView> discounts = jdbcTemplate.query(
                            sqlDiscount,
                            (rs, rowNum) -> {
                                OrderDiscountView od = new OrderDiscountView();
                                od.setOrderDiscountId(rs.getInt("OrderDiscountID"));
                                od.setCouponId(rs.getInt("CouponID"));
                                od.setDiscountType(rs.getString("DiscountType"));
                                od.setDiscountValue(rs.getDouble("DiscountValue"));
                                od.setDescription(rs.getString("Description"));
                                od.setCreatedAt(
                                    rs.getTimestamp("CreatedAt") != null
                                        ? rs.getTimestamp("CreatedAt").toLocalDateTime()
                                        : null
                                );
                                od.setCouponCode(rs.getString("Code"));
                                od.setCouponType(rs.getString("CouponType"));
                                return od;
                            },
                            orderId // truyền thẳng vào đây
                    );

                    order.setDiscounts(discounts);
                    orderMap.put(orderId, order);
                }

                // ✅ thêm item
                OrderItemView item = new OrderItemView();
                item.setOrderItemId((Integer) row.get("orderItemId"));
                item.setOrderId(orderId);
                item.setProductVariantId((Integer) row.get("productVariantId"));
                item.setProductVariantName((String) row.get("productVariantName"));
                item.setQuantity((Integer) row.get("quantity"));
                item.setUnitPrice(((Number) row.get("unitPrice")).doubleValue());
                item.setSubtotal(((Number) row.get("subtotal")).doubleValue());
                item.setFlashSaleId(row.get("flashSaleId") != null ? (Integer) row.get("flashSaleId") : 0);
                item.setFlashSaleName((String) row.get("flashSaleName"));
                item.setProductVariantImage((String) row.get("productVariantImage"));
                item.setDiscountAllocated(((Number) row.get("discountAllocated")).doubleValue());
                item.setFinalPrice(((Number) row.get("finalPrice")).doubleValue());
                item.setUnitFinalPrice(((Number) row.get("unitFinalPrice")).doubleValue());
                order.getItems().add(item);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orders", new ArrayList<>(orderMap.values()));
            result.put("totalPages", out.get("TotalPages"));
            result.put("totalRows", out.get("TotalRows"));
            return result;

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy order by : " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Order getOrderById(String orderId) {
        try {
            String sql = "SELECT * FROM Orders WHERE OrderID = ?";
            var result = jdbcTemplate.query(sql, rowMapperForOrder, orderId);
            
            
            return result.isEmpty() ? null : result.get(0);
            		
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi để dễ debug
            return null;
        }
    }
    
    public int autoCancelUnpaidOrders() {
        String sql = """
            UPDATE Orders
            SET Status = 4
            WHERE Status = 0
              AND DATEDIFF(HOUR, OrderDate, GETDATE()) >= 8
        """;
        return jdbcTemplate.update(sql);
    }
    
    public OrderView getOrderViewByShipmentId(String shipmentId) {
        String sql = "SELECT " +
                "o.OrderID AS orderId, " +
                "o.BuyerID AS buyerId, " +
                "u.FullName AS buyerName, " +
                "o.ShopID AS shopId, " +
                "s.ShopName AS shopName, " +
                "o.OrderDate AS orderDate, " +
                "o.TotalAmount AS totalAmount, " +
                "o.Status AS status, " +
                "o.PaymentMethod AS paymentMethod, " +
                "sh.ShipmentID AS shipmentId, " +
                "sh.Status AS shipmentStatus, " +
                "o.FlashSaleStatus AS flashSaleStatus, " +
                "o.Refund AS refund, " +
                "o.ShippingFee AS shippingFee, " +
                "o.OrderNote AS orderNote, " +
                "o.CancelReason AS cancelReason, " +
                "o.CancelledAt AS cancelledAt, " +
                "o.ConfirmedAt AS confirmedAt, " +
                "o.CompletedAt AS completedAt, " +

                "i.OrderItemID AS orderItemId, " +
                "i.OrderID AS itemOrderId, " +
                "i.ProductVariantID AS productVariantId, " +
                "pv.VariantName AS productVariantName, " +
                "i.Quantity AS quantity, " +
                "i.UnitPrice AS unitPrice, " +
                "i.FlashSaleID AS itemFlashSaleId, " +
                "f.Name AS itemFlashSaleName, " +
                "i.Subtotal AS subtotal, " +
                "pv.Image AS productVariantImage, " +

                "od.OrderDiscountID AS orderDiscountId, " +
                "od.CouponID, " +
                "od.DiscountType, " +
                "od.DiscountValue, " +
                "od.Description, " +
                "od.CreatedAt, " +
                "c.Code, " +
                "c.CouponType, " +
                "i.DiscountAllocated, " +
                "i.FinalPrice, " +
                "i.UnitFinalPrice " +

                "FROM Orders o " +
                "LEFT JOIN OrderItems i ON o.OrderID = i.OrderID " +
                "LEFT JOIN Users u ON o.BuyerID = u.UserID " +
                "LEFT JOIN Shops s ON o.ShopID = s.ShopID " +
                "LEFT JOIN FlashSales f ON i.FlashSaleID = f.FlashSaleID " +
                "LEFT JOIN ProductVariants pv ON i.ProductVariantID = pv.VariantID " +
                "LEFT JOIN OrderDiscounts od ON od.OrderID = o.OrderID " +
                "LEFT JOIN Coupons c ON c.CouponID = od.CouponID " +
                "LEFT JOIN Shipments sh ON sh.OrderID = o.OrderID " +
                "WHERE sh.ShipmentID = ?";

        try {
            return jdbcTemplate.query(sql, rs -> {
                OrderView order = null;
                Map<Integer, OrderItemView> itemMap = new LinkedHashMap<>();
                Map<Integer, OrderDiscountView> discountMap = new LinkedHashMap<>();

                while (rs.next()) {
                    if (order == null) {
                        order = new OrderView();
                        order.setOrderId(rs.getString("orderId"));
                        order.setBuyerId(rs.getInt("buyerId"));
                        order.setBuyerName(rs.getString("buyerName"));
                        order.setShopId(rs.getInt("shopId"));
                        order.setShopName(rs.getString("shopName"));
                        order.setOrderDate(rs.getTimestamp("orderDate") != null
                                ? rs.getTimestamp("orderDate").toLocalDateTime()
                                : null);
                        order.setTotalAmount(rs.getDouble("totalAmount"));
                        order.setStatus(rs.getInt("status"));
                        order.setPaymentMethod(rs.getString("paymentMethod"));
                        order.setShipmentId(rs.getString("shipmentId"));
                        order.setFlashSaleStatus(rs.getInt("flashSaleStatus"));
                        order.setRefund(rs.getDouble("refund"));
                        order.setShippingFee(rs.getDouble("shippingFee"));
                        order.setCancelReason(rs.getString("cancelReason"));
                        order.setOrderNote(rs.getString("orderNote"));
                        order.setConfirmedAt(rs.getTimestamp("confirmedAt") != null
                                ? rs.getTimestamp("confirmedAt").toLocalDateTime()
                                : null);
                        order.setCancelledAt(rs.getTimestamp("cancelledAt") != null
                                ? rs.getTimestamp("cancelledAt").toLocalDateTime()
                                : null);
                        order.setCompletedAt(rs.getTimestamp("completedAt") != null
                                ? rs.getTimestamp("completedAt").toLocalDateTime()
                                : null);
                    }

                    // ✅ Xử lý items (tránh duplicate)
                    int itemId = rs.getInt("orderItemId");
                    if (itemId > 0 && !itemMap.containsKey(itemId)) {
                        OrderItemView item = new OrderItemView();
                        item.setOrderItemId(itemId);
                        item.setOrderId(rs.getString("itemOrderId"));
                        item.setProductVariantId(rs.getInt("productVariantId"));
                        item.setProductVariantName(rs.getString("productVariantName"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getDouble("unitPrice"));
                        item.setFlashSaleId(rs.getInt("itemFlashSaleId"));
                        item.setFlashSaleName(rs.getString("itemFlashSaleName"));
                        item.setSubtotal(rs.getDouble("subtotal"));
                        item.setProductVariantImage(rs.getString("productVariantImage"));
                        item.setDiscountAllocated(rs.getDouble("discountAllocated"));
                        item.setFinalPrice(rs.getDouble("finalPrice"));
                        item.setUnitFinalPrice(rs.getDouble("unitFinalPrice"));

                        itemMap.put(itemId, item);
                    }

                    // ✅ Xử lý discounts (tránh duplicate)
                    Integer discountId = rs.getObject("orderDiscountId", Integer.class);
                    if (discountId != null && !discountMap.containsKey(discountId)) {
                        OrderDiscountView od = new OrderDiscountView();
                        od.setOrderDiscountId(discountId);
                        od.setCouponId(rs.getInt("CouponID"));
                        od.setDiscountType(rs.getString("DiscountType"));
                        od.setDiscountValue(rs.getDouble("DiscountValue"));
                        od.setDescription(rs.getString("Description"));
                        od.setCreatedAt(rs.getTimestamp("CreatedAt") != null
                                ? rs.getTimestamp("CreatedAt").toLocalDateTime()
                                : null);
                        od.setCouponCode(rs.getString("Code"));
                        od.setCouponType(rs.getString("CouponType"));

                        discountMap.put(discountId, od);
                    }
                }

                if (order != null) {
                    order.setItems(new ArrayList<>(itemMap.values()));
                    order.setDiscounts(new ArrayList<>(discountMap.values()));
                }

                return order;
            }, shipmentId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy order " + shipmentId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public int totalOrderByShop(int shopId) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM Orders
                WHERE ShopID = ?
                  AND Status IN ('5', '6', '8')
            """;

            var count  = jdbcTemplate.queryForObject(sql, Integer.class, shopId);
            return count != null ? count : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
