package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.InventoryLog;

@Repository
public class InventoryLogRepository {
	  @Autowired
	    private JdbcTemplate jdbcTemplate;

	    // RowMapper cho InventoryLog
	    private RowMapper<InventoryLog> rowMapperForInventoryLog = new RowMapper<InventoryLog>() {
	        @Override
	        public InventoryLog mapRow(ResultSet rs, int rowNum) throws SQLException {
	            InventoryLog log = new InventoryLog();
	            log.setLogId(rs.getInt("LogID"));
	            log.setVariantId(rs.getLong("VariantID"));
	            log.setLogType(rs.getString("LogType"));
	            log.setQuantity(rs.getInt("Quantity"));
	            log.setUnitCost(rs.getDouble("UnitCost"));
	            log.setRemaining(rs.getInt("Remaining"));
	            log.setRefOrderId(rs.getString("RefOrderID"));
	            log.setRefImportId(rs.getInt("RefImportID"));
//	            log.setRefOrderId(rs.getInt("RefOrderID"));
	            Timestamp ts = rs.getTimestamp("CreatedAt");
	            log.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
	            return log;
	        }
	    };

	    // Thêm log mới
	    public int addLog(InventoryLog log) {
	        String sql = "INSERT INTO InventoryLogs (VariantID, LogType, Quantity, UnitCost, Remaining, RefOrderID, CreatedAt) " +
	                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
	        return jdbcTemplate.update(sql,
	                log.getVariantId(),
	                log.getLogType(),
	                log.getQuantity(),
	                log.getUnitCost(),
	                log.getRemaining(),
	                log.getRefOrderId(),
	                Timestamp.valueOf(log.getCreatedAt() != null ? log.getCreatedAt() : LocalDateTime.now())
	        );
	    }

	    // Cập nhật log (chỉ cập nhật quantity, unitCost, remaining)
	    public int updateLog(InventoryLog log) {
	        String sql = "UPDATE InventoryLogs SET Quantity = ?, UnitCost = ?, Remaining = ? WHERE LogID = ?";
	        return jdbcTemplate.update(sql, log.getQuantity(), log.getUnitCost(), log.getRemaining(), log.getLogId());
	    }

	    // Xóa log theo ID
	    public int deleteLog(int logId) {
	        String sql = "DELETE FROM InventoryLogs WHERE LogID = ?";
	        return jdbcTemplate.update(sql, logId);
	    }

	    // Lấy tất cả log theo variantId
	    public List<InventoryLog> findByVariantId(int variantId) {
	        String sql = "SELECT * FROM InventoryLogs WHERE VariantID = ? ORDER BY CreatedAt DESC";
	        return jdbcTemplate.query(sql, rowMapperForInventoryLog, variantId);
	    }

	    // Lấy log theo refOrderId
	    public List<InventoryLog> findByRefOrderId(int refOrderId) {
	        String sql = "SELECT * FROM InventoryLogs WHERE RefOrderID = ?";
	        return jdbcTemplate.query(sql, rowMapperForInventoryLog, refOrderId);
	    }

	    // Lấy log mới nhất của variant
	    public InventoryLog findLatestByVariantId(int variantId) {
	    	String sql = "SELECT TOP 1 * FROM InventoryLogs WHERE VariantID = ? ORDER BY CreatedAt DESC";
	        List<InventoryLog> list = jdbcTemplate.query(sql, rowMapperForInventoryLog, variantId);
	        return list.isEmpty() ? null : list.get(0);
	    }
	 // Lấy số lượng tồn kho mới nhất cho 1 Variant
	    public int getStock(int variantId) {
	        String sql = """
	            SELECT 
	                ISNULL(SUM(
	                    CASE 
	                        WHEN LogType = 'IMPORT' THEN Remaining
	                        WHEN LogType = 'RESERVE' THEN -Quantity  -- kho ảo đang giữ
	                        WHEN LogType = 'EXPORT' THEN 0          -- đã trừ kho
	                        WHEN LogType = 'RETURN' THEN Quantity   -- hàng trả về cộng lại
	                        ELSE 0
	                    END
	                ), 0) AS AvailableQty
	            FROM InventoryLogs
	            WHERE VariantID = ?
	        """;

	        try {
	            Integer stock = jdbcTemplate.queryForObject(sql, Integer.class, variantId);
	            return stock != null ? stock : 0;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return 0;
	        }
	    }
 // Lấy log theo ID
	    public InventoryLog findById(int logId) {
	        String sql = "SELECT * FROM InventoryLogs WHERE LogID = ?";
	        List<InventoryLog> list = jdbcTemplate.query(sql, rowMapperForInventoryLog, logId);
	        return list.isEmpty() ? null : list.get(0);
	    }
}
