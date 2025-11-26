package projectSem4.com.model.repositories;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.Shipment;

@Repository
public class ShipmentRepository {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private final RowMapper<Shipment> mapper = new RowMapper<>() {
        @Override
        public Shipment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Shipment s = new Shipment();
            s.setShipmentID(rs.getString("ShipmentID"));
            s.setShipperID((Integer) rs.getObject("ShipperID"));
            s.setPickupAddress(rs.getString("PickupAddress"));
            s.setDeliveryAddress(rs.getString("DeliveryAddress"));
            s.setRecipientName(rs.getString("RecipientName"));
            s.setRecipientPhone(rs.getString("RecipientPhone"));
            s.setStatus(rs.getInt("Status"));
            s.setAssignedAt(rs.getTimestamp("AssignedAt") != null ?
                    rs.getTimestamp("AssignedAt").toLocalDateTime() : null);
            s.setPickedAt(rs.getTimestamp("PickedAt") != null ?
                    rs.getTimestamp("PickedAt").toLocalDateTime() : null);
            s.setDeliveredAt(rs.getTimestamp("DeliveredAt") != null ?
                    rs.getTimestamp("DeliveredAt").toLocalDateTime() : null);
            s.setFailedReason(rs.getString("FailedReason"));
            s.setProofImages(rs.getString("ProofImages"));
            s.setNotes(rs.getString("Notes"));
            s.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            s.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
            s.setShippingFee(rs.getBigDecimal("ShippingFee"));
            s.setDiscountShipping(rs.getBigDecimal("DiscountShipping"));
            s.setPayoutToShipper(rs.getBigDecimal("PayoutToShipper"));
            s.setFinalFee(rs.getBigDecimal("FinalFee"));
            s.setOrderID(rs.getString("OrderID"));
            s.setRefundID(rs.getInt("RefundID"));
            return s;
        }
    };

    public List<Shipment> findAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM Shipments", mapper);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] findAll() failed: " + ex.getMessage());
            return Collections.emptyList();
        }
    }
    public List<Shipment> findAllStatusPending() {
        String sql = "{call sp_GetPendingShipments()}";

        try {
            return jdbcTemplate.query(sql, mapper);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] findAllStatusPending() failed: " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    public Shipment findById(String id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Shipments WHERE ShipmentID = ?", mapper, id);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] findById(" + id + ") failed: " + ex.getMessage());
            return null;
        }
    }
    
    public Shipment findByIdShipper(int id) {
        try {
        	String sql = "SELECT TOP 1\r\n"
            		
            		+ "  s.*\r\n"
            		+ "FROM Shipments s\r\n"
            		+ "WHERE s.ShipperID = ?\r\n"
            		+ "AND s.Status IN (2, 3, 5)\r\n "
            		+ "ORDER BY s.AssignedAt DESC";
        	var a = jdbcTemplate.query(sql, mapper, id);
            return a.isEmpty() ? null : a.get(0);
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] findById(" + id + ") failed: " + ex.getMessage());
            return null;
        }
    }

    public boolean insertByOrder(Shipment s, int buyerId, String orderId) {
        String sql = "{call sp_CreateShipmentWithHistory(?, ?, ?, ?, ?, ?)}";

        try {
            return jdbcTemplate.execute(connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setString(1, s.getShipmentID());
                cs.setString(2, orderId);    
                cs.setInt(3, buyerId);    
                cs.setString(4, s.getPickupAddress());
                cs.setString(5, s.getDeliveryAddress());
                if (s.getNotes() != null && !s.getNotes().isEmpty()) {
                    cs.setString(6, s.getNotes());
                } else {
                    cs.setNull(6, java.sql.Types.NVARCHAR);
                }
                
                return cs;
            }, (CallableStatementCallback<Boolean>) cs -> {
                boolean hasResult = cs.execute();
                if (hasResult) {
                    ResultSet rs = cs.getResultSet();
                    if (rs.next()) {
                    	String result = rs.getString("Result");
                    	String msg = rs.getString("Message");
                    	System.out.println("🟢 [sp_CreateShipmentWithHistory] Result: " + result + " | " + msg);
                    	return "SUCCESS".equals(result);
                    }
                }
                return false;
            });

        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] insert() failed: " + ex.getMessage());
            return false;
        }
    }
    public boolean insertByRefund(Shipment s, int buyerId, int refundID) {
    	String sql = "{call sp_CreateReturnShipment(?, ?, ?, ?, ?, ?)}";
    	
    	try {
    		return jdbcTemplate.execute(connection -> {
    			CallableStatement cs = connection.prepareCall(sql);
    			cs.setString(1, s.getShipmentID());
    			cs.setInt(2, refundID);    
    			cs.setInt(3, buyerId);    
    			cs.setString(4, s.getDeliveryAddress());
    			cs.setString(5, s.getPickupAddress());
    			if (s.getNotes() != null && !s.getNotes().isEmpty()) {
    				cs.setString(6, s.getNotes());
    			} else {
    				cs.setNull(6, java.sql.Types.NVARCHAR);
    			}
    			
    			return cs;
    		}, (CallableStatementCallback<Boolean>) cs -> {
    			boolean hasResult = cs.execute();
    			if (hasResult) {
    				ResultSet rs = cs.getResultSet();
    				if (rs.next()) {
    					String result = rs.getString("Result");
    					String msg = rs.getString("Message");
    					System.out.println("🟢 [sp_CreateShipmentWithHistory] Result: " + result + " | " + msg);
    					return "SUCCESS".equals(result);
    				}
    			}
    			return false;
    		});
    		
    	} catch (DataAccessException ex) {
    		System.err.println("❌ [ShipmentRepository] insert() failed: " + ex.getMessage());
    		return false;
    	}
    }
    public boolean accept(Shipment s) {
    	String sql = "{call sp_ShipperAcceptShipment(?, ?)}";
    	
    	try {
    		return jdbcTemplate.execute(connection -> {
    			CallableStatement cs = connection.prepareCall(sql);
    			cs.setString(1, s.getShipmentID());
    			cs.setInt(2, s.getShipperID());   
    			
    			
    			return cs;
    		}, (CallableStatementCallback<Boolean>) cs -> {
    			boolean hasResult = cs.execute();
    			if (hasResult) {
    				ResultSet rs = cs.getResultSet();
    				if (rs.next()) {
    					String result = rs.getString("Result");
                    	String msg = rs.getString("Message");
                    	System.out.println("🟢 [sp_ShipperAcceptShipment] Result: " + result + " | " + msg);
                    	return "SUCCESS".equals(result);
    				}
    			}
    			return false;
    		});
    		
    	} catch (DataAccessException ex) {
    		System.err.println("❌ [ShipmentRepository] insert() failed: " + ex.getMessage());
    		return false;
    	}
    }
    
    public boolean updateStatus(Shipment s, String description) {
    	String sql = "{call sp_UpdateShipmentStatus(?, ?, ?, ?, ?, ?)}";
    	
    	try {
    		return jdbcTemplate.execute(connection -> {
    			CallableStatement cs = connection.prepareCall(sql);
    			cs.setString(1, s.getShipmentID());
    			cs.setInt(2, s.getShipperID());   
    			cs.setInt(3, s.getStatus());
    			 if (!("".equals(description)) && !description.isEmpty()) {
                     cs.setString(4, description);
                 } else {
                     cs.setNull(4, java.sql.Types.NVARCHAR);
                 }
    			 if (s.getFailedReason() != null && !s.getFailedReason().isEmpty()) {
                     cs.setString(5, s.getFailedReason());
                 } else {
                     cs.setNull(5, java.sql.Types.NVARCHAR);
                 }
    			 if (s.getProofImages() != null && !s.getProofImages().isEmpty()) {
                     cs.setString(6, s.getProofImages());
                 } else {
                     cs.setNull(6, java.sql.Types.NVARCHAR);
                 }
    			return cs;
    		}, (CallableStatementCallback<Boolean>) cs -> {
    			boolean hasResult = cs.execute();
    			if (hasResult) {
    				ResultSet rs = cs.getResultSet();
    				if (rs.next()) {
    					String result = rs.getString("Result");
                    	System.out.println("🟢 [sp_UpdateShipmentStatus] Result: " + result + " | ");
                    	return "SUCCESS".equals(result);
    				}
    			}
    			return false;
    		});
    		
    	} catch (DataAccessException ex) {
    		System.err.println("❌ [ShipmentRepository] insert() failed: " + ex.getMessage());
    		return false;
    	}
    }

    public boolean update(Shipment s) {
        String sql = """
            UPDATE Shipments
            SET ShipperID = ?, PickupAddress = ?, DeliveryAddress = ?, RecipientName = ?, RecipientPhone = ?,
                Status = ?, AssignedAt = ?, PickedAt = ?, DeliveredAt = ?, FailedReason = ?,
                ProofImages = ?, Notes = ?, UpdatedAt = GETDATE()
            WHERE ShipmentID = ?
        """;
        try {
            return jdbcTemplate.update(sql,
                    s.getShipperID(),
                    s.getPickupAddress(),
                    s.getDeliveryAddress(),
                    s.getRecipientName(),
                    s.getRecipientPhone(),
                    s.getStatus(),
                    s.getAssignedAt(),
                    s.getPickedAt(),
                    s.getDeliveredAt(),
                    s.getFailedReason(),
                    s.getProofImages(),
                    s.getNotes(),
                    s.getShipmentID()) > 0;
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] update() failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean delete(String shipmentID) {
        try {
            return jdbcTemplate.update("DELETE FROM Shipments WHERE ShipmentID = ?", shipmentID) > 0;
        } catch (DataAccessException ex) {
            System.err.println("❌ [ShipmentRepository] delete() failed: " + ex.getMessage());
            return false;
        }
    }
    
    public Shipment findByOrderId(String orderId) {
    	try {
			String sql = "select * from shipments where OrderID = ? ";
			var rs = jdbcTemplate.query(sql, mapper, orderId);
			return rs.isEmpty() ? null : rs.get(0);
			} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    }
}
