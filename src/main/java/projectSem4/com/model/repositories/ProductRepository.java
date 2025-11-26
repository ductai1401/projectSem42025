package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final RowMapper<Product> rowMapperForProduct = new RowMapper<Product>() {
		@Override
		public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
			Product p = new Product();
			p.setProductId(rs.getInt("ProductID"));
			p.setShopId(rs.getInt("ShopID"));
			p.setCategoryId(rs.getInt("CategoryID"));
			p.setImage(rs.getString("Image"));
			p.setProductName(rs.getString("ProductName"));
			p.setDescription(rs.getString("Description"));
			p.setProductOption(rs.getString("productOption")); // có thể null
			p.setStatus(rs.getInt("Status"));
			p.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
			p.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
			return p;
		}
	};

	/*
	 * ========================================================= CREATE (flow 2
	 * bước) - Nếu p.getImage() == null: KHÔNG chèn cột Image để SQL dùng DEFAULT -
	 * Trả về ProductID (set vào entity luôn)
	 * =========================================================
	 */
	public Integer createProductReturningId(Product p) {
		KeyHolder kh = new GeneratedKeyHolder();

		final boolean hasImage = p.getImage() != null && !p.getImage().isBlank();

		final String sqlWithImage = """
				    INSERT INTO Products
				      (ShopID, CategoryID, Image, ProductName, Description, productOption, Status, CreatedAt, UpdatedAt)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		final String sqlNoImage = """
				    INSERT INTO Products
				      (ShopID, CategoryID, ProductName, Description, productOption, Status, CreatedAt, UpdatedAt)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";

		int affected;
		if (hasImage) {
			affected = jdbcTemplate.update(con -> {
				PreparedStatement ps = con.prepareStatement(sqlWithImage, Statement.RETURN_GENERATED_KEYS);
				int i = 1;
				ps.setInt(i++, p.getShopId());
				ps.setInt(i++, p.getCategoryId());
				ps.setString(i++, p.getImage()); // có ảnh
				ps.setString(i++, p.getProductName());
				ps.setString(i++, p.getDescription());
				ps.setString(i++, p.getProductOption());
				ps.setInt(i++, p.getStatus());
				ps.setTimestamp(i++, Timestamp.valueOf(p.getCreatedAt()));
				ps.setTimestamp(i, Timestamp.valueOf(p.getUpdatedAt()));
				return ps;
			}, kh);
		} else {
			affected = jdbcTemplate.update(con -> {
				PreparedStatement ps = con.prepareStatement(sqlNoImage, Statement.RETURN_GENERATED_KEYS);
				int i = 1;
				ps.setInt(i++, p.getShopId());
				ps.setInt(i++, p.getCategoryId());
				ps.setString(i++, p.getProductName());
				ps.setString(i++, p.getDescription());
				ps.setString(i++, p.getProductOption());
				ps.setInt(i++, p.getStatus());
				ps.setTimestamp(i++, Timestamp.valueOf(p.getCreatedAt()));
				ps.setTimestamp(i, Timestamp.valueOf(p.getUpdatedAt()));
				return ps;
			}, kh);
		}

		if (affected > 0 && kh.getKey() != null) {
			Integer id = kh.getKey().intValue();
			p.setProductId(id);
			return id;
		}
		return null;
	}

	/* Giữ lại signature cũ để tương thích, nhưng dùng method trên */
	public String createProduct(Product p) {
		try {
			Integer id = createProductReturningId(p);
			return (id != null) ? "Tạo sản phẩm thành công!" : "Tạo sản phẩm thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi tạo sản phẩm: " + e.getMessage());
			return "Lỗi hệ thống khi tạo sản phẩm!";
		}
	}

	/*
	 * ========================================================= UPDATE - Nếu muốn
	 * không đổi ảnh khi p.getImage()==null, dùng 2 câu lệnh
	 * =========================================================
	 */
	public String updateProduct(Product p) {
		try {
			int rows;
			if (p.getImage() == null) {
				String sql = """
						    UPDATE Products
						       SET ShopID=?,
						           CategoryID=?,
						           ProductName=?,
						           Description=?,
						           productOption=?,
						           Status=?,
						           UpdatedAt=?
						     WHERE ProductID=?
						""";
				rows = jdbcTemplate.update(sql, p.getShopId(), p.getCategoryId(), p.getProductName(),
						p.getDescription(), p.getProductOption(), p.getStatus(), Timestamp.valueOf(p.getUpdatedAt()),
						p.getProductId());
			} else {
				String sql = """
						    UPDATE Products
						       SET ShopID=?,
						           CategoryID=?,
						           Image=?,
						           ProductName=?,
						           Description=?,
						           productOption=?,
						           Status=?,
						           UpdatedAt=?
						     WHERE ProductID=?
						""";
				rows = jdbcTemplate.update(sql, p.getShopId(), p.getCategoryId(), p.getImage(), p.getProductName(),
						p.getDescription(), p.getProductOption(), p.getStatus(), Timestamp.valueOf(p.getUpdatedAt()),
						p.getProductId());
			}
			return rows > 0 ? "Cập nhật sản phẩm thành công!" : "Cập nhật sản phẩm thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật sản phẩm!";
		}
	}

	/* Cập nhật ảnh (bước 2) */
	public int updateProductImage(int productId, String image) {
		String sql = "UPDATE Products SET Image = ?, UpdatedAt = SYSDATETIME() WHERE ProductID = ?";
		return jdbcTemplate.update(sql, image, productId);
	}

	// DELETE
	public String deleteProduct(int id) {
		try {
			String sql = "DELETE FROM Products WHERE ProductID=?";
			int rows = jdbcTemplate.update(sql, id);
			return rows > 0 ? "Xóa sản phẩm thành công!" : "Xóa sản phẩm thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi xóa sản phẩm: " + e.getMessage());
			return "Lỗi hệ thống khi xóa sản phẩm!";
		}
	}

	// READ
	public Product findById(int id) {
		try {
			String sql = "SELECT * FROM Products WHERE ProductID=?";
			List<Product> list = jdbcTemplate.query(sql, rowMapperForProduct, id);
			return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			System.err.println("Lỗi khi tìm sản phẩm ID=" + id + ": " + e.getMessage());
			return null;
		}
	}

	public List<Product> findAll() {
		try {
			String sql = "SELECT * FROM Products ORDER BY ProductID DESC";
			return jdbcTemplate.query(sql, rowMapperForProduct);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy tất cả sản phẩm: " + e.getMessage());
			return List.of();
		}
	}

	// Pagination
	public List<Product> findAllPaged(int page, int size) {
		try {
			int offset = (page - 1) * size;
			String sql = """
					    SELECT * FROM Products
					    ORDER BY ProductID DESC
					    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
					""";
			return jdbcTemplate.query(sql, rowMapperForProduct, offset, size);
		} catch (Exception e) {
			System.err.println("Lỗi khi phân trang sản phẩm: " + e.getMessage());
			return List.of();
		}
	}

	// Search (by name/description) – dùng param binding
	public List<Product> searchProducts(String keyword, Integer categoryId, Integer shopId, int page, int size) {
		try {
			int offset = (page - 1) * size;
			String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

			StringBuilder sql = new StringBuilder("""
					    SELECT * FROM Products
					    WHERE (ProductName LIKE ? OR Description LIKE ?)
					""");
			List<Object> args = new ArrayList<>();
			args.add(like);
			args.add(like);

			if (categoryId != null) {
				sql.append(" AND CategoryID = ?");
				args.add(categoryId);
			}
			if (shopId != null) {
				sql.append(" AND ShopID = ?");
				args.add(shopId);
			}

			sql.append(" ORDER BY ProductID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
			args.add(offset);
			args.add(size);

			return jdbcTemplate.query(sql.toString(), rowMapperForProduct, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
			return List.of();
		}
	}

	public int getTotalProducts() {
		try {
			String sql = "SELECT COUNT(*) FROM Products";
			return jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (Exception e) {
			System.err.println("Lỗi khi đếm sản phẩm: " + e.getMessage());
			return 0;
		}
	}

	public int countSearchProducts(String keyword, Integer categoryId, Integer shopId) {
		try {
			String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

			StringBuilder sql = new StringBuilder("""
					    SELECT COUNT(*) FROM Products
					    WHERE (ProductName LIKE ? OR Description LIKE ?)
					""");
			List<Object> args = new ArrayList<>();
			args.add(like);
			args.add(like);

			if (categoryId != null) {
				sql.append(" AND CategoryID = ?");
				args.add(categoryId);
			}
			if (shopId != null) {
				sql.append(" AND ShopID = ?");
				args.add(shopId);
			}

			return jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi khi đếm kết quả tìm kiếm sản phẩm: " + e.getMessage());
			return 0;
		}
	}

	// Tiện ích: kiểm tra trùng tên theo Shop
	public boolean existsNameInShop(int shopId, String normalizedLowerName, Integer excludeProductId) {
		try {
			String sql = """
					    SELECT 1
					    FROM Products
					    WHERE ShopID = ?
					      AND LOWER(LTRIM(RTRIM(ProductName))) = ?
					      AND (? IS NULL OR ProductID <> ?)
					""";
			List<Integer> rs = jdbcTemplate.query(sql, (r, i) -> 1, shopId, normalizedLowerName, excludeProductId,
					excludeProductId);
			return !rs.isEmpty();
		} catch (Exception e) {
			System.err.println("Lỗi khi kiểm tra tên sản phẩm trùng trong shop: " + e.getMessage());
			return false;
		}
	}

	// Lấy CategoryID theo ProductID
	public Integer getCategoryId(int productId) {
		try {
			String sql = "SELECT CategoryID FROM Products WHERE ProductID = ?";
			return jdbcTemplate.queryForObject(sql, Integer.class, productId);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy CategoryID cho ProductID=" + productId + ": " + e.getMessage());
			return null;
		}
	}

	/** Cập nhật cột productOption (cho phép null) + UpdatedAt */
	public int updateProductOption(int productId, String productOption) {
		final String sql = "UPDATE Products " + "SET productOption = ?, UpdatedAt = SYSDATETIME() "
				+ "WHERE ProductID = ?";
		return jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql);
			if (productOption == null) {
				ps.setNull(1, Types.NVARCHAR);
			} else {
				ps.setString(1, productOption);
			}
			ps.setInt(2, productId);
			return ps;
		});
	}

	/**
	 * Cập nhật variantConfig (JSON) + UpdatedAt. - Ưu tiên cột VariantConfig (đúng
	 * phong cách đặt tên của bảng hiện tại). - Nếu DB của bạn đang dùng snake_case:
	 * thử variant_config. - Nếu cả hai cột chưa tồn tại -> fallback tạm vào
	 * productOption để không vỡ flow. (Khi đã thêm cột thật sự, chỉ cần đổi SQL ưu
	 * tiên ở trên là xong.)
	 */
	public int updateVariantConfig(int productId, String variantConfigJson) {
		// 1) Thử cột PascalCase: VariantConfig
		final String sqlVariantConfigPascal = "UPDATE Products " + "SET VariantConfig = ?, UpdatedAt = SYSDATETIME() "
				+ "WHERE ProductID = ?";

		try {
			return jdbcTemplate.update(con -> {
				PreparedStatement ps = con.prepareStatement(sqlVariantConfigPascal);
				if (variantConfigJson == null) {
					ps.setNull(1, Types.NVARCHAR);
				} else {
					ps.setString(1, variantConfigJson);
				}
				ps.setInt(2, productId);
				return ps;
			});
		} catch (Exception ignoreIfColumnNotExists) {
			// Cột VariantConfig có thể chưa tồn tại
		}

		// 2) Thử cột snake_case: variant_config
		final String sqlVariantConfigSnake = "UPDATE Products " + "SET variant_config = ?, UpdatedAt = SYSDATETIME() "
				+ "WHERE ProductID = ?";
		try {
			return jdbcTemplate.update(con -> {
				PreparedStatement ps = con.prepareStatement(sqlVariantConfigSnake);
				if (variantConfigJson == null) {
					ps.setNull(1, Types.NVARCHAR);
				} else {
					ps.setString(1, variantConfigJson);
				}
				ps.setInt(2, productId);
				return ps;
			});
		} catch (Exception ignoreIfColumnNotExists) {
			// Cột variant_config cũng chưa có
		}

		// 3) Fallback tạm: lưu chung vào productOption để không gãy chức năng
		final String sqlFallback = "UPDATE Products " + "SET productOption = ?, UpdatedAt = SYSDATETIME() "
				+ "WHERE ProductID = ?";
		return jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(sqlFallback);
			if (variantConfigJson == null) {
				ps.setNull(1, Types.NVARCHAR);
			} else {
				ps.setString(1, variantConfigJson);
			}
			ps.setInt(2, productId);
			return ps;
		});
	}

	public List<Product> findByCategoryPagedSorted(int categoryId, int page, // 1-based
			int size, String sortKey // "latest" | "price_low_high" | "price_high_low" | "name_az"
	) {
		try {
			int offset = Math.max(0, (page - 1) * size);

			// Chọn ORDER BY theo sortKey
			String orderBy;
			switch (sortKey == null ? "" : sortKey) {
			case "name_az" -> orderBy = "ProductName ASC";
			case "price_low_high" -> orderBy = "Price ASC"; // đổi nếu bạn join từ variants
			case "price_high_low" -> orderBy = "Price DESC"; // đổi nếu bạn join từ variants
			case "latest" -> orderBy = "CreatedAt DESC";
			default -> orderBy = "ProductID DESC";
			}

			// Nếu bạn CHƯA có cột Price trong Products, bỏ 2 case price_* ở trên hoặc thay
			// bằng join.
			String sql = """
					    SELECT * FROM Products
					    WHERE CategoryID = ?
					    ORDER BY %s
					    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
					""".formatted(orderBy);

			return jdbcTemplate.query(sql, rowMapperForProduct, categoryId, offset, size);
		} catch (Exception e) {
			System.err.println("Lỗi findByCategoryPagedSorted: " + e.getMessage());
			return List.of();
		}
	}

	public int countByCategoryId(int categoryId) {
		try {
			String sql = "SELECT COUNT(*) FROM Products WHERE CategoryID = ?";
			return jdbcTemplate.queryForObject(sql, Integer.class, categoryId);
		} catch (Exception e) {
			System.err.println("Lỗi countByCategoryId: " + e.getMessage());
			return 0;
		}
	}

	public List<Product> findByCategoryId(int categoryId) {
		try {
			String sql = """
					    SELECT * FROM Products
					    WHERE CategoryID = ?
					    ORDER BY ProductID DESC
					""";
			return jdbcTemplate.query(sql, rowMapperForProduct, categoryId);
		} catch (Exception e) {
			System.err.println("Lỗi findByCategoryId: " + e.getMessage());
			return List.of();
		}
	}

	public int countByCategoryIdsAndKeyword(List<Integer> categoryIds, String keyword) {
		if (categoryIds == null || categoryIds.isEmpty())
			return 0;

		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		String inPlaceholders = categoryIds.stream().map(id -> "?").collect(Collectors.joining(","));

		String sql = """
				SELECT COUNT(*) FROM Products
				WHERE (ProductName LIKE ? OR Description LIKE ?)
				  AND CategoryID IN (""" + inPlaceholders + ")";

		List<Object> args = new ArrayList<>();
		args.add(like);
		args.add(like);
		args.addAll(categoryIds);

		try {
			return jdbcTemplate.queryForObject(sql, Integer.class, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi countByCategoryIdsAndKeyword: " + e.getMessage());
			return 0;
		}
	}

	/** Lấy list theo nhiều category + keyword + phân trang + sort */
	public List<Product> findByCategoryIdsAndKeywordPagedSorted(List<Integer> categoryIds, String keyword, int page,
			int size, String sortKey) {

		if (categoryIds == null || categoryIds.isEmpty())
			return List.of();

		int offset = Math.max(0, (page - 1) * size);
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

		// Sắp xếp: name_az | latest | (price_* cần JOIN bảng variants -> tạm bỏ)
		String orderBy;
		switch (sortKey == null ? "" : sortKey) {
		case "name_az" -> orderBy = "ProductName ASC";
		case "latest" -> orderBy = "CreatedAt DESC";
		// Nếu có cột Price trong Products thì mở 2 case dưới:
		// case "price_low_high" -> orderBy = "Price ASC";
		// case "price_high_low" -> orderBy = "Price DESC";
		default -> orderBy = "ProductID DESC";
		}

		String inPlaceholders = categoryIds.stream().map(id -> "?").collect(Collectors.joining(","));

		String sql = """
				SELECT * FROM Products
				WHERE (ProductName LIKE ? OR Description LIKE ?)
				  AND CategoryID IN (""" + inPlaceholders + ") " + "ORDER BY " + orderBy + " "
				+ "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

		List<Object> args = new ArrayList<>();
		args.add(like);
		args.add(like);
		args.addAll(categoryIds);
		args.add(offset);
		args.add(size);

		try {
			return jdbcTemplate.query(sql, rowMapperForProduct, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi findByCategoryIdsAndKeywordPagedSorted: " + e.getMessage());
			return List.of();
		}
	}

	// Lấy danh sách sản phẩm theo ShopId
	public List<Product> findByShopId(Integer shopId) {
		try {
			String sql = """
					    SELECT * FROM Products
					     WHERE ShopID = ?
					     ORDER BY ProductID DESC
					""";
			return jdbcTemplate.query(sql, rowMapperForProduct, shopId);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy sản phẩm theo ShopID=" + shopId + ": " + e.getMessage());
			return List.of();
		}
	}

	public List<Product> searchProductsWithLimit(Integer shopId, String keyword, int limit) {
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

		StringBuilder sql = new StringBuilder("""
				    SELECT TOP (?) *
				    FROM Products
				    WHERE (ProductName LIKE ? OR Description LIKE ?)
				""");

		List<Object> args = new ArrayList<>();
		args.add(limit);
		args.add(like);
		args.add(like);

		if (shopId != null && shopId > 0) {
			sql.append(" AND ShopID = ?");
			args.add(shopId);
		}

		sql.append(" ORDER BY ProductID DESC");

		try {
			return jdbcTemplate.query(sql.toString(), rowMapperForProduct, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi searchProductsWithLimit: " + e.getMessage());
			return List.of();
		}
	}

	public List<Product> findByProductNameContainingIgnoreCase(String keyword) {
		try {
			String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
			String sql = """
					    SELECT * FROM Products
					    WHERE LOWER(ProductName) LIKE LOWER(?)
					    ORDER BY ProductID DESC
					""";
			return jdbcTemplate.query(sql, rowMapperForProduct, like);
		} catch (Exception e) {
			System.err.println("Lỗi findByProductNameContainingIgnoreCase: " + e.getMessage());
			return List.of();
		}
	}

	public int countByShopId(Integer shopId) {
		if (shopId == null)
			return 0;
		try {
			String sql = "SELECT COUNT(*) FROM Products WHERE ShopID = ?";
			return jdbcTemplate.queryForObject(sql, Integer.class, shopId);
		} catch (Exception e) {
			System.err.println("Lỗi countByShopId shopId=" + shopId + ": " + e.getMessage());
			return 0;
		}
	}

}
