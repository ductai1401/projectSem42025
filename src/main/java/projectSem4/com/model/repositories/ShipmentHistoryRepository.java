package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.ShipmentHistory;

@Repository
public class ShipmentHistoryRepository {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private final RowMapper<ShipmentHistory> mapper = new RowMapper<>() {
        @Override
        public ShipmentHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShipmentHistory h = new ShipmentHistory();
            h.setHistoryID(rs.getInt("HistoryID"));
            h.setShipmentID(rs.getString("ShipmentID"));
            h.setStatus(rs.getInt("Status"));
            h.setDescription(rs.getString("Description"));
            h.setUpdatedBy((Integer) rs.getObject("UpdatedBy"));
            h.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            return h;
        }
    };

    public List<ShipmentHistory> findByShipment(String shipmentID) {
        try {
            String sql = "SELECT * FROM ShipmentHistory WHERE ShipmentID = ? ORDER BY CreatedAt ASC";
            return jdbcTemplate.query(sql, mapper, shipmentID);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentHistoryRepository] findByShipment() failed: " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean insert(ShipmentHistory h) {
        String sql = """
            INSERT INTO ShipmentHistory (ShipmentID, Status, Description, UpdatedBy, CreatedAt)
            VALUES (?, ?, ?, ?, GETDATE())
        """;
        try {
            return jdbcTemplate.update(sql,
                    h.getShipmentID(),
                    h.getStatus(),
                    h.getDescription(),
                    h.getUpdatedBy()) > 0;
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentHistoryRepository] insert() failed: " + ex.getMessage());
            return false;
        }
    }
}
