package projectSem4.com.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectSem4.com.dto.CouponForm;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.enums.CouponType;
import projectSem4.com.model.enums.DiscountTypeEnum;
import projectSem4.com.model.repositories.CouponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponRepository repo;

    public CouponService(CouponRepository repo) {
        this.repo = repo;
    }

    /* ================== Helpers ================== */

    /** Chuẩn hóa code: trim, bỏ khoảng trắng, viết hoa. */
    private static String normCode(String code) {
        if (code == null) return null;
        return code.trim().replaceAll("\\s+", "").toUpperCase();
    }

    /** Check rule chung cho cả admin & seller. */
    private static void validateBusiness(Coupon c, boolean isAdminFlow, Integer mustShopIdOrNull) {
        if (c.getStartDate() == null || c.getEndDate() == null)
            throw new RuntimeException("Start/End date is required");
        if (c.getEndDate().isBefore(c.getStartDate()))
            throw new RuntimeException("EndDate must be after StartDate");

        // Phân quyền theo luồng Admin / Shop
        if (isAdminFlow) {
        	
            if (CouponType.PLATFORM.equals(c.getCouponType()))
                throw new RuntimeException("Admin chỉ được tạo/sửa PLATFORM coupon");
            if (c.getShopId() != null)
                throw new RuntimeException("PLATFORM coupon không được gán ShopID");
        } else {
            if (CouponType.SHOP.equals(c.getCouponType()))
                throw new RuntimeException("Shop chỉ được tạo/sửa SHOP coupon");
            if (c.getShopId() == null || !c.getShopId().equals(mustShopIdOrNull))
                throw new RuntimeException("Coupon không thuộc shop hiện tại");
        }

        // Discount value
        if (c.getDiscountValue() == null || c.getDiscountValue().compareTo(BigDecimal.ZERO) < 0)
            throw new RuntimeException("DiscountValue phải >= 0");

        if (DiscountTypeEnum.PERCENT.equals(c.getDiscountType())) {
            if (c.getDiscountValue().compareTo(BigDecimal.ZERO) < 0
                    || c.getDiscountValue().compareTo(new BigDecimal("100")) > 0)
                throw new RuntimeException("Percent phải trong khoảng 0–100");
            if (c.getMaxDiscount() != null && c.getMaxDiscount().compareTo(BigDecimal.ZERO) < 0)
                throw new RuntimeException("MaxDiscount phải >= 0");
        }

        if (c.getMinOrderAmount() == null || c.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new RuntimeException("MinOrderAmount phải >= 0");

        if (c.getUsageLimit() == null || c.getUsageLimit() < 0)
            throw new RuntimeException("UsageLimit phải >= 0");

        if (c.getUsedCount() == null) c.setUsedCount(0);
        if (c.getUsedCount() < 0 || c.getUsedCount() > c.getUsageLimit())
            throw new RuntimeException("UsedCount không hợp lệ");
    }

    private static Coupon mapFromFormForAdmin(CouponForm f) {
        Coupon c = new Coupon();
        c.setCode(normCode(f.getCode()));
        c.setCouponType(CouponType.PLATFORM.toString());
        c.setDiscountType(String.valueOf(f.getDiscountType()));
        c.setDiscountValue(f.getDiscountValue());
        c.setMaxDiscount(f.getMaxDiscount());
        c.setMinOrderAmount(f.getMinOrderAmount());
        c.setStartDate(f.getStartDate());
        c.setEndDate(f.getEndDate());
        c.setUsageLimit(f.getUsageLimit());
        c.setUsedCount(0);
        c.setShopId(null);
        c.setStatus(f.getStatus());
        return c;
    }

    private static Coupon mapFromFormForShop(CouponForm f, int shopId) {
        Coupon c = new Coupon();
        c.setCode(normCode(f.getCode()));
        c.setCouponType(CouponType.SHOP.toString());
        c.setDiscountType(String.valueOf(f.getDiscountType()));
        c.setDiscountValue(f.getDiscountValue());
        c.setMaxDiscount(f.getMaxDiscount());
        c.setMinOrderAmount(f.getMinOrderAmount());
        c.setStartDate(f.getStartDate());
        c.setEndDate(f.getEndDate());
        c.setUsageLimit(f.getUsageLimit());
        c.setUsedCount(0);
        c.setShopId(shopId);
        c.setStatus(f.getStatus());
        return c;
    }

    /* ================== Admin (PLATFORM) ================== */

    public List<Coupon> adminList(int page, int size, String q) {
        return repo.findAllPlatform(page, size, q);
    }

    public int adminCount(String q) {
        return repo.countPlatform(q);
    }

    @Transactional
    public void adminCreate(CouponForm form) {
        Coupon c = mapFromFormForAdmin(form);
        try {
        	 validateBusiness(c, true, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return;
		}
       

        if (repo.existsCodePlatform(c.getCode()))
            throw new RuntimeException("Code đã tồn tại trên toàn sàn");

        repo.insertPlatform(c);
    }

    public Optional<Coupon> findById(int couponId) {
        return repo.findById(couponId);
    }

    @Transactional
    public void adminUpdate(int couponId, CouponForm form) {
        Coupon db = repo.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (db.getShopId() != null)
            throw new RuntimeException("Không thể sửa coupon SHOP trong luồng admin");

        Coupon c = mapFromFormForAdmin(form);
        c.setCouponId(couponId);

        if (CouponType.PLATFORM.name().equals(c.getCouponType()))
            throw new RuntimeException("Không thể đổi loại sang SHOP");

        validateBusiness(c, true, null);
        repo.updatePlatform(c);
    }

    @Transactional
    public void adminDelete(int couponId) {
        int rows = repo.deletePlatform(couponId);
        if (rows == 0)
            throw new RuntimeException("Coupon not found hoặc không phải PLATFORM");
    }

    /* ================== Seller (SHOP) ================== */

    public List<Coupon> shopList(int shopId, int page, int size, String q) {
        return repo.findAllByShop(shopId, page, size, q);
    }

    public int shopCount(int shopId, String q) {
        return repo.countByShop(shopId, q);
    }

    @Transactional
    public void shopCreate(int shopId, CouponForm form) {
        Coupon c = mapFromFormForShop(form, shopId);
        validateBusiness(c, false, shopId);

        if (repo.existsCodeInShop(c.getCode(), shopId))
            throw new RuntimeException("Code đã tồn tại trong shop của bạn");

        repo.insertForShop(c);
    }

    @Transactional
    public void shopUpdate(int shopId, int couponId, CouponForm form) {
        Coupon db = repo.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (db.getShopId() == null || !db.getShopId().equals(shopId))
            throw new RuntimeException("Coupon không thuộc shop của bạn");

        Coupon c = mapFromFormForShop(form, shopId);
        c.setCouponId(couponId);

        if (CouponType.SHOP.equals(c.getCouponType()))
            throw new RuntimeException("Không thể đổi loại sang PLATFORM");

        validateBusiness(c, false, shopId);
        repo.updateForShop(c);
    }

    @Transactional
    public void shopDelete(int shopId, int couponId) {
        int rows = repo.deleteForShop(couponId, shopId);
        if (rows == 0)
            throw new RuntimeException("Coupon not found hoặc không thuộc shop của bạn");
    }

    /* ================== Utils / Public APIs ================== */

    /** Kiểm tra coupon có đang active theo thời gian & status hay không. */
    public boolean isActiveNow(Coupon c) {
        if (c == null || c.getStatus() == null || c.getStatus() != 1) return false;

        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(c.getStartDate()) || now.isAfter(c.getStartDate()))
                && (now.isBefore(c.getEndDate()) || now.isEqual(c.getEndDate()));
    }

    /**
     * Lấy list coupon theo loại (PLATFORM / SHOP).
     * - SHOP: dùng findByShopId (đã filter Status=1, Date, UsageLimit ở repo).
     * - PLATFORM: tạm dùng findAllPlatform rồi filter active bằng isActiveNow.
     */
    public List<Coupon> getByType(String couponType, Integer shopId) {
        if (couponType == null || couponType.isBlank()) {
            return Collections.emptyList();
        }

        CouponType type;
        try {
            type = CouponType.valueOf(couponType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Sai type → trả list rỗng, tránh văng lỗi
            return Collections.emptyList();
        }

        if (type == CouponType.SHOP) {
            // SHOP: bắt buộc có shopId > 0
            if (shopId == null || shopId <= 0) return Collections.emptyList();
            return repo.findByShopId(shopId); // đã filter active trong repo
        } else if (type == CouponType.PLATFORM) {
            // PLATFORM: tạm lấy tối đa 1000 record rồi filter active
            List<Coupon> list = repo.findAllPlatform(0, 1000, "");
            return list.stream()
                    .filter(this::isActiveNow)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /** Lấy coupon theo id kiểu Long (trả về null nếu không tồn tại). */
//    public Coupon getById(Long id) {
//        if (id == null) return null;
//        Optional<Coupon> op = repo.findById(id.intValue());
//        return op.orElse(null);
//    }

    
	public List<Coupon> getAll(){
		return repo.findAllCoupons();
	}
	
//	public List<Coupon> getByType(String couponType, Integer shopId){
//	    if(shopId != null && shopId > 0) {
//	        return repo.findByType(couponType, shopId);
//	    } else {
//	        return repo.findByType(couponType);
//	    }
//	}
	public Coupon getById(Long id){
		
		return repo.findById(id);
	} 
	
	public List<Coupon> getByShop(int shopId){
		if(shopId < 0) {
			return null;
		}
		return repo.findByShopId(shopId);
	} 
	

}
