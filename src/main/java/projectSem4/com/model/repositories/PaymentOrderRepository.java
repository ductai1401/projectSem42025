package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.PaymentOrder;

@Repository
public class PaymentOrderRepository {
	private final JdbcTemplate jdbcTemplate;

    public PaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ PaymentOrderRepository initialized. hashCode: " + System.identityHashCode(this));
	}
    
    private final RowMapper<PaymentOrder> rowMapper = new RowMapper<>() {
        @Override
        public PaymentOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
            PaymentOrder po = new PaymentOrder();
            po.setId(rs.getInt("ID"));
            po.setPaymentId(rs.getInt("PaymentID"));
            po.setOrderId(rs.getString("OrderID"));
            return po;
        }
    };

    // Thêm mapping order → payment
    public boolean save(PaymentOrder paymentOrder) {
        String sql = "INSERT INTO PaymentOrders(PaymentID, OrderID) VALUES (?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, paymentOrder.getPaymentId(), paymentOrder.getOrderId());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy tất cả orderId theo paymentId
    public List<PaymentOrder> findByPaymentId(int paymentId) {
        String sql = "SELECT * FROM PaymentOrders WHERE PaymentID = ?";
        try {
            return jdbcTemplate.query(sql, rowMapper, paymentId);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    

    // Lấy tất cả paymentId theo orderId
    public List<PaymentOrder> findByOrderId(String orderId) {
        String sql = "SELECT * FROM PaymentOrders WHERE OrderID = ?";
        try {
            return jdbcTemplate.query(sql, rowMapper, orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Xóa tất cả mapping theo paymentId
    public boolean deleteByPaymentId(int paymentId) {
        String sql = "DELETE FROM PaymentOrders WHERE PaymentID = ?";
        try {
            int rows = jdbcTemplate.update(sql, paymentId);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa tất cả mapping theo orderId
    public boolean deleteByOrderId(String orderId) {
        String sql = "DELETE FROM PaymentOrders WHERE OrderID = ?";
        try {
            int rows = jdbcTemplate.update(sql, orderId);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
