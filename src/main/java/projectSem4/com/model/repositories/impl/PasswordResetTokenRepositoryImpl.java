package projectSem4.com.model.repositories.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.PasswordResetToken;
import projectSem4.com.model.repositories.PasswordResetTokenRepository;

@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final JdbcTemplate jdbc;

    @Autowired
    public PasswordResetTokenRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<PasswordResetToken> MAPPER = new RowMapper<PasswordResetToken>() {
        @Override
        public PasswordResetToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            PasswordResetToken p = new PasswordResetToken();
            p.setTokenId(rs.getInt("TokenID"));
            p.setUserId(rs.getInt("UserID"));
            p.setToken(rs.getString("Token"));
            p.setExpiresAt(rs.getTimestamp("ExpiresAt").toLocalDateTime());
            p.setUsed(rs.getBoolean("Used"));
            p.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            p.setRequestIP(rs.getString("RequestIP"));
            p.setUserAgent(rs.getString("UserAgent"));
            return p;
        }
    };

    @Override
    public void createOrReplace(Integer userId,
                                String tokenPlain,
                                LocalDateTime expiresAt,
                                String userAgent,
                                String requestIp) {
        // Hủy các token cũ còn mở
        jdbc.update("UPDATE dbo.PasswordResetTokens SET Used = 1 WHERE UserID = ? AND Used = 0", userId);

        // Tạo token mới
        String sql = "INSERT INTO dbo.PasswordResetTokens " +
                     "(UserID, Token, ExpiresAt, Used, CreatedAt, RequestIP, UserAgent) " +
                     "VALUES (?, ?, ?, 0, SYSDATETIME(), ?, ?)";
        jdbc.update(sql,
            userId,
            tokenPlain,
            java.sql.Timestamp.valueOf(expiresAt),
            requestIp,
            userAgent
        );
    }

    @Override
    public PasswordResetToken findValidByToken(String tokenPlain) {
        String sql = "SELECT TOP 1 TokenID, UserID, Token, ExpiresAt, Used, CreatedAt, RequestIP, UserAgent " +
                     "FROM dbo.PasswordResetTokens " +
                     "WHERE Token = ? AND Used = 0 AND ExpiresAt > SYSDATETIME() " +
                     "ORDER BY TokenID DESC";
        try {
            return jdbc.queryForObject(sql, MAPPER, tokenPlain);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void markUsed(Integer tokenId) {
        jdbc.update("UPDATE dbo.PasswordResetTokens SET Used = 1 WHERE TokenID = ?", tokenId);
    }

    @Override
    public void invalidateAllByUserId(Integer userId) {
        jdbc.update("UPDATE dbo.PasswordResetTokens SET Used = 1 WHERE UserID = ? AND Used = 0", userId);
    }
}
