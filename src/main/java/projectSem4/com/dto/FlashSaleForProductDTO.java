package projectSem4.com.dto;

import java.time.LocalDateTime;

public class FlashSaleForProductDTO {

    private Integer flashSaleItemId;
    private Integer flashSaleId;
    private Integer productId;
    private Integer quantity;
    private Integer percern;       // % giảm (đang dùng trường Percern trong bảng)
    private Float   totalAmount;   // bạn đang dùng như doanh thu cộng dồn

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Phục vụ hiển thị giá: lấy từ Product.price + percern
    private Double basePrice;      // giá gốc của Product (nếu có)
    private Double salePrice;      // = basePrice * (100 - percern)/100

    public FlashSaleForProductDTO() {}

    public Integer getFlashSaleItemId() { return flashSaleItemId; }
    public void setFlashSaleItemId(Integer flashSaleItemId) { this.flashSaleItemId = flashSaleItemId; }

    public Integer getFlashSaleId() { return flashSaleId; }
    public void setFlashSaleId(Integer flashSaleId) { this.flashSaleId = flashSaleId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPercern() { return percern; }
    public void setPercern(Integer percern) { this.percern = percern; }

    public Float getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Float totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
}
