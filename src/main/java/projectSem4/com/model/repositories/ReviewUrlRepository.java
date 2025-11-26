package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.ReviewUrl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewUrlRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ReviewUrl> rowMapper = (rs, i) -> {
        ReviewUrl r = new ReviewUrl();
        r.setMediaId(rs.getInt("MediaID"));
        r.setReviewId(rs.getInt("ReviewID"));
        r.setMediaUrl(rs.getString("MediaURL"));
        r.setType(rs.getInt("Type"));
        r.setDisplayOrder(rs.getInt("DisplayOrder"));

        // các cột hiển thị nếu SELECT có join thêm
        try { rs.findColumn("UserName"); r.setReviewUserName(rs.getString("UserName")); } catch (SQLException ignore) {}
        try { rs.findColumn("ProductName"); r.setProductName(rs.getString("ProductName")); } catch (SQLException ignore) {}
        return r;
    };

    /* =============== CREATE (returning id) =============== */
    public Integer createReturningId(ReviewUrl m) {
        final String sql = """
                INSERT INTO ReviewUrl (ReviewID, MediaURL, Type, DisplayOrder)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setInt(i++, m.getReviewId());
            ps.setString(i++, m.getMediaUrl());
            ps.setInt(i++, m.getType() == null ? 0 : m.getType());
            ps.setInt(i, m.getDisplayOrder() == null ? 0 : m.getDisplayOrder());
            return ps;
        }, kh);

        if (rows > 0 && kh.getKey() != null) {
            int id = kh.getKey().intValue();
            m.setMediaId(id);
            return id;
        }
        return null;
    }

    public String create(ReviewUrl m) {
        try {
            Integer id = createReturningId(m);
            return id != null ? "Tạo media thành công!" : "Tạo media thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi create ReviewUrl: " + e.getMessage());
            return "Lỗi hệ thống khi tạo media!";
        }
    }

    /* =============== UPDATE =============== */
    public String update(ReviewUrl m) {
        try {
            String sql = """
                    UPDATE ReviewUrl
                       SET ReviewID=?,
                           MediaURL=?,
                           Type=?,
                           DisplayOrder=?
                     WHERE MediaID=?
                    """;
            int rows = jdbcTemplate.update(sql,
                    m.getReviewId(), m.getMediaUrl(),
                    m.getType(), m.getDisplayOrder(),
                    m.getMediaId());
            return rows > 0 ? "Cập nhật media thành công!" : "Cập nhật media thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi update ReviewUrl: " + e.getMessage());
            return "Lỗi hệ thống khi cập nhật media!";
        }
    }

    /* =============== DELETE =============== */
    public String delete(int mediaId) {
        try {
            int rows = jdbcTemplate.update("DELETE FROM ReviewUrl WHERE MediaID=?", mediaId);
            return rows > 0 ? "Xoá media thành công!" : "Xoá media thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi delete ReviewUrl: " + e.getMessage());
            return "Lỗi hệ thống khi xoá media!";
        }
    }

    /* =============== READ =============== */
    public ReviewUrl findById(int id) {
        try {
            var list = jdbcTemplate.query("SELECT * FROM ReviewUrl WHERE MediaID=?", rowMapper, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("Lỗi findById ReviewUrl: " + e.getMessage());
            return null;
        }
    }

    public List<ReviewUrl> findAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM ReviewUrl ORDER BY MediaID DESC", rowMapper);
        } catch (Exception e) {
            System.err.println("Lỗi findAll ReviewUrl: " + e.getMessage());
            return List.of();
        }
    }

    public List<ReviewUrl> findByReviewId(int reviewId) {
        try {
            String sql = """
                    SELECT * FROM ReviewUrl
                     WHERE ReviewID=?
                     ORDER BY DisplayOrder ASC, MediaID ASC
                    """;
            return jdbcTemplate.query(sql, rowMapper, reviewId);
        } catch (Exception e) {
            System.err.println("Lỗi findByReviewId: " + e.getMessage());
            return List.of();
        }
    }

    /* Phân trang + lọc theo type (tuỳ chọn) */
    public List<ReviewUrl> findByReviewPaged(int reviewId, Integer type, int page, int size) {
        try {
            int offset = Math.max(0, (page - 1) * size);
            StringBuilder sql = new StringBuilder("""
                    SELECT * FROM ReviewUrl WHERE ReviewID=?
                    """);
            List<Object> args = new ArrayList<>();
            args.add(reviewId);
            if (type != null) {
                sql.append(" AND Type=?");
                args.add(type);
            }
            sql.append(" ORDER BY DisplayOrder ASC, MediaID ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            args.add(offset);
            args.add(size);
            return jdbcTemplate.query(sql.toString(), rowMapper, args.toArray());
        } catch (Exception e) {
            System.err.println("Lỗi findByReviewPaged: " + e.getMessage());
            return List.of();
        }
    }

    public int countByReview(int reviewId, Integer type) {
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ReviewUrl WHERE ReviewID=?");
            List<Object> args = new ArrayList<>();
            args.add(reviewId);
            if (type != null) {
                sql.append(" AND Type=?");
                args.add(type);
            }
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        } catch (Exception e) {
            System.err.println("Lỗi countByReview: " + e.getMessage());
            return 0;
        }
    }

    /* Đổi thứ tự hiển thị nhanh */
    public int updateDisplayOrder(int mediaId, int newOrder) {
        String sql = "UPDATE ReviewUrl SET DisplayOrder=? WHERE MediaID=?";
        return jdbcTemplate.update(sql, newOrder, mediaId);
    }
}
