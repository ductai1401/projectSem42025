package projectSem4.com.model.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.Category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Repository
public class CategoryRepository {

    private static final Logger log = LoggerFactory.getLogger(CategoryRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper null-safe cho Timestamp + 2 cột mới Image/CategoryOption
    private static final RowMapper<Category> CATEGORY_MAPPER = new RowMapper<>() {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            Category c = new Category();
            c.setCategoryId(rs.getInt("CategoryID"));

            int parentId = rs.getInt("ParentID");
            c.setParentCategory(rs.wasNull() ? null : parentId);

            c.setCategoryName(rs.getString("CategoryName"));
            c.setDescription(rs.getString("Description"));
            c.setSortOrder(rs.getInt("SortOrder"));
            c.setStatus(rs.getInt("Status"));

            // ✅ map 2 cột mới
            c.setImage(rs.getString("Image"));
            c.setCategoryOption(rs.getString("CategoryOption"));

            c.setCreatedAt(tsToLdt(rs.getTimestamp("CreatedAt")));
            c.setUpdatedAt(tsToLdt(rs.getTimestamp("UpdatedAt")));
            return c;
        }

        @Nullable
        private LocalDateTime tsToLdt(@Nullable Timestamp ts) {
            return (ts == null) ? null : ts.toLocalDateTime();
        }
    };

    // ================== CRUD ==================

    public String createCategory(Category category) {
        try {
            String sql = """
                INSERT INTO Categories
                  (ParentID, CategoryName, Description, SortOrder, Status, CreatedAt, UpdatedAt, Image, CategoryOption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            int rows = jdbcTemplate.update(sql,
                    category.getParentCategory(),
                    category.getCategoryName(),
                    category.getDescription(),
                    category.getSortOrder(),
                    category.getStatus(),
                    category.getCreatedAt(),
                    category.getUpdatedAt(),
                    category.getImage(),              // ✅ mới
                    category.getCategoryOption()      // ✅ mới
            );
            return rows > 0 ? "Tạo danh mục thành công!" : "Tạo danh mục thất bại!";
        } catch (Exception e) {
            log.error("Lỗi khi tạo danh mục", e);
            return "Lỗi hệ thống khi tạo danh mục!";
        }
    }

    public String updateCategory(Category category) {
        try {
            String sql = """
                UPDATE Categories
                   SET ParentID=?,
                       CategoryName=?,
                       Description=?,
                       SortOrder=?,
                       Status=?,
                       UpdatedAt=?,
                       Image=?,
                       CategoryOption=?
                 WHERE CategoryID=?
                """;
            int rows = jdbcTemplate.update(sql,
                    category.getParentCategory(),
                    category.getCategoryName(),
                    category.getDescription(),
                    category.getSortOrder(),
                    category.getStatus(),
                    category.getUpdatedAt(),
                    category.getImage(),             // ✅ mới
                    category.getCategoryOption(),    // ✅ mới
                    category.getCategoryId()
            );
            return rows > 0 ? "Cập nhật thành công!" : "Cập nhật thất bại!";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật danh mục id={}", category.getCategoryId(), e);
            return "Lỗi hệ thống khi cập nhật danh mục!";
        }
    }

    public String deleteCategory(int id) {
        try {
            String sql = "DELETE FROM Categories WHERE CategoryID=?";
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0 ? "Xóa thành công!" : "Xóa thất bại!";
        } catch (Exception e) {
            log.error("Lỗi khi xóa danh mục id={}", id, e);
            return "Lỗi hệ thống khi xóa danh mục!";
        }
    }

    @Nullable
    public Category findById(int id) {
        try {
            String sql = "SELECT * FROM Categories WHERE CategoryID=?";
            List<Category> list = jdbcTemplate.query(sql, CATEGORY_MAPPER, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.error("Lỗi khi tìm danh mục ID={}", id, e);
            return null;
        }
    }

    public List<Category> findAll() {
        try {
            String sql = "SELECT * FROM Categories";
            return jdbcTemplate.query(sql, CATEGORY_MAPPER);
        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả danh mục", e);
            return Collections.emptyList();
        }
    }

    // ================== Phân trang / Tìm kiếm ==================
    public List<Category> findAllPaged(int page, int size) {
        try {
            int offset = Math.max(0, (page - 1) * size);
            String sql = """
                SELECT * FROM Categories
                ORDER BY CategoryID DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
            return jdbcTemplate.query(sql, CATEGORY_MAPPER, offset, size);
        } catch (Exception e) {
            log.error("Lỗi phân trang page={}, size={}", page, size, e);
            return Collections.emptyList();
        }
    }

