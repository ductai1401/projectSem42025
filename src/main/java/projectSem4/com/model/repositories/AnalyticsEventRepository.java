package projectSem4.com.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import projectSem4.com.model.entities.AnalyticsEvent;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AnalyticsEventRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ========= RowMapper =========
    private final RowMapper<AnalyticsEvent> rowMapper = (rs, rowNum) -> {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setEventId(rs.getLong("EventID"));
        e.setUserId(rs.getObject("UserID") != null ? rs.getInt("UserID") : null);
        e.setEventType(rs.getString("EventType"));
        e.setProductId(rs.getObject("ProductID") != null ? rs.getInt("ProductID") : null);
        e.setSearchKeyword(rs.getString("SearchKeyword"));
        e.setSessionId(rs.getString("SessionID"));
        e.setDeviceInfo(rs.getString("DeviceInfo"));

        Timestamp ts = rs.getTimestamp("Timestamp");
        if (ts != null) e.setTimestamp(ts.toLocalDateTime());

        // New columns
        e.setEventCount(rs.getObject("EventCount") != null ? rs.getInt("EventCount") : null);
        Timestamp up = rs.getTimestamp("UpdatedAt");
        if (up != null) e.setUpdatedAt(up.toLocalDateTime());

        return e;
    };

    // ========= INSERT (trả về id) =========
    public Long insert(AnalyticsEvent e) {
        String sql = """
            INSERT INTO AnalyticsEvents
              (UserID, EventType, ProductID, SearchKeyword, SessionID, DeviceInfo, Timestamp, EventCount, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            if (e.getUserId() != null) ps.setInt(i++, e.getUserId()); else ps.setNull(i++, Types.INTEGER);
            ps.setString(i++, e.getEventType());
            if (e.getProductId() != null) ps.setInt(i++, e.getProductId()); else ps.setNull(i++, Types.INTEGER);
            ps.setString(i++, trimOrNull(e.getSearchKeyword()));
            ps.setString(i++, trimOrNull(e.getSessionId()));
            ps.setString(i++, trimOrNull(e.getDeviceInfo()));

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(i++, Timestamp.valueOf(e.getTimestamp() != null ? e.getTimestamp() : now));
            ps.setInt(i++, e.getEventCount() != null ? e.getEventCount() : 1);
            ps.setTimestamp(i, Timestamp.valueOf(now));
            return ps;
        }, kh);

        if (kh.getKey() != null) {
            Long id = kh.getKey().longValue();
            e.setEventId(id);
            return id;
        }
        return null;
    }

    // ========= UPSERT MẪU (gộp EventCount nếu đã có cùng "khóa logic") =========
    // Khóa logic: UserID, EventType, ProductID (NULL-coalesce 0), SearchKeyword (''), SessionID
    public int increaseEventCount(Integer userId, String eventType, Integer productId, String searchKeyword, String sessionId) {
        String sql = """
            UPDATE AnalyticsEvents
               SET EventCount = EventCount + 1,
                   UpdatedAt  = SYSDATETIME()
             WHERE EventType = ?
               AND ISNULL(UserID, 0)    = ISNULL(?, 0)
               AND ISNULL(ProductID, 0) = ISNULL(?, 0)
               AND ISNULL(SearchKeyword, '') = ISNULL(?, '')
               AND ISNULL(SessionID, '')     = ISNULL(?, '')
        """;
        return jdbcTemplate.update(sql,
                eventType,
                userId,
                productId,
                trimOrNull(searchKeyword),
                trimOrNull(sessionId));
    }

    /** Upsert tổng quát: nếu tăng không thành công (0 rows) thì insert mới */
    public Long upsert(AnalyticsEvent e) {
        int rows = increaseEventCount(e.getUserId(), e.getEventType(), e.getProductId(), e.getSearchKeyword(), e.getSessionId());
        if (rows > 0) return null; // đã gộp vào bản ghi cũ
        return insert(e);          // chưa có -> tạo mới
    }

    // ========= Một số helper upsert theo use case =========
    public void logSearch(Integer userId, String keyword, String sessionId, String deviceInfo) {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setUserId(userId);
        e.setEventType("SEARCH");
        e.setProductId(null);
        e.setSearchKeyword(keyword);
        e.setSessionId(sessionId);
        e.setDeviceInfo(deviceInfo);
        e.setTimestamp(LocalDateTime.now());
        upsert(e);
    }

    public void logViewProduct(Integer userId, Integer productId, String sessionId, String deviceInfo) {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setUserId(userId);
        e.setEventType("VIEW_PRODUCT");
        e.setProductId(productId);
        e.setSearchKeyword(null);
        e.setSessionId(sessionId);
        e.setDeviceInfo(deviceInfo);
        e.setTimestamp(LocalDateTime.now());
        upsert(e);
    }

    public void logAddToCart(Integer userId, Integer productId, String sessionId, String deviceInfo) {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setUserId(userId);
        e.setEventType("ADD_TO_CART");
        e.setProductId(productId);
        e.setSessionId(sessionId);
        e.setDeviceInfo(deviceInfo);
        e.setTimestamp(LocalDateTime.now());
        upsert(e);
    }

    public void logCheckoutStart(Integer userId, String sessionId, String deviceInfo) {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setUserId(userId);
        e.setEventType("CHECKOUT_START");
        e.setSessionId(sessionId);
        e.setDeviceInfo(deviceInfo);
        e.setTimestamp(LocalDateTime.now());
        upsert(e);
    }

    public void logCheckoutComplete(Integer userId, String sessionId, String deviceInfo) {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setUserId(userId);
        e.setEventType("CHECKOUT_COMPLETE");
        e.setSessionId(sessionId);
        e.setDeviceInfo(deviceInfo);
        e.setTimestamp(LocalDateTime.now());
        upsert(e);
    }

    // ========= READ =========
    public AnalyticsEvent findById(Long id) {
        try {
            String sql = "SELECT * FROM AnalyticsEvents WHERE EventID = ?";
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<AnalyticsEvent> findAll() {
        String sql = "SELECT * FROM AnalyticsEvents ORDER BY EventID DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<AnalyticsEvent> findByUserId(Integer userId) {
        String sql = """
            SELECT * FROM AnalyticsEvents
             WHERE UserID = ?
             ORDER BY EventID DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public List<AnalyticsEvent> findSearchByKeyword(String keyword) {
        String sql = """
            SELECT * FROM AnalyticsEvents
             WHERE EventType = 'SEARCH'
               AND SearchKeyword LIKE ?
             ORDER BY EventID DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, "%" + (keyword == null ? "" : keyword.trim()) + "%");
    }

    // ========= DELETE =========
    public int deleteById(Long id) {
        String sql = "DELETE FROM AnalyticsEvents WHERE EventID = ?";
        return jdbcTemplate.update(sql, id);
    }

    // ========= STATS / AGGREGATIONS =========
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM AnalyticsEvents";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int countByEventType(String type) {
        String sql = "SELECT SUM(EventCount) FROM AnalyticsEvents WHERE EventType = ?";
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class, type);
        return v == null ? 0 : v;
    }

    public int countByProductId(Integer productId, String type) {
        String sql = """
            SELECT SUM(EventCount)
              FROM AnalyticsEvents
             WHERE ProductID = ?
               AND EventType = ?
        """;
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class, productId, type);
        return v == null ? 0 : v;
    }

    /** Top sản phẩm được VIEW nhiều nhất (tính theo tổng EventCount) */
    public List<Integer> topViewedProducts(int limit) {
        String sql = """
            SELECT TOP (?) ProductID
              FROM AnalyticsEvents
             WHERE EventType = 'VIEW_PRODUCT' AND ProductID IS NOT NULL
             GROUP BY ProductID
             ORDER BY SUM(EventCount) DESC
        """;
        return jdbcTemplate.query(sql, (rs, i) -> rs.getInt("ProductID"), limit);
    }

    /** Top từ khóa search nhiều nhất (theo tổng EventCount) */
    public List<String> topSearchKeywords(int limit) {
        String sql = """
            SELECT TOP (?) SearchKeyword
              FROM AnalyticsEvents
             WHERE EventType = 'SEARCH' AND SearchKeyword IS NOT NULL AND LTRIM(RTRIM(SearchKeyword)) <> ''
             GROUP BY SearchKeyword
             ORDER BY SUM(EventCount) DESC
        """;
        return jdbcTemplate.query(sql, (rs, i) -> rs.getString("SearchKeyword"), limit);
    }

    /** Đếm theo window thời gian */
    public int countByTypeInWindow(String type, LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT SUM(EventCount)
              FROM AnalyticsEvents
             WHERE EventType = ?
               AND Timestamp >= ?
               AND Timestamp <  ?
        """;
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class,
                type, Timestamp.valueOf(from), Timestamp.valueOf(to));
        return v == null ? 0 : v;
    }

    /** Xóa các event cũ hơn X ngày (nếu cần dọn rác) */
    public int deleteOlderThan(LocalDateTime before) {
        String sql = "DELETE FROM AnalyticsEvents WHERE Timestamp < ?";
        return jdbcTemplate.update(sql, Timestamp.valueOf(before));
    }

    // ========= Utils =========
    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
