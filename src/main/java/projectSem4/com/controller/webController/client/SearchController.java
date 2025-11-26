package projectSem4.com.controller.webController.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import projectSem4.com.model.entities.Product;
import projectSem4.com.model.repositories.CategoryRepository;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.service.admin.ProductService;
import projectSem4.com.service.client.SearchKeyService;
import projectSem4.com.service.admin.AnalyticsEventService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Autowired private ProductService productService;
    @Autowired private SearchKeyService searchKeyService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ShopRepository shopRepository;
    @Autowired private AnalyticsEventService analyticsEventService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @GetMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "12") Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam Map<String, String> params,
            Model model,
            HttpServletRequest request
    ) {
        if (keyword == null || keyword.isBlank()) {
            return "redirect:/home";
        }

        final String kw = keyword.trim();

        // 1) tăng đếm keyword (cho gợi ý/xếp hạng tìm kiếm phổ biến) – cả khách cũng tính
        try { searchKeyService.increaseSearchCount(kw); } catch (Exception ignore) {}

        // 2) ghi analytics CHỈ khi là user đã đăng nhập
        try {
            HttpSession session = request.getSession(false); // ❗ không tạo session mới
            Integer userId = null;
            if (session != null) {
                Object uid = session.getAttribute("userId");
                if (uid instanceof Integer) userId = (Integer) uid;
            }
            if (userId != null) {
                AnalyticsEventService.AnalyticsEventCreateRequest evt = new AnalyticsEventService.AnalyticsEventCreateRequest();
                evt.userId = userId;
                evt.eventType = "SEARCH";
                evt.searchKeyword = kw;
                evt.sessionId = (session != null ? session.getId() : null);
                evt.deviceInfo = request.getHeader("User-Agent");
                analyticsEventService.createEvent(evt);
            }
        } catch (Exception e) {
            System.err.println("Analytics log skipped: " + e.getMessage());
        }

        // 3) lấy sản phẩm theo keyword (giới hạn cứng để tránh quá nặng)
        List<Product> allResults = productService.searchProducts(null, kw, 200);

        // 4) build filter map từ productOption
        Map<String, Set<String>> filterMap = new HashMap<>();
        for (Product p : allResults) {
            String opt = p.getProductOption();
            if (opt == null || opt.isBlank()) continue;
            try {
                Map<String, Object> opts = MAPPER.readValue(opt, new TypeReference<Map<String, Object>>(){});
                for (Map.Entry<String, Object> e : opts.entrySet()) {
                    filterMap.putIfAbsent(e.getKey(), new HashSet<>());
                    Object v = e.getValue();
                    if (v instanceof List<?>) {
                        for (Object it : (List<?>) v) filterMap.get(e.getKey()).add(String.valueOf(it));
                    } else if (v != null) {
                        filterMap.get(e.getKey()).add(String.valueOf(v));
                    }
                }
            } catch (Exception ex) {
                System.err.println("Parse option fail productId=" + p.getProductId() + ": " + ex.getMessage());
            }
        }

        // 5) lọc theo khoảng giá và option
        List<Product> filtered = allResults.stream().filter(p -> {
            try {
                // khoảng giá
                Double minPrice = params.containsKey("minPrice") && !params.get("minPrice").isBlank()
                        ? Double.valueOf(params.get("minPrice")) : null;
                Double maxPrice = params.containsKey("maxPrice") && !params.get("maxPrice").isBlank()
                        ? Double.valueOf(params.get("maxPrice")) : null;
                Double price = productService.buildMinPriceMap(List.of(p)).get(p.getProductId());
                if (minPrice != null && (price == null || price < minPrice)) return false;
                if (maxPrice != null && (price == null || price > maxPrice)) return false;

                // options động (opt_color, opt_size, ...)
                if (p.getProductOption() != null && !p.getProductOption().isBlank()) {
                    Map<String, Object> opts = MAPPER.readValue(p.getProductOption(), new TypeReference<Map<String, Object>>(){});
                    for (String key : filterMap.keySet()) {
                        String paramKey = "opt_" + key;
                        if (params.containsKey(paramKey)) {
                            String expected = params.get(paramKey);
                            Object v = opts.get(key);
                            if (v instanceof List<?>) {
                                if (!((List<?>) v).contains(expected)) return false;
                            } else if (v != null) {
                                if (!expected.equals(String.valueOf(v))) return false;
                            }
                        }
                    }
                }
            } catch (Exception ignore) {}
            return true;
        }).collect(Collectors.toList());

        // 6) sort
        if ("newest".equalsIgnoreCase(sortBy)) {
            filtered.sort(Comparator.comparing(Product::getCreatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("priceAsc".equalsIgnoreCase(sortBy)) {
            filtered.sort(Comparator.comparing(
                    p -> productService.buildMinPriceMap(List.of(p)).get(p.getProductId()),
                    Comparator.nullsLast(Double::compareTo)
            ));
        } else if ("priceDesc".equalsIgnoreCase(sortBy)) {
            filtered.sort(Comparator.comparing(
                    (Product p) -> productService.buildMinPriceMap(List.of(p)).get(p.getProductId()),
                    Comparator.nullsLast(Double::compareTo)
            ).reversed());
        }

        // 7) phân trang
        int total = filtered.size();
        size = Math.max(1, Math.min(size, 60)); // chặn size quá lớn
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) size));
        page = Math.max(1, Math.min(page, totalPages));

        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<Product> paged = (from < to) ? filtered.subList(from, to) : List.of();

        // 8) map giá min cho trang hiển thị
        Map<Integer, Double> priceMap = productService.buildMinPriceMap(paged);

        // 9) model
        model.addAttribute("keyword", kw);
        model.addAttribute("results", paged);
        model.addAttribute("priceMap", priceMap);
        model.addAttribute("filterMap", filterMap);
        model.addAttribute("shops", shopRepository.findAllShops());
        model.addAttribute("categories", categoryRepository.findAll());

        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sortBy", sortBy);

        return "client/search";
    }
}
