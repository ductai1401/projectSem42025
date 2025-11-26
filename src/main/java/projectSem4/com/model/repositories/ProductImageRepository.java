package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.ProductImage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductImageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ProductImage> rowMapper = (ResultSet rs, int rowNum) -> {
        ProductImage img = new ProductImage();
        img.setImageId(rs.getInt("ImageID"));
        img.setProductId(rs.getInt("ProductID"));
        img.setImageUrl(rs.getString("ImageURL"));
        img.setAltText(rs.getString("AltText"));
        img.setDisplayOrder(rs.getInt("DisplayOrder"));
        return img;
    };

    /* ========================== CREATE ========================== */

    public Integer create(ProductImage img) {
        try {
            final String sql = """
                INSERT INTO ProductImages (ProductID, ImageURL, AltText, DisplayOrder)
                VALUES (?, ?, ?, ?)
            """;
            KeyHolder kh = new GeneratedKeyHolder();
            int affected = jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                ps.setInt(i++, img.getProductId());
                ps.setString(i++, img.getImageUrl());
                ps.setString(i++, img.getAltText());
                ps.setInt(i, img.getDisplayOrder() == null ? 0 : img.getDisplayOrder());
                return ps;
            }, kh);

            if (affected > 0 && kh.getKey() != null) {
                Integer id = kh.getKey().intValue();
                img.setImageId(id);
                return id;
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Lỗi create ProductImage: " + e.getMessage());
            return null;
        }
    }

    public int[][] createMany(int productId, List<ProductImage> images) {
        try {
            if (images == null || images.isEmpty()) return new int[0][];
            final String sql = """
                INSERT INTO ProductImages (ProductID, ImageURL, AltText, DisplayOrder)
                VALUES (?, ?, ?, ?)
            """;
            return jdbcTemplate.batchUpdate(sql, images, images.size(), (ps, img) -> {
                int i = 1;
                ps.setInt(i++, productId);

                // đúng getter theo entity của bạn
                ps.setString(i++, img.getImageUrl());

                if (img.getAltText() == null) ps.setNull(i++, Types.NVARCHAR);
                else ps.setString(i++, img.getAltText());

                if (img.getDisplayOrder() == null) ps.setNull(i, Types.INTEGER); // hoặc ps.setInt(i, 0);
                else ps.setInt(i, img.getDisplayOrder());
            });
        } catch (Exception e) {
            System.err.println("❌ Lỗi createMany ProductImage: " + e.getMessage());
            return new int[0][];
        }
    }



    /* =========================== READ =========================== */

    public ProductImage findById(int imageId) {
        try {
            String sql = "SELECT * FROM ProductImages WHERE ImageID = ?";
            List<ProductImage> list = jdbcTemplate.query(sql, rowMapper, imageId);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("❌ Lỗi findById ProductImage: " + e.getMessage());
            return null;
        }
    }

    public List<ProductImage> findByProductId(int productId) {
        try {
            String sql = """
                SELECT * FROM ProductImages
                WHERE ProductID = ?
                ORDER BY DisplayOrder ASC, ImageID ASC
            """;
            return jdbcTemplate.query(sql, rowMapper, productId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi findByProductId ProductImage: " + e.getMessage());
            return List.of();
        }
    }

    public int countByProductId(int productId) {
        try {
            String sql = "SELECT COUNT(*) FROM ProductImages WHERE ProductID = ?";
            Integer n = jdbcTemplate.queryForObject(sql, Integer.class, productId);
            return n == null ? 0 : n;
        } catch (Exception e) {
            System.err.println("❌ Lỗi countByProductId ProductImage: " + e.getMessage());
            return 0;
        }
    }

    /* ========================== UPDATE ========================== */

    public int update(ProductImage img) {
        try {
            String sql = """
                UPDATE ProductImages
                   SET ImageURL = ?,
                       AltText = ?,
                       DisplayOrder = ?
                 WHERE ImageID = ?
            """;
            return jdbcTemplate.update(sql,
                    img.getImageUrl(),
                    img.getAltText(),
                    img.getDisplayOrder() == null ? 0 : img.getDisplayOrder(),
                    img.getImageId());
        } catch (Exception e) {
            System.err.println("❌ Lỗi update ProductImage: " + e.getMessage());
            return 0;
        }
    }

    public int updateDisplayOrder(int imageId, int displayOrder) {
        try {
            String sql = "UPDATE ProductImages SET DisplayOrder = ? WHERE ImageID = ?";
            return jdbcTemplate.update(sql, displayOrder, imageId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi updateDisplayOrder ProductImage: " + e.getMessage());
            return 0;
        }
    }

    public int swapDisplayOrder(int productId, int imageIdA, int imageIdB) {
        try {
            String q = "SELECT ImageID, DisplayOrder FROM ProductImages WHERE ProductID = ? AND ImageID IN (?, ?)";
            List<ProductImage> two = jdbcTemplate.query(q, (rs, i) -> {
                ProductImage pi = new ProductImage();
                pi.setImageId(rs.getInt("ImageID"));
                pi.setDisplayOrder(rs.getInt("DisplayOrder"));
                return pi;
            }, productId, imageIdA, imageIdB);

            if (two.size() != 2) return 0;

            Integer orderA = two.get(0).getImageId() == imageIdA ? two.get(0).getDisplayOrder() : two.get(1).getDisplayOrder();
            Integer orderB = two.get(0).getImageId() == imageIdB ? two.get(0).getDisplayOrder() : two.get(1).getDisplayOrder();

            List<Object[]> params = new ArrayList<>();
            params.add(new Object[]{orderB, imageIdA});
            params.add(new Object[]{orderA, imageIdB});
            return jdbcTemplate.batchUpdate("UPDATE ProductImages SET DisplayOrder=? WHERE ImageID=?", params).length;
        } catch (Exception e) {
            System.err.println("❌ Lỗi swapDisplayOrder ProductImage: " + e.getMessage());
            return 0;
        }
    }

    /* =========================== DELETE ========================== */

    public int deleteById(int imageId) {
        try {
            String sql = "DELETE FROM ProductImages WHERE ImageID = ?";
            return jdbcTemplate.update(sql, imageId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi deleteById ProductImage: " + e.getMessage());
            return 0;
        }
    }

    public int deleteByProductId(int productId) {
        try {
            String sql = "DELETE FROM ProductImages WHERE ProductID = ?";
            return jdbcTemplate.update(sql, productId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi deleteByProductId ProductImage: " + e.getMessage());
            return 0;
        }
    }
}
