package projectSem4.com.service.seller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.entities.ShopAddress;
import projectSem4.com.model.modelViews.ShopView;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.model.repositories.UserRepository;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ShopRepository shopRepo;

    private ObjectMapper mapper;

    public ProfileService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Load profile cho trang web
    public Map<String, Object> loadProfile(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        HttpSession session = request.getSession(false);
        
        
    	if(session == null|| session.getAttribute("shopId") == null) {
    		return null;
    	}
    	var idShop = 0;
    	try {
			idShop = (Integer) session.getAttribute("shopId");
		} catch (Exception e) {
			return null;
		}
        var shopProfile = shopRepo.findById(idShop);
        result.put("shopProfile", shopProfile);
        
        
        Address shopAd = null;
        String strAddress = "";
        if(shopProfile.getAddress() != null) {
        	Addresses addresses = Optional.ofNullable(shopProfile.getAddress())
                    .filter(s -> !s.trim().isEmpty())
                    .map(s -> {
                        try {
                            return mapper.readValue(s, Addresses.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElse(new Addresses());

             shopAd = addresses.getShopAddresses().getWarehouseAddress();

            try {
                String shopProfileJson = mapper.writeValueAsString(shopProfile);
                session.setAttribute("shopProfileSS", shopProfileJson);

                if (shopAd != null) {
                    String warehouseJson = mapper.writeValueAsString(shopAd);
                    session.setAttribute("addressSS", warehouseJson);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            strAddress = shopAd.getProvinceName() + ", " +
                                shopAd.getDistrictName() + ", " +
                                shopAd.getWardName() + ", " +
                                shopAd.getStreet();

            result.put("addressStr", strAddress);
            result.put("address", shopAd);
            return result;
        }
        result.put("addressStr", "");
        result.put("address", "");
        return result;
    }

    // Update địa chỉ (API)
    public Map<String, Object> updateAddress(Address newAddress, Integer userId, Integer shopId, String oldAddressJson) {
    	Map<String, Object> res = new HashMap<>();
    	Map<String, String> errors = new HashMap<>();
        if (shopId == null || shopId == 0) {
            res.put("success", false);
            res.put("message", "Không tìm thấy shop");
            return res;
        }

        if (oldAddressJson == null) {
            res.put("success", false);
            res.put("message", "Không tìm thấy địa chỉ");
            return res;
        }
        Address addressOld = null;
        try {
        	addressOld = mapper.readValue(oldAddressJson, Address.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
     // Validate address
        if (newAddress == null || isBlank(newAddress.getProvinceId())) {
            errors.put("provinceId", "Province is required");
        }
        if (newAddress == null || isBlank(newAddress.getDistrictId())) {
            errors.put("districtId", "District is required");
        }
        if (newAddress == null || isBlank(newAddress.getWardCode())) {
            errors.put("wardCode", "Ward/Commune is required");
        }
        if (newAddress == null || isBlank(newAddress.getStreet())) {
            errors.put("street", "Street address is required");
        }
        if (!errors.isEmpty()) {
            return Map.of("success", false, "errors", errors);
        }
        
        if (Objects.equals(addressOld.getStreet(), newAddress.getStreet()) &&
        	    Objects.equals(addressOld.getDistrictId(), newAddress.getDistrictId()) &&
        	    Objects.equals(addressOld.getProvinceId(), newAddress.getProvinceId()) &&
        	    Objects.equals(addressOld.getWardCode(), newAddress.getWardCode())) {
        	    res.put("success", false);
        	    res.put("message", "Không có gì để thay đổi");
        	    return res;
        	}

        var user = userRepo.findById(userId);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Không tìm thấy user");
            return res;
        }

        // update lại địa chỉ
        Addresses addresses = Optional.ofNullable(user.getAddresses())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> {
                    try {
                        return mapper.readValue(s, Addresses.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(new Addresses());

        ShopAddress shopAddr = new ShopAddress();
        shopAddr.setWarehouseAddress(newAddress);
        addresses.setShopAddresses(shopAddr);

        try {
            String json = mapper.writeValueAsString(addresses);
            user.setAddresses(json);
            user.setUpdatedAt(LocalDateTime.now());
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Lỗi khi convert JSON");
            return res;
        }

        int updated = userRepo.updateAddressShop(user);
        if (updated > 0) {
            res.put("success", true);
            res.put("message", "Thành công");
        } else {
            res.put("success", false);
            res.put("message", "Thất bại");
        }

        return res;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateInfoShop(Integer userId, Shop shop, String phone) {
    	
    	 Map<String, String> errors = new HashMap<>();
    	 if (userId == null) {
             return Map.of("success", false,
            		 "message", "Không tìm thay user");
         }
    	 var shopOld = shopRepo.findById(shop.getShopId());
         if (shopOld == null) {
             return Map.of("success", false,
                     "errors", Map.of("global", "Không tìm thay shop"));
         }
    	 if (shop == null || shop.getShopName() == null || shop.getShopName().trim().isEmpty()) {
             errors.put("shopName", "Shop name is required");
         } else if (shop.getShopName().length() > 100) {
             errors.put("shopName", "Shop name must be less than 100 characters");
         }
    	 if (shop == null || shop.getDescription() == null || shop.getDescription().trim().isEmpty()) {
             errors.put("description", "Description is required");
         } else if (shop.getDescription().length() > 500) {
             errors.put("description", "Description must be less than 500 characters");
         } else if (shop.getDescription().matches(".*<[^>]*>.*")) {
             errors.put("description", "Description cannot contain HTML tags or code");
         }
        if (phone == null || phone.isEmpty()) {
            errors.put("phone", "Phone is required");
        } else if (phone.length() < 10 || phone.length() > 10 ) {
        	errors.put("phone", "Invalid phone number (10)");
        }
        
        if (!errors.isEmpty()) {
            return Map.of("success", false, "errors", errors);
        }
        
        var isChange = false;
        if(!shopOld.getDescription().equals(shop.getDescription().trim())) {
        	isChange = true;
        }
        if(!shopOld.getFullName().equals(shop.getShopName())) {
        	isChange = true;
        }
        if(!shopOld.getPhone().equals(phone.trim())) {
        	isChange = true;
        }
        
        if(!isChange) {
        	 return Map.of("success", false,
                     "message", "khong co gi thay doi");
        }
        
     // Cập nhật đồng bộ trong transaction
        if (!shopOld.getPhone().equals(phone.trim())) {
            int updatedUser = userRepo.updatePhone(userId, phone.trim());
            if (updatedUser <= 0)
                throw new RuntimeException("Cập nhật phone thất bại");
        }

        int updatedShop = shopRepo.updateShop(shop);
        if (updatedShop <= 0)
            throw new RuntimeException("Cập nhật shop thất bại");

        return Map.of("success", true, "message", "Cập nhật thành công");
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
