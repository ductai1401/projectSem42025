package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.BuyerNotification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BuyerNotificationRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final RowMapper<BuyerNotification> rowMapper = (rs, rowNum) -> {
		BuyerNotification n = new BuyerNotification();
		n.setNotificationId(rs.getInt("NotificationID"));
		n.setUserId(rs.getInt("UserID"));

		Object tpl = rs.getObject("TemplateID");
		n.setTemplateId(tpl == null ? null : rs.getInt("TemplateID"));

		n.setContent(rs.getString("Content"));
		n.setRedirectUrl(rs.getString("RedirectUrl"));

		Object typeObj = rs.getObject("Type");
		n.setType(typeObj == null ? null : rs.getInt("Type"));

		Timestamp cAt = rs.getTimestamp("CreatedAt");
		n.setCreatedAt(cAt != null ? cAt.toLocalDateTime() : null);

		Object readObj = rs.getObject("IsRead");
		n.setIsRead(readObj == null ? null : rs.getBoolean("IsRead"));

		Object stObj = rs.getObject("Status");
		n.setStatus(stObj == null ? null : rs.getInt("Status"));

		return n;
	};

	/* ========================= CREATE ========================= */

	/**
	 * Thêm thông báo (snapshot) và trả về ID. Nếu CreatedAt null → SYSDATETIME().
	 * Nếu Status null → mặc định 1.
	 */
	public Integer createReturningId(BuyerNotification n) {
		try {
			boolean hasCreatedAt = n.getCreatedAt() != null;
			Integer status = (n.getStatus() == null ? 1 : n.getStatus());

			String sqlWithTime = """
					    INSERT INTO BuyerNotifications
					      (UserID, TemplateID, Content, RedirectUrl, Type, CreatedAt, IsRead, Status)
					    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
					""";
			String sqlNoTime = """
					    INSERT INTO BuyerNotifications
					      (UserID, TemplateID, Content, RedirectUrl, Type, CreatedAt, IsRead, Status)
					    VALUES (?, ?, ?, ?, ?, SYSDATETIME(), ?, ?)
					""";

			KeyHolder kh = new GeneratedKeyHolder();
			int rows;

			if (hasCreatedAt) {
				rows = jdbcTemplate.update(con -> {
					PreparedStatement ps = con.prepareStatement(sqlWithTime, Statement.RETURN_GENERATED_KEYS);
					int i = 1;
					ps.setInt(i++, n.getUserId());
					if (n.getTemplateId() == null)
						ps.setObject(i++, null);
					else
						ps.setInt(i++, n.getTemplateId());
					ps.setString(i++, n.getContent());
					ps.setString(i++, n.getRedirectUrl());
					if (n.getType() == null)
						ps.setObject(i++, null);
					else
						ps.setInt(i++, n.getType());
					ps.setTimestamp(i++, Timestamp.valueOf(n.getCreatedAt()));
					ps.setBoolean(i++, n.getIsRead() != null ? n.getIsRead() : false);
					ps.setInt(i, status);
					return ps;
				}, kh);
			} else {
				rows = jdbcTemplate.update(con -> {
					PreparedStatement ps = con.prepareStatement(sqlNoTime, Statement.RETURN_GENERATED_KEYS);
					int i = 1;
					ps.setInt(i++, n.getUserId());
					if (n.getTemplateId() == null)
						ps.setObject(i++, null);
					else
						ps.setInt(i++, n.getTemplateId());
					ps.setString(i++, n.getContent());
					ps.setString(i++, n.getRedirectUrl());
					if (n.getType() == null)
						ps.setObject(i++, null);
					else
						ps.setInt(i++, n.getType());
					ps.setBoolean(i++, n.getIsRead() != null ? n.getIsRead() : false);
					ps.setInt(i, status);
					return ps;
				}, kh);
			}

			if (rows > 0 && kh.getKey() != null) {
				Integer id = kh.getKey().intValue();
				n.setNotificationId(id);
				if (n.getCreatedAt() == null)
					n.setCreatedAt(LocalDateTime.now()); // best-effort
				n.setStatus(status);
				return id;
			}
			return null;
		} catch (Exception e) {
			System.err.println("Lỗi createReturningId BuyerNotification: " + e.getMessage());
			return null;
		}
	}

	public String create(BuyerNotification n) {
		return createReturningId(n) != null ? "Tạo thông báo thành công!" : "Tạo thông báo thất bại!";
	}

	/* ========================= UPDATE ========================= */

	public String markRead(int notificationId, boolean read) {
		try {
			String sql = "UPDATE BuyerNotifications SET IsRead=? WHERE NotificationID=?";
			int rows = jdbcTemplate.update(sql, read, notificationId);
			return rows > 0 ? "Cập nhật trạng thái đọc thành công!" : "Cập nhật trạng thái đọc thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi markRead: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật trạng thái đọc!";
		}
	}

	/** Đổi status tùy ý (ví dụ 1→mới, 2→đã click/mở) */
	public int markStatus(int notificationId, int status) {
		try {
			String sql = "UPDATE BuyerNotifications SET Status=? WHERE NotificationID=?";
			return jdbcTemplate.update(sql, status, notificationId);
		} catch (Exception e) {
			System.err.println("Lỗi markStatus: " + e.getMessage());
			return 0;
		}
	}

	/** Dùng cho sự kiện click: set Status=2 */
	public int markClicked(int notificationId) {
		return markStatus(notificationId, 2);
	}

	/** Nếu muốn vừa đánh dấu đã đọc, vừa status=2 khi mở */
	public int markReadAndClicked(int notificationId) {
		try {
			String sql = "UPDATE BuyerNotifications SET IsRead=1, Status=2 WHERE NotificationID=?";
			return jdbcTemplate.update(sql, notificationId);
		} catch (Exception e) {
			System.err.println("Lỗi markReadAndClicked: " + e.getMessage());
			return 0;
		}
	}

	public String updateSnapshot(int notificationId, String content, String redirectUrl, Integer type) {
		try {
			String sql = """
					    UPDATE BuyerNotifications
					       SET Content=?,
					           RedirectUrl=?,
					           Type=?
					     WHERE NotificationID=?
					""";
			int rows = jdbcTemplate.update(sql, content, redirectUrl, type, notificationId);
			return rows > 0 ? "Cập nhật thông báo thành công!" : "Cập nhật thông báo thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi updateSnapshot: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật thông báo!";
		}
	}

	public String updateContent(int notificationId, String content, String redirectUrl, Integer type) {
		try {
			String sql = """
					    UPDATE BuyerNotifications
					    SET
					        Content     = COALESCE(?, Content),
					        RedirectUrl = COALESCE(?, RedirectUrl),
					        Type        = COALESCE(?, Type)
					    WHERE NotificationID = ?
					""";
			int rows = jdbcTemplate.update(sql, content, redirectUrl, type, notificationId);
			return rows > 0 ? "Cập nhật thông báo thành công!" : "Cập nhật thông báo thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi updateContent: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật thông báo!";
		}
	}

	/* ========================= DELETE ========================= */

	public String deleteById(int notificationId) {
		try {
			String sql = "DELETE FROM BuyerNotifications WHERE NotificationID=?";
			int rows = jdbcTemplate.update(sql, notificationId);
			return rows > 0 ? "Xóa thông báo thành công!" : "Xóa thông báo thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi deleteById: " + e.getMessage());
			return "Lỗi hệ thống khi xóa thông báo!";
		}
	}

	public int deleteAllOfUser(int userId) {
		try {
			String sql = "DELETE FROM BuyerNotifications WHERE UserID=?";
			return jdbcTemplate.update(sql, userId);
		} catch (Exception e) {
			System.err.println("Lỗi deleteAllOfUser: " + e.getMessage());
			return 0;
		}
	}

	/* ========================== READ ========================== */

	public BuyerNotification findById(int notificationId) {
		try {
			String sql = "SELECT * FROM BuyerNotifications WHERE NotificationID=?";
			List<BuyerNotification> list = jdbcTemplate.query(sql, rowMapper, notificationId);
			return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			System.err.println("Lỗi findById: " + e.getMessage());
			return null;
		}
	}

	public List<BuyerNotification> findAll() {
		try {
			String sql = "SELECT * FROM BuyerNotifications ORDER BY NotificationID DESC";
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			System.err.println("Lỗi findAll: " + e.getMessage());
			return List.of();
		}
	}

	public List<BuyerNotification> findAllPaged(int page, int size) {
		try {
			int offset = Math.max(0, (page - 1) * size);
			String sql = """
					    SELECT * FROM BuyerNotifications
					    ORDER BY NotificationID DESC
					    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
					""";
			return jdbcTemplate.query(sql, rowMapper, offset, size);
		} catch (Exception e) {
			System.err.println("Lỗi findAllPaged: " + e.getMessage());
			return List.of();
		}
	}

	public List<BuyerNotification> findByUser(int userId) {
		try {
			String sql = """
					    SELECT * FROM BuyerNotifications
					     WHERE UserID=?
					     ORDER BY NotificationID DESC
					""";
			return jdbcTemplate.query(sql, rowMapper, userId);
		} catch (Exception e) {
			System.err.println("Lỗi findByUser: " + e.getMessage());
			return List.of();
		}
	}

	public List<BuyerNotification> findUnreadByUser(int userId) {
		try {
			String sql = """
					    SELECT * FROM BuyerNotifications
					     WHERE UserID=? AND IsRead=0
					     ORDER BY NotificationID DESC
					""";
			return jdbcTemplate.query(sql, rowMapper, userId);
		} catch (Exception e) {
			System.err.println("Lỗi findUnreadByUser: " + e.getMessage());
			return List.of();
		}
	}

	public int countUnreadByUser(int userId) {
		try {
			String sql = "SELECT COUNT(*) FROM BuyerNotifications WHERE UserID=? AND IsRead=0";
			return jdbcTemplate.queryForObject(sql, Integer.class, userId);
		} catch (Exception e) {
			System.err.println("Lỗi countUnreadByUser: " + e.getMessage());
			return 0;
		}
	}

	public int getTotalNotifications() {
		try {
			String sql = "SELECT COUNT(*) FROM BuyerNotifications";
			return jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (Exception e) {
			System.err.println("Lỗi getTotalNotifications: " + e.getMessage());
			return 0;
		}
	}

	/** Đánh dấu tất cả thông báo của user là đã đọc */
	public int markAllReadOfUser(int userId) {
		try {
			String sql = "UPDATE BuyerNotifications SET IsRead=1 WHERE UserID=? AND IsRead=0";
			return jdbcTemplate.update(sql, userId);
		} catch (Exception e) {
			System.err.println("Lỗi markAllReadOfUser: " + e.getMessage());
			return 0;
		}
	}

	public boolean existsByUserAndTemplate(int userId, int templateId) {
		try {
			String sql = "SELECT 1 FROM BuyerNotifications WHERE UserID=? AND TemplateID=?";
			List<Integer> list = jdbcTemplate.query(sql, (rs, i) -> 1, userId, templateId);
			return !list.isEmpty();
		} catch (Exception e) {
			System.err.println("Lỗi existsByUserAndTemplate: " + e.getMessage());
			return false;
		}
	}

	// BuyerNotificationRepository.java
	public int countStatus1ByUser(int userId) {
		try {
			String sql = "SELECT COUNT(*) FROM BuyerNotifications WHERE UserID=? AND Status=1";
			return jdbcTemplate.queryForObject(sql, Integer.class, userId);
		} catch (Exception e) {
			System.err.println("Lỗi countStatus1ByUser: " + e.getMessage());
			return 0;
		}
	}

	/** (tuỳ chọn) lấy top N thông báo mới nhất để đổ dropdown */
	public List<BuyerNotification> findByUserTopN(int userId, int limit) {
		try {
			String sql = """
					    SELECT TOP (?) * FROM BuyerNotifications
					     WHERE UserID=?
					     ORDER BY CreatedAt DESC, NotificationID DESC
					""";
			return jdbcTemplate.query(sql, rowMapper, limit, userId);
		} catch (Exception e) {
			System.err.println("Lỗi findByUserTopN: " + e.getMessage());
			return List.of();
		}
	}

}
