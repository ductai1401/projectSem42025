package projectSem4.com.model.repositories;



import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import projectSem4.com.controller.apiController.client.WishlistItemApiController;
import projectSem4.com.model.entities.PlatformEarning;
import projectSem4.com.model.modelViews.RefundView;
import projectSem4.com.service.client.SearchKeyService;

@Repository
public class PlatformEarningRepository {

    private final SearchKeyService searchKeyService;

    private final WishlistItemApiController wishlistItemApiController;
	private final JdbcTemplate jdbcTemplate;

    public PlatformEarningRepository(JdbcTemplate jdbcTemplate, WishlistItemApiController wishlistItemApiController, SearchKeyService searchKeyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.wishlistItemApiController = wishlistItemApiController;
        this.searchKeyService = searchKeyService;
    }
    
    private RowMapper<PlatformEarning> rowMapperForPlatformEarning = new RowMapper<PlatformEarning>() {
		
    	@Override
        public PlatformEarning mapRow(ResultSet rs, int rowNum) throws SQLException {
            PlatformEarning e = new PlatformEarning();
            e.setEarningId(rs.getInt("EarningID"));
            e.setOrderId(rs.getString("OrderID"));
            e.setShopId(rs.getInt("ShopID"));
            e.setBaseAmount(rs.getBigDecimal("BaseAmount"));
            e.setDiscountPlatform(rs.getBigDecimal("DiscountPlatform"));
            e.setDiscountShop(rs.getBigDecimal("DiscountShop"));
            e.setShippingFee(rs.getBigDecimal("ShippingFee"));
            e.setVoucherCode(rs.getString("VoucherCode"));
            e.setCommissionRate(rs.getBigDecimal("CommissionRate"));
            e.setCommissionAmount(rs.getBigDecimal("CommissionAmount"));
            e.setRefundedAmount(rs.getBigDecimal("RefundedAmount"));
            e.setRefundedCommission(rs.getBigDecimal("RefundedCommission"));
            e.setAdjustedCommission(rs.getBigDecimal("AdjustedCommission"));
            e.setPlatformGrossIncome(rs.getBigDecimal("PlatformGrossIncome"));
            e.setPlatformNetIncome(rs.getBigDecimal("PlatformNetIncome"));
            e.setShopNetIncome(rs.getBigDecimal("ShopNetIncome"));
            e.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            e.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
            return e;
        }
	};

    // Thêm bản ghi mới khi đơn hàng thành công
    public int insert(PlatformEarning e) {
    	try {
    		String sql = """
    	            INSERT INTO PlatformEarnings (
    	                OrderID, ShopID, BaseAmount, DiscountPlatform, DiscountShop, ShippingFee, VoucherCode,
    	                CommissionRate, CommissionAmount, RefundedAmount, RefundedCommission, AdjustedCommission,
    	                PlatformNetIncome, ShopNetIncome
    	            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    	        """;

    	        return jdbcTemplate.update(sql,
    	                e.getOrderId(),
    	                e.getShopId(),
    	                e.getBaseAmount(),
    	                e.getDiscountPlatform(),
    	                e.getDiscountShop(),
    	                e.getShippingFee(),
    	                e.getVoucherCode(),
    	                e.getCommissionRate(),
    	                e.getCommissionAmount(),
    	                e.getRefundedAmount(),
    	                e.getRefundedCommission(),
    	                e.getAdjustedCommission(),
    	                e.getPlatformNetIncome(),
    	                e.getShopNetIncome()
    	        );
		} catch (Exception e2) {
			e2.printStackTrace();
			return 0;
		}
        
    }

