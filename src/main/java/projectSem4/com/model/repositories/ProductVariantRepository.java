package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.ProductVariant;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductVariantRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final RowMapper<ProductVariant> rowMapperForProductVariant = (rs, rowNum) -> {
		ProductVariant v = new ProductVariant();
		v.setVariantId(rs.getLong("VariantID"));
		v.setProductId(rs.getInt("ProductID"));
		v.setVarianName(rs.getString("VariantName")); // JSON string
		v.setSKU(rs.getString("SKU"));
		v.setImage(rs.getString("Image"));

		Object priceObj = rs.getObject("Price");
		v.setPrice(priceObj == null ? null : rs.getDouble("Price"));

		Object qtyObj = rs.getObject("StockQuantity");
		v.setStockQuantity(qtyObj == null ? null : rs.getInt("StockQuantity"));

		Object stObj = rs.getObject("Status");
		v.setStatus(stObj == null ? null : rs.getBoolean("Status"));

		Timestamp cAt = rs.getTimestamp("CreatedAt");
		Timestamp uAt = rs.getTimestamp("UpdatedAt");
		v.setCreatedAt(cAt != null ? cAt.toLocalDateTime() : LocalDateTime.now());
		v.setUpdatedAt(uAt != null ? uAt.toLocalDateTime() : v.getCreatedAt());
		return v;
	};

	// ===================== CREATE =====================

	/** Insert và gán lại VariantID vào entity. */
	public String createProductVariant(ProductVariant variant) {
		try {
			String sql = """
					    INSERT INTO ProductVariants
					      (ProductID, VariantName, SKU, Image, Price, StockQuantity, Status, CreatedAt, UpdatedAt)
					    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
					""";
			KeyHolder kh = new GeneratedKeyHolder();
			int rows = jdbcTemplate.update(con -> {
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, variant.getProductId());
				ps.setString(2, variant.getVarianName()); // JSON compact
				ps.setString(3, variant.getSKU());
				ps.setString(4, variant.getImage());

				if (variant.getPrice() == null)
					ps.setObject(5, null);
				else
					ps.setDouble(5, variant.getPrice());

				if (variant.getStockQuantity() == null)
					ps.setObject(6, null);
				else
					ps.setInt(6, variant.getStockQuantity());

				ps.setBoolean(7, variant.getStatus() != null ? variant.getStatus() : true);
				ps.setTimestamp(8, Timestamp.valueOf(variant.getCreatedAt()));
				ps.setTimestamp(9, Timestamp.valueOf(variant.getUpdatedAt()));
				return ps;
			}, kh);

			if (rows > 0 && kh.getKey() != null) {
				variant.setVariantId(kh.getKey().longValue()); // gán id mới
				return "Tạo biến thể sản phẩm thành công!";
			}
			return "Tạo biến thể sản phẩm thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi tạo biến thể sản phẩm: " + e.getMessage());
			return "Lỗi hệ thống khi tạo biến thể sản phẩm!";
		}
	}

	// ===================== UPDATE =====================

	public String updateProductVariant(ProductVariant variant) {
		try {
			String sql = """
					    UPDATE ProductVariants
					    SET VariantName=?, SKU=?, Image=?, Price=?, StockQuantity=?, Status=?, UpdatedAt=?
					    WHERE VariantID=?
					""";
			int rows = jdbcTemplate.update(sql, variant.getVarianName(), variant.getSKU(), variant.getImage(),
					variant.getPrice(), variant.getStockQuantity(), variant.getStatus(),
					Timestamp.valueOf(variant.getUpdatedAt()), variant.getVariantId());
			return rows > 0 ? "Cập nhật biến thể sản phẩm thành công!" : "Cập nhật biến thể sản phẩm thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi cập nhật biến thể sản phẩm: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật biến thể sản phẩm!";
		}
	}

	// ===================== DELETE =====================

	public String deleteProductVariant(long id) {
		try {
			String sql = "DELETE FROM ProductVariants WHERE VariantID=?";
			int rows = jdbcTemplate.update(sql, id);
			return rows > 0 ? "Xóa biến thể sản phẩm thành công!" : "Xóa biến thể sản phẩm thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi xóa biến thể sản phẩm: " + e.getMessage());
			return "Lỗi hệ thống khi xóa biến thể sản phẩm!";
		}
	}

	// ===================== READ =====================

	public ProductVariant findById(long id) {
		try {
			String sql = "SELECT * FROM ProductVariants WHERE VariantID=?";
			List<ProductVariant> list = jdbcTemplate.query(sql, rowMapperForProductVariant, id);
			return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			System.err.println("Lỗi khi tìm biến thể sản phẩm ID=" + id + ": " + e.getMessage());
			return null;
		}
	}

	public List<ProductVariant> findAll() {
		try {
			String sql = "SELECT * FROM ProductVariants ORDER BY VariantID DESC";
			return jdbcTemplate.query(sql, rowMapperForProductVariant);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy tất cả biến thể sản phẩm: " + e.getMessage());
			return List.of();
		}
	}

	// Overload để khớp Service (Integer)
	public List<ProductVariant> findByProductId(Integer productId) {
		return findByProductId(productId.longValue());
	}

	public List<ProductVariant> findByProductId(long productId) {
		try {
			String sql = "SELECT * FROM ProductVariants WHERE ProductID=? ORDER BY VariantID DESC";
			return jdbcTemplate.query(sql, rowMapperForProductVariant, productId);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy biến thể của sản phẩm ID=" + productId + ": " + e.getMessage());
			return List.of();
		}
	}

	public List<ProductVariant> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = """
					    SELECT * FROM ProductVariants
					    ORDER BY VariantID DESC
					    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
					""";
			return jdbcTemplate.query(sql, rowMapperForProductVariant, offset, size);
		} catch (Exception e) {
			System.err.println("Lỗi khi phân trang biến thể sản phẩm: " + e.getMessage());
			return List.of();
		}
	}

	// ===================== SEARCH / COUNT =====================

	public List<ProductVariant> searchVariants(String keyword, int page, int size) {
		try {
			int offset = (page - 1) * size;
			String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
			String sql = """
					    SELECT * FROM ProductVariants
					    WHERE VariantName LIKE ? OR SKU LIKE ?
					    ORDER BY VariantID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
					""";
			return jdbcTemplate.query(sql, rowMapperForProductVariant, like, like, offset, size);
		} catch (Exception e) {
			System.err.println("Lỗi khi tìm kiếm biến thể sản phẩm: " + e.getMessage());
			return List.of();
		}
	}

	public int countSearchVariants(String keyword) {
		try {
			String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
			String sql = """
					    SELECT COUNT(*) FROM ProductVariants
					    WHERE VariantName LIKE ? OR SKU LIKE ?
					""";
			return jdbcTemplate.queryForObject(sql, Integer.class, like, like);
		} catch (Exception e) {
			System.err.println("Lỗi khi đếm kết quả tìm kiếm biến thể sản phẩm: " + e.getMessage());
			return 0;
		}
	}

	public int getTotalProductVariants() {
		try {
			String sql = "SELECT COUNT(*) FROM ProductVariants";
			return jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (Exception e) {
			System.err.println("Lỗi khi đếm biến thể sản phẩm: " + e.getMessage());
			return 0;
		}
	}

	// ===================== EXISTS / UTIL =====================

	/** Kiểm tra trùng SKU trong cùng sản phẩm (bỏ qua 1 id nếu có). */
	public boolean existsSKUInProduct(long productId, String SKU, Long excludeVariantId) {
		try {
			String sql = """
					    SELECT 1
					    FROM ProductVariants
					    WHERE ProductID = ? AND SKU = ? AND (? IS NULL OR VariantID <> ?)
					""";
			List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, productId, SKU, excludeVariantId, excludeVariantId);
			return !rs.isEmpty();
		} catch (Exception e) {
			System.err.println("Lỗi khi kiểm tra trùng SKU trong sản phẩm: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Kiểm tra trùng thuộc tính (VariantName – JSON compact) trong cùng sản phẩm.
	 */
	public boolean existsVariantNameInProduct(long productId, String variantNameJson, Long excludeVariantId) {
		try {
			String sql = """
					    SELECT 1
					    FROM ProductVariants
					    WHERE ProductID = ? AND VariantName = ?
					      AND (? IS NULL OR VariantID <> ?)
					""";
			List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, productId, variantNameJson, excludeVariantId,
					excludeVariantId);
			return !rs.isEmpty();
		} catch (Exception e) {
			System.err.println("Lỗi khi kiểm tra trùng VariantName JSON: " + e.getMessage());
			return false;
		}
	}

	// ===================== STATUS / BULK =====================

	public boolean updateStatus(long variantId, boolean status) {
		try {
			String sql = "UPDATE ProductVariants SET Status=?, UpdatedAt=? WHERE VariantID=?";
			int rows = jdbcTemplate.update(sql, status, LocalDateTime.now(), variantId);
			return rows > 0;
		} catch (Exception e) {
			System.err.println("Lỗi khi cập nhật trạng thái biến thể ID=" + variantId + ": " + e.getMessage());
			return false;
		}
	}

	public int deleteAllVariantsOfProduct(long productId) {
		try {
			String sql = "DELETE FROM ProductVariants WHERE ProductID=?";
			return jdbcTemplate.update(sql, productId);
		} catch (Exception e) {
			System.err.println("Lỗi khi xóa tất cả biến thể của sản phẩm ID=" + productId + ": " + e.getMessage());
			return 0;
		}
	}

	public Double getMaxPriceOfProduct(long productId) {
		try {
			String sql = "SELECT MAX(Price) FROM ProductVariants WHERE ProductID=?";
			return jdbcTemplate.queryForObject(sql, Double.class, productId);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy giá cao nhất sản phẩm ID=" + productId + ": " + e.getMessage());
			return null;
		}
	}

	public Double getMinPriceOfProduct(long productId) {
		try {
			String sql = "SELECT MIN(Price) FROM ProductVariants WHERE ProductID=?";
			return jdbcTemplate.queryForObject(sql, Double.class, productId);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy giá thấp nhất sản phẩm ID=" + productId + ": " + e.getMessage());
			return null;
		}
	}

	public ProductVariant findFirstByProductIdOrderByPriceAsc(Integer productId) {
		try {
			String sql = """
					    SELECT TOP 1 * FROM ProductVariants
					    WHERE ProductID=? AND Price IS NOT NULL
					    ORDER BY Price ASC
					""";
			List<ProductVariant> rs = jdbcTemplate.query(sql, rowMapperForProductVariant, productId);
			return rs.isEmpty() ? null : rs.get(0);
		} catch (Exception e) {
			System.err.println("findCheapestVariantOfProduct error: " + e.getMessage());
			return null;
		}
	}

	public Integer getProductIdByVariantId(Integer variantId) {
		try {
			String sql = "SELECT ProductID FROM ProductVariants WHERE VariantID = ?";
			return jdbcTemplate.queryForObject(sql, Integer.class, variantId);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy ProductID từ VariantID=" + variantId + ": " + e.getMessage());
			return null;
		}
	}
	
}
