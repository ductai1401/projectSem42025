package projectSem4.com.dto;

public class ShopCouponDto {
	private Long shopId;
    private Long shopCouponId;
    private Long platformCouponId;
    private Long freeshipCouponId;
    
    public ShopCouponDto() {}
	public ShopCouponDto(Long shopId, Long shopCouponId, Long platformCouponId, Long freeshipCouponId) {
		super();
		this.shopId = shopId;
		this.shopCouponId = shopCouponId;
		this.platformCouponId = platformCouponId;
		this.freeshipCouponId = freeshipCouponId;
	}
	public Long getShopId() {
		return shopId;
	}
	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}
	public Long getShopCouponId() {
		return shopCouponId;
	}
	public void setShopCouponId(Long shopCouponId) {
		this.shopCouponId = shopCouponId;
	}
	public Long getPlatformCouponId() {
		return platformCouponId;
	}
	public void setPlatformCouponId(Long platformCouponId) {
		this.platformCouponId = platformCouponId;
	}
	public Long getFreeshipCouponId() {
		return freeshipCouponId;
	}
	public void setFreeshipCouponId(Long freeshipCouponId) {
		this.freeshipCouponId = freeshipCouponId;
	}
	@Override
	public String toString() {
		return "ShopCouponDto [shopId=" + shopId + ", shopCouponId=" + shopCouponId + ", platformCouponId="
				+ platformCouponId + ", freeshipCouponId=" + freeshipCouponId + "]";
	}
	
    
    
    
    // getter setter
}
