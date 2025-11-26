package projectSem4.com.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSem4.com.model.entities.AnalyticsEvent;
import projectSem4.com.model.repositories.AnalyticsEventRepository;
import projectSem4.com.model.utils.MyValidate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsEventService {

    private final AnalyticsEventRepository repo;

    @Autowired
    public AnalyticsEventService(AnalyticsEventRepository repo) {
        this.repo = repo;
    }

    // =================== CREATE (RAW INSERT) ===================
    // Trường hợp bạn muốn luôn tạo bản ghi mới (không gộp)
    @Transactional
    public Map<String, Object> createEvent(AnalyticsEventCreateRequest req) {
        if (MyValidate.isBlank(req.eventType)) {
            return Map.of("ok", false, "message", "EventType không được để trống");
        }

        try {
            AnalyticsEvent e = new AnalyticsEvent();
            e.setUserId(req.userId);
            e.setEventType(req.eventType.toUpperCase(Locale.ROOT)); // normalize
            e.setProductId(req.productId);
            e.setSearchKeyword(req.searchKeyword);
            e.setSessionId(req.sessionId);
            e.setDeviceInfo(req.deviceInfo);
            e.setTimestamp(LocalDateTime.now());
            // EventCount & UpdatedAt do repo set mặc định (1, now) trong insert()

            Long id = repo.insert(e);
            if (id == null) {
                return Map.of("ok", false, "message", "Không lấy được EventID sau khi insert.");
            }
            return Map.of("ok", true, "eventId", id);

        } catch (Exception ex) {
            return Map.of("ok", false, "message", "Lỗi tạo sự kiện: " + ex.getMessage());
        }
    }

    // =================== CREATE OR INCREASE (UPSERT/GỘP) ===================
    // Dùng khi bạn muốn hạn chế phình dữ liệu: nếu đã có cùng khóa logic thì tăng EventCount
    @Transactional
    public Map<String, Object> createOrIncrease(AnalyticsEventCreateRequest req) {
        if (MyValidate.isBlank(req.eventType)) {
            return Map.of("ok", false, "message", "EventType không được để trống");
        }

        try {
            AnalyticsEvent e = new AnalyticsEvent();
            e.setUserId(req.userId);
            e.setEventType(req.eventType.toUpperCase(Locale.ROOT));
            e.setProductId(req.productId);
            e.setSearchKeyword(req.searchKeyword);
            e.setSessionId(req.sessionId);
            e.setDeviceInfo(req.deviceInfo);
            e.setTimestamp(LocalDateTime.now());

            // repo.upsert: nếu tồn tại -> tăng EventCount, nếu chưa -> insert dòng mới
            Long insertedId = repo.upsert(e);
            return (insertedId == null)
                    ? Map.of("ok", true, "merged", true)    // đã gộp vào bản ghi cũ
                    : Map.of("ok", true, "merged", false, "eventId", insertedId); // đã insert mới

        } catch (Exception ex) {
            return Map.of("ok", false, "message", "Lỗi upsert sự kiện: " + ex.getMessage());
        }
    }

    // =================== HELPER LOGGERS (UPSERT) ===================
    // Các hàm tiện ích để ghi sự kiện thường gặp theo cơ chế gộp:

    @Transactional
    public void logSearch(Integer userIdOrNull, String keyword, String sessionId, String deviceInfo) {
        repo.logSearch(userIdOrNull, safe(keyword), safe(sessionId), safe(deviceInfo));
    }

    @Transactional
    public void logViewProduct(Integer userIdOrNull, Integer productId, String sessionId, String deviceInfo) {
        repo.logViewProduct(userIdOrNull, productId, safe(sessionId), safe(deviceInfo));
    }

    @Transactional
    public void logAddToCart(Integer userIdOrNull, Integer productId, String sessionId, String deviceInfo) {
        repo.logAddToCart(userIdOrNull, productId, safe(sessionId), safe(deviceInfo));
    }

    @Transactional
    public void logCheckoutStart(Integer userIdOrNull, String sessionId, String deviceInfo) {
        repo.logCheckoutStart(userIdOrNull, safe(sessionId), safe(deviceInfo));
    }

    @Transactional
    public void logCheckoutComplete(Integer userIdOrNull, String sessionId, String deviceInfo) {
        repo.logCheckoutComplete(userIdOrNull, safe(sessionId), safe(deviceInfo));
    }

    // =================== READ ===================
    public AnalyticsEvent findById(Long id) {
        return repo.findById(id);
    }

    public List<AnalyticsEvent> findAll() {
        return repo.findAll();
    }

    public List<AnalyticsEvent> findByUserId(Integer userId) {
        return repo.findByUserId(userId);
    }

    public List<AnalyticsEvent> searchByKeyword(String keyword) {
        return repo.findSearchByKeyword(keyword);
    }

    // =================== DELETE ===================
    @Transactional
    public boolean deleteEvent(Long id) {
        return repo.deleteById(id) > 0;
    }

    // =================== STATS ===================
    public int countAllEvents() {
        return repo.countAll();
    }

    /** Đếm tổng theo loại (tính theo SUM(EventCount)) */
    public int countByEventType(String type) {
        if (type == null) return 0;
        return repo.countByEventType(type.toUpperCase(Locale.ROOT));
    }

    /** Đếm tổng lượt xem 1 sản phẩm (SUM(EventCount) của VIEW_PRODUCT) */
    public int countProductViews(Integer productId) {
        return repo.countByProductId(productId, "VIEW_PRODUCT");
    }

    public List<Integer> getTopViewedProducts(int limit) {
        return repo.topViewedProducts(limit);
    }

    public List<String> getTopSearchKeywords(int limit) {
        return repo.topSearchKeywords(limit);
    }

    /** Đếm theo khoảng thời gian (vd. hôm nay, 7 ngày gần nhất…) */
    public int countTypeInWindow(String type, LocalDateTime from, LocalDateTime to) {
        if (type == null || from == null || to == null) return 0;
        return repo.countByTypeInWindow(type.toUpperCase(Locale.ROOT), from, to);
    }

    // =================== AGGREGATION (DASHBOARD) ===================
    public AnalyticsStatsDTO buildStats() {
        try {
            int total     = repo.countAll();
            int views     = repo.countByEventType("VIEW_PRODUCT");
            int searches  = repo.countByEventType("SEARCH");
            int carts     = repo.countByEventType("ADD_TO_CART");
            int checkoutS = repo.countByEventType("CHECKOUT_START");
            int checkoutC = repo.countByEventType("CHECKOUT_COMPLETE");
            return new AnalyticsStatsDTO(total, views, searches, carts, checkoutS, checkoutC);
        } catch (Exception e) {
            return new AnalyticsStatsDTO();
        }
    }

    // =================== HOUSEKEEPING ===================
    /** Xoá event cũ hơn before (nếu cần dọn rác) */
    @Transactional
    public int deleteOlderThan(LocalDateTime before) {
        if (before == null) return 0;
        return repo.deleteOlderThan(before);
    }

    // =================== DTOs ===================
    public static class AnalyticsEventCreateRequest {
        public Integer userId;
        public String  eventType;     // VIEW_PRODUCT | SEARCH | ADD_TO_CART | CHECKOUT_START | CHECKOUT_COMPLETE ...
        public Integer productId;
        public String  searchKeyword;
        public String  sessionId;
        public String  deviceInfo;
    }

    public static class AnalyticsStatsDTO {
        public int totalEvents;
        public int totalViews;
        public int totalSearches;
        public int totalAddToCart;
        public int totalCheckoutStart;
        public int totalCheckoutComplete;

        public AnalyticsStatsDTO() {}

        public AnalyticsStatsDTO(int totalEvents,
                                 int totalViews,
                                 int totalSearches,
                                 int totalAddToCart,
                                 int totalCheckoutStart,
                                 int totalCheckoutComplete) {
            this.totalEvents = totalEvents;
            this.totalViews = totalViews;
            this.totalSearches = totalSearches;
            this.totalAddToCart = totalAddToCart;
            this.totalCheckoutStart = totalCheckoutStart;
            this.totalCheckoutComplete = totalCheckoutComplete;
        }
    }

    // =================== Utils ===================
    private String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
