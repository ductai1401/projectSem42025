package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.WishlistItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WishlistItemRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<WishlistItem> rowMapper = (rs, rowNum) -> {
        WishlistItem w = new WishlistItem();
        w.setWishlistId(rs.getInt("WishlistID"));
        w.setUserId(rs.getInt("UserID"));
        w.setProductId(rs.getInt("ProductID"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        w.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        w.setStatus(rs.getInt("Status"));
        return w;
    };

    /* ========================= CREATE =========================
       - Nếu createdAt null: dùng SYSDATETIME() ở SQL
       - Trả về WishlistID (gán vào entity)
    ============================================================ */
    public Integer addItemReturningId(WishlistItem w) {
        try {
            final boolean hasCreatedAt = w.getCreatedAt() != null;
            final String sqlWithCreatedAt = """
                    INSERT INTO WishlistItems (UserID, ProductID, CreatedAt, Status)
                    VALUES (?, ?, ?, ?)
                    """;
            final String sqlNoCreatedAt = """
                    INSERT INTO WishlistItems (UserID, ProductID, CreatedAt, Status)
                    VALUES (?, ?, SYSDATETIME(), ?)
                    """;

            KeyHolder kh = new GeneratedKeyHolder();
            int affected;

            if (hasCreatedAt) {
                affected = jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(sqlWithCreatedAt, Statement.RETURN_GENERATED_KEYS);
                    int i = 1;
                    ps.setInt(i++, w.getUserId());
                    ps.setInt(i++, w.getProductId());
                    ps.setTimestamp(i++, Timestamp.valueOf(w.getCreatedAt()));
                    ps.setInt(i, w.getStatus() == null ? 1 : w.getStatus());
                    return ps;
                }, kh);
            } else {
                affected = jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(sqlNoCreatedAt, Statement.RETURN_GENERATED_KEYS);
                    int i = 1;
                    ps.setInt(i++, w.getUserId());
                    ps.setInt(i++, w.getProductId());
                    ps.setInt(i, w.getStatus() == null ? 1 : w.getStatus());
                    return ps;
                }, kh);
            }

            if (affected > 0 && kh.getKey() != null) {
                Integer id = kh.getKey().intValue();
                w.setWishlistId(id);
                if (w.getCreatedAt() == null) w.setCreatedAt(LocalDateTime.now()); // best-effort
                return id;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Lỗi addItemReturningId: " + e.getMessage());
            return null;
        }
    }

    /* Giữ API trả về message giống style bạn đang dùng */
    public String addItem(WishlistItem w) {
        try {
            Integer id = addItemReturningId(w);
            return (id != null) ? "Thêm wishlist item thành công!" : "Thêm wishlist item thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm wishlist item: " + e.getMessage());
            return "Lỗi hệ thống khi thêm wishlist item!";
        }
    }

    /* ========================= UPDATE ========================= */
    public String updateStatus(int wishlistId, int status) {
        try {
            String sql = "UPDATE WishlistItems SET Status = ? WHERE WishlistID = ?";
            int rows = jdbcTemplate.update(sql, status, wishlistId);
            return rows > 0 ? "Cập nhật trạng thái thành công!" : "Cập nhật trạng thái thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi updateStatus: " + e.getMessage());
            return "Lỗi hệ thống khi cập nhật trạng thái!";
        }
    }

    /* (Tuỳ chọn) cập nhật lại CreatedAt thủ công */
    public int updateCreatedAt(int wishlistId, LocalDateTime createdAt) {
        try {
            String sql = "UPDATE WishlistItems SET CreatedAt = ? WHERE WishlistID = ?";
            return jdbcTemplate.update(sql, Timestamp.valueOf(createdAt), wishlistId);
        } catch (Exception e) {
            System.err.println("Lỗi updateCreatedAt: " + e.getMessage());
            return 0;
        }
    }

    /* ========================= DELETE ========================= */
    public String deleteById(int wishlistId) {
        try {
            String sql = "DELETE FROM WishlistItems WHERE WishlistID = ?";
            int rows = jdbcTemplate.update(sql, wishlistId);
            return rows > 0 ? "Xóa wishlist item thành công!" : "Xóa wishlist item thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi deleteById: " + e.getMessage());
            return "Lỗi hệ thống khi xóa wishlist item!";
        }
    }

    public int deleteByUserAndProduct(int userId, int productId) {
        try {
            String sql = "DELETE FROM WishlistItems WHERE UserID = ? AND ProductID = ?";
            return jdbcTemplate.update(sql, userId, productId);
        } catch (Exception e) {
            System.err.println("Lỗi deleteByUserAndProduct: " + e.getMessage());
            return 0;
        }
    }

    /* ========================== READ ========================== */
    public WishlistItem findById(int wishlistId) {
        try {
            String sql = "SELECT * FROM WishlistItems WHERE WishlistID = ?";
            List<WishlistItem> list = jdbcTemplate.query(sql, rowMapper, wishlistId);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("Lỗi findById: " + e.getMessage());
            return null;
        }
    }

    public List<WishlistItem> findAll() {
        try {
            String sql = "SELECT * FROM WishlistItems ORDER BY WishlistID DESC";
            return jdbcTemplate.query(sql, rowMapper);
        } catch (Exception e) {
            System.err.println("Lỗi findAll: " + e.getMessage());
            return List.of();
        }
    }

    public List<WishlistItem> findAllPaged(int page, int size) {
        try {
            int offset = Math.max(0, (page - 1) * size);
            String sql = """
                    SELECT * FROM WishlistItems
                    ORDER BY WishlistID DESC
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                    """;
            return jdbcTemplate.query(sql, rowMapper, offset, size);
        } catch (Exception e) {
            System.err.println("Lỗi phân trang wishlist: " + e.getMessage());
            return List.of();
        }
    }

    public List<WishlistItem> findByUserId(int userId) {
        try {
            String sql = """
                    SELECT * FROM WishlistItems
                    WHERE UserID = ?
                    ORDER BY WishlistID DESC
                    """;
            return jdbcTemplate.query(sql, rowMapper, userId);
        } catch (Exception e) {
            System.err.println("Lỗi findByUserId: " + e.getMessage());
            return List.of();
        }
    }

    public boolean existsByUserAndProduct(int userId, int productId) {
        try {
            String sql = "SELECT 1 FROM WishlistItems WHERE UserID = ? AND ProductID = ?";
            List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, userId, productId);
            return !rs.isEmpty();
        } catch (Exception e) {
            System.err.println("Lỗi existsByUserAndProduct: " + e.getMessage());
            return false;
        }
    }

    public int countByUserId(int userId) {
        try {
            String sql = "SELECT COUNT(*) FROM WishlistItems WHERE UserID = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, userId);
        } catch (Exception e) {
            System.err.println("Lỗi countByUserId: " + e.getMessage());
            return 0;
        }
    }

    public int getTotalItems() {
        try {
            String sql = "SELECT COUNT(*) FROM WishlistItems";
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            System.err.println("Lỗi getTotalItems: " + e.getMessage());
            return 0;
        }
    }

    /* ===================== HÀM TIỆN ÍCH ====================== */
    /** Thêm nếu chưa tồn tại (theo UserID + ProductID), trả về ID mới hoặc ID đang có */
    public Integer addIfNotExists(int userId, int productId) {
        try {
            String findSql = "SELECT * FROM WishlistItems WHERE UserID = ? AND ProductID = ?";
            List<WishlistItem> list = jdbcTemplate.query(findSql, rowMapper, userId, productId);
            if (!list.isEmpty()) {
                return list.get(0).getWishlistId();
            }
            WishlistItem w = new WishlistItem();
            w.setUserId(userId);
            w.setProductId(productId);
            w.setStatus(1);
            return addItemReturningId(w);
        } catch (Exception e) {
            System.err.println("Lỗi addIfNotExists: " + e.getMessage());
            return null;
        }
    }
 // Lấy danh sách user đã wishlist *bất kỳ* sản phẩm nào của 1 shop
    public List<Integer> findUserIdsByShop(Integer shopId) {
        String sql = """
            SELECT DISTINCT w.UserID
            FROM WishlistItems w
            JOIN Products p ON p.ProductID = w.ProductID
            WHERE p.ShopId = ?
              AND (w.Status IS NULL OR w.Status = 1)   -- nếu bạn dùng cờ active
        """;
        return jdbcTemplate.queryForList(sql, Integer.class, shopId);
    }

    // Lấy danh sách user đã wishlist *một sản phẩm cụ thể*
    public List<Integer> findUserIdsByProduct(Integer productId) {
        String sql = """
            SELECT DISTINCT UserID
            FROM WishlistItems
            WHERE ProductID = ?
              AND (Status IS NULL OR Status = 1)
        """;
        return jdbcTemplate.queryForList(sql, Integer.class, productId);
    }

}
