package projectSem4.com.dto;


import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.User;

public class RegisterSalesDTO {
	private User user;
	private Shop shop;
	private Address address;
	
	public RegisterSalesDTO() {}
	
	public RegisterSalesDTO(User user, Shop shop, Address address) {
		this.user = user;
		this.shop = shop;
		this.address = address;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "RegisterSalesDTO [user=" + user + ", shop=" + shop + ", address=" + address + "]";
	}
	
	
	
}
