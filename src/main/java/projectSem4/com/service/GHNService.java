package projectSem4.com.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectSem4.*;

import java.util.HashMap;
import java.util.Map;
@Service
public class GHNService {
	@Value("${ghn.token}")
    private String token;

    @Value("${ghn.shop-id}")
    private String shopId;

    @Value("${ghn.base-url}")
    private String baseUrl;
    
    @Value("${ghn.defaultFromDistrictId}")
    private int defaultFromDistrictId;

    private final RestTemplate restTemplate;

    public GHNService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.set("ShopId", shopId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public String getProvinces() {
        String url = baseUrl + "/master-data/province";
        HttpEntity<Void> entity = new HttpEntity<>(defaultHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
    }

    public String getDistricts(int provinceId) {
        String url = baseUrl + "/master-data/district";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("province_id", provinceId), defaultHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }

    public String getWards(int districtId) {
        String url = baseUrl + "/master-data/ward";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("district_id", districtId), defaultHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }

    public String calculateFee(int fromDistrict, int toDistrict, String toWardCode, int weight, int value) {
        String url = baseUrl + "/v2/shipping-order/fee";

        Map<String, Object> body = new HashMap<>();
        body.put("service_type_id", 2);
        body.put("from_district_id", fromDistrict);
        body.put("to_district_id", toDistrict);
        body.put("to_ward_code", toWardCode);
        body.put("weight", weight);
        body.put("cod_value", value);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, defaultHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }

//    public String createOrder(GHNOrderRequest request) {
//        try {
//            String url = baseUrl + "/v2/shipping-order/create";
//
//            // Chuẩn bị dữ liệu body cho GHN
//            Map<String, Object> body = new HashMap<>();
//            body.put("payment_type_id", request.getPaymentTypeId());
//            body.put("note", request.getRequiredNote());
//            body.put("required_note", request.getRequiredNote());
//            body.put("to_name", request.getToName());
//            body.put("to_phone", request.getToPhone());
//            body.put("to_address", request.getToAddress());
//            body.put("to_district_id", request.getToDistrictId());
//            body.put("to_ward_code", request.getToWardCode());
//            body.put("cod_amount", request.getCodAmount());
//            body.put("weight", request.getWeight());
//            body.put("service_type_id", 2); // GHN dịch vụ tiêu chuẩn
//            body.put("items", request.getItems());
//            body.put("from_district_id", defaultFromDistrictId);
//            body.put("shop_id", shopId);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Token", token);
//            headers.set("ShopId", String.valueOf(shopId));
//            
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
//
//            // 👉 Log body gửi đi
//            System.out.println("Body gửi GHN: " + new ObjectMapper().writeValueAsString(body));
//
//            // Gửi request tạo đơn
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//
//            // 👉 Log phản hồi
//            System.out.println("Phản hồi GHN: " + response.getBody());
//            return response.getBody();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Tạo đơn hàng thất bại: " + e.getMessage();
//        }
//    }

    public String getOrderDetail(String orderCode) {
        String url = baseUrl + "/v2/shipping-order/detail";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("order_code", orderCode), defaultHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }

    public String cancelOrder(String orderCode) {
        String url = baseUrl + "/v2/switch-status/cancel";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("order_codes", new String[]{orderCode}), defaultHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }
}
