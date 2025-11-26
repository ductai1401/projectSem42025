package projectSem4.com.model.repositories;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.modelViews.ShopView;

@Repository
public class ShopRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ ShopRepository initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho Shop
	private RowMapper<Shop> rowMapperForShop = new RowMapper<Shop>() {
		@Override
		public Shop mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer ShopID = rs.getInt("ShopID");
			Integer UserID = rs.getInt("UserID");
			String shopName = rs.getString("ShopName");
			String description = rs.getString("Description");
			String logo = rs.getString("LogoURL");
			Double rating = rs.getDouble("Rating");
			Integer status = rs.getInt("Status");
			LocalDateTime createdAt = rs.getTimestamp("CreatedAt").toLocalDateTime();
			LocalDateTime updatedAt = rs.getTimestamp("UpdatedAt").toLocalDateTime();

			// Map dữ liệu và tạo đối tượng Shop
			Shop shop = new Shop(ShopID, UserID, shopName, description, logo, rating,status,
					createdAt, updatedAt);
			
			return shop;
		}
	};

	// Tạo một Shop mới
	public Map<String, Object> createShop(Shop shop) {
		Map<String, Object> a = new HashMap<>();
		
		try {
			String sql = "INSERT INTO Shops (UserID, shopName, Description, CreatedAt, UpdatedAt) "
					+ "VALUES (?, ?, ?, ?, ?)";
			int rows = jdbcTemplate.update(sql,
					shop.getUserId(),
					shop.getShopName(), shop.getDescription(),
					shop.getCreatedAt(), shop.getUpdatedAt());
			if(rows > 0) {
				a.put("message", "Shop registration successful!");
				a.put("rows", rows);
			} else {
				a.put("message", "Shop registration failed!");
				a.put("rows", rows);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			a.put("message", "Shop registration failed!");
			a.put("rows", 0);
		}
		return a;
	}

	// Tìm Shop theo tên
//	public Categories findByName(String ShopName) {
//		try {
//			String sql = "SELECT * FROM Categories WHERE ShopName = ?";
//			List<Categories> categories = jdbcTemplate.query(sql, rowMapperForShop, ShopName);
//			return categories.isEmpty() ? null : categories.get(0);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

	

	// Cập nhật thông tin Shop
	public int updateShop(Shop shop) {
		try {
			String sql = "UPDATE Shops SET ShopName = ?, Description = ? "
					+ ", UpdatedAt = ? WHERE ShopID = ?  and UserID = ?";
			int rows = jdbcTemplate.update(sql,
					
					shop.getShopName(), shop.getDescription(),
					shop.getUpdatedAt(), shop.getShopId(), shop.getUserId());
			return rows;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// Xóa Shop theo ID
	public String deleteShop(int ShopId) {
		try {
			String sql = "Update FROM Shops Set Status = ? WHERE ShopID = ?";
			int rows = jdbcTemplate.update(sql, 4, ShopId);
			return rows > 0 ? "Shop deleted successfully!" : "Deleting the shop failed!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error when deleting Shop : " + e.getMessage();
		}
	}

	
	
	public List<Shop> findAllShops() {
        try {
            String sql = "SELECT * FROM Shops ORDER BY ShopID DESC";
            return jdbcTemplate.query(sql, rowMapperForShop);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả cửa hàng: " + e.getMessage());
            return List.of();
        }
    }

	// Tìm Shop theo trang và số lượng
	public List<Shop> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = "SELECT * FROM Shops ORDER BY ShopID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
			return jdbcTemplate.query(sql, rowMapperForShop, offset, size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Tìm kiếm Shop fillter
	public Map<String, Object> searchAndPagi(int page, int size, String keyword, Double minRating, Integer status, String sortBy) {
	    Map<String, Object> res = new HashMap<>();
	    try {
	    	
	    	if(status == 0) {
	    		status = null;
	    	}
	        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
	            .withProcedureName("GetFilteredShops")
	            .declareParameters(
	                new SqlParameter("PageIndex", Types.INTEGER),
	                new SqlParameter("PageSize", Types.INTEGER),
	                new SqlParameter("Search", Types.NVARCHAR),
	                new SqlParameter("Status", Types.INTEGER),
	                new SqlParameter("SortBy", Types.NVARCHAR),
	                new SqlParameter("MinRating", Types.DECIMAL),
	                new SqlOutParameter("TotalRows", Types.INTEGER),
	                new SqlOutParameter("TotalPages", Types.INTEGER)
	            )
	            .returningResultSet("shops", (rs, row) -> {
	                ShopView a = new ShopView();
	                a.setShopId(rs.getInt("ShopID"));
	                a.setShopName(rs.getString("ShopName"));
	                a.setFullName(rs.getNString("OwnerName"));
	                a.setRating(rs.getDouble("Rating"));
	                a.setStatus(rs.getInt("Status"));
	                a.setLogo(rs.getString("LogoURL"));
	                Timestamp created = rs.getTimestamp("CreatedAt");
	                if (created != null) {
	                    a.setCreatedAt(created.toLocalDateTime());
	                }
	                a.setTotalRevenue(rs.getBigDecimal("TotalRevenue"));
	                return a;
	            });

	        MapSqlParameterSource params = new MapSqlParameterSource()
	            .addValue("PageIndex", page)
	            .addValue("PageSize", size)
	            .addValue("Search", keyword)
	            .addValue("Status", status)
	            .addValue("SortBy", sortBy)
	            .addValue("MinRating", minRating);

	        Map<String, Object> result = call.execute(params);
	        List<ShopView> shops = (List<ShopView>) result.get("shops");
	        Integer totalRows = (Integer) result.get("TotalRows");
	        Integer totalPages = (Integer) result.get("TotalPages");

	        res.put("shops", shops);
	        res.put("totalRows", totalRows);
	        res.put("totalPages", totalPages);
	        res.put("page", page);

	    } catch (Exception e) {
	        e.printStackTrace();
	        res = null;
	    }
	    return res;
	}

	 // Lấy cửa hàng theo ID
    public ShopView findById(int shopId) {
        try {
            String sql = "SELECT \r\n"
            		+ "        s.ShopID AS shopId,\r\n"
            		+ "        s.ShopName AS shopName,\r\n"
            		+ "        s.LogoURL AS logo,\r\n"
            		+ "		s.Description as description,\r\n"
            		+ "		s.Rating as rating,\r\n"
            		+ "		s.UpdatedAt as updatedAt,\r\n"
            		+ "		u.Email as email,\r\n"
            		+ "        u.Addresses AS address,\r\n"
            		+ "		u.Image as image,\r\n"
            		+ "        s.Status AS status,\r\n"
            		+ "        s.CreatedAt AS createdAt,\r\n"
            		+ "        u.UserID AS userId,\r\n"
            		+ "        u.FullName AS fullName,\r\n"
            		+ "        u.PhoneNumber AS Phone\r\n"
            		+ "    FROM Shops AS s\r\n"
            		+ "    JOIN Users AS u ON s.UserID  = u.UserID\r\n"
            		+ "    WHERE s.ShopID = ? ";
            List<ShopView> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ShopView.class), shopId);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("Error finding store ID=" + shopId + ": " + e.getMessage());
            return null;
        }
    }
    
    // Lấy cửa hàng theo ID
    public ShopView findByUserId(int userId) {
        try {
            String sql = "SELECT \r\n"
            		+ "        s.ShopID AS shopId,\r\n"
            		+ "        s.ShopName AS shopName,\r\n"
            		+ "        s.LogoURL AS logo,\r\n"
            		+ "		s.Description as description,\r\n"
            		+ "		s.Rating as rating,\r\n"
            		+ "		s.UpdatedAt as updatedAt,\r\n"
            		+ "		u.Email as email,\r\n"
            		+ "        u.Addresses AS address,\r\n"
            		+ "		u.Image as image,\r\n"
            		+ "        s.Status AS status,\r\n"
            		+ "        s.CreatedAt AS createdAt,\r\n"
            		+ "        u.UserID AS userId,\r\n"
            		+ "        u.FullName AS fullName,\r\n"
            		+ "        u.PhoneNumber AS Phone\r\n"
            		+ "    FROM Shops AS s\r\n"
            		+ "    JOIN Users AS u ON s.UserID  = u.UserID\r\n"
            		+ "    WHERE u.UserID = ? ";
            List<ShopView> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ShopView.class), userId);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("Error finding store ID=" + userId + ": " + e.getMessage());
            return null;
        }
    }

    // Lấy tên cửa hàng theo shopId
    public String getShopNameById(int shopId) {
        try {
            String sql = "SELECT ShopName FROM Shops WHERE ShopID=?";
            return jdbcTemplate.queryForObject(sql, String.class, shopId);
        } catch (Exception e) {
            System.err.println("Error when retrieving store name ID=" + shopId + ": " + e.getMessage());
            return null;
        }
    }
    public Integer getShopIdByUserId(int userId) {
        try {
            String sql = "SELECT ShopId FROM Shops WHERE UserId = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, userId);
        } catch (Exception e) {
            System.err.println("Error when retrieving shopId for UserID=" + userId + ": " + e.getMessage());
            return 0;
        }
    }
    
    public boolean updateStatusShop(int shopId, int status) {
    	try {
			String sql = "Update Shops set status = ? AND UpdatedAt = ? where ShopID = ?";
			var rs = jdbcTemplate.update(sql, status, LocalDateTime.now(), shopId);
			return rs > 0;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
    }
}
