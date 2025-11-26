package projectSem4.com.model.repositories;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.ShopStat;

@Repository
public class ShopstatRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private RowMapper<ShopStat> rowmapperForShopStat = new RowMapper<ShopStat>() {
		
		@Override
		public ShopStat mapRow(ResultSet rs, int rowNum) throws SQLException {
			var s = new ShopStat(
						rs.getInt("ShopStatId"),
						rs.getInt("ShopId"),
						rs.getInt("Year"),
						rs.getInt("Week"),
						rs.getInt("Month"),
						rs.getString("PeriodType"),
						rs.getDouble("TotalRevenue"),
						rs.getDouble("FlashSaleRevenue"),
						rs.getInt("OrderCount"),
						LocalDateTime.parse(rs.getDate(rowNum).toString()) 
					);
					
			return null;
		}
		
		
		
	};
}
