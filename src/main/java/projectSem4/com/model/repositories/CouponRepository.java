package projectSem4.com.model.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.enums.CouponEnums.CouponType;
import projectSem4.com.model.enums.CouponEnums.DiscountType;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponRepository {

	private final JdbcTemplate jdbc;

	public CouponRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	private final RowMapper<Coupon> mapper = (rs, rowNum) -> {
		Coupon c = new Coupon();
		c.setCouponId(rs.getInt("CouponID"));
		c.setCode(rs.getString("Code"));
		c.setCouponType(rs.getString("CouponType"));
		c.setDiscountType(String.valueOf(rs.getString("DiscountType")));
		c.setDiscountValue(rs.getBigDecimal("DiscountValue"));
		c.setMaxDiscount(rs.getBigDecimal("MaxDiscount"));
		c.setMinOrderAmount(rs.getBigDecimal("MinOrderAmount"));
		c.setStartDate(rs.getTimestamp("StartDate").toLocalDateTime());
		c.setEndDate(rs.getTimestamp("EndDate").toLocalDateTime());
		c.setUsageLimit(rs.getInt("UsageLimit"));
		c.setUsedCount(rs.getInt("UsedCount"));
		int shopId = rs.getInt("ShopID");
		c.setShopId(rs.wasNull() ? null : shopId);
		c.setStatus(rs.getInt("Status"));
		return c;
	};

	public List<Coupon> findAllPlatform(int page, int size, String q) {
		String sql = """
				    SELECT * FROM Coupons
				    WHERE ShopID IS NULL
				      AND (? = '' OR Code LIKE '%' + ? + '%')
				    ORDER BY CouponID DESC
				    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
				""";
		String kw = q == null ? "" : q;
		return jdbc.query(sql, mapper, kw, kw, page * size, size);
	}

	public int countPlatform(String q) {
		String sql = """
				    SELECT COUNT(*) FROM Coupons
				    WHERE ShopID IS NULL AND (? = '' OR Code LIKE '%' + ? + '%')
				""";
		String kw = q == null ? "" : q;
		return jdbc.queryForObject(sql, Integer.class, kw, kw);
	}

	public List<Coupon> findAllByShop(int shopId, int page, int size, String q) {
		String sql = """
				    SELECT * FROM Coupons
				    WHERE ShopID = ?
				      AND (? = '' OR Code LIKE '%' + ? + '%')
				    ORDER BY CouponID DESC
				    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
				""";
		String kw = q == null ? "" : q;
		return jdbc.query(sql, mapper, shopId, kw, kw, page * size, size);
	}

	public int countByShop(int shopId, String q) {
		String sql = """
				    SELECT COUNT(*) FROM Coupons
				    WHERE ShopID = ? AND (? = '' OR Code LIKE '%' + ? + '%')
				""";
		String kw = q == null ? "" : q;
		return jdbc.queryForObject(sql, Integer.class, shopId, kw, kw);
	}

	public Optional<Coupon> findById(int id) {
		String sql = "SELECT * FROM Coupons WHERE CouponID = ?";
		var list = jdbc.query(sql, mapper, id);
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	public boolean existsCodePlatform(String code) {
		String sql = "SELECT 1 FROM Coupons WHERE Code = ? AND ShopID IS NULL";
		return !jdbc.query(sql, (rs, rn) -> 1, code).isEmpty();
	}

	public boolean existsCodeInShop(String code, int shopId) {
		String sql = "SELECT 1 FROM Coupons WHERE Code = ? AND ShopID = ?";
		return !jdbc.query(sql, (rs, rn) -> 1, code, shopId).isEmpty();
	}

	public void insertPlatform(Coupon c) {
		try {
			String sql = """
					  INSERT INTO Coupons
					  (Code, CouponType, DiscountType, DiscountValue, MaxDiscount, MinOrderAmount,
					   StartDate, EndDate, UsageLimit, UsedCount, ShopID, Status)
					  VALUES (?,?,?,?,?,?,?,?,?,?,NULL,?)
					""";

			jdbc.update(sql, c.getCode(), c.getCouponType(), // enum -> NVARCHAR
					c.getDiscountType(), // ✅ String -> NVARCHAR
					c.getDiscountValue(), c.getMaxDiscount(), c.getMinOrderAmount(),
					java.sql.Timestamp.valueOf(c.getStartDate()), java.sql.Timestamp.valueOf(c.getEndDate()),
					c.getUsageLimit(), c.getUsedCount(), c.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}

	public int insertForShop(Coupon c) {
		try {
			String sql = """
					  INSERT INTO Coupons
					  (Code,CouponType,DiscountType,DiscountValue,MaxDiscount,MinOrderAmount,
					   StartDate,EndDate,UsageLimit,UsedCount,ShopID,Status)
					  VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
					""";
			return jdbc.update(sql, c.getCode(), c.getCouponType(), c.getDiscountType(), c.getDiscountValue(),
					c.getMaxDiscount(), c.getMinOrderAmount(), java.sql.Timestamp.valueOf(c.getStartDate()),
					java.sql.Timestamp.valueOf(c.getEndDate()), c.getUsageLimit(), c.getUsedCount(), c.getShopId(),
					c.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
	}

	public boolean updatePlatform(Coupon c) {
		try {
			String sql = """
					  UPDATE Coupons
					  SET Code = ?, CouponType = ?, DiscountType = ?, DiscountValue = ?,
					      MaxDiscount = ?, MinOrderAmount = ?, StartDate = ?, EndDate = ?,
					      UsageLimit = ?, UsedCount = ?, Status = ?
					  WHERE CouponID = ? AND ShopID IS NULL
					""";

			var rs = jdbc.update(sql, c.getCode(), c.getCouponType(), c.getDiscountType(), // ✅ String
					c.getDiscountValue(), c.getMaxDiscount(), c.getMinOrderAmount(),
					java.sql.Timestamp.valueOf(c.getStartDate()), java.sql.Timestamp.valueOf(c.getEndDate()),
					c.getUsageLimit(), c.getUsedCount(), c.getStatus(), c.getCouponId());
			return rs > 0 ? true : false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	public int updateForShop(Coupon c) {
		try {
			String sql = """
					  UPDATE Coupons SET
					    Code=?, CouponType=?, DiscountType=?, DiscountValue=?, MaxDiscount=?, MinOrderAmount=?,
					    StartDate=?, EndDate=?, UsageLimit=?, UsedCount=?, Status=?
					  WHERE CouponID=? AND ShopID=?
					""";
			return jdbc.update(sql, c.getCode(), c.getCouponType(), c.getDiscountType(), c.getDiscountValue(),
					c.getMaxDiscount(), c.getMinOrderAmount(), java.sql.Timestamp.valueOf(c.getStartDate()),
					java.sql.Timestamp.valueOf(c.getEndDate()), c.getUsageLimit(), c.getUsedCount(), c.getStatus(),
					c.getCouponId(), c.getShopId());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
	}

	public int deletePlatform(int id) {
		try {
			return jdbc.update("DELETE FROM Coupons WHERE CouponID=? AND ShopID IS NULL", id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
	}

	public int deleteForShop(int id, int shopId) {
		try {
			return jdbc.update("DELETE FROM Coupons WHERE CouponID=? AND ShopID=?", id, shopId);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
			
			// TODO: handle exception
		}
	
	}
//
//	public List<Coupon> findByShopId(int shopId) {
//		try {
//			String sql = "SELECT * FROM Coupons WHERE ShopID = ? " + "AND Status = 1"
//					+ "  AND GETDATE() BETWEEN StartDate AND EndDate" + "  AND UsedCount < UsageLimit";
//			;
//			return jdbc.query(sql, mapper, shopId);
//		} catch (Exception e) {
//			System.err.println("Lỗi khi lấy tất cả voucher bang shopID : " + e.getMessage());
//			return List.of();
//		}
//	}
//	
//	Tìm Coupon theo code
	public Coupon findByCode(String CouponCode) {
		try {
			String sql = "SELECT * FROM Coupons WHERE Code = ?"
					+ "              AND Status = 1"
					+ "              AND GETDATE() BETWEEN StartDate AND EndDate"
					+ "              AND UsedCount < UsageLimit";
			List<Coupon> Coupon = jdbc.query(sql, mapper, CouponCode);
			return Coupon.isEmpty() ? null : Coupon.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Coupon findById(long id) {
		try {
			String sql = "SELECT * FROM Coupons WHERE CouponID = ?"
					+ "              AND Status = 1"
					+ "              AND GETDATE() BETWEEN StartDate AND EndDate"
					+ "              AND UsedCount < UsageLimit";
			List<Coupon> Coupon = jdbc.query(sql, mapper, id);
			return Coupon.isEmpty() ? null : Coupon.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
    public List<Coupon> findByShopId(int shopId) {
    	try {
    		 String sql = "SELECT * FROM Coupons WHERE ShopID = ? "
    				 + "AND Status = 1"
 					+ "  AND GETDATE() BETWEEN StartDate AND EndDate"
 					+ "  AND UsedCount < UsageLimit";
    				 ;
            return jdbc.query(sql, mapper, shopId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả voucher bang shopID : " + e.getMessage());
            return List.of();
        }
    }
    
    // Lấy Coupon theo couponType
    public List<Coupon> findByType(String couponType) {
    	try {
    		 String sql = "SELECT * FROM Coupons WHERE CouponType = ?"
    				 + "              AND Status = 1"
 					+ "              AND GETDATE() BETWEEN StartDate AND EndDate"
 					+ "              AND UsedCount < UsageLimit";
    		 ;
            return jdbc.query(sql, mapper, couponType);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả voucher bang couponType: " + e.getMessage());
            return List.of();
        }
    }
    
    public List<Coupon> findByType(String couponType , int shopId) {
    	try {
    		 String sql = "SELECT * FROM Coupons WHERE CouponType = ? and ShopID = ? " 
    				 + "              AND Status = 1"
 					+ "              AND GETDATE() BETWEEN StartDate AND EndDate"
 					+ "              AND UsedCount < UsageLimit";
    		 ;
            return jdbc.query(sql, mapper, couponType, shopId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả voucher bang couponType: " + e.getMessage());
            return List.of();
        }
    }

	
	
	public List<Coupon> findAllCoupons() {
        try {
            String sql = "SELECT * FROM Coupons Where Status != 0 ORDER BY CouponId DESC ";
            return jdbc.query(sql, mapper);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả cửa hàng: " + e.getMessage());
            return List.of();
        }
    }

	// Tìm Coupon theo trang và số lượng
	public List<Coupon> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = "SELECT * FROM Coupons ORDER BY CouponId DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
			return jdbc.query(sql, mapper, offset, size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
