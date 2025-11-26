package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.OrderDiscount;

@Repository
public class OrderDiscountRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ OrderDiscountItem initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho OrderDiscount
	private RowMapper<OrderDiscount> rowMapperForOrderDiscount = new RowMapper<OrderDiscount>() {
		@Override
		public OrderDiscount mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer OrderDiscountID = rs.getInt("OrderDiscountID");
			Integer CouponID = rs.getInt("CouponID");
			String OrderID = rs.getString("OrderID");
			String DiscountType = rs.getString("DiscountType");
			String Description = rs.getString("Description");
			Double DiscountValue = rs.getDouble("DiscountValue");
			LocalDateTime CreatedAt = rs.getTimestamp("CreatedAt").toLocalDateTime();

			// Map dữ liệu và tạo đối tượng OrderDiscount
			OrderDiscount OrderDiscount = new OrderDiscount(OrderDiscountID, OrderID, CouponID, DiscountType,
					Description, DiscountValue, CreatedAt);

			return OrderDiscount;
		}
	};

	// Tạo một OrderDiscount mới
	public Map<String, Object> createOrderDiscount(OrderDiscount OrderDiscount) {
		Map<String, Object> a = new HashMap<>();

		try {
			String sql = "INSERT INTO OrderDiscounts (OrderID ,CouponID ,DiscountType , DiscountValue, Description, CreatedAt) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
			int rows = jdbcTemplate.update(sql, OrderDiscount.getOrderId(), OrderDiscount.getCouponId(),
					OrderDiscount.getDiscountType(), OrderDiscount.getDiscountValue(),
					OrderDiscount.getDescription() != null ? OrderDiscount.getDescription() : "",
					OrderDiscount.getCreatedAt());
			if (rows > 0) {
				a.put("message", "OrderDiscount registration successful!");
				a.put("rows", rows);
			} else {
				a.put("message", "OrderDiscount registration failed!");
				a.put("rows", rows);
			}

		} catch (Exception e) {
			e.printStackTrace();
			a.put("message", "OrderDiscount registration failed!");
			a.put("rows", 0);
		}
		return a;
	}

	public OrderDiscount getOrderDiscountByTypeCoupon(String orderId, String type) {
		Map<String, Object> a = new HashMap<>();

		try {
			String sql = "SELECT * FROM OrderDiscounts od JOIN Coupons c ON od.CouponID = c.CouponID WHere "
					+ " od.OrderID = ? AND c.CouponType = ?";

			var rows = jdbcTemplate.query(sql, rowMapperForOrderDiscount, orderId, type);

			return rows.isEmpty() ? null : rows.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public List<OrderDiscount> getOrderDiscountByOId(String orderId) {
		Map<String, Object> a = new HashMap<>();

		try {
			String sql = "SELECT * FROM OrderDiscounts od JOIN Coupons c ON od.CouponID = c.CouponID WHere "
					+ " od.OrderID = ? ";

			var rows = jdbcTemplate.query(sql, rowMapperForOrderDiscount, orderId);

			return rows.isEmpty() ? null : rows;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean updateDisCountValue(OrderDiscount data) {
	    try {
	        String sql = "UPDATE OrderDiscounts SET DiscountValue = ? WHERE OrderDiscountID = ?";
	        int rows = jdbcTemplate.update(sql, data.getDiscountValue(), data.getOrderDiscountId());
	        return rows > 0;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean deleteDisCount(OrderDiscount data) {

		try {
			String sql = "Delete FROM OrderDiscounts WHere OrderDiscountID = ? ";

			var rows = jdbcTemplate.update(sql, 
					data.getOrderDiscountId());

			return rows > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
