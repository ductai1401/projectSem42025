package projectSem4.com.model.entities;

import java.util.List;

public class Addresses {
	 private List<Address> customerAddresses;  // Danh sách địa chỉ khách hàng
	    private ShopAddress shopAddresses;      // Địa chỉ của shop (business, warehouse, pickup)

	    public Addresses() {}

	    public Addresses(List<Address> customerAddresses, ShopAddress shopAddresses) {
	        this.customerAddresses = customerAddresses;
	        this.shopAddresses = shopAddresses;
	    }

	    // Getter & Setter
	    public List<Address> getCustomerAddresses() { return customerAddresses; }
	    public void setCustomerAddresses(List<Address> customerAddresses) { this.customerAddresses = customerAddresses; }

	    public ShopAddress getShopAddresses() { return shopAddresses; }
	    public void setShopAddresses(ShopAddress shopAddresses) { this.shopAddresses = shopAddresses; }

		@Override
		public String toString() {
			return "Addresses [customerAddresses=" + customerAddresses + ", shopAddresses=" + shopAddresses + "]";
		}
	    
	    
}
