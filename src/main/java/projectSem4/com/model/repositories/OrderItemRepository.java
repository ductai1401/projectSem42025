package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.OrderItem;
import projectSem4.com.model.modelViews.OrderItemView;
import projectSem4.com.model.modelViews.ShopView;

@Repository
public class OrderItemRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ OrderItemItem initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho OrderItem
	private RowMapper<OrderItem> rowMapperForOrderItem = new RowMapper<OrderItem>() {
		@Override
		public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer OrderItemID = rs.getInt("OrderItemID");
			Integer ProductVariantID = rs.getInt("ProductVariantID");
			String OrderID = rs.getString("OrderID");
			Double UnitPrice = rs.getDouble("UnitPrice");

			Integer Quantity = rs.getInt("Quantity");
			Integer QtyRefunded = rs.getInt("QtyRefunded");
			Integer ItemRefundStatus = rs.getInt("ItemRefundStatus");
			Integer FlashSaleID = rs.getInt("FlashSaleID");
			Double Subtotal = rs.getDouble("Subtotal");
			Double discountAllocated = rs.getDouble("DiscountAllocated");
			Double FinalPrice = rs.getDouble("finalPrice");
			Double unitFinalPrice = rs.getDouble("UnitFinalPrice");
			Double AmountRefunded = rs.getDouble("AmountRefunded");
			
			

			// Map dữ liệu và tạo đối tượng OrderItem
			OrderItem OrderItem = new OrderItem(OrderItemID,OrderID,ProductVariantID,
					Quantity,UnitPrice,FlashSaleID,Subtotal,discountAllocated,FinalPrice,unitFinalPrice,
					QtyRefunded,AmountRefunded,ItemRefundStatus
					);
			
			return OrderItem;
		}
	};

	// Tạo một OrderItem mới
	public Map<String, Object> createOrderItem(OrderItem OrderItem) {
		Map<String, Object> a = new HashMap<>();
		
		try {
			String sql = "INSERT INTO OrderItems (OrderID ,ProductVariantID ,Quantity , UnitPrice, FlashSaleID, DiscountAllocated) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
			int rows = jdbcTemplate.update(sql,
					OrderItem.getOrderId(),
					OrderItem.getProductVariantId(), OrderItem.getQuantity(),
					OrderItem.getUnitPrice(), OrderItem.getFlashSaleId(),
					OrderItem.getDiscountAllocated());
				
			if(rows > 0) {
				a.put("message", "OrderItem registration successful!");
				a.put("rows", rows);
			} else {
				a.put("message", "OrderItem registration failed!");
				a.put("rows", rows);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			a.put("message", "OrderItem registration failed!");
			a.put("rows", 0);
		}
		return a;
	}
	
	public List<OrderItemView> getByOrderID(String orderId){
		List<OrderItemView> lItem = new ArrayList<>();
		try {
			String sql = "select \r\n"
					+ "	oi.FlashSaleID as flashSaleId,\r\n"
					+ "	oi.OrderID as orderId,\r\n"
					+ "	oi.OrderItemID as orderItemId,\r\n"
					+ "	oi.ProductVariantID as productVariantId,\r\n"
					+ "	oi.Quantity as quantity,\r\n"
					+ "	oi.Subtotal as subtotal,\r\n"
					+ "	oi.UnitPrice as unitPrice,\r\n"
					+ "	oi.DiscountAllocated as discountAllocated,\r\n"
					+ "	oi.FinalPrice as finalPrice,\r\n"
					+ "	oi.UnitFinalPrice as unitFinalPrice,\r\n"
					+ "	pv.VariantName as productVariantName,\r\n"
					+ "	fs.Name as flashSaleName\r\n"
					+ "from OrderItems as oi\r\n"
					+ "join ProductVariants as pv on pv.VariantID  = oi.ProductVariantID\r\n"
					+ "left join FlashSales as fs on fs.FlashSaleID = oi.FlashSaleID\r\n"
					+ "where oi.OrderID = ?";
			var rows = jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(OrderItemView.class), orderId);
			if(rows != null) {
				lItem = rows;
			} else {
				lItem = null;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			lItem = null;
		}
		return lItem;
	}
	
	public OrderItem getOrderItemById(int orderItemId) {
		try {
			String sql = "SELECT * FROM OrderItems WHERE OrderItemID = ?";
			var result = jdbcTemplate.query(sql, rowMapperForOrderItem, orderItemId);
			return result.isEmpty() ? null : result.get(0);
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<OrderItem> getAllByOrderId(String orderId) {
		try {
			String sql = "SELECT * FROM OrderItems WHERE OrderID = ?";
			var result = jdbcTemplate.query(sql, rowMapperForOrderItem, orderId);
			return result.isEmpty() ? null : result;
		} catch (Exception e) {
			return null;
		}
	}
	public boolean updateDiscountAllocated(OrderItem data) {
		try {
			String sql = "Update FROM OrderItems set DiscountAllocated = ? WHERE OrderItemID = ?";
			var result = jdbcTemplate.update(sql, data.getDiscountAllocated(), data.getOrderItemId());
			return result > 0;
		} catch (Exception e) {
			return false;
		}
	}
}
