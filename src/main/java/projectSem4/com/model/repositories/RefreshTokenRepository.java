package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.RefreshToken;

@Repository
public class RefreshTokenRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;

	
	// RowMapper cho RefreshToken
	// RowMapper cho RefreshToken
    private RowMapper<RefreshToken> rowMapperForRefreshToken = new RowMapper<RefreshToken>() {
        @Override
        public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefreshToken rt = new RefreshToken();
            rt.setRefreshTokenId(rs.getLong("RefreshTokenID"));
            rt.setUserId(rs.getInt("UserID"));
            rt.setToken(rs.getString("Token"));
            rt.setDeviceId(rs.getString("DeviceID"));
            rt.setIssuedAt(rs.getTimestamp("IssuedAt").toLocalDateTime());
            rt.setExpiresAt(rs.getTimestamp("ExpiresAt").toLocalDateTime());
            rt.setRevoked(rs.getBoolean("Revoked"));
            if (rs.getTimestamp("RevokedAt") != null) {
                rt.setRevokedAt(rs.getTimestamp("RevokedAt").toLocalDateTime());
            }
            return rt;
        }
    };
	
    // Tìm token theo chuỗi token
    public RefreshToken findByToken(String token) {
        String sql = "SELECT * FROM RefreshTokens WHERE Token = ?";
        return jdbcTemplate.queryForObject(sql, rowMapperForRefreshToken, token);
    }
	
	 public RefreshToken findByUserIdAndDeviceId(Integer userId, String deviceId) {
		 RefreshToken a = new RefreshToken();
		 try {
			 String sql = "SELECT * FROM RefreshTokens WHERE UserID = ? AND DeviceID = ?";
			    List<RefreshToken> tokens = jdbcTemplate.query(sql, rowMapperForRefreshToken, userId, deviceId);
			    return tokens.isEmpty() ? null : tokens.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			a = null;
		}
	        
	        return a;
	    }

	public void deleteByUserId(Integer userId, String deviceId) {
		 String sql = "UPDATE RefreshTokens SET Revoked = 1, RevokedAt = SYSDATETIME() " +
                 "WHERE UserID = ? AND DeviceId = ?";
	    jdbcTemplate.update(sql, userId, deviceId);
	}
	
	
	public void deleteByUserIdAndDeviceType(Long userId, String deviceType) {
	    String sql = "DELETE FROM RefreshTokens WHERE UserID = ? AND DeviceType = ?";
	    jdbcTemplate.update(sql, userId, deviceType);
	}
	
	public int revokeByTokenAndDevice(String token, String deviceId) {
	    String sql = "UPDATE RefreshTokens SET Revoked=1, RevokedAt=SYSDATETIME() " +
	                 "WHERE Token=? AND DeviceId=? AND Revoked=0";
	    return jdbcTemplate.update(sql, token, deviceId);
	}

    public void save(RefreshToken rt) {
    	try {
    		String sql = """
    	            INSERT INTO RefreshTokens
    	            (UserID, Token, DeviceId, DeviceType, UserAgent, IPAddress, 
    	             IssuedAt, ExpiresAt, Revoked, RevokedAt)
    	            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    	            """;

    	        jdbcTemplate.update(sql,
    	            rt.getUserId(),
    	            rt.getToken(),
    	            rt.getDeviceId(),
    	            rt.getDeviceType(),
    	            rt.getUserAgent(),
    	            rt.getIpAddress(),
    	            rt.getIssuedAt(),
    	            rt.getExpiresAt(),
    	            rt.isRevoked(),
    	            rt.getRevokedAt()
    	        );
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    public void updateToken(RefreshToken rt) {
    	try {
    		String sql = """
                    UPDATE RefreshTokens
                    SET Token = ?, IssuedAt = ?, ExpiresAt = ?, Revoked = 0, RevokedAt = NULL
                    WHERE UserID = ? AND DeviceID = ?
                """;
                jdbcTemplate.update(sql,
                    rt.getToken(),
                    rt.getIssuedAt(),
                    rt.getExpiresAt(),
                    rt.getUserId(),
                    rt.getDeviceId()
                );
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    
    
    public void revokeByUserAndDevice(Integer userId, String deviceId) {
        String sql = "UPDATE RefreshTokens " +
                     "SET revoked = 1, revokedAt = ? " +
                     "WHERE userId = ? AND deviceId = ?";
        jdbcTemplate.update(sql, LocalDateTime.now(), userId, deviceId);
    }

    public void revokeAllByUser(Integer userId) {
        String sql = "UPDATE RefreshTokens " +
                     "SET revoked = 1, revokedAt = ? " +
                     "WHERE userId = ?";
        jdbcTemplate.update(sql, LocalDateTime.now(), userId);
    }
}
