package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.SearchHistory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SearchHistoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<SearchHistory> rowMapper = (ResultSet rs, int rowNum) -> {
        SearchHistory sh = new SearchHistory();
        sh.setSearchId(rs.getInt("SearchID"));
        sh.setKeyWord(rs.getString("Keyword"));
        sh.setFilters(rs.getString("Filters"));
        sh.setResultSnapshot(rs.getString("ResultSnapshot"));
        sh.setSearchCount(rs.getInt("SearchCount"));
        sh.setTotalRevenue(rs.getBigDecimal("TotalRevenue"));
        sh.setReason(rs.getString("Reason"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        sh.setCreateAt(ts != null ? ts.toLocalDateTime() : null);
        return sh;
    };

    /* ========================== CREATE ========================== */

    public Integer create(SearchHistory sh) {
        try {
            final String sql = """
                INSERT INTO SearchHistory (Keyword, Filters, ResultSnapshot, SearchCount, TotalRevenue, Reason, CreatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            KeyHolder kh = new GeneratedKeyHolder();
            int affected = jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                ps.setString(i++, sh.getKeyWord());
                ps.setString(i++, sh.getFilters());
                ps.setString(i++, sh.getResultSnapshot());
                ps.setInt(i++, sh.getSearchCount() == null ? 0 : sh.getSearchCount());
                ps.setBigDecimal(i++, sh.getTotalRevenue());
                ps.setString(i++, sh.getReason());
                ps.setTimestamp(i, sh.getCreateAt() == null ? Timestamp.valueOf(LocalDateTime.now()) : Timestamp.valueOf(sh.getCreateAt()));
                return ps;
            }, kh);

            if (affected > 0 && kh.getKey() != null) {
                Integer id = kh.getKey().intValue();
                sh.setSearchId(id);
                return id;
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Lỗi create SearchHistory: " + e.getMessage());
            return null;
        }
    }

    /* =========================== READ =========================== */

    public SearchHistory findById(long searchId) {
        try {
            String sql = "SELECT * FROM SearchHistory WHERE SearchID = ?";
            List<SearchHistory> list = jdbcTemplate.query(sql, rowMapper, searchId);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("❌ Lỗi findById SearchHistory: " + e.getMessage());
            return null;
        }
    }

    public List<SearchHistory> findAll() {
        try {
            String sql = "SELECT * FROM SearchHistory ORDER BY CreatedAt DESC";
            return jdbcTemplate.query(sql, rowMapper);
        } catch (Exception e) {
            System.err.println("❌ Lỗi findAll SearchHistory: " + e.getMessage());
            return List.of();
        }
    }

    public List<SearchHistory> findByKeyword(String keyword) {
        try {
            String sql = "SELECT * FROM SearchHistory WHERE Keyword LIKE ? ORDER BY CreatedAt DESC";
            return jdbcTemplate.query(sql, rowMapper, "%" + keyword + "%");
        } catch (Exception e) {
            System.err.println("❌ Lỗi findByKeyword SearchHistory: " + e.getMessage());
            return List.of();
        }
    }

    public int countAll() {
        try {
            String sql = "SELECT COUNT(*) FROM SearchHistory";
            Integer n = jdbcTemplate.queryForObject(sql, Integer.class);
            return n == null ? 0 : n;
        } catch (Exception e) {
            System.err.println("❌ Lỗi countAll SearchHistory: " + e.getMessage());
            return 0;
        }
    }

    /* ========================== UPDATE ========================== */

    public int update(SearchHistory sh) {
        try {
            String sql = """
                UPDATE SearchHistory
                   SET Keyword = ?,
                       Filters = ?,
                       ResultSnapshot = ?,
                       SearchCount = ?,
                       TotalRevenue = ?,
                       Reason = ?,
                       CreatedAt = ?
                 WHERE SearchID = ?
            """;
            return jdbcTemplate.update(sql,
                    sh.getKeyWord(),
                    sh.getFilters(),
                    sh.getResultSnapshot(),
                    sh.getSearchCount(),
                    sh.getTotalRevenue(),
                    sh.getReason(),
                    sh.getCreateAt() == null ? Timestamp.valueOf(LocalDateTime.now()) : Timestamp.valueOf(sh.getCreateAt()),
                    sh.getSearchId());
        } catch (Exception e) {
            System.err.println("❌ Lỗi update SearchHistory: " + e.getMessage());
            return 0;
        }
    }

    /* =========================== DELETE ========================== */

    public int deleteById(long searchId) {
        try {
            String sql = "DELETE FROM SearchHistory WHERE SearchID = ?";
            return jdbcTemplate.update(sql, searchId);
        } catch (Exception e) {
            System.err.println("❌ Lỗi deleteById SearchHistory: " + e.getMessage());
            return 0;
        }
    }

    public int deleteAll() {
        try {
            String sql = "DELETE FROM SearchHistory";
            return jdbcTemplate.update(sql);
        } catch (Exception e) {
            System.err.println("❌ Lỗi deleteAll SearchHistory: " + e.getMessage());
            return 0;
        }
    }

    /* ===================== CUSTOM QUERIES ======================= */

    // Lấy top keywords được tìm nhiều nhất
    public List<Object[]> findTopKeywords(int limit) {
        try {
            String sql = """
                SELECT TOP(?) Keyword, SUM(SearchCount) AS TotalSearch
                  FROM SearchHistory
                 GROUP BY Keyword
                 ORDER BY TotalSearch DESC
            """;
            return jdbcTemplate.query(sql, (rs, i) -> new Object[]{
                    rs.getString("Keyword"),
                    rs.getInt("TotalSearch")
            }, limit);
        } catch (Exception e) {
            System.err.println("❌ Lỗi findTopKeywords SearchHistory: " + e.getMessage());
            return List.of();
        }
    }
}
