package projectSem4.com.dto;

import java.util.List;

public class CheckCartItem {
	 private List<Long> cartItemIds;
	 private List<ShopCouponDto> shopCoupons;
	 private String methodPayment;
	 private String idAddress;
	 private String token;
	 
	 public CheckCartItem() {}

	 

	public CheckCartItem(List<Long> cartItemIds, List<ShopCouponDto> shopCoupons, String methodPayment,
			String idAddress, String token) {
		super();
		this.cartItemIds = cartItemIds;
		this.shopCoupons = shopCoupons;
		this.methodPayment = methodPayment;
		this.idAddress = idAddress;
		this.token = token;
	}



	public String getToken() {
		return token;
	}



	public void setToken(String token) {
		this.token = token;
	}



	public String getIdAddress() {
		return idAddress;
	}





	public void setIdAddress(String idAddress) {
		this.idAddress = idAddress;
	}





	public String getMethodPayment() {
		return methodPayment;
	}



	public void setMethodPayment(String methodPayment) {
		this.methodPayment = methodPayment;
	}



	public List<Long> getCartItemIds() {
		return cartItemIds;
	}

	public void setCartItemIds(List<Long> cartItemIds) {
		this.cartItemIds = cartItemIds;
	}


	public List<ShopCouponDto> getShopCoupons() {
		return shopCoupons;
	}

	public void setShopCoupons(List<ShopCouponDto> shopCoupons) {
		this.shopCoupons = shopCoupons;
	}



	@Override
	public String toString() {
		return "CheckCartItem [cartItemIds=" + cartItemIds + ", shopCoupons=" + shopCoupons + ", methodPayment="
				+ methodPayment + ", idAddress=" + idAddress + ", token=" + token + "]";
	}



}
