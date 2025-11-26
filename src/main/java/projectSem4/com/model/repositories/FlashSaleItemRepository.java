package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.dto.FlashSaleForProductDTO;
import projectSem4.com.model.entities.FlashSaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FlashSaleItemRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /* ========== ROW MAPPER (entity) ========== */
    private final RowMapper<FlashSaleItem> rowMapper = (rs, rowNum) -> {
        FlashSaleItem it = new FlashSaleItem();
        it.setFlashSaleItemId(rs.getInt("FlashSaleItemID"));
        it.setFlashSaleId(rs.getInt("FlashSaleID"));
        it.setProductId(rs.getInt("ProductID"));
        it.setQuantity(rs.getInt("Quantity"));
        it.setPercern(rs.getInt("Percern"));
        it.setTotalAmount(rs.getFloat("TotalAmount"));
        return it;
    };

    /* ========== CREATE (returning id) ========== */
    public Integer createReturningId(FlashSaleItem item) {
        final String sql = """
            INSERT INTO FlashSaleItems (FlashSaleID, ProductID, Quantity, Percern, TotalAmount)
            VALUES (?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setInt(i++, item.getFlashSaleId());
            ps.setInt(i++, item.getProductId());
            ps.setInt(i++, item.getQuantity());
            ps.setInt(i++, item.getPercern() == null ? 0 : item.getPercern());
            ps.setFloat(i,  item.getTotalAmount() == null ? 0f : item.getTotalAmount());
            return ps;
        }, kh);
        if (rows > 0 && kh.getKey() != null) {
            int id = kh.getKey().intValue();
            item.setFlashSaleItemId(id);
            return id;
        }
        return null;
    }

    /* ========== BULK CREATE ========== */
    public int[][] bulkCreate(List<FlashSaleItem> items) {
        if (items == null || items.isEmpty()) return new int[0][];
        final String sql = """
            INSERT INTO FlashSaleItems (FlashSaleID, ProductID, Quantity, Percern, TotalAmount)
            VALUES (?, ?, ?, ?, ?)
        """;
        return jdbcTemplate.batchUpdate(sql, items, items.size(), (ps, it) -> {
            int i = 1;
            ps.setInt(i++, it.getFlashSaleId());
            ps.setInt(i++, it.getProductId());
            ps.setInt(i++, it.getQuantity());
            ps.setInt(i++, it.getPercern() == null ? 0 : it.getPercern());
            ps.setFloat(i,  it.getTotalAmount() == null ? 0f : it.getTotalAmount());
        });
    }

    /* ========== UPDATE ========== */
    public String update(FlashSaleItem item) {
        final String sql = """
            UPDATE FlashSaleItems
               SET FlashSaleID = ?,
                   ProductID   = ?,
                   Quantity    = ?,
                   Percern     = ?,
                   TotalAmount = ?
             WHERE FlashSaleItemID = ?
        """;
        try {
            int rows = jdbcTemplate.update(sql,
                    item.getFlashSaleId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPercern() == null ? 0 : item.getPercern(),
                    item.getTotalAmount() == null ? 0f : item.getTotalAmount(),
                    item.getFlashSaleItemId());
            return rows > 0 ? "Cập nhật FlashSaleItem thành công!" : "Cập nhật FlashSaleItem thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi update FlashSaleItem: " + e.getMessage());
            return "Lỗi hệ thống khi cập nhật FlashSaleItem!";
        }
    }

    /* ========== DELETE (BY ITEM) ========== */
    public String delete(int flashSaleItemId) {
        final String sql = "DELETE FROM FlashSaleItems WHERE FlashSaleItemID = ?";
        try {
            int rows = jdbcTemplate.update(sql, flashSaleItemId);
            return rows > 0 ? "Xóa FlashSaleItem thành công!" : "Xóa FlashSaleItem thất bại!";
        } catch (Exception e) {
            System.err.println("Lỗi delete FlashSaleItem: " + e.getMessage());
            return "Lỗi hệ thống khi xóa FlashSaleItem!";
        }
    }

    /* ========== NEW: DELETE BY PRODUCT (toàn bộ) ========== */
    public int deleteByProductId(int productId) {
        final String sql = "DELETE FROM FlashSaleItems WHERE ProductID = ?";
        return jdbcTemplate.update(sql, productId);
    }

    /* ========== NEW: DELETE BY PRODUCT chỉ các item thuộc FlashSale đang hiệu lực ========== */
    // Giả định DB là SQL Server (CURRENT_TIMESTAMP, JOIN + DELETE alias i). Nếu MySQL → đổi DELETE i → DELETE i.*
    public int deleteActiveByProductId(int productId) {
        final String sql = """
            DELETE i
              FROM FlashSaleItems i
              JOIN FlashSales f ON f.FlashSaleID = i.FlashSaleID
             WHERE i.ProductID = ?
               AND f.StartDate <= CURRENT_TIMESTAMP
               AND f.EndDate   >= CURRENT_TIMESTAMP
        """;
        return jdbcTemplate.update(sql, productId);
    }

    /* ========== NEW: DELETE theo FlashSale + Product (xoá 1 mục trong 1 FS) ========== */
    public int deleteByFlashSaleIdAndProductId(int flashSaleId, int productId) {
        final String sql = "DELETE FROM FlashSaleItems WHERE FlashSaleID = ? AND ProductID = ?";
        return jdbcTemplate.update(sql, flashSaleId, productId);
    }

    /* ========== FIND ONE ========== */
    public FlashSaleItem findById(int id) {
        try {
            final String sql = "SELECT * FROM FlashSaleItems WHERE FlashSaleItemID = ?";
            List<FlashSaleItem> list = jdbcTemplate.query(sql, rowMapper, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("Lỗi findById FlashSaleItem: " + e.getMessage());
            return null;
        }
    }

    /* ========== FIND BY FLASHSALE ========== */
    public List<FlashSaleItem> findByFlashSaleId(int flashSaleId) {
        try {
            final String sql = """
                SELECT * FROM FlashSaleItems
                 WHERE FlashSaleID = ?
                 ORDER BY FlashSaleItemID DESC
            """;
            return jdbcTemplate.query(sql, rowMapper, flashSaleId);
        } catch (Exception e) {
            System.err.println("Lỗi findByFlashSaleId: " + e.getMessage());
            return List.of();
        }
    }

    /* ========== PAGINATION ========== */
    public List<FlashSaleItem> findPagedByFlashSaleId(int flashSaleId, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        try {
            final String sql = """
                SELECT * FROM FlashSaleItems
                 WHERE FlashSaleID = ?
                 ORDER BY FlashSaleItemID DESC
                 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;
            return jdbcTemplate.query(sql, rowMapper, flashSaleId, offset, size);
        } catch (Exception e) {
            System.err.println("Lỗi findPagedByFlashSaleId: " + e.getMessage());
            return List.of();
        }
    }

    public int countByFlashSaleId(int flashSaleId) {
        try {
            final String sql = "SELECT COUNT(*) FROM FlashSaleItems WHERE FlashSaleID = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, flashSaleId);
        } catch (Exception e) {
            System.err.println("Lỗi countByFlashSaleId: " + e.getMessage());
            return 0;
        }
    }

    /* ========== BUSINESS HELPERS ========== */

    // Kiểm tra trùng Product trong cùng 1 FlashSale
    public boolean existsProductInFlashSale(int flashSaleId, int productId, Integer excludeItemId) {
        try {
            final String sql = """
                SELECT 1 FROM FlashSaleItems
                 WHERE FlashSaleID = ?
                   AND ProductID   = ?
                   AND (? IS NULL OR FlashSaleItemID <> ?)
            """;
            List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1,
                    flashSaleId, productId, excludeItemId, excludeItemId);
            return !rs.isEmpty();
        } catch (Exception e) {
            System.err.println("Lỗi existsProductInFlashSale: " + e.getMessage());
            return false;
        }
    }

    // Cộng dồn số tiền đã bán
    public int addToTotalAmount(int flashSaleItemId, float delta) {
        final String sql = """
            UPDATE FlashSaleItems
               SET TotalAmount = TotalAmount + ?
             WHERE FlashSaleItemID = ?
        """;
        return jdbcTemplate.update(sql, delta, flashSaleItemId);
    }

    // Tìm theo danh sách product
    public List<FlashSaleItem> findByFlashSaleIdAndProductIds(int flashSaleId, List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        String placeholders = String.join(",", productIds.stream().map(x -> "?").toList());
        String sql = "SELECT * FROM FlashSaleItems WHERE FlashSaleID = ? AND ProductID IN (" + placeholders + ")";
        List<Object> args = new ArrayList<>();
        args.add(flashSaleId);
        args.addAll(productIds);
        try {
            return jdbcTemplate.query(sql, rowMapper, args.toArray());
        } catch (Exception e) {
            System.err.println("Lỗi findByFlashSaleIdAndProductIds: " + e.getMessage());
            return List.of();
        }
    }

    /* ========== JOIN với FlashSales để lấy theo productId ========== */

    // Mapper cho DTO khi JOIN với FlashSales
    private final RowMapper<FlashSaleForProductDTO> rowMapperFs = (rs, rowNum) -> {
        FlashSaleForProductDTO d = new FlashSaleForProductDTO();
        d.setFlashSaleItemId(rs.getInt("FlashSaleItemID"));
        d.setFlashSaleId(rs.getInt("FlashSaleID"));
        d.setProductId(rs.getInt("ProductID"));
        d.setQuantity(rs.getInt("Quantity"));
        d.setPercern(rs.getInt("Percern"));
        d.setTotalAmount(rs.getFloat("TotalAmount"));
        Timestamp s = rs.getTimestamp("StartDate");
        Timestamp e = rs.getTimestamp("EndDate");
        d.setStartDate(s == null ? null : s.toLocalDateTime());
        d.setEndDate(e == null ? null : e.toLocalDateTime());
        return d;
    };

    /** Lấy TẤT CẢ flash sale (mọi thời điểm) chứa productId. */
    public List<FlashSaleForProductDTO> findAllFlashSalesByProductId(int productId) {
        final String sql = """
            SELECT i.FlashSaleItemID, i.FlashSaleID, i.ProductID, i.Quantity, i.Percern, i.TotalAmount,
                   f.StartDate, f.EndDate
              FROM FlashSaleItems i
              JOIN FlashSales f ON f.FlashSaleID = i.FlashSaleID
             WHERE i.ProductID = ?
             ORDER BY f.StartDate DESC
        """;
        return jdbcTemplate.query(sql, rowMapperFs, productId);
    }

    /** Lấy flash sale ĐANG HIỆU LỰC cho productId (ưu tiên kết thúc sớm nhất). */
    public FlashSaleForProductDTO findActiveFlashSaleByProductId(int productId) {
        final String sql = """
            SELECT TOP 1
                   i.FlashSaleItemID, i.FlashSaleID, i.ProductID, i.Quantity, i.Percern, i.TotalAmount,
                   f.StartDate, f.EndDate
              FROM FlashSaleItems i
              JOIN FlashSales f ON f.FlashSaleID = i.FlashSaleID
             WHERE i.ProductID = ?
               AND f.StartDate <= CURRENT_TIMESTAMP
               AND f.EndDate   >= CURRENT_TIMESTAMP
             ORDER BY f.EndDate ASC
        """;
        List<FlashSaleForProductDTO> list = jdbcTemplate.query(sql, rowMapperFs, productId);
        return list.isEmpty() ? null : list.get(0);
    }
}
