package projectSem4.com.model.repositories;




import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.dto.RefundOrderDTO;
import projectSem4.com.model.entities.Order;
import projectSem4.com.model.entities.RefundRequest;
import projectSem4.com.model.entities.Wallet;
import projectSem4.com.model.modelViews.OrderView;
import projectSem4.com.model.modelViews.RefundItemView;
import projectSem4.com.model.modelViews.RefundView;

@Repository
public class RefundRequestRepository {
	@Autowired
	private RefundItemRepository rIRepo;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ RefundRequest initialized. hashCode: " + System.identityHashCode(this));
	}
	private RowMapper<RefundRequest> rowMapperRefundRequest = new RowMapper<RefundRequest>() {
	    @Override
	    public RefundRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
	        Integer RefundID = rs.getInt("RefundID");
	        String OrderID = rs.getString("OrderID");
	        Integer PaymentID = rs.getInt("PaymentID");
	        Integer BuyerID = rs.getInt("BuyerID");
	        Integer ShopID = rs.getInt("ShopID");
	        Integer ProcessedBy = rs.getInt("ProcessedBy");
	        Double Amount = rs.getDouble("Amount");
	        Double OriginalAmount = rs.getDouble("OriginalAmount");
	        String Reason = rs.getString("Reason");
	        String Status = rs.getString("Status");
	        String Evidence = rs.getString("Evidence");
	        String RefundType = rs.getString("RefundType");
	        String RefundMethod = rs.getString("RefundMethod");
	        String history = rs.getString("History");
	        String Notes = rs.getString("Notes"); // 🟡 có vẻ bị gán nhầm?

	        LocalDateTime CreatedAt   = toLocalDateTimeOrNull(rs.getTimestamp("CreatedAt"));
	        LocalDateTime UpdatedAt   = toLocalDateTimeOrNull(rs.getTimestamp("UpdatedAt"));
	        LocalDateTime ProcessDate = toLocalDateTimeOrNull(rs.getTimestamp("ProcessDate"));
	        LocalDateTime adminApprovedAt   = toLocalDateTimeOrNull(rs.getTimestamp("AdminApprovedAt"));
	        LocalDateTime adminRejectedAt   = toLocalDateTimeOrNull(rs.getTimestamp("AdminRejectedAt"));
	        LocalDateTime shopRejectedAt = toLocalDateTimeOrNull(rs.getTimestamp("ShopRejectedAt"));
	        String adminNotes = rs.getString("AdminNotes"); 
	        Integer processedByAdmin = rs.getInt("ProcessedByAdmin");
	        return new RefundRequest(
	            RefundID, OrderID, PaymentID, BuyerID, ShopID, Amount,
	            Reason, Status, Evidence, CreatedAt, UpdatedAt, ProcessDate,
	            RefundType, Notes, OriginalAmount, history, RefundMethod,
	            ProcessedBy,adminApprovedAt,adminRejectedAt,adminNotes,processedByAdmin,shopRejectedAt
	        );
	    }

	    private LocalDateTime toLocalDateTimeOrNull(Timestamp ts) {
	        return ts != null ? ts.toLocalDateTime() : null;
	    }
	};
	
	public Integer create(RefundRequest request) {

	   
	    KeyHolder keyHolder = new GeneratedKeyHolder();

	    try {
	        jdbcTemplate.update(con -> {
	        	String sql = "";
	    	    if("VNPAY".equals(request.getRefundMethod())){
	    	    	sql = """
	    	    	        INSERT INTO RefundRequests (OrderID, PaymentID, BuyerID, ShopID, Amount, Reason, Status, Evidence, CreatedAt,
	    	    	        RefundType,RefundMethod,OriginalAmount
	    	    	        )
	    	    	        VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), ?, ?, ?)
	    	    	        """;
	    	    } else {
	    	    	sql = """
	    	        INSERT INTO RefundRequests (OrderID, BuyerID, ShopID, Amount, Reason, Status, Evidence, CreatedAt,
	    	        RefundType,RefundMethod,OriginalAmount
	    	        )
	    	        VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), ?, ?, ?)
	    	        """;
	    	    }
	        	int idx = 1;
	            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	            ps.setString(idx++, request.getOrderId());
	            if(request.getPaymentId() != null) {
	            	ps.setInt(idx++, request.getPaymentId());
	            }
	            
	            ps.setInt(idx++, request.getBuyerId());
	            ps.setInt(idx++, request.getShopId());
	            ps.setDouble(idx++, request.getAmount());
	            ps.setString(idx++, request.getReason());
	            ps.setString(idx++, request.getStatus());
	            ps.setString(idx++, request.getEvidence());
	            ps.setString(idx++, request.getRefundType());
	            ps.setString(idx++, request.getRefundMethod());
	            ps.setDouble(idx++, request.getOriginalAmount());
	            return ps;
	        }, keyHolder);

	        // Lấy ID vừa insert
	        Number key = keyHolder.getKey();
	        if (key != null) {
	            return key.intValue();
	        }
	        return null;

	    } catch (DataAccessException e) {
	        System.err.println("❌ Lỗi khi tạo refund request: " + e.getMessage());
	        return null;
	    }
	}

    // Tìm theo ID
    public RefundRequest findById(int refundRequestId) {
        String sql = "SELECT * FROM RefundRequests WHERE RefundID = ?";
        try {
            List<RefundRequest> result = jdbcTemplate.query(sql, rowMapperRefundRequest, refundRequestId);
            return result.isEmpty() ? null : result.get(0) ;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi tìm refund request: " + e.getMessage());
            return null;
        }
    }
    
    public RefundRequest findByIdAndShop(int refundRequestId, int shopId) {
        String sql = "SELECT * FROM RefundRequests WHERE RefundID = ? AND ShopID = ?";
        try {
            List<RefundRequest> result = jdbcTemplate.query(sql, rowMapperRefundRequest, refundRequestId, shopId);
            return result.isEmpty() ? null : result.get(0) ;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi tìm refund request: " + e.getMessage());
            return null;
        }
    }
    
    public RefundRequest findByIdAndBuyer(int refundRequestId, int buyerId) {
        String sql = "SELECT * FROM RefundRequests WHERE RefundID = ? AND BuyerID = ?";
        try {
            List<RefundRequest> result = jdbcTemplate.query(sql, rowMapperRefundRequest, refundRequestId, buyerId);
            return result.isEmpty() ? null : result.get(0) ;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi tìm refund request: " + e.getMessage());
            return null;
        }
    }

    // Tìm theo Order
    public RefundRequest findByOrder(String orderId) {
        String sql = "SELECT * FROM RefundRequests WHERE OrderID = ?";
        try {
        	var r = jdbcTemplate.query(sql, rowMapperRefundRequest, orderId);
            return r.isEmpty() ? null : r.get(0);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tìm refund theo order: " + e.getMessage());
            return null;
        }
    }
 // Tìm theo Order
    public RefundView findViewByOrder(String orderId) {
    	String sql = """
                SELECT r.RefundID, r.OrderID, r.PaymentID, r.BuyerID, u.FullName AS buyerName, u.PhoneNumber AS buyerPhone,
                       r.ShopID, r.Amount, r.Reason, r.Status, r.Evidence, r.CreatedAt, r.UpdatedAt, r.ProcessDate,
                       r.RefundType, r.Notes, r.OriginalAmount, r.History, r.RefundMethod, r.ProcessedBy, r.ProcessedByAdmin,
                       r.AdminApprovedAt, r.AdminRejectedAt, r.ShopRejectedAt, r.AdminNotes
                FROM RefundRequests r
                JOIN Users u ON r.BuyerID = u.UserID
                WHERE r.OrderID = ?
            """;

            try {
                return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                    RefundView r = new RefundView();
                    r.setRefundId(rs.getInt("RefundID"));
                    r.setOrderId(rs.getString("OrderID"));
                    r.setPaymentId(rs.getString("PaymentID"));
                    r.setBuyerId(rs.getInt("BuyerID"));
                    r.setBuyerName(rs.getString("buyerName"));
                    r.setBuyerPhone(rs.getString("buyerPhone"));
                    r.setShopId(rs.getInt("ShopID"));
                    r.setAmount(rs.getDouble("Amount"));
                    r.setReason(rs.getString("Reason"));
                    r.setStatus(rs.getString("Status"));
                    r.setEvidence(rs.getString("Evidence"));
                    r.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                    r.setUpdatedAt(rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null);
                    r.setProcessDate(rs.getTimestamp("ProcessDate") != null ? rs.getTimestamp("ProcessDate").toLocalDateTime() : null);
                    r.setRefundType(rs.getString("RefundType"));
                    r.setNotes(rs.getString("Notes"));
                    r.setOriginalAmount(rs.getDouble("OriginalAmount"));
                    r.setHistory(rs.getString("History"));
                    r.setRefundMethod(rs.getString("RefundMethod"));
                    r.setProcessedBy(rs.getInt("ProcessedBy"));
                    r.setAdminApprovedAt(rs.getTimestamp("AdminApprovedAt") != null ? rs.getTimestamp("AdminApprovedAt").toLocalDateTime() : null);
                    r.setAdminNotes(rs.getString("AdminNotes"));
                    r.setAdminRejectedAt(rs.getTimestamp("AdminRejectedAt") != null ? rs.getTimestamp("AdminRejectedAt").toLocalDateTime() : null);
                    r.setProcessedByAdmin(rs.getInt("ProcessedByAdmin"));
                    r.setShopRejectedAt(rs.getTimestamp("ShopRejectedAt") != null ? rs.getTimestamp("ShopRejectedAt").toLocalDateTime() : null);
                    return r;
                }, orderId);
            } catch (EmptyResultDataAccessException e) {
                // Không tìm thấy dữ liệu, trả về null
                return null;
            } catch (DataAccessException e) {
                // Lỗi khác từ database
                e.printStackTrace();
                return null;
            }
    }

    // Cập nhật trạng thái
    public boolean updateStatusByShop(RefundRequest data) {
        try {
            String sql;
            Object[] params;

            switch (data.getStatus()) {
                case "SHOP_REJECTED":
                	  sql = "UPDATE RefundRequests SET Status = ?, Notes = ?, ProcessedBy = ?, ShopRejectedAt = SYSDATETIME(), UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
                      params = new Object[]{data.getStatus(), data.getNotes(), data.getProcessedBy(), data.getRefundId()};
                      break;
                case "SHOP_APPROVED":
                	sql = "UPDATE RefundRequests SET Status = ?, Notes = ?, ProcessedBy = ?, ProcessDate = SYSDATETIME(), UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
                	params = new Object[]{data.getStatus(), data.getNotes(), data.getProcessedBy(), data.getRefundId()};
                	break;
                case "WAITING_FOR_RETURN":
                    sql = "UPDATE RefundRequests SET Status = ?, ProcessedBy = ?, ProcessDate = ?, UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
                    params = new Object[]{data.getStatus(), data.getProcessedBy(), data.getProcessDate(), data.getRefundId()};
                    break;

                default:
                    sql = "UPDATE RefundRequests SET Status = ?, UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
                    params = new Object[]{data.getStatus(), data.getRefundId()};
                    break;
            }

            int rows = jdbcTemplate.update(sql, params);
            
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi cập nhật trạng thái refund: " + e.getMessage());
            return false;
        }
    }
    public boolean updateStatusByAdmin(RefundRequest data) {
    	try {
    		String sql;
    		Object[] params;
    		
    		switch (data.getStatus()) {
    		case "ADMIN_APPROVED":
    			sql = "UPDATE RefundRequests SET Status = ?, AdminNotes = ?, ProcessedByAdmin = ?, AdminApprovedAt = ?, UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
    			params = new Object[]{data.getStatus(), data.getAdminNotes(), data.getProcessedByAdmin(), data.getAdminApprovedAt(), data.getRefundId()};
    			break;
    		case "ADMIN_REJECT":
    			sql = "UPDATE RefundRequests SET Status = ?, AdminNotes = ?, ProcessedByAdmin = ?, AdminRejectedAt = ?, UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
    			params = new Object[]{data.getStatus(), data.getAdminNotes(), data.getProcessedByAdmin(), data.getAdminRejectedAt(), data.getRefundId()};
    			break;
    		default:
    			sql = "UPDATE RefundRequests SET Status = ?, UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
    			params = new Object[]{data.getStatus(), data.getRefundId()};
    			break;
    		}
    		
    		int rows = jdbcTemplate.update(sql, params);
    		
    		return rows > 0;
    	} catch (DataAccessException e) {
    		System.err.println("❌ Lỗi khi cập nhật trạng thái refund: " + e.getMessage());
    		return false;
    	}
    }
    public boolean updateStatusByBuyer(int refundRequestId, String status) {
        String sql = "UPDATE RefundRequests SET Status = ?, UpdatedAt = SYSDATETIME() WHERE RefundID = ?";
        try {
            int rows = jdbcTemplate.update(sql, status, refundRequestId);
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi cập nhật trạng thái refund: " + e.getMessage());
            return false;
        }
    }

    // Xóa
    public boolean delete(int refundRequestId) {
        String sql = "DELETE FROM RefundRequests WHERE RefundRequestID = ?";
        try {
            int rows = jdbcTemplate.update(sql, refundRequestId);
            return rows > 0;
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi xóa refund request: " + e.getMessage());
            return false;
        }
    }

    // Lấy tất cả
    public List<RefundRequest> findAll() {
        String sql = "SELECT * FROM RefundRequests";
        try {
            return jdbcTemplate.query(sql, rowMapperRefundRequest);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi lấy danh sách refund: " + e.getMessage());
            return List.of();
        }
    }
 // Lấy tất cả
    public List<RefundRequest> findAllByBuyer(int userID) {
        String sql = "SELECT * FROM RefundRequests WHERE BuyerID = ? Orderby UpdatedAt desc";
        try {
            return jdbcTemplate.query(sql, rowMapperRefundRequest,userID);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi lấy danh sách refund: " + e.getMessage());
            return List.of();
        }
    }
    
    public List<RefundRequest> findAllByShop(int shoppID) {
        String sql = "SELECT * FROM RefundRequests WHERE ShopID = ? Orderby UpdatedAt desc";
        try {
            return jdbcTemplate.query(sql, rowMapperRefundRequest, shoppID);
        } catch (DataAccessException e) {
            System.err.println("❌ Lỗi khi lấy danh sách refund: " + e.getMessage());
            return List.of();
        }
    }
    
    public Map<String, Object> filterRefundByShopId(int page, int size, String status, int shopId, String keyword, Date date) {
        Map<String, Object> res = new HashMap<>();
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("FilterRefundsByShop")
                .declareParameters(
                    new SqlParameter("PageIndex", Types.INTEGER),
                    new SqlParameter("PageSize", Types.INTEGER),
                    new SqlParameter("ShopId", Types.INTEGER),
                    new SqlParameter("Status", Types.NVARCHAR),
                    new SqlParameter("Keyword", Types.NVARCHAR),
	                new SqlParameter("Date", Types.DATE),
                    new SqlOutParameter("TotalPages", Types.INTEGER),
                    new SqlOutParameter("TotalRows", Types.INTEGER)
                )
                .returningResultSet("refunds", new BeanPropertyRowMapper<>(RefundView.class));

            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("PageIndex", page)
                .addValue("PageSize", size)
                .addValue("Status", status != null && !status.equals("0") ? status : null)
                .addValue("ShopId", shopId)
                .addValue("Keyword", keyword == "" ? null : keyword)
	            .addValue("Date", date != null ? new java.sql.Date(date.getTime()) : null);

            Map<String, Object> result = call.execute(params);

            @SuppressWarnings("unchecked")
            List<RefundView> refunds = (List<RefundView>) result.get("refunds");

            res.put("refunds", refunds);
            res.put("totalRows", result.get("TotalRows"));
            res.put("totalPages", result.get("TotalPages"));
            res.put("page", page);

        } catch (Exception e) {
            e.printStackTrace();
            res = null;
        }
        return res;
    }
    public Map<String, Object> filterRefundByAdmin(int page, int size, String status, String keyword, Date date) {
    	Map<String, Object> res = new HashMap<>();
    	try {
    		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
    				.withProcedureName("FilterRefundsByAdmin")
    				.declareParameters(
    						new SqlParameter("PageIndex", Types.INTEGER),
    						new SqlParameter("PageSize", Types.INTEGER),
    						new SqlParameter("Status", Types.NVARCHAR),
    						new SqlParameter("Keyword", Types.NVARCHAR),
    						new SqlParameter("Date", Types.DATE),
    						new SqlOutParameter("TotalPages", Types.INTEGER),
    						new SqlOutParameter("TotalRows", Types.INTEGER)
    						)
    				.returningResultSet("refunds", new BeanPropertyRowMapper<>(RefundView.class));
    		
    		MapSqlParameterSource params = new MapSqlParameterSource()
    				.addValue("PageIndex", page)
    				.addValue("PageSize", size)
    				.addValue("Status", status != null && !status.equals("0") ? status : null)
    				.addValue("Keyword", keyword == "" ? null : keyword)
    				.addValue("Date", date != null ? new java.sql.Date(date.getTime()) : null);
    		
    		Map<String, Object> result = call.execute(params);
    		
    		@SuppressWarnings("unchecked")
    		List<RefundView> refunds = (List<RefundView>) result.get("refunds");
    		 // 🔽 Gọi repo RefundItem để gắn vào từng Refund
            for (RefundView refund : refunds) {
                List<RefundItemView> items = rIRepo.getViewAllByRefundId(refund.getRefundId());
                refund.setRefundItem(items);
            }
    		
    		res.put("refunds", refunds);
    		res.put("totalRows", result.get("TotalRows"));
    		res.put("totalPages", result.get("TotalPages"));
    		res.put("page", page);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		res = null;
    	}
    	return res;
    }

    
    public List<RefundOrderDTO> fillterRefundByBuyerId(int userId, String refundStatus, int pageIndex, int pageSize) {
    	// Gọi procedure
    	try {
    		String sql = "EXEC GetRefundRequestsPagedByBuyer @UserID=?, @RefundStatus=?, @PageIndex=?, @PageSize=?";
            return jdbcTemplate.query(
                sql,
                BeanPropertyRowMapper.newInstance(RefundOrderDTO.class),
                userId, refundStatus, pageIndex, pageSize
            );     
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    	
	}
//    public Map<String, Object> fillterRefundByBuyerId(int page, int size, String status, int buyerId) {
//    	Map<String, Object> res = new HashMap<>();
//    	try {
//    		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
//    				.withProcedureName("GetRefundRequestsPagedByBuyer")
//    				.declareParameters(
//    						new SqlParameter("PageIndex", Types.INTEGER),
//    						new SqlParameter("PageSize", Types.INTEGER),
//    						new SqlParameter("BuyerId", Types.INTEGER),
//    						new SqlParameter("Status", Types.NVARCHAR),
//    						new SqlOutParameter("TotalPages", Types.INTEGER),
//    						new SqlOutParameter("TotalRows", Types.INTEGER)
//    						)
//    				.returningResultSet("refunds", new BeanPropertyRowMapper<>(RefundRequest.class));
//    		
//    		MapSqlParameterSource params = new MapSqlParameterSource()
//    				.addValue("PageIndex", page)
//    				.addValue("PageSize", size)
//    				.addValue("Status", status)
//    				.addValue("BuyerId", buyerId);
//    		
//    		Map<String, Object> result = call.execute(params);
//    		
//    		@SuppressWarnings("unchecked")
//    		List<RefundRequest> refunds = (List<RefundRequest>) result.get("refunds");
//    		
//    		res.put("refunds", refunds);
//    		res.put("totalRows", result.get("TotalRows"));
//    		res.put("totalPages", result.get("TotalPages"));
//    		res.put("page", page);
//    		
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    		res = null;
//    	}
//    	return res;
//    }
    
    public RefundView getRefundById(Integer refundId) {
        String sql = """
            SELECT r.RefundID, r.OrderID, r.PaymentID, r.BuyerID, u.FullName AS buyerName, u.PhoneNumber AS buyerPhone,
                   r.ShopID, r.Amount, r.Reason, r.Status, r.Evidence, r.CreatedAt, r.UpdatedAt, r.ProcessDate,
                   r.RefundType, r.Notes, r.OriginalAmount, r.History, r.RefundMethod, r.ProcessedBy, r.AdminApprovedAt,
                   r.AdminRejectedAt, r.ShopRejectedAt, r.AdminNotes, r.ProcessedByAdmin
            FROM RefundRequests r
            JOIN Users u ON r.BuyerID = u.UserID
            WHERE r.RefundID = ?
        """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                RefundView r = new RefundView();
                r.setRefundId(rs.getInt("RefundID"));
                r.setOrderId(rs.getString("OrderID"));
                r.setPaymentId(rs.getString("PaymentID"));
                r.setBuyerId(rs.getInt("BuyerID"));
                r.setBuyerName(rs.getString("buyerName"));
                r.setBuyerPhone(rs.getString("buyerPhone"));
                r.setShopId(rs.getInt("ShopID"));
                r.setAmount(rs.getDouble("Amount"));
                r.setReason(rs.getString("Reason"));
                r.setStatus(rs.getString("Status"));
                r.setEvidence(rs.getString("Evidence"));
                r.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                r.setUpdatedAt(rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null);
                r.setProcessDate(rs.getTimestamp("ProcessDate") != null ? rs.getTimestamp("ProcessDate").toLocalDateTime() : null);
                r.setRefundType(rs.getString("RefundType"));
                r.setNotes(rs.getString("Notes"));
                r.setOriginalAmount(rs.getDouble("OriginalAmount"));
                r.setHistory(rs.getString("History"));
                r.setRefundMethod(rs.getString("RefundMethod"));
                r.setProcessedBy(rs.getInt("ProcessedBy"));
                r.setAdminApprovedAt(rs.getTimestamp("AdminApprovedAt") != null ? rs.getTimestamp("AdminApprovedAt").toLocalDateTime() : null);
                r.setAdminNotes(rs.getString("AdminNotes"));
                r.setAdminRejectedAt(rs.getTimestamp("AdminRejectedAt") != null ? rs.getTimestamp("AdminRejectedAt").toLocalDateTime() : null);
                r.setProcessedByAdmin(rs.getInt("ProcessedByAdmin"));
                r.setShopRejectedAt(rs.getTimestamp("ShopRejectedAt") != null ? rs.getTimestamp("ShopRejectedAt").toLocalDateTime() : null);
                return r;
            }, refundId);
        } catch (EmptyResultDataAccessException e) {
            // Không tìm thấy dữ liệu, trả về null
            return null;
        } catch (DataAccessException e) {
            // Lỗi khác từ database
            e.printStackTrace();
            return null;
        }
    }

}
