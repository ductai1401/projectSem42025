package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.Role;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RoleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void logSingletonInstance() {
        System.out.println("✅ RoleRepository đã được khởi tạo. hashCode: " + System.identityHashCode(this));
    }

    // RowMapper cho Role
    private RowMapper<Role> rowMapperForRole = new RowMapper<Role>() {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer roleId = rs.getInt("RoleID");
            String roleName = rs.getString("RoleName");
            String description = rs.getString("Description");

            // Map dữ liệu và tạo đối tượng Role
            return new Role(roleId, roleName, description, null, null);
        }
    };

    public String createRole(Role role) {
        try {
            String sql = "INSERT INTO Roles (RoleName, Description) VALUES (?, ?)";
            int rows = jdbcTemplate.update(sql, role.getRoleName(), role.getDescription());
            return rows > 0 ? "Tạo role thành công!" : "Tạo role thất bại!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi tạo role: " + e.getMessage();
        }
    }

    // Tìm Role theo ID
    public Role findById(int roleId) {
        try {
            String sql = "SELECT * FROM Roles WHERE RoleID = ?";
            List<Role> roles = jdbcTemplate.query(sql, rowMapperForRole, roleId);
            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Tìm Role theo tên
    public Role findByRoleName(String roleName) {
        try {
            String sql = "SELECT * FROM Roles WHERE RoleName = ?";
            List<Role> roles = jdbcTemplate.query(sql, rowMapperForRole, roleName);
            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lấy tất cả các Role
    public List<Role> findAll() {
        try {
            String sql = "SELECT * FROM Roles";
            return jdbcTemplate.query(sql, rowMapperForRole);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Cập nhật thông tin Role
    public String updateRole(Role role) {
        try {
            String sql = "UPDATE Roles SET RoleName = ?, Description = ? WHERE RoleID = ?";
            int rows = jdbcTemplate.update(sql, role.getRoleName(), role.getDescription(), role.getRoleId());
            return rows > 0 ? "Cập nhật role thành công!" : "Cập nhật role thất bại!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi cập nhật role: " + e.getMessage();
        }
    }

    // Xóa Role theo ID
    public String deleteRole(int roleId) {
        try {
            String sql = "DELETE FROM Roles WHERE RoleID = ?";
            int rows = jdbcTemplate.update(sql, roleId);
            return rows > 0 ? "Xóa role thành công!" : "Xóa role thất bại!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi xóa role: " + e.getMessage();
        }
    }

    // Tìm kiếm Role theo tên (hoặc mô tả)
    public List<Role> searchRoles(String keyword) {
        try {
            String sql = "SELECT * FROM Roles WHERE RoleName LIKE ? OR Description LIKE ?";
            return jdbcTemplate.query(sql, rowMapperForRole, "%" + keyword + "%", "%" + keyword + "%");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Đếm tổng số Role
    public int getTotalRoles() {
        try {
            String sql = "SELECT COUNT(*) FROM Roles";
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // Tìm role với phân trang và tìm kiếm theo từ khóa
    public List<Role> findByRolePaged(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        StringBuilder sql = new StringBuilder("SELECT * FROM Roles");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" WHERE RoleName LIKE ? OR Description LIKE ?");
        }

        sql.append(" ORDER BY RoleID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        Object[] params;
        if (keyword != null && !keyword.isBlank()) {
            params = new Object[] { "%" + keyword + "%", "%" + keyword + "%", offset, size };
        } else {
            params = new Object[] { offset, size };
        }

        return jdbcTemplate.query(sql.toString(), rowMapperForRole, params);
    }
}
