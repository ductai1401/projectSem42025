package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ UserRepository initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho User
	private RowMapper<User> rowMapperForUser = new RowMapper<User>() {
		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer userId = rs.getInt("UserID");
			String roleId = rs.getString("RoleID");
			String fullName = rs.getString("FullName");
			String email = rs.getString("Email");
			String passwordHash = rs.getString("PasswordHash");
			String addresses = rs.getString("Addresses");
			String phoneNumber = rs.getString("PhoneNumber");
			Integer status = rs.getInt("Status");
			String image = rs.getString("Image");
			LocalDateTime createdAt = rs.getTimestamp("CreatedAt").toLocalDateTime();
			LocalDateTime updatedAt = rs.getTimestamp("UpdatedAt").toLocalDateTime();

			// Map dữ liệu và tạo đối tượng User
			return new User(userId, roleId, fullName, email, passwordHash, addresses, phoneNumber, status, image,
					createdAt, updatedAt);
		}
	};

	// Tạo một người dùng mới
	public String createUser(User user) {
		try {
			String sql = "INSERT INTO Users (RoleID, FullName, Email, PasswordHash, Addresses, PhoneNumber, Status, Image, CreatedAt, UpdatedAt) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			int rows = jdbcTemplate.update(sql, user.getRoleId(), user.getFullName(), user.getEmail(),
					user.getPasswordHash(), user.getAddresses(), user.getPhoneNumber(), user.getStatus(),
					user.getImage(), user.getCreatedAt(), user.getUpdatedAt());
			return rows > 0 ? "User created successfully!" : "User creation failed!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while creating user: " + e.getMessage();
		}
	}

	// Tìm người dùng theo email (username)
	public User findByEmail(String email) {
		try {
			String sql = "SELECT * FROM Users WHERE Email = ?";
			List<User> User = jdbcTemplate.query(sql, rowMapperForUser, email);
			return User.isEmpty() ? null : User.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Tìm người dùng theo ID
	public User findById(int userId) {
		try {
			String sql = "SELECT * FROM Users WHERE UserID = ?";
			List<User> User = jdbcTemplate.query(sql, rowMapperForUser, userId);
			return User.isEmpty() ? null : User.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// UserRepository.java

	public String updateUser(User user) {
		try {
			String sql = "UPDATE Users SET RoleID = ?, FullName = ?, Email = ?, PasswordHash = ?, Addresses = ?, PhoneNumber = ?, [Status] = ?, [Image] = ?, UpdatedAt = ? "
					+ "WHERE UserID = ?";
			int rows = jdbcTemplate.update(sql, user.getRoleId(), user.getFullName(), user.getEmail(),
					user.getPasswordHash(), user.getAddresses(), user.getPhoneNumber(), user.getStatus(),
					user.getImage(), user.getUpdatedAt(), user.getUserId());
			return rows > 0 ? "User updated successfully!" : "User update failed!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while updating user: " + e.getMessage();
		}
	}

	public int updateImage(int userId, String image) {
		String sql = "UPDATE User SET [Image] = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?";
		return jdbcTemplate.update(sql, image, userId);
	}

	// Xóa người dùng theo ID
	public String deleteUser(int userId) {
		try {
			String sql = "DELETE FROM User WHERE UserID = ?";
			int rows = jdbcTemplate.update(sql, userId);
			return rows > 0 ? "Users deleted successfully!" : "User deletion failed!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while deleting user: " + e.getMessage();
		}
	}

	// Lấy tất cả người dùng
	public List<User> findAll() {
		try {
			String sql = "SELECT * FROM Users";
			return jdbcTemplate.query(sql, rowMapperForUser);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Tìm người dùng theo tên (hoặc email nếu dùng email làm username)
	public User findByUsername(String username) {
		String sql = "SELECT * FROM Users WHERE Email = ?"; // Sử dụng email thay vì username nếu bạn dùng email
		List<User> User = jdbcTemplate.query(sql, rowMapperForUser, username);
		return User.isEmpty() ? null : User.get(0);
	}

	// Tìm kiếm người dùng theo tên
	public List<User> searchUserByName(String keyword) {
		try {
			String sql = "SELECT * FROM Users WHERE FullName LIKE ?";
			return jdbcTemplate.query(sql, rowMapperForUser, "%" + keyword + "%");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Tìm người dùng theo trang và số lượng
	public List<User> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = "SELECT * FROM Users ORDER BY UserID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
			return jdbcTemplate.query(sql, rowMapperForUser, offset, size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Đếm tổng số người dùng
	public int getTotalUser() {
		try {
			String sql = "SELECT COUNT(*) FROM Users";
			return jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public boolean existsByEmail(String email) {
		String sql = "SELECT COUNT(1) FROM Users WHERE Email = ?";
		Integer c = jdbcTemplate.queryForObject(sql, Integer.class, email);
		return c != null && c > 0;
	}

	// Lấy danh sách theo role + keyword + phân trang
	public List<User> findByRolePaged(String roleId, String keyword, int page, int size) {
		int offset = (page - 1) * size;
		StringBuilder sql = new StringBuilder("SELECT * FROM Users WHERE RoleID = ?");
		Object kw = null;
		if (keyword != null && !keyword.isBlank()) {
			sql.append(" AND (FullName LIKE ? OR Email LIKE ? OR PhoneNumber LIKE ?)");
			kw = "%" + keyword.trim() + "%";
			return jdbcTemplate.query(
					sql.append(" ORDER BY UserID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY").toString(),
					rowMapperForUser, roleId, kw, kw, kw, offset, size);
		} else {
			return jdbcTemplate.query(
					sql.append(" ORDER BY UserID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY").toString(),
					rowMapperForUser, roleId, offset, size);
		}
	}

	public int countByRole(String roleId, String keyword) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Users WHERE RoleID = ?");
		if (keyword != null && !keyword.isBlank()) {
			String kw = "%" + keyword.trim() + "%";
			return jdbcTemplate.queryForObject(
					sql.append(" AND (FullName LIKE ? OR Email LIKE ? OR PhoneNumber LIKE ?)").toString(),
					Integer.class, roleId, kw, kw, kw);
		} else {
			return jdbcTemplate.queryForObject(sql.toString(), Integer.class, roleId);
		}
	}

	// Khóa/Mở khóa: nếu Status=1 -> 0; 0 -> 1
	public void toggleStatus(int userId) {
		String sql = "UPDATE Users SET Status = CASE WHEN Status = 1 THEN 0 ELSE 1 END, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?";
		jdbcTemplate.update(sql, userId);
	}

	public int updatePassword(int userId, String passwordHash) {
		String sql = "UPDATE Users SET PasswordHash = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?";
		return jdbcTemplate.update(sql, passwordHash, userId);
	}

	public int updatePhone(int userId, String phone) {
		String sql = "UPDATE Users SET PhoneNumber = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?";
		return jdbcTemplate.update(sql, phone, userId);
	}

	public int updateAddressShop(User user) {
		String sql = "UPDATE Users SET Addresses = ?, UpdatedAt = SYSUTCDATETIME(), PhoneNumber = ? , RoleID = ? WHERE UserID = ?";
		try {
			return jdbcTemplate.update(sql, user.getAddresses(), user.getPhoneNumber(), user.getRoleId(),
					user.getUserId());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}

	public void updatePasswordHash(Integer userId, String newHash) {
		String sql = "UPDATE dbo.Users SET PasswordHash = ?, UpdatedAt = SYSDATETIME() WHERE UserID = ?";
		jdbcTemplate.update(sql, newHash, userId);
	}

	public int updateAddressCus(User user) {
		String sql = "UPDATE Users SET Addresses = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?";
		try {
			return jdbcTemplate.update(sql, user.getAddresses(), user.getUserId());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}

	// Lấy tên hiển thị (FullName) của user theo ID
	public String findUsernameById(Integer userId) {
		try {
			String sql = "SELECT FullName FROM Users WHERE UserID = ?";
			return jdbcTemplate.queryForObject(sql, String.class, userId);
		} catch (Exception e) {
			// Nếu không có FullName, fallback sang Email hoặc trả về null
			try {
				String sql = "SELECT Email FROM Users WHERE UserID = ?";
				String email = jdbcTemplate.queryForObject(sql, String.class, userId);
				if (email != null && email.contains("@")) {
					return email.substring(0, email.indexOf("@"));
				}
			} catch (Exception ignored) {
			}
			return null;
		}
	}

}