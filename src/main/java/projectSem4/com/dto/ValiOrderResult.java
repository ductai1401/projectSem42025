package projectSem4.com.dto;

import java.util.List;
import java.util.Map;

import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Coupon;
import projectSem4.com.model.modelViews.CartItemView;

public class ValiOrderResult {
	private List<CartItemView> cartItems;
    private Map<Long, Coupon> shopCouponMap;
    private Map<Long, Coupon> platformCouponMap;
    private Map<Long, Coupon> freeshipCouponMap;
    private Map<Long, Double> shippingFeeMap;
    private Address BuyerAddress;
    private List<String> errors;
    private boolean valid;
    
    public ValiOrderResult() {}

	





	public ValiOrderResult(List<CartItemView> cartItems, Map<Long, Coupon> shopCouponMap,
			Map<Long, Coupon> platformCouponMap, Map<Long, Coupon> freeshipCouponMap, Map<Long, Double> shippingFeeMap,
			Address buyerAddress, List<String> errors, boolean valid) {
		super();
		this.cartItems = cartItems;
		this.shopCouponMap = shopCouponMap;
		this.platformCouponMap = platformCouponMap;
		this.freeshipCouponMap = freeshipCouponMap;
		this.shippingFeeMap = shippingFeeMap;
		BuyerAddress = buyerAddress;
		this.errors = errors;
		this.valid = valid;
	}

	public Address getBuyerAddress() {
		return BuyerAddress;
	}

	public void setBuyerAddress(Address buyerAddress) {
		BuyerAddress = buyerAddress;
	}







	public Map<Long, Double> getShippingFeeMap() {
		return shippingFeeMap;
	}





	public void setShippingFeeMap(Map<Long, Double> shippingFeeMap) {
		this.shippingFeeMap = shippingFeeMap;
	}





	public List<CartItemView> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItemView> cartItems) {
		this.cartItems = cartItems;
	}

	public Map<Long, Coupon> getShopCouponMap() {
		return shopCouponMap;
	}

	public void setShopCouponMap(Map<Long, Coupon> shopCouponMap) {
		this.shopCouponMap = shopCouponMap;
	}

	public Map<Long, Coupon> getPlatformCouponMap() {
		return platformCouponMap;
	}

	public void setPlatformCouponMap(Map<Long, Coupon> platformCouponMap) {
		this.platformCouponMap = platformCouponMap;
	}

	public Map<Long, Coupon> getFreeshipCouponMap() {
		return freeshipCouponMap;
	}

	public void setFreeshipCouponMap(Map<Long, Coupon> freeshipCouponMap) {
		this.freeshipCouponMap = freeshipCouponMap;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}





	@Override
	public String toString() {
		return "ValiOrderResult [cartItems=" + cartItems + ", shopCouponMap=" + shopCouponMap + ", platformCouponMap="
				+ platformCouponMap + ", freeshipCouponMap=" + freeshipCouponMap + ", shippingFeeMap=" + shippingFeeMap
				+ ", errors=" + errors + ", valid=" + valid + "]";
	}

	
    
	
}
