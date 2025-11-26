package projectSem4.com.dto;

public class CartItemDto {
	 private Integer variantId;
	    private Integer quantity;
	    
	    public CartItemDto() {
	        // constructor mặc định
	    } 

	    public CartItemDto(Integer variantId, Integer quantity) {
			super();
			this.variantId = variantId;
			this.quantity = quantity;
		}
		// getter & setter
	    public Integer getVariantId() {
	        return variantId;
	    }
	    public void setVariantId(Integer variantId) {
	        this.variantId = variantId;
	    }
	    public Integer getQuantity() {
	        return quantity;
	    }
	    public void setQuantity(Integer quantity) {
	        this.quantity = quantity;
	    }
		@Override
		public String toString() {
			return "CartItemDto [variantId=" + variantId + ", quantity=" + quantity + "]";
		}
	    
}
