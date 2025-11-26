package projectSem4.com.model.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import projectSem4.com.model.entities.UserDevice;

@Repository
public class UserDeviceRepository {
	@Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper cho UserDevice
    private RowMapper<UserDevice> rowMapper = new RowMapper<UserDevice>() {
        @Override
        public UserDevice mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDevice device = new UserDevice();
            device.setUserDeviceId(rs.getInt("UserDeviceID"));
            device.setUserId(rs.getInt("UserID"));
            device.setDeviceId(rs.getString("DeviceId"));
            device.setLastSeenAt(rs.getTimestamp("LastSeenAt").toLocalDateTime());
            device.setUserAgent(rs.getString("UserAgent"));
            device.setDeviceType(rs.getString("DeviceType"));
            device.setIpAddress(rs.getString("IPAddress"));
            return device;
        }
    };

    public List<UserDevice> findByUserId(int userId) {
        String sql = "SELECT * FROM UserDevices WHERE UserID = ?";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public Optional<UserDevice> findByDeviceId(String deviceId) {
        String sql = "SELECT * FROM UserDevices WHERE DeviceId = ?";
        List<UserDevice> result = jdbcTemplate.query(sql, rowMapper, deviceId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public void save(UserDevice device) {
        String sql = """
            INSERT INTO UserDevices (UserID, DeviceId, LastSeenAt, UserAgent, DeviceType, IPAddress)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
            device.getUserId(),
            device.getDeviceId(),
            device.getLastSeenAt() != null ? device.getLastSeenAt() : LocalDateTime.now(),
            device.getUserAgent(),
            device.getDeviceType(),
            device.getIpAddress()
        );
    }

    public void updateLastSeen(int userDeviceId) {
        String sql = "UPDATE UserDevices SET LastSeenAt = ? WHERE UserDeviceID = ?";
        jdbcTemplate.update(sql, LocalDateTime.now(), userDeviceId);
    }
    public void updateDeviceInfo(Integer userDeviceId, String ipAddress, String userAgent, String deviceType, LocalDateTime lastSeenAt) {
        String sql = """
            UPDATE UserDevices
            SET IPAddress = ?, UserAgent = ?, DeviceType = ?, LastSeenAt = ?
            WHERE UserDeviceID = ?
            """;
        jdbcTemplate.update(sql, ipAddress, userAgent, deviceType, lastSeenAt, userDeviceId);
    }

    public void deleteById(Integer userDeviceId) {
        String sql = "DELETE FROM UserDevices WHERE UserDeviceID = ?";
        jdbcTemplate.update(sql, userDeviceId);
    }
    
    public int touchLastSeen(Long requestUserId, String deviceId) {
        String sql = "UPDATE UserDevices SET LastSeenAt = SYSDATETIME() WHERE UserID = ? AND DeviceId = ?";
        return jdbcTemplate.update(sql, requestUserId, deviceId);
    }
    public void deleteByUserId(Integer userId) {
        String sql = "DELETE FROM UserDevices WHERE UserID = ?";
        jdbcTemplate.update(sql, userId);
    }
    
}