    public List<Category> searchCategories(String keyword, int page, int size) {
        try {
            int offset = Math.max(0, (page - 1) * size);
            String like = "%" + (keyword == null ? "" : keyword) + "%";
            String sql = """
                SELECT * FROM Categories
                WHERE CategoryName LIKE ?
                ORDER BY CategoryID DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
            return jdbcTemplate.query(sql, CATEGORY_MAPPER, like, offset, size);
        } catch (Exception e) {
            log.error("Lỗi tìm kiếm keyword='{}'", keyword, e);
            return Collections.emptyList();
        }
    }

    public int getTotalCategories() {
        try {
            String sql = "SELECT COUNT(*) FROM Categories";
            Integer n = jdbcTemplate.queryForObject(sql, Integer.class);
            return (n == null) ? 0 : n;
        } catch (Exception e) {
            log.error("Lỗi đếm danh mục", e);
            return 0;
        }
    }

    public int countSearchCategories(String keyword) {
        try {
            String like = "%" + (keyword == null ? "" : keyword) + "%";
            String sql = "SELECT COUNT(*) FROM Categories WHERE CategoryName LIKE ?";
            Integer n = jdbcTemplate.queryForObject(sql, Integer.class, like);
            return (n == null) ? 0 : n;
        } catch (Exception e) {
            log.error("Lỗi đếm kết quả tìm kiếm keyword='{}'", keyword, e);
            return 0;
        }
    }

    // ================== Tiện ích nghiệp vụ ==================

    public boolean existsSiblingName(@Nullable Integer parentId, String normalizedLowerName, @Nullable Integer excludeId) {
        try {
            String sql = """
                SELECT COUNT(*)
                  FROM Categories
                 WHERE ( (ParentID IS NULL AND ? IS NULL) OR ParentID = ? )
                   AND LOWER(TRIM(REPLACE(CategoryName, '  ', ' '))) = ?
                   AND ( ? IS NULL OR CategoryID <> ? )
                """;
            Integer n = jdbcTemplate.queryForObject(sql, Integer.class,
                    parentId, parentId,
                    normalizedLowerName,
                    excludeId, excludeId
            );
            return n != null && n > 0;
        } catch (Exception e) {
            log.error("Lỗi kiểm tra tên trùng parentId={}, excludeId={}", parentId, excludeId, e);
            return false;
        }
    }

    public boolean hasChildren(int id) {
        try {
            String sql = """
                SELECT 1 FROM Categories WHERE ParentID = ?
                OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY
                """;
            List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, id);
            return !rs.isEmpty();
        } catch (Exception e) {
            log.error("Lỗi kiểm tra danh mục con id={}", id, e);
            return false;
        }
    }

    // ================== Cho category picker ==================

    public List<Category> findRoots() {
        try {
            String sql = """
                SELECT * FROM Categories
                 WHERE ParentID IS NULL AND Status = 1
                 ORDER BY SortOrder, CategoryName
                """;
            return jdbcTemplate.query(sql, CATEGORY_MAPPER);
        } catch (Exception e) {
            log.error("Lỗi findRoots()", e);
            return Collections.emptyList();
        }
    }

    public List<Category> findChildren(Integer parentId) {
        try {
            String sql = """
                SELECT * FROM Categories
                 WHERE ParentID = ? AND Status = 1
                 ORDER BY SortOrder, CategoryName
                """;
            return jdbcTemplate.query(sql, CATEGORY_MAPPER, parentId);
        } catch (Exception e) {
            log.error("Lỗi findChildren(parentId={})", parentId, e);
            return Collections.emptyList();
        }
    }

    public List<Category> searchByName(String keyword) {
        try {
            String like = "%" + (keyword == null ? "" : keyword) + "%";
            String sql = """
                SELECT * FROM Categories
                 WHERE Status = 1 AND CategoryName LIKE ?
                 ORDER BY SortOrder, CategoryName
                """;
            return jdbcTemplate.query(sql, CATEGORY_MAPPER, like);
        } catch (Exception e) {
            log.error("Lỗi searchByName(keyword='{}')", keyword, e);
            return Collections.emptyList();
        }
    }

    public boolean existsById(Integer id) {
        try {
            String sql = "SELECT COUNT(*) FROM Categories WHERE CategoryID = ?";
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, id);
            return cnt != null && cnt > 0;
        } catch (Exception e) {
            log.error("Lỗi existsById(id={})", id, e);
            return false;
        }
    }

    public List<String> getBreadcrumbNames(Integer id) {
        try {
            Category cur = findById(id);
            if (cur == null) return Collections.emptyList();

            LinkedList<String> names = new LinkedList<>();
            while (cur != null) {
                names.addFirst(cur.getCategoryName()); // root -> leaf
                Integer parent = cur.getParentCategory();
                if (parent == null) break;
                cur = findById(parent);
            }
            return names;
        } catch (Exception e) {
            log.error("Lỗi getBreadcrumbNames(id={})", id, e);
            return Collections.emptyList();
        }
    }

    public String getCategoryNameById(int categoryId) {
        try {
            String sql = "SELECT CategoryName FROM Categories WHERE CategoryID=?";
            return jdbcTemplate.queryForObject(sql, String.class, categoryId);
        } catch (Exception e) {
            log.error("Lỗi khi lấy tên danh mục ID={}", categoryId, e);
            return null;
        }
    }
}
