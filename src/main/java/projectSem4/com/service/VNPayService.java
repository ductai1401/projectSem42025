package projectSem4.com.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

	@Autowired
	private HttpServletRequest request; // inject request hiện tại

	// Config từ VNPAY portal
	private final String vnp_TmnCode = "EJQRVJO4"; // Merchant code
	private final String vnp_HashSecret = "FM9N3FSI0QUUF6MXJV6TVAOT7VI1Q8R2"; // Secret key
	private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
	private final String vnp_ReturnUrl = "https://74ac00262c58.ngrok-free.app/vnpay-return";
	private final String vnp_IpnUrl = "https://74ac00262c58.ngrok-free.app/vnpay-ipn";
	private final String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction"; // API Refund/Search


	public boolean testVnpayServer() {
        try {

        	URI uri = URI.create(vnp_Url);
        	HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();

            con.setRequestMethod("GET");
            con.setConnectTimeout(2000);
            con.connect();

            return con.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------------------
    // 2. Gọi API querydr để kiểm tra Terminal có được approve chưa
    // Nếu merchant lỗi → nhận các mã như 09, 91, 71, 79...
    // ------------------------------------------------------------------------------------
    public boolean checkMerchantStatus() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "querydr");
            params.put("vnp_TmnCode", vnp_TmnCode);

            // Mã test — không cần tồn tại thật
            params.put("vnp_TxnRef", "TEST" + System.currentTimeMillis());
            params.put("vnp_IpAddr", "127.0.0.1");
            params.put("vnp_OrderInfo", "CHECK_CONNECTION");
            params.put("vnp_TransactionNo", "0");

            String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            params.put("vnp_CreateDate", createDate);

            // Build hash
            String hashData = buildHashData(params);
            String secureHash = hmacSHA512(vnp_HashSecret, hashData);
            params.put("vnp_SecureHash", secureHash);

            // Call API
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> req = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = rest.postForEntity(vnp_ApiUrl, req, Map.class);

            Map body = response.getBody();
            if (body == null) return false;

            String code = (String) body.get("vnp_ResponseCode");

            // 00 = OK
            return "00".equals(code);

        } catch (Exception e) {
            return false;
        }
    }

	// Tạo link thanh toán
	public String createPaymentUrl(Double usdAmount, String orderInfo, String orderId) throws Exception {
		
		// B1: Kiểm tra server
        if (!testVnpayServer()) {
            throw new RuntimeException("Không kết nối được đến máy chủ VNPAY.");
        }

        // B2: Kiểm tra merchant (tránh lỗi 71)
        if (!checkMerchantStatus()) {
            throw new RuntimeException("Merchant/Terminal chưa được kích hoạt. Không thể thanh toán.");
        }
		Map<String, String> vnp_Params = new HashMap<>();
		vnp_Params.put("vnp_Version", "2.1.0");
		vnp_Params.put("vnp_Command", "pay");
		vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
		long amount = Math.round(usdAmount * 100); // nhân 100 theo spec

		if (amount < 5000 || amount > 1_000_000_000L) {
			throw new IllegalArgumentException("Số tiền giao dịch phải trong khoảng 5.000 – 1.000.000.000 VND");
		}
		vnp_Params.put("vnp_Amount", String.valueOf(amount));

		vnp_Params.put("vnp_CurrCode", "VND");
		vnp_Params.put("vnp_TxnRef", orderId); // UNIQUE cho mỗi giao dịch
		vnp_Params.put("vnp_OrderInfo", orderInfo);
		vnp_Params.put("vnp_OrderType", "other");
		vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);

		// Dùng base URL động
		vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
		vnp_Params.put("vnp_IpAddr", "127.0.0.1");

		// Thêm thời gian
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String createDate = formatter.format(new Date());
		vnp_Params.put("vnp_CreateDate", createDate);

		// Sắp xếp và ký SHA256
		List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
		Collections.sort(fieldNames);
		StringBuilder hashData = new StringBuilder();
		StringBuilder query = new StringBuilder();

		for (String name : fieldNames) {
			String value = vnp_Params.get(name);
			if (value != null && !value.isEmpty()) {
				hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
						.append('&');
				query.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
			}
		}

		// Bỏ dấu & cuối
		hashData.setLength(hashData.length() - 1);
		query.setLength(query.length() - 1);

		String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
		query.append("&vnp_SecureHash=").append(secureHash);

		return vnp_Url + "?" + query;

	}

	public String refund(String txnRef, String transDate, String transNo, Double usdAmount, String reason, String createdBy, String transactionType)
	        throws Exception {
		Map<String, String> vnp_Params = new LinkedHashMap<>();
	    vnp_Params.put("vnp_RequestId", String.valueOf(System.currentTimeMillis()));
	    vnp_Params.put("vnp_Version", "2.1.0");
	    vnp_Params.put("vnp_Command", "refund");
	    vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
	    vnp_Params.put("vnp_TransactionType", transactionType); // full refund
	    vnp_Params.put("vnp_TxnRef", txnRef);
	    long amount = Math.round(usdAmount * 100);
	    vnp_Params.put("vnp_Amount", String.valueOf(amount));
	    vnp_Params.put("vnp_OrderInfo", reason);
	    vnp_Params.put("vnp_TransactionNo", transNo);
	    vnp_Params.put("vnp_CreateBy", createdBy);
	    vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
	    vnp_Params.put("vnp_TransDate", transDate);
	    vnp_Params.put("vnp_IpAddr", "127.0.0.1");
	    

	    // 🔐 Sinh chữ ký SHA512
	    String hashData = buildQueryUrl(vnp_Params);
	    String secureHash = hmacSHA512(vnp_HashSecret, hashData);
	    vnp_Params.put("vnp_SecureHash", secureHash);

	    // ⚙️ Gửi POST request
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
	    body.setAll(vnp_Params);

	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

	    RestTemplate restTemplate = new RestTemplate();
	    ResponseEntity<String> response = restTemplate.postForEntity(vnp_ApiUrl, request, String.class);

	    return response.getBody();
	}

