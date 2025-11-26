package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.NotificationTemplate;

import java.sql.*;
import java.util.List;

@Repository
public class NotificationTemplateRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final RowMapper<NotificationTemplate> rowMapper = (rs, rowNum) -> {
		NotificationTemplate t = new NotificationTemplate();
		t.setTemplateId(rs.getInt("TemplateID"));
		try {
			t.setCode(rs.getString("Code"));
		} catch (SQLException ignored) {
		}
		t.setTitle(rs.getString("Title"));
		t.setContent(rs.getString("Content"));
		t.setRedirectUrl(rs.getString("RedirectUrl"));
		t.setType(rs.getInt("Type"));
		try {
			t.setActive(rs.getBoolean("Active"));
		} catch (SQLException ignored) {
		}
		Timestamp created = rs.getTimestamp("CreatedAt");
		if (created != null)
			t.setCreatedAt(created.toLocalDateTime());
		return t;
	};

	/* ========== CREATE ========== */
	public Integer createReturningId(NotificationTemplate tpl) {
		final String sql = """
				    INSERT INTO NotificationTemplates (Code, Title, Content, RedirectUrl, Type, Active, CreatedAt)
				    VALUES (?, ?, ?, ?, ?, ?, SYSDATETIME())
				""";
		KeyHolder kh = new GeneratedKeyHolder();
		jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, tpl.getCode());
			ps.setString(2, tpl.getTitle());
			ps.setString(3, tpl.getContent());
			ps.setString(4, tpl.getRedirectUrl());
			if (tpl.getType() != null)
				ps.setInt(5, tpl.getType());
			else
				ps.setNull(5, Types.INTEGER);
			ps.setBoolean(6, (boolean) (tpl.getActive() != null ? tpl.getActive() : 1));
			return ps;
		}, kh);

		Number key = kh.getKey();
		return key != null ? key.intValue() : null;
	}

	/* ========== READ ========== */

	public NotificationTemplate findById(Integer id) {
		String sql = "SELECT * FROM NotificationTemplates WHERE TemplateID = ?";
		List<NotificationTemplate> list = jdbcTemplate.query(sql, rowMapper, id);
		return list.isEmpty() ? null : list.get(0);
	}

	public NotificationTemplate findByCode(String code) {
		if (code == null || code.isBlank())
			return null;
		String sql = "SELECT * FROM NotificationTemplates WHERE Code = ? AND (Active IS NULL OR Active = 1)";
		List<NotificationTemplate> list = jdbcTemplate.query(sql, rowMapper, code.trim());
		return list.isEmpty() ? null : list.get(0);
	}

	public List<NotificationTemplate> findAll() {
		String sql = "SELECT * FROM NotificationTemplates ORDER BY TemplateID DESC";
		return jdbcTemplate.query(sql, rowMapper);
	}

	public List<NotificationTemplate> findAllPaged(int page, int size) {
		int p = Math.max(1, page);
		int s = Math.max(1, size);
		int offset = (p - 1) * s;
		String sql = """
				    SELECT * FROM NotificationTemplates
				    ORDER BY TemplateID DESC
				    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
				""";
		return jdbcTemplate.query(sql, rowMapper, offset, s);
	}

	/* ========== UPDATE ========== */
	public String updateTemplate(NotificationTemplate tpl) {
		String sql = """
				    UPDATE NotificationTemplates
				       SET Code = ?, Title = ?, Content = ?, RedirectUrl = ?, Type = ?, Active = ?
				     WHERE TemplateID = ?
				""";
		int rows = jdbcTemplate.update(sql, tpl.getCode(), tpl.getTitle(), tpl.getContent(), tpl.getRedirectUrl(),
				tpl.getType(), tpl.getActive(), tpl.getTemplateId());
		return rows > 0 ? "Cập nhật template thành công!" : "Cập nhật template thất bại!";
	}

	/* ========== DELETE ========== */
	public String deleteById(Integer id) {
		try {
			String sql = "DELETE FROM NotificationTemplates WHERE TemplateID = ?";
			int rows = jdbcTemplate.update(sql, id);
			return rows > 0 ? "Xóa template thành công!" : "Xóa template thất bại!";
		} catch (Exception e) {
			return "Lỗi khi xóa template: " + e.getMessage();
		}
	}
}