    // Cập nhật thông tin refund
    public int updateRefund(String orderId, double refundedAmount, double refundedCommission,
                            double adjustedCommission, double platformNetIncome, double shopNetIncome) {
        
        
        try {
        	String sql = """
                    UPDATE PlatformEarnings
                    SET RefundedAmount = ?, RefundedCommission = ?, AdjustedCommission = ?,
                        PlatformNetIncome = ?, ShopNetIncome = ?, UpdatedAt = SYSDATETIME()
                    WHERE OrderID = ?
                """;
                return jdbcTemplate.update(sql,
                        refundedAmount, refundedCommission, adjustedCommission,
                        platformNetIncome, shopNetIncome, orderId
                );
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
    }

    public List<PlatformEarning> findByShopId(int shopID, int pageIndex, int pageSize) {
       
        try {
        	 String sql = """
        	            SELECT * FROM PlatformEarnings
        	            WHERE ShopID = ?
        	            ORDER BY CreatedAt DESC
        	            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        	        """;
        	        int offset = (pageIndex - 1) * pageSize;
        	        return jdbcTemplate.query(sql, rowMapperForPlatformEarning, shopID, offset, pageSize);
        	        
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
			
		}
    }

    // ---------------------------
    // 2️⃣ Lấy danh sách trong khoảng thời gian
    // ---------------------------
    public Map<String, Object> findByShopIdAndDateRange(
            int shopId, Date startDate, Date endDate, int pageIndex, int pageSize) {
    	Map<String, Object> res = new HashMap<>();
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("FilterPlatformEarningsByShop")
                .declareParameters(
                    new SqlParameter("PageIndex", Types.INTEGER),
                    new SqlParameter("PageSize", Types.INTEGER),
                    new SqlParameter("ShopId", Types.INTEGER),
                    new SqlParameter("StartDate", Types.NVARCHAR),
                    new SqlParameter("EndDate", Types.NVARCHAR),
                    new SqlOutParameter("TotalPages", Types.INTEGER),
                    new SqlOutParameter("TotalRows", Types.INTEGER)
                )
                .returningResultSet("earnings", rowMapperForPlatformEarning);

            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("PageIndex", pageIndex)
                .addValue("PageSize", pageSize)
                .addValue("StartDate", startDate != null ? new java.sql.Date(startDate.getTime()) : null)
                .addValue("ShopId", shopId)
                .addValue("EndDate", endDate != null ? new java.sql.Date(endDate.getTime()) : null);

            Map<String, Object> result = call.execute(params);

            @SuppressWarnings("unchecked")
            List<RefundView> earnings = (List<RefundView>) result.get("earnings");

            res.put("earnings", earnings);
            res.put("totalRows", result.get("TotalRows"));
            res.put("totalPages", result.get("TotalPages"));
            res.put("page", pageIndex);

        } catch (Exception e) {
            e.printStackTrace();
            res = null;
        }
        return res;
    }

 // 🧮 Tổng BaseAmount theo Shop
    public BigDecimal sumBaseAmountByShopID(Integer shopID) {
    	try {
    		String sql = "SELECT COALESCE(SUM(BaseAmount - RefundedAmount), 0) FROM PlatformEarnings WHERE ShopID = ?";
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, shopID);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
    }

    // 💸 Tổng Commission theo Shop
    public BigDecimal sumCommissionByShopID(Integer shopID) {
    	try {
    		String sql = "SELECT COALESCE(SUM(AdjustedCommission), 0) FROM PlatformEarnings WHERE ShopID = ?";
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, shopID);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
        
    }

