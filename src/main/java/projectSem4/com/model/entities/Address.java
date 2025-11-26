package projectSem4.com.model.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {
	 private String id;  // id của địa chỉ, để dễ cập nhật
	    private String street;
	    private String wardCode;
	    private String wardName;
	    private String districtId;
	    private String districtName;
	    private String provinceId;
	    private String provinceName;
	    private String phone;
	    
	    @JsonProperty("isDefault")
	    private boolean isDefault; 

	    public Address() {}

		public Address(String id, String street, String wardCode, String wardName, String districtId,
				String districtName, String provinceId, String provinceName, String phone, boolean isDefault) {
			super();
			this.id = id;
			this.street = street;
			this.wardCode = wardCode;
			this.wardName = wardName;
			this.districtId = districtId;
			this.districtName = districtName;
			this.provinceId = provinceId;
			this.provinceName = provinceName;
			this.phone = phone;
			this.isDefault = isDefault;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getWardCode() {
			return wardCode;
		}

		public void setWardCode(String wardCode) {
			this.wardCode = wardCode;
		}

		public String getWardName() {
			return wardName;
		}

		public void setWardName(String wardName) {
			this.wardName = wardName;
		}

		public String getDistrictId() {
			return districtId;
		}

		public void setDistrictId(String districtId) {
			this.districtId = districtId;
		}

		public String getDistrictName() {
			return districtName;
		}

		public void setDistrictName(String districtName) {
			this.districtName = districtName;
		}

		public String getProvinceId() {
			return provinceId;
		}

		public void setProvinceId(String provinceId) {
			this.provinceId = provinceId;
		}

		public String getProvinceName() {
			return provinceName;
		}

		public void setProvinceName(String provinceName) {
			this.provinceName = provinceName;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public boolean isDefault() {
			return isDefault;
		}

		public void setDefault(boolean isDefault) {
			this.isDefault = isDefault;
		}

		@Override
		public String toString() {
			return "Address [id=" + id + ", street=" + street + ", wardCode=" + wardCode + ", wardName=" + wardName
					+ ", districtId=" + districtId + ", districtName=" + districtName + ", provinceId=" + provinceId
					+ ", provinceName=" + provinceName + ", phone=" + phone + ", isDefault=" + isDefault + "]";
		}

		
	   

		
	    
	    
}
