package projectSem4.com.controller.apiController.admin;

import java.sql.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import projectSem4.com.service.admin.ShopService;


@RestController
@RequestMapping("/api/shop")
public class ShopApiController {

    @Autowired
    private ShopService shopService;

    @PostMapping("/loadData")
    public Map<String, Object> loadData(@RequestBody Map<String, Object> params) {
        String keyword = (String) params.getOrDefault("keyword", "");
        Object statusObj = params.get("status");
        Object dateObj = params.get("date");
        Object pageObj = params.get("page");

        Integer status = null;
        if (statusObj != null && !statusObj.toString().isBlank()) {
            try {
                status = ((Number) statusObj).intValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                status = null;
            }
        }

        Date date = null;
        if (dateObj instanceof String && !((String) dateObj).isEmpty()) {
            try {
                date = java.sql.Date.valueOf((String) dateObj); // yyyy-MM-dd
            } catch (IllegalArgumentException e) {
                // date sai định dạng
            }
        }

        int pageNumber = 1;
        if (pageObj != null) {
            pageNumber = Integer.parseInt(pageObj.toString());
        }

        int pageSize = 5;

        return shopService.searchAndPagi(pageNumber, pageSize, keyword, null, status, keyword);
    }
}
