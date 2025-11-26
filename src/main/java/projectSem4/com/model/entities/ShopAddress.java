package projectSem4.com.model.entities;

public class ShopAddress {

    private Address warehouseAddress;


    public ShopAddress() {}

    public ShopAddress(Address warehouseAddress) {
       
        this.warehouseAddress = warehouseAddress;

    }

    public Address getWarehouseAddress() { return warehouseAddress; }
    public void setWarehouseAddress(Address warehouseAddress) { this.warehouseAddress = warehouseAddress; }

	@Override
	public String toString() {
		return "ShopAddress [warehouseAddress=" + warehouseAddress + "]";
	}

    
}
