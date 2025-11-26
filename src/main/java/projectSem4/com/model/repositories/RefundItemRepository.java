package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.security.auth.message.callback.PrivateKeyCallback.Request;
import projectSem4.com.dto.RefundOrderDTO;
import projectSem4.com.model.entities.RefundItem;
import projectSem4.com.model.entities.RefundRequest;
import projectSem4.com.model.modelViews.RefundItemView;

@Repository
public class RefundItemRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ RefundItem initialized. hashCode: " + System.identityHashCode(this));
	}
	private RowMapper<RefundItem> rowMapperRefundItem = new RowMapper<RefundItem>() {
		@Override
		public RefundItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer ID = rs.getInt("ID");
			Integer RefundID = rs.getInt("RefundID");
			Integer OrderItemID = rs.getInt("OrderItemID");
			Integer Quantity = rs.getInt("Quantity");
			Double RefundAmount = rs.getDouble("RefundAmount");
			

			// Map dữ liệu và tạo đối tượng 
			RefundItem rq = new RefundItem(ID,RefundID,OrderItemID,Quantity,RefundAmount);
			
			return rq;
		}
		
	};
	
	public boolean create(RefundItem data) {
		try {
			String sql = "INsert into RefundItems (RefundID, OrderItemID, Quantity,RefundAmount) "
					+ " VALUES (?,?,?,?)";
			var rs = jdbcTemplate.update(sql, data.getRefundId(), data.getOrderItemId(), data.getQuantity(), data.getRefundAmount());
			if(rs > 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	} 
	
	public List<RefundItem> getAllByRefundId(int refundId){
		try {
			String sql = "SELECT * FROM RefundItems where RefundID = ?";
			var rs = jdbcTemplate.query(sql, rowMapperRefundItem, refundId);
			return rs;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public List<RefundItemView> getViewAllByRefundId(int refundId){
		try {
			String sql = "SELECT "
					+ " rI.ID as id, "
					+ " rI.RefundID as refundId, "
					+ " rI.Quantity as quantity, "
					+ " rI.RefundAmount as refundAmount, "
					+ " pv.Image as productVariantImage, "
					+ " pV.VariantID as productVariantID, "
					+ " pV.VariantName as productVariantName, "
					+ " p.ProductName as productName  "
					+ " FROM RefundItems rI "
					+ "JOIN OrderItems oI on oI.OrderItemID = rI.OrderItemID "
					+ "JOIN ProductVariants pV on oI.ProductVariantID = pV.VariantID "
					+ "JOIN Products p on p.ProductID = pV.ProductID "
					+ "where RefundID = ?";
			var rs = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(RefundItemView.class), refundId);
			return rs;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
