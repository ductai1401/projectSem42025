package projectSem4.com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import projectSem4.com.service.GHNService;

@RestController
@RequestMapping("/api/ghn")
public class GHNController {
    private final GHNService ghnService;

    public GHNController(GHNService ghnService) {
        this.ghnService = ghnService;
    }

    @GetMapping("/provinces")
    public ResponseEntity<Map<String, Object>> provinces() throws JsonProcessingException {
        String json = ghnService.getProvinces();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(json, new TypeReference<>() {});
        return ResponseEntity.ok(map);
    }

    @PostMapping("/districts")
    public ResponseEntity<Map<String, Object>> districts(@RequestParam("provinceId") int provinceId) {
        Map<String, Object> map = new HashMap<>();
        try {
            String json = ghnService.getDistricts(provinceId);
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping("/wards")
    public ResponseEntity<Map<String, Object>> wards(@RequestParam("districtId") int districtId) {
        Map<String, Object> map = new HashMap<>();
        try {
            String json = ghnService.getWards(districtId);
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping("/fee")
    public ResponseEntity<String> fee(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ghnService.calculateFee(
                (int) body.get("fromDistrict"),
                (int) body.get("toDistrict"),
                body.get("toWardCode").toString(),
                (int) body.get("weight"),
                (int) body.get("value")
        ));
    }

    // Nếu muốn dùng create order, bạn có thể uncomment và chỉnh sửa như sau:
    /*
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody GHNOrderRequest request) {
        System.out.println("🟡 Dữ liệu nhận từ client:");
        try {
            System.out.println(new ObjectMapper().writeValueAsString(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            // Thêm item mặc định nếu rỗng
            if (request.getItems() == null || request.getItems().isEmpty()) {
                List<GHNOrderRequest.Item> defaultItems = new ArrayList<>();

                GHNOrderRequest.Item item1 = new GHNOrderRequest.Item();
                item1.setName("Áo sơ mi");
                item1.setQuantity(1);

                GHNOrderRequest.Item item2 = new GHNOrderRequest.Item();
                item2.setName("Quần tây");
                item2.setQuantity(2);

                defaultItems.add(item1);
                defaultItems.add(item2);

                request.setItems(defaultItems);
            }

            request.setRequiredNote(request.getRequiredNote().trim());
            var response = ghnService.createOrder(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Tạo đơn hàng thất bại: " + e.getMessage());
        }
    }
    */

    @GetMapping("/detail")
    public ResponseEntity<String> detail(@RequestParam("orderCode") String orderCode) {
        return ResponseEntity.ok(ghnService.getOrderDetail(orderCode));
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancel(@RequestParam("orderCode") String orderCode) {
        return ResponseEntity.ok(ghnService.cancelOrder(orderCode));
    }
}
