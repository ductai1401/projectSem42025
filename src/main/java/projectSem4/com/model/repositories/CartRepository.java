package projectSem4.com.model.repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.Cart;
import projectSem4.com.model.entities.InventoryLog;
import projectSem4.com.model.entities.Cart;


@Repository
public class CartRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ CartRepository initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho Cart
	private RowMapper<Cart> rowMapperForCart = new RowMapper<Cart>() {
		@Override
		public Cart mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			
			LocalDateTime createdAt = rs.getTimestamp("CreatedAt").toLocalDateTime();
			LocalDateTime updatedAt = rs.getTimestamp("UpdatedAt").toLocalDateTime();
			return new Cart(
						rs.getInt("CartID"),
						rs.getInt("cartId"),
						createdAt,
						updatedAt
						
					);
		}
	};

//	 Tạo một Cart mới
	public Cart createCart(int userId) {
	    try {
	        String sql = "INSERT INTO Carts (UserID, CreatedAt) VALUES (?, ?)";
	        LocalDateTime now = LocalDateTime.now();

	        KeyHolder keyHolder = new GeneratedKeyHolder();
	        jdbcTemplate.update(connection -> {
	            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	            ps.setInt(1, userId);
	            ps.setTimestamp(2, Timestamp.valueOf(now));
	            return ps;
	        }, keyHolder);

	        Integer cartId = keyHolder.getKey().intValue();
	        if (cartId != null) {
	            Cart cart = new Cart();
	            cart.setCartId(cartId);
	            cart.setUserId(userId);
	            cart.setCreatedAt(now);
	            return cart;
	        }

	        return null;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	
	public int AddCart(int usertId, int variantId, int quantity) {
		int a = 0;
		
		try {
			SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
			        .withProcedureName("AddToCart")
			        .declareParameters(
	                new SqlParameter("UserID", Types.INTEGER),
	                new SqlParameter("VariantID", Types.INTEGER),
	                new SqlParameter("Quantity", Types.INTEGER),
	                new SqlOutParameter("Result", Types.INTEGER) 
	                
	            );
		  
		  MapSqlParameterSource params = new MapSqlParameterSource()
			        .addValue("UserID", usertId)
			        .addValue("VariantID", variantId)
			        .addValue("Quantity", quantity);
			        
		  Map<String, Object> result = call.execute(params);
		
		  Integer affectedRows = (Integer) result.get("Result");
		  if(affectedRows != null) {
			  a = affectedRows;	
				System.out.println(a);
		  }
		  
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return a;
	}

	// Cập nhật thông tin Cart
	public String updateCart(Cart cart) {
		try {
			String sql = "UPDATE Carts SET UserID = ?,"
					+ ", UpdatedAt = ? WHERE CartId = ?";
			int rows = jdbcTemplate.update(sql,
					cart.getUserId(),
					cart.getCreatedAt(), cart.getUpdatedAt(),cart.getUserId());
			return rows > 0 ? "Cart updated successfully ! " : "Cart update failed !";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error when updating the Cart : " + e.getMessage();
		}
	}

	// Xóa Cart theo ID
	public String deleteCart(int cartId) {
		try {
			String sql = "delete FROM Carts WHERE cartId = ?";
			int rows = jdbcTemplate.update(sql, 4, cartId);
			return rows > 0 ? "Cart deleted successfully!" : "Deleting the Cart failed!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error when deleting Cart : " + e.getMessage();
		}
	}
	
	// Lấy cart theo userID
    public Cart findByUserId(int userId) {
        String sql = "SELECT * FROM Carts WHERE UserID = ?";
        List<Cart> list = jdbcTemplate.query(sql, rowMapperForCart, userId);
        return list.isEmpty() ? null : list.get(0);
    }

	
	
	public List<Cart> findAllCarts() {
        try {
            String sql = "SELECT * FROM Carts ORDER BY cartId DESC";
            return jdbcTemplate.query(sql, rowMapperForCart);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả cửa hàng: " + e.getMessage());
            return List.of();
        }
    }

	// Tìm Cart theo trang và số lượng
	public List<Cart> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = "SELECT * FROM Carts ORDER BY cartId DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
			return jdbcTemplate.query(sql, rowMapperForCart, offset, size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	

    // Lấy tên cửa hàng theo cartId
    public String getCartNameById(int cartId) {
        try {
            String sql = "SELECT CartName FROM Carts WHERE cartId=?";
            return jdbcTemplate.queryForObject(sql, String.class, cartId);
        } catch (Exception e) {
            System.err.println("Error when retrieving store name ID=" + cartId + ": " + e.getMessage());
            return null;
        }
    }
}
