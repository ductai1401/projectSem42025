package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import projectSem4.com.model.entities.CartItem;
import projectSem4.com.model.modelViews.CartItemView;
import projectSem4.com.model.modelViews.ShopView;

@Repository
public class CartItemRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper cho CartItem
    private RowMapper<CartItem> rowMapperForCartItem = new RowMapper<CartItem>() {
        @Override
        public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            CartItem item = new CartItem();
            item.setCartItemId(rs.getInt("CartItemID"));
            item.setCartId(rs.getInt("CartID"));
            item.setVariantId(rs.getInt("VariantID"));
            item.setQuantity(rs.getInt("Quantity"));
            return item;
        }
    };

    // Thêm item vào cart
    public int addCartItem(CartItem item) {
        String sql = "INSERT INTO CartItems (CartID, VariantID, Quantity) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, item.getCartId(), item.getVariantId(), item.getQuantity());
    }

    // Cập nhật số lượng item trong cart
    public int updateCartItem(CartItem item) {
        String sql = "UPDATE CartItems SET Quantity = ? WHERE CartItemID = ?";
        return jdbcTemplate.update(sql, item.getQuantity(), item.getCartItemId());
    }

    // Xóa item khỏi cart
    public int deleteCartItem(int cartItemId) {
        String sql = "DELETE FROM CartItems WHERE CartItemID = ?";
        return jdbcTemplate.update(sql, cartItemId);
    }
    
    public int deleteCartItem(int cartId, int variantId) {
    	try {
    		String sql = "DELETE FROM CartItems WHERE CartID = ? AND VariantID = ?";
            return jdbcTemplate.update(sql, cartId, variantId);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
        
    }

    // Xóa toàn bộ item trong giỏ hàng theo cartId
    public int clearCart(int cartId) {
        String sql = "DELETE FROM CartItems WHERE CartID = ?";
        return jdbcTemplate.update(sql, cartId);
    }

    // Lấy toàn bộ item trong giỏ hàng
    public List<CartItem> findByCartId(int cartId) {
        String sql = "SELECT * FROM CartItems WHERE CartID = ?";
        return jdbcTemplate.query(sql, rowMapperForCartItem, cartId);
    }
    
    public List<CartItemView> findByUserId(int userId) {
    	List<CartItemView> cartItems = new ArrayList<>(); 
		 try {
			 SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
				        .withProcedureName("GetCartItemsByUser")
				        .declareParameters(
				                new SqlParameter("UserID", Types.INTEGER)
				            )
				        .returningResultSet("cartItems", new BeanPropertyRowMapper<>(CartItemView.class));

				    MapSqlParameterSource params = new MapSqlParameterSource()
				    		.addValue("UserID", userId);
				  

				    var result = call.execute(params);
				    var a = (List<CartItemView>) result.get("cartItems");
				    if(a != null || !a.isEmpty()) {
				    	cartItems = a;
				    }else {
				    	cartItems = null;
				    }
				    return cartItems ;
				    
		} catch (Exception e) {
			e.printStackTrace();
			return cartItems = null;
		}
		 
    }
    
    public List<CartItemView> findByJsonVariant(String json) {
    	List<CartItemView> cartItems = new ArrayList<>(); 
		 try {
			 SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
				        .withProcedureName("GetCartItemsByVariantJson")
				        .declareParameters(
				                new SqlParameter("CartItemsJson", Types.NVARCHAR)
				            )
				        .returningResultSet("cartItems", new BeanPropertyRowMapper<>(CartItemView.class));

				    MapSqlParameterSource params = new MapSqlParameterSource()
				    		.addValue("CartItemsJson", json);
				  

				    var result = call.execute(params);
				    var a = (List<CartItemView>) result.get("cartItems");
				    if(a != null || !a.isEmpty()) {
				    	cartItems = a;
				    }else {
				    	cartItems = null;
				    }
				    return cartItems ;
				    
		} catch (Exception e) {
			e.printStackTrace();
			return cartItems = null;
		}
		 
    }

    // Lấy 1 item theo ID
    public CartItem findById(int cartItemId) {
    	try {
    		String sql = "SELECT * FROM CartItems WHERE CartItemID = ?";
            List<CartItem> list = jdbcTemplate.query(sql, rowMapperForCartItem, cartItemId);
            return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
    }

    // Kiểm tra item có tồn tại theo cartId + variantId (dùng khi add sản phẩm, tránh trùng)
    public CartItem findByCartAndVariant(int cartId, int variantId) {
    	try {
    		String sql = "SELECT * FROM CartItems WHERE CartID = ? AND VariantID = ?";
            List<CartItem> list = jdbcTemplate.query(sql, rowMapperForCartItem, cartId, variantId);
            return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        
    }
    
    public int updateByCartAndVariant(int cartId, int variantId, int quantity) {
    	try {
    		String sql = "UPDATE CartItems SET Quantity = ? WHERE CartID = ? AND VariantID = ?";
    		return jdbcTemplate.update(sql, quantity ,cartId, variantId);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
    }
    
    public List<CartItemView> findAllByIdAndUser(int userId,List<Long> ids) {
    	try {
    		
    		if (ids == null || ids.isEmpty()) {
                return Collections.emptyList();
            }
    		
    		String idsJson = new ObjectMapper().writeValueAsString(ids);
    		SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
			        .withProcedureName("GetCartItemsByUserAndIds")
			        .declareParameters(
			                new SqlParameter("UserID", Types.INTEGER),
			                new SqlParameter("CartItemIds", Types.NVARCHAR)
			            )
			        .returningResultSet("cartItems", new BeanPropertyRowMapper<>(CartItemView.class));

			    MapSqlParameterSource params = new MapSqlParameterSource()
			    		.addValue("UserID", userId)
			    	    .addValue("CartItemIds", idsJson);
			    	
			  

			    Map<String, Object> result = call.execute(params);
			    List<CartItemView> cartItems = (List<CartItemView>) result.get("cartItems");
			    
			    return cartItems == null ? null : cartItems;
          
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
    }
}
