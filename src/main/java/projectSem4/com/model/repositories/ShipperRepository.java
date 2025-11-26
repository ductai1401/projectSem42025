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

import projectSem4.com.model.entities.Shipper;

@Repository
public class ShipperRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private final RowMapper<Shipper> mapper = new RowMapper<>() {
        @Override
        public Shipper mapRow(ResultSet rs, int rowNum) throws SQLException {
            Shipper s = new Shipper();
            s.setShipperID(rs.getInt("ShipperID"));
            s.setUserID(rs.getInt("UserID"));
            s.setFullName(rs.getString("FullName"));
            s.setPhoneNumber(rs.getString("PhoneNumber"));
            s.setStatus(rs.getInt("Status"));
            s.setAvailable(rs.getBoolean("IsAvailable"));
            s.setTotalDeliveries(rs.getInt("TotalDeliveries"));
            s.setRating(rs.getDouble("Rating"));
            s.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            s.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
            return s;
        }
    };

    public List<Shipper> findAll() {
        try {
            String sql = "SELECT * FROM Shippers";
            return jdbcTemplate.query(sql, mapper);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipperRepository] findAll() failed: " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    public Shipper findById(int id) {
        try {
            String sql = "SELECT * FROM Shippers WHERE ShipperID = ?";
            return jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipperRepository] findById(" + id + ") failed: " + ex.getMessage());
            return null;
        }
    }
    public Shipper findByIdUser(int id) {
    	try {
    		String sql = "SELECT * FROM Shippers WHERE UserID = ?";
    		return jdbcTemplate.queryForObject(sql, mapper, id);
    	} catch (DataAccessException ex) {
    		System.err.println("❌ [ShipperRepository] findById(" + id + ") failed: " + ex.getMessage());
    		return null;
    	}
    }

    public boolean insert(Shipper shipper) {
        String sql = """
            INSERT INTO Shippers (UserID, FullName, PhoneNumber, Status, IsAvailable,
                                  TotalDeliveries, Rating, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
        """;
        try {
            return jdbcTemplate.update(sql,
                    shipper.getUserID(),
                    shipper.getFullName(),
                    shipper.getPhoneNumber(),
                    shipper.getStatus(),
                    shipper.isAvailable(),
                    shipper.getTotalDeliveries(),
                    shipper.getRating()) > 0;
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipperRepository] insert() failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean update(Shipper shipper) {
        String sql = """
            UPDATE Shippers
            SET FullName = ?, PhoneNumber = ?, Status = ?, IsAvailable = ?, 
                TotalDeliveries = ?, Rating = ?, UpdatedAt = GETDATE()
            WHERE ShipperID = ?
        """;
        try {
            return jdbcTemplate.update(sql,
                    shipper.getFullName(),
                    shipper.getPhoneNumber(),
                    shipper.getStatus(),
                    shipper.isAvailable(),
                    shipper.getTotalDeliveries(),
                    shipper.getRating(),
                    shipper.getShipperID()) > 0;
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipperRepository] update() failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        try {
            String sql = "DELETE FROM Shippers WHERE ShipperID = ?";
            return jdbcTemplate.update(sql, id) > 0;
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipperRepository] delete() failed: " + ex.getMessage());
            return false;
        }
    }
}
