package projectSem4.com.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSem4.com.dto.FlashSaleForProductDTO;
import projectSem4.com.model.entities.FlashSaleItem;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.repositories.FlashSaleItemRepository;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.model.utils.MyValidate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlashSaleItemService {

    @Autowired private FlashSaleItemRepository  flashSaleItemRepository;
    @Autowired private ProductRepository        productRepository;
    @Autowired private ProductVariantRepository productVariantRepository; // ✅ dùng giá từ biến thể

    // =================== READ ===================

    public List<ItemDTO> findByFlashSaleId(int flashSaleId) {
        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleId(flashSaleId);
        return enrich(items);
    }

    public PagedResult<ItemDTO> findPagedByFlashSaleId(int flashSaleId, int page, int size) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        List<FlashSaleItem> rows = flashSaleItemRepository.findPagedByFlashSaleId(flashSaleId, p, s);
        int total = flashSaleItemRepository.countByFlashSaleId(flashSaleId);
        return new PagedResult<>(enrich(rows), total, p, s);
    }

    // =================== CREATE ===================

    public Map<String, Object> createOne(CreateRequest req) {
        Map<String, Object> res = new HashMap<>();

        MyValidate.SimpleErrors errs = validateCreate(req);
        if (errs.hasErrors()) {
            res.put("ok", false);
            res.put("message", errs.firstMessage());
            res.put("errors", errs.all());
            return res;
        }

        try {
            boolean exists = flashSaleItemRepository.existsProductInFlashSale(req.flashSaleId, req.productId, null);
            if (exists) {
                res.put("ok", false);
                res.put("message", "Sản phẩm đã tồn tại trong Flash Sale.");
                return res;
            }

            FlashSaleItem it = new FlashSaleItem();
            it.setFlashSaleId(req.flashSaleId);
            it.setProductId(req.productId);
            it.setQuantity(req.quantity);
            it.setPercern(req.percern == null ? 0 : Math.max(0, req.percern));
            it.setTotalAmount(req.totalAmount == null ? 0f : Math.max(0f, req.totalAmount.floatValue()));

            Integer id = flashSaleItemRepository.createReturningId(it);
            if (id == null) {
                res.put("ok", false);
                res.put("message", "Không lấy được FlashSaleItemID sau khi tạo.");
                return res;
            }

            res.put("ok", true);
            res.put("flashSaleItemId", id);
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Lỗi tạo FlashSaleItem: " + e.getMessage());
            return res;
        }
    }

    @Transactional
    public Map<String, Object> bulkCreate(int flashSaleId, List<CreateRequest> requests) {
        Map<String, Object> res = new HashMap<>();
        if (requests == null || requests.isEmpty()) {
            res.put("ok", false);
            res.put("message", "Danh sách rỗng.");
            return res;
        }

        for (CreateRequest r : requests) {
            r.flashSaleId = flashSaleId;
            MyValidate.SimpleErrors e = validateCreate(r);
            if (e.hasErrors()) {
                res.put("ok", false);
                res.put("message", "Có phần tử không hợp lệ: " + e.firstMessage());
                res.put("errors", e.all());
                return res;
            }
        }

        Set<Integer> seen = new HashSet<>();
        for (CreateRequest r : requests) {
            if (!seen.add(r.productId)) {
                res.put("ok", false);
                res.put("message", "Danh sách chứa sản phẩm trùng nhau: " + r.productId);
                return res;
            }
        }

        List<Integer> productIds = requests.stream().map(r -> r.productId).collect(Collectors.toList());
        List<FlashSaleItem> existed = flashSaleItemRepository
                .findByFlashSaleIdAndProductIds(flashSaleId, productIds);

        if (existed != null && !existed.isEmpty()) {
            res.put("ok", false);
            res.put("message", "Một số sản phẩm đã tồn tại trong Flash Sale.");
            res.put("existedProductIds", existed.stream().map(FlashSaleItem::getProductId).collect(Collectors.toList()));
            return res;
        }

        List<FlashSaleItem> items = new ArrayList<>();
        for (CreateRequest r : requests) {
            FlashSaleItem it = new FlashSaleItem();
            it.setFlashSaleId(flashSaleId);
            it.setProductId(r.productId);
            it.setQuantity(r.quantity);
            it.setPercern(r.percern == null ? 0 : Math.max(0, r.percern));
            it.setTotalAmount(r.totalAmount == null ? 0f : Math.max(0f, r.totalAmount.floatValue()));
            items.add(it);
        }

        try {
            int[][] result = flashSaleItemRepository.bulkCreate(items);
            int okCount = 0;
            if (result != null) {
                for (int[] arr : result) {
                    for (int v : arr) if (v != java.sql.Statement.EXECUTE_FAILED) okCount++;
                }
            }
            boolean ok = okCount >= items.size();

            res.put("ok", ok);
            res.put("affected", okCount);
            if (!ok) res.put("message", "Chèn chưa đủ bản ghi mong muốn.");
            return res;

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", "Lỗi bulk create: " + e.getMessage());
            return res;
        }
    }

    // =================== UPDATE ===================

    public Map<String, Object> update(UpdateRequest req) {
        Map<String, Object> res = new HashMap<>();
        if (req.flashSaleItemId == null || req.flashSaleItemId <= 0) {
            res.put("ok", false);
            res.put("message", "Thiếu hoặc sai FlashSaleItemID.");
            return res;
        }
        FlashSaleItem exist = flashSaleItemRepository.findById(req.flashSaleItemId);
        if (exist == null) {
            res.put("ok", false);
            res.put("message", "Không tìm thấy FlashSaleItem.");
            return res;
        }

        if (req.productId != null && req.productId <= 0) {
            res.put("ok", false);
            res.put("message", "ProductID không hợp lệ.");
            return res;
        }
        if (req.quantity != null && req.quantity < 0) {
            res.put("ok", false);
            res.put("message", "Quantity phải >= 0.");
            return res;
        }
        if (req.percern != null && req.percern < 0) {
            res.put("ok", false);
            res.put("message", "Percern phải >= 0.");
            return res;
        }
        if (req.totalAmount != null && req.totalAmount < 0) {
            res.put("ok", false);
            res.put("message", "TotalAmount phải >= 0.");
            return res;
        }

        if (req.productId != null) {
            boolean dup = flashSaleItemRepository.existsProductInFlashSale(
                    exist.getFlashSaleId(), req.productId, exist.getFlashSaleItemId());
            if (dup) {
                res.put("ok", false);
                res.put("message", "Sản phẩm đã tồn tại trong Flash Sale.");
                return res;
            }
        }

        if (req.productId   != null) exist.setProductId(req.productId);
        if (req.quantity    != null) exist.setQuantity(req.quantity);
        if (req.percern     != null) exist.setPercern(req.percern);
        if (req.totalAmount != null) exist.setTotalAmount(req.totalAmount.floatValue());

        String msg = flashSaleItemRepository.update(exist);
        boolean ok = msg != null && msg.toLowerCase(Locale.ROOT).contains("thành công");
        res.put("ok", ok);
        res.put("message", msg);
        res.put("flashSaleItemId", exist.getFlashSaleItemId());
        return res;
    }

    // =================== DELETE ===================

    public Map<String, Object> delete(Integer flashSaleItemId) {
        Map<String, Object> res = new HashMap<>();
        if (flashSaleItemId == null || flashSaleItemId <= 0) {
            res.put("ok", false);
            res.put("message", "Thiếu hoặc sai FlashSaleItemID.");
            return res;
        }
        String msg = flashSaleItemRepository.delete(flashSaleItemId);
        boolean ok = msg != null && msg.toLowerCase(Locale.ROOT).contains("thành công");
        res.put("ok", ok);
        res.put("message", msg);
        res.put("flashSaleItemId", flashSaleItemId);
        return res;
    }

    /* NEW: Hủy tất cả FlashSaleItem của 1 product
       - onlyActive = true: chỉ xoá các item thuộc FlashSale đang hiệu lực
       - onlyActive = false: xoá tất cả item của product trong mọi FlashSale */
    @Transactional
    public Map<String, Object> cancelAllByProductId(Integer productId, boolean onlyActive) {
        Map<String, Object> res = new HashMap<>();
        if (productId == null || productId <= 0) {
            res.put("ok", false);
            res.put("message", "productId không hợp lệ.");
            return res;
        }
        int affected = onlyActive
                ? flashSaleItemRepository.deleteActiveByProductId(productId)
                : flashSaleItemRepository.deleteByProductId(productId);
        res.put("ok", true);
        res.put("deleted", affected);
        return res;
    }

    /* NEW: Hủy 1 mục theo FlashSaleID + ProductID (tiện khi bạn biết FS nào chứa SP) */
    @Transactional
    public Map<String, Object> cancelByFlashSaleAndProduct(Integer flashSaleId, Integer productId) {
        Map<String, Object> res = new HashMap<>();
        if (flashSaleId == null || flashSaleId <= 0 || productId == null || productId <= 0) {
            res.put("ok", false);
            res.put("message", "flashSaleId/productId không hợp lệ.");
            return res;
        }
        int affected = flashSaleItemRepository.deleteByFlashSaleIdAndProductId(flashSaleId, productId);
        res.put("ok", true);
        res.put("deleted", affected);
        return res;
    }

    // =================== BUSINESS HELPERS ===================

    public Map<String, Object> addRevenue(Integer flashSaleItemId, Double amount) {
        Map<String, Object> res = new HashMap<>();
        if (flashSaleItemId == null || flashSaleItemId <= 0) {
            res.put("ok", false);
            res.put("message", "Thiếu hoặc sai FlashSaleItemID.");
            return res;
        }
        if (amount == null || amount <= 0) {
            res.put("ok", false);
            res.put("message", "Số tiền phải > 0.");
            return res;
        }
        int rows = flashSaleItemRepository.addToTotalAmount(flashSaleItemId, amount.floatValue());
        res.put("ok", rows > 0);
        res.put("affected", rows);
        return res;
    }

    // =================== VALIDATION & ENRICH ===================

    private MyValidate.SimpleErrors validateCreate(CreateRequest r) {
        MyValidate.SimpleErrors errs = new MyValidate.SimpleErrors();
        if (r.flashSaleId == null || r.flashSaleId <= 0) errs.add("flashSaleId", "FlashSaleID không hợp lệ.");
        if (r.productId   == null || r.productId   <= 0) errs.add("productId",   "ProductID không hợp lệ.");
        if (r.quantity    == null || r.quantity    <  0) errs.add("quantity",    "Quantity phải >= 0.");
        if (r.percern     != null && r.percern     <  0) errs.add("percern",     "Percern phải >= 0.");
        if (r.totalAmount != null && r.totalAmount <  0) errs.add("totalAmount", "TotalAmount phải >= 0.");

        if (r.productId != null && r.productId > 0) {
            try {
                Product p = productRepository.findById(r.productId);
                if (p == null) errs.add("productId", "Không tìm thấy Product.");
            } catch (Exception ignored) {
                errs.add("productId", "Không xác thực được Product.");
            }
        }
        return errs;
    }

    private List<ItemDTO> enrich(List<FlashSaleItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();

        List<Integer> productIds = items.stream()
                .map(FlashSaleItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Product> productMap = new HashMap<>();
        for (Integer pid : productIds) {
            try {
                Product p = productRepository.findById(pid);
                if (p != null) productMap.put(pid, p);
            } catch (Exception ignored) {}
        }

        List<ItemDTO> out = new ArrayList<>(items.size());
        for (FlashSaleItem it : items) {
            Product p = productMap.get(it.getProductId());
            out.add(ItemDTO.from(it, p));
        }
        return out;
    }

    // =================== DTOs ===================

    public static class CreateRequest {
        public Integer flashSaleId;
        public Integer productId;
        public Integer quantity;
        public Integer percern;     // optional
        public Double  totalAmount; // optional
    }

    public static class UpdateRequest {
        public Integer flashSaleItemId;
        public Integer productId;      // optional
        public Integer quantity;       // optional
        public Integer percern;        // optional
        public Double  totalAmount;    // optional
    }

    public static class ItemDTO {
        public Integer flashSaleItemId;
        public Integer flashSaleId;
        public Integer productId;
        public Integer quantity;
        public Integer percern;
        public Float   totalAmount;

        public String  productName;
        public String  image;
        public Double  basePrice; // nếu cần, có thể gán p.getPrice()

        public static ItemDTO from(FlashSaleItem it, Product p) {
            ItemDTO d = new ItemDTO();
            d.flashSaleItemId = it.getFlashSaleItemId();
            d.flashSaleId     = it.getFlashSaleId();
            d.productId       = it.getProductId();
            d.quantity        = it.getQuantity();
            d.percern         = it.getPercern();
            d.totalAmount     = it.getTotalAmount();

            if (p != null) {
                d.productName = p.getProductName();
                d.image       = p.getImage();
                // d.basePrice   = p.getPrice(); // bật nếu entity có
            }
            return d;
        }
    }

    public static class PagedResult<T> {
        public List<T> data;
        public int total;
        public int page;
        public int size;

        public PagedResult() {}
        public PagedResult(List<T> data, int total, int page, int size) {
            this.data = data;
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }

    /** Helper tạo nhanh 1 item (dùng ở controller). */
    public Map<String, Object> createItem(Integer flashSaleId, Integer productId, Integer quantity, Integer percern) {
        CreateRequest r = new CreateRequest();
        r.flashSaleId = flashSaleId;
        r.productId   = productId;
        r.quantity    = quantity;
        r.percern     = percern;
        return createOne(r);
    }

    // =================== NEW: Lấy FlashSale từ productId + tính salePrice (theo biến thể) ===================

    /** Lấy giá gốc từ các biến thể của product:
     *  - Ưu tiên biến thể có Price nhỏ nhất (findFirstByProductIdOrderByPriceAsc).
     *  - Fallback sang getMinPriceOfProduct nếu cần.
     */
    private Double getBasePriceFromVariants(Integer productId) {
        if (productId == null || productId <= 0) return null;
        try {
            var cheapest = productVariantRepository.findFirstByProductIdOrderByPriceAsc(productId);
            if (cheapest != null && cheapest.getPrice() != null) return cheapest.getPrice();
        } catch (Exception ignored) {}
        try {
            return productVariantRepository.getMinPriceOfProduct(productId.longValue());
        } catch (Exception ignored) {}
        return null;
    }

    /** Lấy flash sale ĐANG hiệu lực theo productId và tính salePrice từ percern + giá biến thể. */
    public FlashSaleForProductDTO getActiveFlashSaleByProductId(Integer productId) {
        if (productId == null || productId <= 0) return null;

        FlashSaleForProductDTO dto = flashSaleItemRepository.findActiveFlashSaleByProductId(productId);
        if (dto == null) return null;

        try {
            Double base = getBasePriceFromVariants(productId);
            if (base != null) {
                dto.setBasePrice(base);
                if (dto.getPercern() != null && dto.getPercern() > 0) {
                    double sale = Math.max(0d, base * (100 - dto.getPercern()) / 100.0);
                    dto.setSalePrice(sale);
                }
            }
        } catch (Exception ignored) {}
        return dto;
    }

    /** Lấy TẤT CẢ flash sale (mọi thời điểm) cho productId; tính salePrice từ biến thể nếu có thể. */
    public List<FlashSaleForProductDTO> getAllFlashSalesByProductId(Integer productId) {
        if (productId == null || productId <= 0) return List.of();
        List<FlashSaleForProductDTO> list = flashSaleItemRepository.findAllFlashSalesByProductId(productId);

        try {
            Double base = getBasePriceFromVariants(productId);
            if (base != null) {
                for (FlashSaleForProductDTO dto : list) {
                    dto.setBasePrice(base);
                    if (dto.getPercern() != null && dto.getPercern() > 0) {
                        dto.setSalePrice(Math.max(0d, base * (100 - dto.getPercern()) / 100.0));
                    }
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    /** Trả về salePrice nếu đang Flash Sale; otherwise null. */
    public Double getActiveSalePriceIfAny(Integer productId) {
        FlashSaleForProductDTO dto = getActiveFlashSaleByProductId(productId);
        return (dto != null) ? dto.getSalePrice() : null;
    }

    /** Kiểm tra SP có nằm trong 1 Flash Sale đang hiệu lực không. */
    public boolean isProductInActiveFlashSale(Integer productId) {
        return getActiveFlashSaleByProductId(productId) != null;
    }
}
