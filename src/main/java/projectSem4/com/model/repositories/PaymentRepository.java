package projectSem4.com.model.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.Payment;

@Repository
public class PaymentRepository {

	private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ PaymentRepository initialized. hashCode: " + System.identityHashCode(this));
	}

    private final RowMapper<Payment> paymentRowMapper = new RowMapper<>() {
        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Payment p = new Payment();
            p.setPaymentId(rs.getInt("PaymentID"));
            p.setPaymentAmount(rs.getDouble("PaymentAmount"));
            p.setPaymentMethod(rs.getString("PaymentMethod"));
            p.setStatus(rs.getInt("Status"));
            p.setTransactionCode(rs.getString("TransactionCode"));
            var paymentDate = rs.getTimestamp("PaymentDate");
            if(paymentDate != null) p.setPaymentDate(paymentDate.toLocalDateTime());
            p.setGatewayTransactionNo(rs.getString("GatewayTransactionNo"));
            p.setPaymentUrl(rs.getString("PaymentUrl"));
            Timestamp createdAt = rs.getTimestamp("CreatedAt");
            if (createdAt != null) {
                p.setCreatedAt(createdAt.toLocalDateTime());
            }

            Timestamp updatedAt = rs.getTimestamp("UpdatedAt");
            if (updatedAt != null) {
                p.setUpdatedAt(updatedAt.toLocalDateTime());
            }

            Timestamp expiredAt = rs.getTimestamp("ExpiredAt");
            if (expiredAt != null) {
                p.setExpiredAt(expiredAt.toLocalDateTime());
            } else {
                p.setExpiredAt(null); // có thể null nếu payment chưa set expire
            }
            return p;
        }
    };

    public Integer create(Payment payment) {
        String sql = """
            INSERT INTO Payments 
            (PaymentAmount, PaymentMethod, Status, TransactionCode, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setDouble(1, payment.getPaymentAmount()); // dùng BigDecimal cho chính xác tiền
                ps.setString(2, payment.getPaymentMethod());
                ps.setInt(3, payment.getStatus());
                ps.setString(4, payment.getTransactionCode());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? key.intValue() : null;

        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi tạo Payment: " + e.getMessage());
            return null;
        }
    }

    // Tìm Payment theo ID
    public Payment findById(Long paymentId) {
        String sql = "SELECT * FROM Payments WHERE PaymentID = ?";
        try {
            List<Payment> result = jdbcTemplate.query(sql, paymentRowMapper, paymentId);
            return result.isEmpty() ? null : result.get(0);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi tìm Payment theo ID: " + e.getMessage());
            return null;
        }
    }

    // Tìm Payment theo OrderID (có thể nhiều Payment nếu retry)
    public List<Payment> findByOrderId(String orderId) {
        String sql = "SELECT * FROM Payments WHERE OrderID = ?";
        try {
            return jdbcTemplate.query(sql, paymentRowMapper, orderId);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi tìm Payment theo OrderID: " + e.getMessage());
            return List.of();
        }
    }
 // Tìm Payment theo OrderID (có thể nhiều Payment nếu retry)
    public Payment findByOrderIdAndPayment(String orderId) {
        String sql = "SELECT * FROM Payments as p"
        		+ " JOIN PaymentOrders as pO on pO.PaymentID = p.PaymentID"
        		+ " WHERE OrderID = ? and p.Status = 1";
        try {
        	var rs = jdbcTemplate.query(sql, paymentRowMapper, orderId);
            return rs.isEmpty() ? null : rs.get(0);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi tìm Payment theo OrderID: " + e.getMessage());
            return null;
        }
    }

    // Tìm Payment theo TransactionCode
    public Payment findByTransactionCode(String transactionCode) {
        String sql = "SELECT * FROM Payments WHERE TransactionCode = ?";
        try {
            List<Payment> result = jdbcTemplate.query(sql, paymentRowMapper, transactionCode);
            return result.isEmpty() ? null : result.get(0);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi tìm Payment theo TransactionCode: " + e.getMessage());
            return null;
        }
    }

    // Cập nhật trạng thái, TransactionCode và PaymentDate
    public boolean updateStatusAndTransaction(Payment payment) {
        String sql = """
            UPDATE Payments
            SET Status = ?, GatewayTransactionNo = ?, PaymentDate = ? , UpdatedAt = SYSDATETIME()
            WHERE TransactionCode = ?
            """;
        try {
            int rows = jdbcTemplate.update(sql, payment.getStatus(), payment.getGatewayTransactionNo() == null ? null : payment.getGatewayTransactionNo(), payment.getPaymentDate(), payment.getTransactionCode());
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi cập nhật Payment: " + e.getMessage());
            return false;
        }
    }
    public boolean updateUrl(Payment payment) {
        String sql = """
            UPDATE Payments
            SET  PaymentUrl = ? , ExpiredAt = ? , Status = ? UpdatedAt = SYSDATETIME()
            WHERE TransactionCode = ?
            """;
        try {
            int rows = jdbcTemplate.update(sql,  payment.getPaymentUrl(), payment.getExpiredAt(), payment.getStatus(), payment.getTransactionCode());
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi cập nhật Payment: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateStatus(Payment payment) {
        String sql = """
            UPDATE Payments
            SET Status = ?, UpdatedAt = SYSDATETIME()
            WHERE PaymentID = ?
            """;
        try {
            int rows = jdbcTemplate.update(sql, payment.getStatus(), payment.getPaymentId());
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi cập nhật Payment: " + e.getMessage());
            return false;
        }
    }
     

    // Lấy tất cả Payment
    public List<Payment> findAll() {
        String sql = "SELECT * FROM Payments";
        try {
            return jdbcTemplate.query(sql, paymentRowMapper);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi lấy danh sách Payments: " + e.getMessage());
            return List.of();
        }
    }
    
    
    public Payment findTopByOrderIdOrderByCreatedAtDesc(String orderId) {
        String sql = "SELECT TOP 1 p.*\r\n"
        		+ "FROM Payments p\r\n"
        		+ "JOIN PaymentOrders pO ON pO.PaymentID = p.PaymentID\r\n"
        		+ "WHERE pO.OrderID = ?\r\n"
        		+ "ORDER BY p.CreatedAt DESC";
        try {
        	var result = jdbcTemplate.query(sql, paymentRowMapper, orderId);
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
        	System.err.println("❌ Lỗi lấy payment mới nhất: " + e.getMessage());
        	e.printStackTrace();
            return null; // không có payment nào
        } 
    }
}
