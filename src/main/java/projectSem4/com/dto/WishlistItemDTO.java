package projectSem4.com.dto;

public class WishlistItemDTO {
    private Integer wishlistId;
    private Integer productId;
    private String  productName;
    private String  productImage;      // ảnh sản phẩm / ảnh variant
    private Double  productPrice;      // giá hiển thị (lấy từ variant rẻ nhất/chọn trước)

    // optional để làm đẹp:
    private Double  productOldPrice;   // giá gạch
    private String  discountText;      // ví dụ "-56%"
    private String  variantText;       // ví dụ "Color Family: BLUE"
    private Boolean priceDropped;      // hiển thị “Price dropped”

    // Getters & Setters
    public Integer getWishlistId() { return wishlistId; }
    public void setWishlistId(Integer wishlistId) { this.wishlistId = wishlistId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }

    public Double getProductOldPrice() { return productOldPrice; }
    public void setProductOldPrice(Double productOldPrice) { this.productOldPrice = productOldPrice; }

    public String getDiscountText() { return discountText; }
    public void setDiscountText(String discountText) { this.discountText = discountText; }

    public String getVariantText() { return variantText; }
    public void setVariantText(String variantText) { this.variantText = variantText; }

    public Boolean getPriceDropped() { return priceDropped; }
    public void setPriceDropped(Boolean priceDropped) { this.priceDropped = priceDropped; }

    // ===== Constructors =====
    public WishlistItemDTO() {}

    // Constructor rút gọn: 5 tham số chính
    public WishlistItemDTO(Integer wishlistId, Integer productId, String productName,
                           String productImage, Double productPrice) {
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
    }

    // Constructor đầy đủ 9 tham số
    public WishlistItemDTO(Integer wishlistId, Integer productId, String productName,
                           String productImage, Double productPrice,
                           Double productOldPrice, String discountText,
                           String variantText, Boolean priceDropped) {
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.productOldPrice = productOldPrice;
        this.discountText = discountText;
        this.variantText = variantText;
        this.priceDropped = priceDropped;
    }
}
