package projectSem4.com.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import projectSem4.com.model.entities.Category;
import projectSem4.com.model.entities.InventoryLog;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.ProductVariant;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.repositories.CategoryRepository;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.model.utils.MyValidate;
import projectSem4.com.service.client.InventoryService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final InventoryService inventoryService;

    private static final String UPLOAD_DIR = "D:/uploads/";
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ShopRepository shopRepository;
    @Autowired private ProductVariantRepository productVariantRepository;

    public ProductService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ---------- helpers ----------
    private String safeShopName(Integer shopId) {
        if (shopId == null) return "sàn";
        String name = shopRepository.getShopNameById(shopId);
        return (name == null || name.isBlank()) ? "sàn" : name;
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // =================== READ ===================

    public List<Product> findAllProductsWithNames() {
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            p.setCategoryName(categoryRepository.getCategoryNameById(p.getCategoryId()));
            p.setShopName(safeShopName(p.getShopId())); // fallback "sàn"
        }
        return products;
    }

    public List<Shop> getAllShops() {
        return shopRepository.findAllShops();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // =================== CREATE PRODUCT (JSON) ===================

    public Map<String, Object> createProductJson(ProductCreateRequest req) {
        Map<String, Object> res = new HashMap<>();

        // Chuẩn hóa input
        req.productName = trimOrNull(req.productName);
        req.description = trimOrNull(req.description);
        Integer safeShopId = (req.shopId != null && req.shopId > 0) ? req.shopId : null; // cho phép null = "sàn"

        MyValidate.SimpleErrors errs = new MyValidate.SimpleErrors();
        // Lưu ý: validateProductBasic nên CHO PHÉP shopId = null
        MyValidate.validateProductBasic(req.productName, req.categoryId, safeShopId, req.status, errs);
        if (errs.hasErrors()) {
            res.put("ok", false);
            res.put("message", errs.firstMessage());
            res.put("errors", errs.all());
            return res;
        }

        try {
            Product p = new Product();
            p.setCategoryId(req.categoryId);
            p.setShopId(safeShopId); // null -> "sàn"
            p.setProductName(req.productName);
            p.setDescription(req.description);
            p.setProductOption(req.productOption);
            p.setStatus(req.status == null ? 1 : req.status);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());

            Integer productId = productRepository.createProductReturningId(p);
            if (productId == null) {
                res.put("ok", false);
                res.put("message", "Không lấy được ProductID sau khi tạo sản phẩm.");
                return res;
            }

            res.put("ok", true);
            res.put("productId", productId);
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Lỗi tạo sản phẩm: " + e.getMessage());
            return res;
        }
    }

    // =================== PRODUCT IMAGE ===================

    public Map<String, Object> uploadProductImage(Integer productId, MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        try {
            String err = MyValidate.validateImageFile(file, MAX_IMAGE_BYTES);
            if (err != null) {
                res.put("ok", false);
                res.put("message", err);
                return res;
            }
            String savedName = saveFile(file);

            int rows = productRepository.updateProductImage(productId, savedName);
            if (rows <= 0) {
                res.put("ok", false);
                res.put("message", "Không cập nhật được ảnh sản phẩm.");
                return res;
            }

            res.put("ok", true);
            res.put("image", savedName);
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Upload ảnh thất bại: " + e.getMessage());
            return res;
        }
    }

    // =================== CREATE 1 VARIANT ===================

    @Transactional
    public Map<String, Object> createSingleVariantForProduct(Integer productId, String variantName, String sku,
                                                             String priceStr, String qtyStr, MultipartFile variantImage,
                                                             Integer logType, String unitCostStr, Integer refOrderId) {
        Map<String, Object> res = new HashMap<>();
        try {
            // Parse & validate cơ bản
            String vName = trimOrNull(variantName);
            String vSku  = MyValidate.normalizeSku(sku);
            Double vPrice = MyValidate.parseDoubleSafe(priceStr);
            Integer vQty  = MyValidate.parseIntSafe(qtyStr);
            int initQty   = (vQty == null || vQty < 0) ? 0 : vQty;

            boolean emptyRow = (vName == null) && MyValidate.isBlank(vSku) && vPrice == null && vQty == null
                    && (variantImage == null || variantImage.isEmpty());
            if (emptyRow) {
                res.put("ok", false);
                res.put("message", "Dòng biến thể rỗng.");
                return res;
            }

            if (MyValidate.notBlank(vSku)) {
                boolean dup = productVariantRepository.existsSKUInProduct(productId.longValue(), vSku, null);
                if (dup) {
                    res.put("ok", false);
                    res.put("message", "SKU đã tồn tại trong sản phẩm.");
                    return res;
                }
            }

            // Ảnh
            String savedVariantImage = null;
            if (variantImage != null && !variantImage.isEmpty()) {
                String imgErr = MyValidate.validateImageFile(variantImage, MAX_IMAGE_BYTES);
                if (imgErr != null) {
                    res.put("ok", false);
                    res.put("message", imgErr);
                    return res;
                }
                savedVariantImage = saveFile(variantImage);
            }

            // Tạo variant
            ProductVariant variant = new ProductVariant();
            variant.setProductId(productId);
            variant.setVarianName(vName);
            variant.setSKU(vSku);
            variant.setImage(savedVariantImage);
            variant.setPrice(vPrice);
            variant.setStockQuantity(initQty);
            variant.setStatus(true);
            variant.setCreatedAt(LocalDateTime.now());
            variant.setUpdatedAt(LocalDateTime.now());
            productVariantRepository.createProductVariant(variant);

            if (variant.getVariantId() == null) {
                res.put("ok", false);
                res.put("message", "Tạo biến thể thất bại (chưa có VariantID sau insert).");
                return res;
            }

            // Map log type
            String dbLogType;
            int code = (logType == null) ? 1 : logType;
            switch (code) {
                case 2: dbLogType = "EXPORT"; break;
                case 3: dbLogType = "RETURN"; break;
                case 4: dbLogType = "CANCEL"; break;
                default: dbLogType = "IMPORT"; break;
            }

            // Tồn kho trước đó
            int prevRem;
            try {
                prevRem = inventoryService.getStock(variant.getVariantId().intValue());
            } catch (Exception ignore) {
                prevRem = 0;
            }

            // Delta
            int delta;
            switch (dbLogType) {
                case "EXPORT": delta = -initQty; break;
                case "RETURN": delta =  initQty; break;
                case "CANCEL": delta = -initQty; break;
                case "IMPORT":
                default:       delta =  initQty; break;
            }
            int newRemaining = Math.max(0, prevRem + delta);

            // Ghi log
            if (initQty > 0 || "IMPORT".equals(dbLogType)) {
                Double unitCost = MyValidate.parseDoubleSafe(unitCostStr);
                double cost = (unitCost == null || unitCost < 0) ? 0d : unitCost;

                InventoryLog log = new InventoryLog();
                log.setVariantId(variant.getVariantId());
                log.setLogType(dbLogType);
                log.setQuantity(initQty);
                log.setUnitCost(cost);
                log.setRemaining(newRemaining);
                log.setRefOrderId(null);
                log.setCreatedAt(LocalDateTime.now());
                inventoryService.addLog(log);
            }

            res.put("ok", true);
            res.put("variantId", variant.getVariantId());
            res.put("image", savedVariantImage);
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Tạo biến thể thất bại: " + e.getMessage());
            return res;
        }
    }

    // =================== DETAIL ===================

    public ProductDetailResponse getProductDetail(Integer productId) {
        Product p = productRepository.findById(productId);
        if (p == null) {
            return new ProductDetailResponse(false, null, Collections.emptyList(), Collections.emptyList());
        }

        p.setCategoryName(categoryRepository.getCategoryNameById(p.getCategoryId()));
        p.setShopName(safeShopName(p.getShopId()));

        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        if (variants == null) variants = Collections.emptyList();

        List<OptionPair> optionPairs = parseProductOptionToPairs(p.getProductOption());
        List<VariantDTO> variantDTOs = variants.stream().map(VariantDTO::fromEntity).collect(Collectors.toList());

        return new ProductDetailResponse(true, p, variantDTOs, optionPairs);
    }

    // =================== HELPERS ===================

    private String saveFile(MultipartFile file) throws IOException {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        String fileName = UUID.randomUUID() + "_" + original.replaceAll("[\\s]+", "_");
        file.transferTo(new File(dir, fileName));
        return fileName;
    }

    private List<OptionPair> parseProductOptionToPairs(String productOptionJson) {
        if (productOptionJson == null || productOptionJson.isBlank()) return Collections.emptyList();
        try {
            Map<String, Object> map = MAPPER.readValue(productOptionJson, new TypeReference<Map<String, Object>>() {});
            List<OptionPair> list = new ArrayList<>();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                String valueStr;
                Object v = e.getValue();
                if (v == null) valueStr = "";
                else if (v instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> lv = (List<Object>) v;
                    valueStr = lv.stream().map(String::valueOf).collect(Collectors.joining(", "));
                } else if (v instanceof Map) {
                    valueStr = MAPPER.writeValueAsString(v);
                } else {
                    valueStr = String.valueOf(v);
                }
                list.add(new OptionPair(e.getKey(), valueStr));
            }
            return list;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    // =================== DTOs ===================

    public static class ProductCreateRequest {
        public Integer categoryId;
        public Integer shopId;       // cho phép null = "sàn"
        public String  productName;
        public String  description;
        public String  productOption; // JSON/Text
        public String  variantConfig; // nếu FE gửi kèm
        public Integer status;        // 0/1
    }

    public static class OptionPair {
        public String key;
        public String value;
        public OptionPair() {}
        public OptionPair(String key, String value) { this.key = key; this.value = value; }
    }

    public static class VariantDTO {
        public Long variantId;
        public Integer productId;
        public String varianName;
        public String sku;
        public String image;
        public Double price;
        public Integer stockQuantity;
        public Boolean status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static VariantDTO fromEntity(ProductVariant v) {
            VariantDTO d = new VariantDTO();
            d.variantId = v.getVariantId();
            d.productId = v.getProductId();
            d.varianName = v.getVarianName();
            d.sku = v.getSKU();
            d.image = v.getImage();
            d.price = v.getPrice();
            d.stockQuantity = v.getStockQuantity();
            d.status = v.getStatus();
            d.createdAt = v.getCreatedAt();
            d.updatedAt = v.getUpdatedAt();
            return d;
        }
    }

    public static class ProductDetailResponse {
        public boolean ok;
        public Product product;
        public List<VariantDTO> variants;
        public List<OptionPair> productOptionPairs;

        public ProductDetailResponse() {}
        public ProductDetailResponse(boolean ok, Product product, List<VariantDTO> variants, List<OptionPair> productOptionPairs) {
            this.ok = ok;
            this.product = product;
            this.variants = variants;
            this.productOptionPairs = productOptionPairs;
        }
    }

    // =================== UPDATE OPTION / VARIANT CONFIG ===================

    public Map<String, Object> updateProductOption(Integer productId, String productOption) {
        Map<String, Object> res = new HashMap<>();
        try {
            Product p = productRepository.findById(productId);
            if (p == null) {
                res.put("ok", false);
                res.put("message", "Không tìm thấy sản phẩm");
                return res;
            }

            String toSave = null;
            if (productOption != null) {
                String raw = productOption.trim();
                if (!raw.isEmpty()) {
                    boolean looksLikeJson = raw.startsWith("{") || raw.startsWith("[");
                    if (looksLikeJson) {
                        try {
                            toSave = MAPPER.writeValueAsString(MAPPER.readTree(raw)); // compact
                        } catch (Exception parseErr) {
                            toSave = raw; // không phải JSON hợp lệ
                        }
                    } else {
                        toSave = raw;
                    }
                }
            }

            int rows = productRepository.updateProductOption(productId, toSave);
            if (rows <= 0) {
                res.put("ok", false);
                res.put("message", "Không cập nhật được productOption");
                return res;
            }

            res.put("ok", true);
            res.put("productId", productId);
            res.put("productOption", toSave);
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Lỗi cập nhật productOption: " + e.getMessage());
            return res;
        }
    }

    public Map<String, Object> updateVariantConfig(Integer productId, Map<String, Object> cfg) {
        Map<String, Object> res = new HashMap<>();
        try {
            Product p = productRepository.findById(productId);
            if (p == null) {
                res.put("ok", false);
                res.put("message", "Không tìm thấy sản phẩm");
                return res;
            }

            String json = null;
            if (cfg != null) {
                Map<String, Object> cleaned = new LinkedHashMap<>();
                for (Map.Entry<String, Object> e : cfg.entrySet()) {
                    Object v = e.getValue();
                    if (v == null) continue;
                    if (v instanceof String s && s.trim().isEmpty()) continue;
                    cleaned.put(e.getKey(), v);
                }
                json = cleaned.isEmpty() ? null : MAPPER.writeValueAsString(cleaned);
            }

            int rows = productRepository.updateVariantConfig(productId, json);
            if (rows <= 0) {
                res.put("ok", false);
                res.put("message", "Không cập nhật được variantConfig");
                return res;
            }

            res.put("ok", true);
            res.put("productId", productId);
            res.put("variantConfig", json);
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Lỗi cập nhật variantConfig: " + e.getMessage());
            return res;
        }
    }

    // =================== READ (single) ===================

    public Product findById(Integer productId) {
        if (productId == null || productId <= 0) return null;
        try {
            return productRepository.findById(productId);
        } catch (Exception e) {
            return null;
        }
    }

    public Product findByIdWithNames(Integer productId) {
        Product p = findById(productId);
        if (p != null) {
            p.setCategoryName(categoryRepository.getCategoryNameById(p.getCategoryId()));
            p.setShopName(safeShopName(p.getShopId()));
        }
        return p;
    }

    // =================== PRICE HELPERS ===================

    public Map<Integer, Double> buildMinPriceMap(List<Product> products) {
        if (products == null || products.isEmpty()) return Map.of();
        Map<Integer, Double> map = new HashMap<>();
        for (Product p : products) {
            Double minPrice = productVariantRepository.getMinPriceOfProduct(p.getProductId());
            map.put(p.getProductId(), minPrice);
        }
        return map;
    }

    public Map<Integer, Double> buildMaxPriceMap(List<Product> products) {
        if (products == null || products.isEmpty()) return Map.of();
        Map<Integer, Double> map = new HashMap<>();
        for (Product p : products) {
            Double maxPrice = productVariantRepository.getMaxPriceOfProduct(p.getProductId());
            map.put(p.getProductId(), maxPrice);
        }
        return map;
    }

    public boolean belongsToShop(Integer productId, Integer shopId) {
        Product p = findById(productId);
        return p != null && shopId != null && shopId.equals(p.getShopId());
    }

    public List<Product> findProductsByShopId(Integer shopId) {
        List<Product> products = productRepository.findByShopId(shopId);
        for (Product p : products) {
            p.setCategoryName(categoryRepository.getCategoryNameById(p.getCategoryId()));
            p.setShopName(safeShopName(p.getShopId())); // fallback "sàn"
        }
        return products;
    }

    public List<Product> searchProducts(Integer shopId, String keyword, int limit) {
        if (limit <= 0) limit = 20;
        return productRepository.searchProductsWithLimit(shopId, keyword, limit);
    }
}
