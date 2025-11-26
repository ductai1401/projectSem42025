package projectSem4.com.model.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;

/**
 * Tiện ích validate + parse an toàn cho Product & ProductVariant.
 * - Có thể dùng với BindingResult, hoặc dùng SimpleErrors/ValidationResult tự quản lý lỗi.
 */
public final class MyValidate {

    private MyValidate() {}

    // ====== Helpers cơ bản ======
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    public static boolean notBlank(String s) { return !isBlank(s); }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static Integer parseIntSafe(String s) {
        if (isBlank(s)) return null;
        try { return Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }

    public static Double parseDoubleSafe(String s) {
        if (isBlank(s)) return null;
        try { return Double.valueOf(s.trim()); } catch (Exception e) { return null; }
    }

    public static boolean maxLength(String s, int max) { return s == null || s.length() <= max; }
    public static boolean positiveOrZero(Number n) { return n == null || n.doubleValue() >= 0d; }
    public static boolean positive(Number n) { return n != null && n.doubleValue() > 0d; }

    public static String normalizeSku(String sku) {
        return sku == null ? null : sku.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    /** Chỉ lấy tên file (tránh path traversal) */
    public static String sanitizeFilename(String original) {
        if (isBlank(original)) return null;
        String base = Paths.get(original).getFileName().toString();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    // ====== File validations ======
    public static boolean isValidImageContentType(String ct) {
        if (ct == null) return false;
        String c = ct.toLowerCase(Locale.ROOT);
        return c.equals("image/png") || c.equals("image/jpeg") || c.equals("image/jpg") || c.equals("image/webp");
    }

    /** Validate file ảnh cơ bản. @return thông báo lỗi (null nếu hợp lệ) */
    public static String validateImageFile(MultipartFile f, long maxBytes) {
        if (f == null || f.isEmpty()) return null; // ảnh không bắt buộc
        if (!isValidImageContentType(f.getContentType()))
            return "File ảnh không đúng định dạng (png, jpg, jpeg, webp).";
        if (f.getSize() > maxBytes)
            return "Kích thước ảnh vượt giới hạn cho phép.";
        return null;
    }

    // ====== Kết quả validate kiểu map (dùng khi không muốn phụ thuộc BindingResult) ======
    public static final class ValidationResult {
        private final Map<String, String> fieldErrors = new LinkedHashMap<>();
        public void addError(String field, String message) {
            if (field != null && message != null) fieldErrors.put(field, message);
        }
        public boolean hasErrors() { return !fieldErrors.isEmpty(); }
        public Map<String, String> getErrors() { return Collections.unmodifiableMap(fieldErrors); }
        public void copyTo(BindingResult br) {
            fieldErrors.forEach((f, m) -> br.addError(new FieldError(br.getObjectName(), f, m)));
        }
        public String firstMessage() { return fieldErrors.values().stream().findFirst().orElse(null); }
    }

    // ====== Bộ gom lỗi siêu gọn cho controller JSON ======
    public static final class SimpleErrors {
        private final Map<String, String> errors = new LinkedHashMap<>();
        public void add(String field, String message) {
            if (message == null) return;
            errors.put(field == null ? "_global" : field, message);
        }
        public boolean hasErrors() { return !errors.isEmpty(); }
        public String firstMessage() { return errors.values().stream().findFirst().orElse(null); }
        public Map<String, String> all() { return Collections.unmodifiableMap(errors); }
    }

    // ====== Checker trùng SKU (truyền từ Repo) ======
    @FunctionalInterface
    public interface SkuExistsChecker {
        boolean exists(long productId, String sku, Long excludeVariantId);
    }

    // ====== Validate Product (bản dùng BindingResult) ======
    public static void validateProductBasic(
            String productName,
            Integer categoryId,
            Integer shopId,
            Integer status,
            BindingResult br
    ) {
        if (isBlank(productName)) {
            br.rejectValue("productName", "name.required", "Tên sản phẩm không được để trống.");
        } else if (!maxLength(productName, 255)) {
            br.rejectValue("productName", "name.max", "Tên sản phẩm tối đa 255 ký tự.");
        }

        if (categoryId == null) {
            br.rejectValue("categoryId", "category.required", "Vui lòng chọn danh mục.");
        }
        if (shopId == null) {
            br.rejectValue("shopId", "shop.required", "Vui lòng chọn cửa hàng.");
        }
        if (status != null && !(status == 0 || status == 1)) {
            br.rejectValue("status", "status.invalid", "Trạng thái không hợp lệ.");
        }
    }

    // ====== Validate Product (bản dùng SimpleErrors) ======
    public static void validateProductBasic(
            String productName,
            Integer categoryId,
            Integer shopId,
            Integer status,
            SimpleErrors errs
    ) {
        if (isBlank(productName)) {
            errs.add("productName", "Tên sản phẩm không được để trống.");
        } else if (!maxLength(productName, 255)) {
            errs.add("productName", "Tên sản phẩm tối đa 255 ký tự.");
        }

        if (categoryId == null) {
            errs.add("categoryId", "Vui lòng chọn danh mục.");
        }
        if (shopId == null) {
            errs.add("shopId", "Vui lòng chọn cửa hàng.");
        }
        if (status != null && !(status == 0 || status == 1)) {
            errs.add("status", "Trạng thái không hợp lệ.");
        }
    }

    // ====== Validate Variants (input mảng từ form) ======
    public static ValidationResult validateVariantsPayload(
            List<String> variantName,
            List<String> sku,
            List<String> priceStr,
            List<String> qtyStr,
            List<MultipartFile> images,
            Long productIdForSkuCheck,     // null nếu productId chưa có
            SkuExistsChecker skuChecker,   // null nếu không cần check trùng
            long maxImageBytes
    ) {
        ValidationResult vr = new ValidationResult();

        int n = maxSize(variantName, sku, priceStr, qtyStr);
        for (int i = 0; i < n; i++) {
            String vName = getAt(variantName, i);
            String vSku  = normalizeSku(getAt(sku, i));
            String pStr  = getAt(priceStr, i);
            String qStr  = getAt(qtyStr, i);
            Double price = parseDoubleSafe(pStr);
            Integer qty  = parseIntSafe(qStr);

            // Bỏ qua hàng hoàn toàn trống
            if (isBlank(vName) && isBlank(vSku) && price == null && qty == null && getFileAt(images, i) == null) {
                continue;
            }

            // Tên biến thể
            if (isBlank(vName)) {
                vr.addError("variantName[" + i + "]", "Variant Name không được để trống.");
            } else if (!maxLength(vName, 200)) {
                vr.addError("variantName[" + i + "]", "Variant Name tối đa 200 ký tự.");
            }

            // SKU
            if (isBlank(vSku)) {
                vr.addError("sku[" + i + "]", "SKU không được để trống.");
            } else if (!vSku.matches("^[A-Z0-9._-]{2,64}$")) {
                vr.addError("sku[" + i + "]", "SKU chỉ gồm A–Z, 0–9, ., _, -, 2–64 ký tự.");
            } else if (productIdForSkuCheck != null && skuChecker != null) {
                boolean dup = skuChecker.exists(productIdForSkuCheck, vSku, null);
                if (dup) vr.addError("sku[" + i + "]", "SKU đã tồn tại trong sản phẩm này.");
            }

            // Giá
            if (price == null || !positive(price)) {
                vr.addError("price[" + i + "]", "Giá phải là số > 0.");
            }

            // Số lượng
            if (qty == null || !positiveOrZero(qty)) {
                vr.addError("quantity[" + i + "]", "Số lượng phải là số ≥ 0.");
            }

            // Ảnh
            MultipartFile f = getFileAt(images, i);
            String imgErr = validateImageFile(f, maxImageBytes);
            if (imgErr != null) {
                vr.addError("variantImages[" + i + "]", imgErr);
            }
        }

        return vr;
    }

    // ====== Private utils cho validate variants ======
    private static int maxSize(List<?>... lists) {
        int max = 0;
        for (List<?> l : lists) {
            if (l != null && l.size() > max) max = l.size();
        }
        return max;
    }

    private static String getAt(List<String> list, int i) {
        return (list != null && list.size() > i) ? list.get(i) : null;
    }

    private static MultipartFile getFileAt(List<MultipartFile> list, int i) {
        return (list != null && list.size() > i) ? list.get(i) : null;
    }
}