// ========== Search Order ==========
	public String queryTransaction(String txnRef, String transDate) throws Exception {
		Map<String, String> vnp_Params = new HashMap<>();
		vnp_Params.put("vnp_Version", "2.1.0");
		vnp_Params.put("vnp_Command", "querydr");
		vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
		vnp_Params.put("vnp_TxnRef", txnRef);
		vnp_Params.put("vnp_TransDate", transDate);
		vnp_Params.put("vnp_IpAddr", "127.0.0.1");

		String queryUrl = buildQueryUrl(vnp_Params);
		String requestUrl = vnp_ApiUrl + "?" + queryUrl;

		RestTemplate rest = new RestTemplate();
		return rest.getForObject(requestUrl, String.class);
	}

	// Hàm tạo secure hash HMAC SHA512

	public static String hmacSHA512(String key, String data) throws Exception {
		Mac hmac512 = Mac.getInstance("HmacSHA512");
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
		hmac512.init(secretKey);
		byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
		StringBuilder hash = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hash.append('0');
			hash.append(hex);
		}
		return hash.toString();
	}
	
	 private String buildQueryUrl(Map<String, String> params) throws Exception {
	        List<String> fieldNames = new ArrayList<>(params.keySet());
	        Collections.sort(fieldNames);

	        StringBuilder hashData = new StringBuilder();
	        StringBuilder query = new StringBuilder();

	        for (String name : fieldNames) {
	            String value = params.get(name);
	            if (value != null && !value.isEmpty()) {
	                hashData.append(name).append('=').append(value).append('&');
	                query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII))
	                     .append('=')
	                     .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
	                     .append('&');
	            }
	        }
	        hashData.deleteCharAt(hashData.length() - 1);
	        query.deleteCharAt(query.length() - 1);

	        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
	        query.append("&vnp_SecureHash=").append(secureHash);
	        return query.toString();
	    }

	public Map<String, String> verifyIpn(Map<String, String> fields) throws Exception {
		Map<String, String> response = new HashMap<>();

		// Lấy chữ ký VNPay gửi về
		String vnp_SecureHash = fields.remove("vnp_SecureHash");

		// Sắp xếp field theo thứ tự alphabet
		List<String> fieldNames = new ArrayList<>(fields.keySet());
		Collections.sort(fieldNames);

		// Build lại chuỗi dữ liệu để hash
		StringBuilder hashData = new StringBuilder();
		for (String name : fieldNames) {
			String value = fields.get(name);
			if (value != null && !value.isEmpty()) {
				if (hashData.length() > 0)
					hashData.append('&');
				// phải encode giống lúc tạo URL
				hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
			}
		}

		// Tạo chữ ký local
		String signValue = hmacSHA512(vnp_HashSecret, hashData.toString());

		// So sánh chữ ký (không phân biệt hoa thường)
		if (!signValue.equalsIgnoreCase(vnp_SecureHash)) {
			response.put("RspCode", "97");
			response.put("Message", "Invalid Signature");
			return response;
		}

		// Hợp lệ
		response.put("RspCode", "00");
		response.put("Message", "Valid Signature");
		return response;
	}
	
	 // ------------------------------------------------------------------------------------
    // Build hashData dùng cho querydr & payment
    // ------------------------------------------------------------------------------------
    private String buildHashData(Map<String, String> params) throws Exception {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String name : fieldNames) {
            String value = params.get(name);
            if (value != null && !value.isEmpty()) {
                hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
            }
        }

        // remove &
        hashData.setLength(hashData.length() - 1);

        return hashData.toString();
    }
}