    // 💰 Tổng Net Income theo Shop
    public BigDecimal sumShopNetIncomeByShopID(Integer shopID) {
       
        try {
        	 String sql = "SELECT COALESCE(SUM(ShopNetIncome), 0) FROM PlatformEarnings WHERE ShopID = ?";
             return jdbcTemplate.queryForObject(sql, BigDecimal.class, shopID);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    }

    // 📅 BaseAmount trong khoảng ngày
    public BigDecimal sumBaseAmountByShopIDAndDateRange(Integer shopID, LocalDateTime start, LocalDateTime end) {
        
        try {
        	String sql = """
                    SELECT COALESCE(SUM(BaseAmount - Refunded	Amount), 0)
                    FROM PlatformEarnings
                    WHERE ShopID = ? AND CreatedAt BETWEEN ? AND ?
                """;
                return jdbcTemplate.queryForObject(sql, BigDecimal.class,
                        shopID, Timestamp.valueOf(start), Timestamp.valueOf(end));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    }

    // 📅 Commission trong khoảng ngày
    public BigDecimal sumCommissionByShopIDAndDateRange(Integer shopID, LocalDateTime start, LocalDateTime end) {
        
        try {
        	String sql = """
                    SELECT COALESCE(SUM(AdjustedCommission), 0)
                    FROM PlatformEarnings
                    WHERE ShopID = ? AND CreatedAt BETWEEN ? AND ?
                """;
                return jdbcTemplate.queryForObject(sql, BigDecimal.class,
                        shopID, Timestamp.valueOf(start), Timestamp.valueOf(end));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    }

    // 📅 NetIncome trong khoảng ngày
    public BigDecimal sumShopNetIncomeByShopIDAndDateRange(Integer shopID, LocalDateTime start, LocalDateTime end) {
       
        try {
        	 String sql = """
        	            SELECT COALESCE(SUM(ShopNetIncome), 0)
        	            FROM PlatformEarnings
        	            WHERE ShopID = ? AND CreatedAt BETWEEN ? AND ?
        	        """;
        	        return jdbcTemplate.queryForObject(sql, BigDecimal.class,
        	                shopID, Timestamp.valueOf(start), Timestamp.valueOf(end));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    }

    // 📦 Đếm tổng số đơn hàng
    public Integer countOrdersByShopID(Integer shopID) {
    	try {
    		String sql = "SELECT COUNT(*) FROM PlatformEarnings WHERE ShopID = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, shopID);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
        
    }

    // 📑 Lấy dữ liệu phân trang
    public List<PlatformEarning> findByShopIDPaged(Integer shopID, int pageIndex, int pageSize) {
    	try {
    		String sql = """
    	            SELECT * FROM PlatformEarnings
    	            WHERE ShopID = ?
    	            ORDER BY CreatedAt DESC
    	            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    	        """;
    	        int offset = (pageIndex - 1) * pageSize;
    	        return jdbcTemplate.query(sql, rowMapperForPlatformEarning, shopID, offset, pageSize);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
        
    }

    // 📑 Lấy dữ liệu phân trang có lọc ngày
    public List<PlatformEarning> findByShopIDAndCreatedAtBetweenPaged(Integer shopID, LocalDateTime start, LocalDateTime end, int pageIndex, int pageSize) {
        
        try {
        	String sql = """
                    SELECT * FROM PlatformEarnings
                    WHERE ShopID = ? AND CreatedAt BETWEEN ? AND ?
                    ORDER BY CreatedAt DESC
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
                int offset = (pageIndex - 1) * pageSize;
                return jdbcTemplate.query(sql, rowMapperForPlatformEarning, shopID,
                        Timestamp.valueOf(start), Timestamp.valueOf(end),
                        offset, pageSize);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    }

    // ✅ Cập nhật trạng thái
    public int updateStatus(int withdrawId, String status) {
    	try {
    		String sql = "UPDATE WithdrawRequests SET Status=?, UpdatedAt=GETDATE() WHERE WithdrawID=?";
            return jdbcTemplate.update(sql, status, withdrawId);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}
        
    }
 /// Không lọc theo ngày
    public List<PlatformEarning> findEarningsByShop(Integer shopID, int offset, int size) {
        String sql = """
            SELECT * FROM PlatformEarnings
            WHERE ShopID = ?
            ORDER BY CreatedAt DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;
        return jdbcTemplate.query(sql, rowMapperForPlatformEarning, shopID, offset, size);
    }

    public int countEarningsByShop(Integer shopID) {
        String sql = "SELECT COUNT(*) FROM PlatformEarnings WHERE ShopID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, shopID);
    }

    // Có lọc theo ngày
    public List<PlatformEarning> findEarningsByShop(Integer shopID, LocalDateTime startDate,
                                                    LocalDateTime endDate, int offset, int size) {
        String sql = """
            SELECT * FROM PlatformEarnings
            WHERE ShopID = ?
              AND CreatedAt BETWEEN ? AND ?
            ORDER BY CreatedAt DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;
        return jdbcTemplate.query(sql, rowMapperForPlatformEarning, shopID, startDate, endDate, offset, size);
    }

    public int countEarningsByShop(Integer shopID, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COUNT(*) FROM PlatformEarnings
            WHERE ShopID = ? AND CreatedAt BETWEEN ? AND ?
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, shopID, startDate, endDate);
    }
}
