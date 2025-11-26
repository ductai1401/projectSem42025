package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.FlashSale;
import projectSem4.com.model.entities.FlashSaleItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FlashSaleRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// ---------- RowMappers ----------
	private final RowMapper<FlashSale> flashSaleRowMapper = (rs, rowNum) -> {
		FlashSale fs = new FlashSale();
		fs.setFlashSaleId(rs.getInt("FlashSaleID"));
		fs.setShopId(rs.getInt("ShopID"));
		fs.setName(rs.getString("Name"));
		Timestamp start = rs.getTimestamp("StartDate");
		Timestamp end = rs.getTimestamp("EndDate");
		fs.setStartDate(start != null ? start.toLocalDateTime() : null);
		fs.setEndDate(end != null ? end.toLocalDateTime() : null);
		try {
			fs.setStatus((Integer) rs.getObject("Status"));
		} catch (SQLException ignore) {
		}
		return fs;
	};

	private final RowMapper<FlashSaleItem> itemRowMapper = (rs, rowNum) -> {
		FlashSaleItem it = new FlashSaleItem();
		it.setFlashSaleItemId(rs.getInt("FlashSaleItemID"));
		it.setFlashSaleId(rs.getInt("FlashSaleID"));
		it.setProductId(rs.getInt("ProductID"));
		it.setQuantity(rs.getInt("Quantity"));
		it.setPercern(rs.getInt("Percent"));
		try {
			it.setStatus((Integer) rs.getObject("Status"));
		} catch (SQLException ignore) {
		}
		return it;
	};

	// =================== FLASH SALES ===================

	// CREATE – trả về ID
	public Integer createReturningId(FlashSale fs) {
		String sql = """
				INSERT INTO FlashSales (ShopID, Name, StartDate, EndDate)
				VALUES (?, ?, ?, ?)
				""";
		KeyHolder kh = new GeneratedKeyHolder();
		int rows = jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			ps.setInt(i++, fs.getShopId());
			ps.setString(i++, fs.getName());
			ps.setTimestamp(i++, Timestamp.valueOf(fs.getStartDate()));
			ps.setTimestamp(i, Timestamp.valueOf(fs.getEndDate()));
			return ps;
		}, kh);
		if (rows > 0 && kh.getKey() != null) {
			int id = kh.getKey().intValue();
			fs.setFlashSaleId(id);
			return id;
		}
		return null;
	}

	public String create(FlashSale fs) {
		try {
			Integer id = createReturningId(fs);
			return id != null ? "Tạo FlashSale thành công!" : "Tạo FlashSale thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi tạo FlashSale: " + e.getMessage());
			return "Lỗi hệ thống khi tạo FlashSale!";
		}
	}

	// UPDATE
	// UPDATE
	public String update(FlashSale fs) {
		String sql = """
				UPDATE FlashSales
				   SET ShopID = ?,
				       Name = ?,
				       StartDate = ?,
				       EndDate = ?,
				       Status = 1          -- ✅ Đánh dấu đã chỉnh sửa
				 WHERE FlashSaleID = ?
				""";
		try {
			int rows = jdbcTemplate.update(sql, fs.getShopId(), fs.getName(), Timestamp.valueOf(fs.getStartDate()),
					Timestamp.valueOf(fs.getEndDate()), fs.getFlashSaleId());
			return rows > 0 ? "Cập nhật FlashSale thành công!" : "Cập nhật FlashSale thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi cập nhật FlashSale: " + e.getMessage());
			return "Lỗi hệ thống khi cập nhật FlashSale!";
		}
	}

	// DELETE
	public String delete(int id) {
		String sql = "DELETE FROM FlashSales WHERE FlashSaleID = ?";
		try {
			int rows = jdbcTemplate.update(sql, id);
			return rows > 0 ? "Xóa FlashSale thành công!" : "Xóa FlashSale thất bại!";
		} catch (Exception e) {
			System.err.println("Lỗi khi xóa FlashSale: " + e.getMessage());
			return "Lỗi hệ thống khi xóa FlashSale!";
		}
	}

	// FIND BY ID
	public FlashSale findById(int id) {
		try {
			String sql = "SELECT * FROM FlashSales WHERE FlashSaleID = ?";
			List<FlashSale> list = jdbcTemplate.query(sql, flashSaleRowMapper, id);
			return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			System.err.println("Lỗi khi tìm FlashSale: " + e.getMessage());
			return null;
		}
	}

	// FIND ALL
	public List<FlashSale> findAll() {
		try {
			String sql = "SELECT * FROM FlashSales WHERE ISNULL(Status,1) <> 2 ORDER BY FlashSaleID DESC";
			return jdbcTemplate.query(sql, flashSaleRowMapper);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy tất cả FlashSale: " + e.getMessage());
			return List.of();
		}
	}

	// 1) Bao gồm cả status=2 (dùng cho trang quản trị cập nhật)
	public List<FlashSale> findAllIncludingExpired() {
		try {
			String sql = "SELECT * FROM FlashSales ORDER BY FlashSaleID DESC";
			return jdbcTemplate.query(sql, flashSaleRowMapper);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy tất cả FlashSale: " + e.getMessage());
			return List.of();
		}
	}

	// 2) Chỉ hiển thị (ẩn status=2) – dùng cho list/search UI nếu cần
	public List<FlashSale> findAllVisible() {
		try {
			String sql = "SELECT * FROM FlashSales WHERE ISNULL(Status,1) <> 2 ORDER BY FlashSaleID DESC";
			return jdbcTemplate.query(sql, flashSaleRowMapper);
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy tất cả FlashSale (visible): " + e.getMessage());
			return List.of();
		}
	}

	// Pagination
	public List<FlashSale> findAllPaged(int page, int size) {
		int offset = Math.max(0, (page - 1) * size);
		try {
			String sql = """
					    SELECT * FROM FlashSales
					     WHERE ISNULL(Status,1) <> 2
					     ORDER BY FlashSaleID DESC
					     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
					""";

			return jdbcTemplate.query(sql, flashSaleRowMapper, offset, size);
		} catch (Exception e) {
			System.err.println("Lỗi phân trang FlashSale: " + e.getMessage());
			return List.of();
		}
	}

	// Search (by name + shop)
	public List<FlashSale> search(String keyword, Integer shopId, int page, int size) {
		int offset = Math.max(0, (page - 1) * size);
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		try {
			StringBuilder sql = new StringBuilder("""
					    SELECT * FROM FlashSales
					     WHERE Name LIKE ?
					       AND ISNULL(Status,1) <> 2
					""");
			List<Object> args = new ArrayList<>();
			args.add(like);

			if (shopId != null) {
				sql.append(" AND ShopID = ?");
				args.add(shopId);
			}

			sql.append(" ORDER BY FlashSaleID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
			args.add(offset);
			args.add(size);

			return jdbcTemplate.query(sql.toString(), flashSaleRowMapper, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi tìm kiếm FlashSale: " + e.getMessage());
			return List.of();
		}
	}

	public int countAll() {
		try {
			return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FlashSales WHERE ISNULL(Status,1) <> 2",
					Integer.class);

		} catch (Exception e) {
			System.err.println("Lỗi countAll FlashSale: " + e.getMessage());
			return 0;
		}
	}

	public int countSearch(String keyword, Integer shopId) {
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		try {
			StringBuilder sql = new StringBuilder("""
					    SELECT COUNT(*) FROM FlashSales
					     WHERE Name LIKE ?
					       AND ISNULL(Status,1) <> 2
					""");
			List<Object> args = new ArrayList<>();
			args.add(like);

			if (shopId != null) {
				sql.append(" AND ShopID = ?");
				args.add(shopId);
			}

			return jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi countSearch FlashSale: " + e.getMessage());
			return 0;
		}
	}

	// =================== FLASH SALE ITEMS ===================

	/** Tìm 1 item id theo flashSaleId + productId (đã tồn tại hay chưa) */
	public Integer findFlashSaleItemId(Integer flashSaleId, Integer productId) {
		try {
			String sql = """
					    SELECT FlashSaleItemID
					      FROM FlashSaleItems
					     WHERE FlashSaleID = ? AND ProductID = ?
					""";
			List<Integer> ids = jdbcTemplate.query(sql, (rs, rn) -> rs.getInt(1), flashSaleId, productId);
			return ids.isEmpty() ? null : ids.get(0);
		} catch (Exception e) {
			System.err.println("Lỗi findFlashSaleItemId: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Upsert FlashSaleItem: nếu đã có (FlashSaleID + ProductID) thì UPDATE; nếu
	 * chưa có thì INSERT và trả về ID mới.
	 */
	public Integer upsertFlashSaleItem(Integer flashSaleId, Integer productId, Integer quantity, Integer percent) {
		try {
			Integer existId = findFlashSaleItemId(flashSaleId, productId);
			if (existId != null) {
				String up = """
						    UPDATE FlashSaleItems
						       SET Quantity = ?, DiscountPercent = ?
						     WHERE FlashSaleItemID = ?
						""";
				int rows = jdbcTemplate.update(up, quantity, percent, existId);
				return rows > 0 ? existId : null;
			} else {
				String ins = """
						    INSERT INTO FlashSaleItems (FlashSaleID, ProductID, Quantity, DiscountPercent)
						    VALUES (?, ?, ?, ?)
						""";
				GeneratedKeyHolder kh = new GeneratedKeyHolder();
				int rows = jdbcTemplate.update(con -> {
					PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS);
					ps.setInt(1, flashSaleId);
					ps.setInt(2, productId);
					ps.setInt(3, quantity);
					ps.setInt(4, percent);
					return ps;
				}, kh);
				if (rows > 0 && kh.getKey() != null) {
					return kh.getKey().intValue();
				}
				return null;
			}
		} catch (Exception e) {
			System.err.println("Lỗi upsertFlashSaleItem: " + e.getMessage());
			return null;
		}
	}

	/** Xóa FlashSaleItem theo id (dùng cho detach) */
	public boolean deleteFlashSaleItemById(Integer flashSaleItemId) {
		try {
			String sql = "DELETE FROM FlashSaleItems WHERE FlashSaleItemID = ?";
			int rows = jdbcTemplate.update(sql, flashSaleItemId);
			return rows > 0;
		} catch (Exception e) {
			System.err.println("Lỗi deleteFlashSaleItemById: " + e.getMessage());
			return false;
		}
	}

	// =================== QUERY THEO PRODUCT ===================

	/**
	 * Lấy FlashSale đang hoạt động cho productId (now BETWEEN StartDate AND
	 * EndDate)
	 */
	public FlashSale findCurrentByProductId(Integer productId) {
		try {
			String sql = """
					    SELECT fs.*
					      FROM FlashSaleItems fsi
					      JOIN FlashSales fs ON fs.FlashSaleID = fsi.FlashSaleID
					     WHERE fsi.ProductID = ?
					       AND ? BETWEEN fs.StartDate AND fs.EndDate
					       AND ISNULL(fs.Status,1) <> 2
					     ORDER BY fs.StartDate DESC
					""";
			LocalDateTime now = LocalDateTime.now();
			List<FlashSale> list = jdbcTemplate.query(sql, flashSaleRowMapper, productId, Timestamp.valueOf(now));
			return list.isEmpty() ? null : list.get(0);
		} catch (Exception e) {
			System.err.println("Lỗi findCurrentByProductId: " + e.getMessage());
			return null;
		}
	}

	/** Lấy danh sách FlashSaleItem của product */
	// Ví dụ findItemsByProductId:
	public List<FlashSaleItem> findItemsByProductId(Integer productId) {
		try {
			String sql = """
					    SELECT FlashSaleItemID, FlashSaleID, ProductID, Quantity, Percern, Status
					      FROM FlashSaleItems
					     WHERE ProductID = ?
					     ORDER BY FlashSaleItemID DESC
					""";
			return jdbcTemplate.query(sql, (rs, rn) -> {
				FlashSaleItem i = new FlashSaleItem();
				i.setFlashSaleItemId(rs.getInt("FlashSaleItemID"));
				i.setFlashSaleId(rs.getInt("FlashSaleID"));
				i.setProductId(rs.getInt("ProductID"));
				i.setQuantity(rs.getInt("Quantity"));
				i.setPercern(rs.getInt("Percern"));
				try {
					i.setStatus((Integer) rs.getObject("Status"));
				} catch (SQLException ignore) {
				}
				return i;
			}, productId);
		} catch (Exception e) {
			System.err.println("Lỗi findItemsByProductId: " + e.getMessage());
			return List.of();
		}
	}

	/** Đặt Status=2 cho FlashSales đã quá hạn EndDate */
	public int markExpiredFlashSales() {
		String sql = """
				    UPDATE fs
				       SET fs.Status = 2
				      FROM FlashSales fs
				     WHERE fs.EndDate < GETDATE()
				       AND ISNULL(fs.Status, 1) <> 2
				""";
		return jdbcTemplate.update(sql);
	}

	/** Đặt Status=2 cho FlashSaleItems thuộc FlashSales đã hết hạn */
	public int markExpiredItemsByParent() {
		String sql = """
				    UPDATE i
				       SET i.Status = 2
				      FROM FlashSaleItems i
				      JOIN FlashSales f ON f.FlashSaleID = i.FlashSaleID
				     WHERE f.EndDate < GETDATE()
				       AND ISNULL(i.Status, 1) <> 2
				""";
		return jdbcTemplate.update(sql);
	}

	// Search: bao gồm expired
	public List<FlashSale> searchIncludingExpired(String keyword, Integer shopId, int page, int size) {
		int offset = Math.max(0, (page - 1) * size);
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		StringBuilder sql = new StringBuilder("""
				    SELECT * FROM FlashSales
				     WHERE Name LIKE ?
				""");
		List<Object> args = new ArrayList<>();
		args.add(like);
		if (shopId != null) {
			sql.append(" AND ShopID = ?");
			args.add(shopId);
		}
		sql.append(" ORDER BY FlashSaleID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
		args.add(offset);
		args.add(size);
		return jdbcTemplate.query(sql.toString(), flashSaleRowMapper, args.toArray());
	}

	// Count: bao gồm expired
	public int countSearchIncludingExpired(String keyword, Integer shopId) {
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		StringBuilder sql = new StringBuilder("""
				    SELECT COUNT(*) FROM FlashSales
				     WHERE Name LIKE ?
				""");
		List<Object> args = new ArrayList<>();
		args.add(like);
		if (shopId != null) {
			sql.append(" AND ShopID = ?");
			args.add(shopId);
		}
		return jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
	}

	// ============ VISIBLE (ẩn status=2) ============
	public List<FlashSale> searchVisible(String keyword, Integer shopId, int page, int size) {
		int offset = Math.max(0, (page - 1) * size);
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		try {
			StringBuilder sql = new StringBuilder("""
					    SELECT * FROM FlashSales
					     WHERE Name LIKE ?
					       AND ISNULL(Status,1) <> 2
					""");
			List<Object> args = new ArrayList<>();
			args.add(like);

			if (shopId != null) {
				sql.append(" AND ShopID = ?");
				args.add(shopId);
			}

			sql.append(" ORDER BY FlashSaleID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
			args.add(offset);
			args.add(size);

			return jdbcTemplate.query(sql.toString(), flashSaleRowMapper, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi searchVisible FlashSale: " + e.getMessage());
			return List.of();
		}
	}

	public int countSearchVisible(String keyword, Integer shopId) {
		String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
		try {
			StringBuilder sql = new StringBuilder("""
					    SELECT COUNT(*) FROM FlashSales
					     WHERE Name LIKE ?
					       AND ISNULL(Status,1) <> 2
					""");
			List<Object> args = new ArrayList<>();
			args.add(like);

			if (shopId != null) {
				sql.append(" AND ShopID = ?");
				args.add(shopId);
			}

			return jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
		} catch (Exception e) {
			System.err.println("Lỗi countSearchVisible FlashSale: " + e.getMessage());
			return 0;
		}
	}

}
