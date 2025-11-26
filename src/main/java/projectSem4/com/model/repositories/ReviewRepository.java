package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /* ========= RowMapper ========= */
    private final RowMapper<Review> rowMapperForReview = (rs, rowNum) -> {
        Review r = new Review();
        r.setReviewId(rs.getInt("ReviewID"));
        r.setProductId(rs.getInt("ProductID"));
        r.setUserId(rs.getInt("UserID"));
        r.setOrderId(rs.getString("OrderID"));
        r.setRating(rs.getInt("Rating"));
        r.setTypeRv(rs.getInt("TypeRV"));
        r.setReviewText(rs.getString("ReviewText"));
        Timestamp cAt = rs.getTimestamp("CreatedAt");
        r.setCreatedAt(cAt != null ? cAt.toLocalDateTime() : null);
        r.setStatus(rs.getInt("Status"));

        // Các cột phụ (nếu SELECT có join và alias tương ứng)
        try { r.setProductName(getSafe(rs, "ProductName")); } catch (SQLException ignored) {}
        try { r.setUserName(getSafe(rs, "UserName")); }     catch (SQLException ignored) {}

        return r;
    };

    private static String getSafe(ResultSet rs, String col) throws SQLException {
        try {
            rs.findColumn(col);
            return rs.getString(col);
        } catch (SQLException ex) {
            return null;
        }
    }

    /* ========= CREATE: trả về ReviewID ========= */
    public Integer createReviewReturningId(Review r) {
        final String sql = """
                INSERT INTO Reviews
                  (ProductID, UserID, OrderID, Rating, TypeRV, ReviewText, CreatedAt, Status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder kh = new GeneratedKeyHolder();
        int affected = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setInt(i++, r.getProductId());
            ps.setInt(i++, r.getUserId());
            ps.setString(i++, r.getOrderId());
            ps.setInt(i++, r.getRating());
            ps.setInt(i++, r.getTypeRv());
            ps.setString(i++, r.getReviewText());
            LocalDateTime createdAt = (r.getCreatedAt() != null) ? r.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(i++, Timestamp.valueOf(createdAt));
            ps.setInt(i, r.getStatus() == null ? 1 : r.getStatus());
            return ps;
        }, kh);

        if (affected > 0 && kh.getKey() != null) {
            Integer id = kh.getKey().intValue();
            r.setReviewId(id);
            return id;
        }
        return null;
    }

    public String createReview(Review r) {
        try {
            Integer id = createReviewReturningId(r);
            return (id != null) ? "Tạo review thành công!" : "Tạo review thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo review: " + e.getMessage());
            return "Lỗi hệ thống khi tạo review!";
        }
    }

    /* ========= UPDATE ========= */
    public String updateReview(Review r) {
        try {
            int rows;
            if (r.getReviewText() == null) {
                String sql = """
                        UPDATE Reviews
                           SET ProductID=?,
                               UserID=?,
                               OrderID=?,
                               Rating=?,
                               TypeRV=?,
                               Status=?
                         WHERE ReviewID=?
                        """;
                rows = jdbcTemplate.update(sql,
                        r.getProductId(), r.getUserId(), r.getOrderId(),
                        r.getRating(), r.getTypeRv(), r.getStatus(),
                        r.getReviewId());
            } else {
                String sql = """
                        UPDATE Reviews
                           SET ProductID=?,
                               UserID=?,
                               OrderID=?,
                               Rating=?,
                               TypeRV=?,
                               ReviewText=?,
                               Status=?
                         WHERE ReviewID=?
                        """;
                rows = jdbcTemplate.update(sql,
                        r.getProductId(), r.getUserId(), r.getOrderId(),
                        r.getRating(), r.getTypeRv(), r.getReviewText(),
                        r.getStatus(), r.getReviewId());
            }
            return rows > 0 ? "Cập nhật review thành công!" : "Cập nhật review thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật review: " + e.getMessage());
            return "Lỗi hệ thống khi cập nhật review!";
        }
    }

    public int updateReviewContent(int reviewId, String reviewText, Integer rating, Integer typeRv) {
        String sql = """
                UPDATE Reviews
                   SET ReviewText = ?,
                       Rating = ?,
                       TypeRV = ?
                 WHERE ReviewID = ?
                """;
        return jdbcTemplate.update(sql, reviewText, rating, typeRv, reviewId);
    }

    public int updateStatus(int reviewId, int status) {
        String sql = "UPDATE Reviews SET Status=? WHERE ReviewID=?";
        return jdbcTemplate.update(sql, status, reviewId);
    }

    /* ========= DELETE ========= */
    public String deleteReview(int id) {
        try {
            String sql = "DELETE FROM Reviews WHERE ReviewID=?";
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0 ? "Xóa review thành công!" : "Xóa review thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa review: " + e.getMessage());
            return "Lỗi hệ thống khi xóa review!";
        }
    }

    /* ========= READ ========= */
    public Review findById(int id) {
        try {
            String sql = """
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE r.ReviewID=?
                    """;
            List<Review> list = jdbcTemplate.query(sql, rowMapperForReview, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm review ID=" + id + ": " + e.getMessage());
            return null;
        }
    }

    public List<Review> findAll() {
        try {
            String sql = """
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                  ORDER BY r.ReviewID DESC
                    """;
            return jdbcTemplate.query(sql, rowMapperForReview);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả review: " + e.getMessage());
            return List.of();
        }
    }

    /* ========= Pagination ========= */
    public List<Review> findAllPaged(int page, int size) {
        try {
            int offset = (page - 1) * size;
            String sql = """
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                  ORDER BY r.ReviewID DESC
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                    """;
            return jdbcTemplate.query(sql, rowMapperForReview, offset, size);
        } catch (Exception e) {
            System.err.println("Lỗi khi phân trang review: " + e.getMessage());
            return List.of();
        }
    }

    /* ========= Search / Filter ========= */
    public List<Review> searchReviews(String keyword,
                                      Integer productId,
                                      Integer userId,
                                      Integer rating,
                                      Integer status,
                                      int page,
                                      int size) {
        try {
            int offset = (page - 1) * size;
            String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

            StringBuilder sql = new StringBuilder("""
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE (r.ReviewText LIKE ? OR r.OrderID LIKE ?)
                    """);

            List<Object> args = new ArrayList<>();
            args.add(like);
            args.add(like);

            if (productId != null) {
                sql.append(" AND r.ProductID = ?");
                args.add(productId);
            }
            if (userId != null) {
                sql.append(" AND r.UserID = ?");
                args.add(userId);
            }
            if (rating != null) {
                sql.append(" AND r.Rating = ?");
                args.add(rating);
            }
            if (status != null) {
                sql.append(" AND r.Status = ?");
                args.add(status);
            }

            sql.append(" ORDER BY r.ReviewID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            args.add(offset);
            args.add(size);

            return jdbcTemplate.query(sql.toString(), rowMapperForReview, args.toArray());
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm review: " + e.getMessage());
            return List.of();
        }
    }

    public int countSearchReviews(String keyword,
                                  Integer productId,
                                  Integer userId,
                                  Integer rating,
                                  Integer status) {
        try {
            String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

            StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(*)
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE (r.ReviewText LIKE ? OR r.OrderID LIKE ?)
                    """);

            List<Object> args = new ArrayList<>();
            args.add(like);
            args.add(like);

            if (productId != null) {
                sql.append(" AND r.ProductID = ?");
                args.add(productId);
            }
            if (userId != null) {
                sql.append(" AND r.UserID = ?");
                args.add(userId);
            }
            if (rating != null) {
                sql.append(" AND r.Rating = ?");
                args.add(rating);
            }
            if (status != null) {
                sql.append(" AND r.Status = ?");
                args.add(status);
            }

            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm kết quả review: " + e.getMessage());
            return 0;
        }
    }

    /* ========= Theo Product ========= */
    public List<Review> findByProductId(int productId) {
        try {
            String sql = """
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE r.ProductID = ?
                  ORDER BY r.ReviewID DESC
                    """;
            return jdbcTemplate.query(sql, rowMapperForReview, productId);
        } catch (Exception e) {
            System.err.println("Lỗi findByProductId: " + e.getMessage());
            return List.of();
        }
    }

    public List<Review> findByProductIdPaged(int productId, int page, int size) {
        try {
            int offset = Math.max(0, (page - 1) * size);
            String sql = """
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE r.ProductID = ?
                  ORDER BY r.CreatedAt DESC
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                    """;
            return jdbcTemplate.query(sql, rowMapperForReview, productId, offset, size);
        } catch (Exception e) {
            System.err.println("Lỗi findByProductIdPaged: " + e.getMessage());
            return List.of();
        }
    }

    public Double getAverageRatingByProduct(int productId) {
        try {
            String sql = """
                    SELECT AVG(CAST(r.Rating AS FLOAT))
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE r.ProductID = ?
                       AND r.Status = 1
                    """;
            return jdbcTemplate.queryForObject(sql, Double.class, productId);
        } catch (Exception e) {
            System.err.println("Lỗi getAverageRatingByProduct: " + e.getMessage());
            return null;
        }
    }

    public int countByProductId(int productId) {
        try {
            String sql = "SELECT COUNT(*) FROM Reviews WHERE ProductID = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, productId);
        } catch (Exception e) {
            System.err.println("Lỗi countByProductId: " + e.getMessage());
            return 0;
        }
    }

    /* ========= Theo User/Order ========= */
    public boolean existsUserReviewForOrder(int userId, String orderId, int productId) {
        try {
            String sql = """
                    SELECT 1
                      FROM Reviews
                     WHERE UserID=? AND OrderID=? AND ProductID=?
                    """;
            List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, userId, orderId, productId);
            return !rs.isEmpty();
        } catch (Exception e) {
            System.err.println("Lỗi existsUserReviewForOrder: " + e.getMessage());
            return false;
        }
    }

    public boolean existsUserReviewForOrder(int userId, String orderId) {
        try {
            String sql = """
                    SELECT 1
                      FROM Reviews
                     WHERE UserID = ? AND OrderID = ?
                    """;
            List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, userId, orderId);
            return !rs.isEmpty();
        } catch (Exception e) {
            System.err.println("Lỗi existsUserReviewForOrder(orderId): " + e.getMessage());
            return false;
        }
    }

    /* ========= (TÙY CHỌN) Theo Variant =========
       Nếu bảng Reviews KHÔNG có cột VariantID, hãy xoá ba hàm dưới đây.
    */
    public List<Review> findByVariantIdPaged(int variantId, int page, int size) {
        try {
            int offset = Math.max(0, (page - 1) * size);
            String sql = """
                    SELECT r.*,
                           u.FullName AS UserName
                      FROM Reviews r
                 LEFT JOIN Users u ON u.UserID = r.UserID
                     WHERE r.VariantID = ?
                  ORDER BY r.CreatedAt DESC
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                    """;
            return jdbcTemplate.query(sql, rowMapperForReview, variantId, offset, size);
        } catch (Exception e) {
            System.err.println("Lỗi findByVariantIdPaged: " + e.getMessage());
            return List.of();
        }
    }

    public int countByVariantId(int variantId) {
        try {
            String sql = "SELECT COUNT(*) FROM Reviews WHERE VariantID = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, variantId);
        } catch (Exception e) {
            System.err.println("Lỗi countByVariantId: " + e.getMessage());
            return 0;
        }
    }

    public Double getAverageRatingByVariant(int variantId) {
        try {
            String sql = """
                    SELECT AVG(CAST(Rating AS FLOAT))
                      FROM Reviews
                     WHERE VariantID = ? AND Status = 1
                    """;
            return jdbcTemplate.queryForObject(sql, Double.class, variantId);
        } catch (Exception e) {
            System.err.println("Lỗi getAverageRatingByVariant: " + e.getMessage());
            return null;
        }
    }
}
