package projectSem4.com.service.seller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.dto.RegisterSalesDTO;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.entities.ShopAddress;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.model.repositories.UserRepository;

@Service
public class RegisterSellingService {

    private final ShopRepository shopRepo;
    private final UserRepository userRepo;

    public RegisterSellingService(ShopRepository shopRepo, UserRepository userRepo) {
        this.shopRepo = shopRepo;
        this.userRepo = userRepo;
    }

    public Map<String, Object> createShop(RegisterSalesDTO dto,HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        User dataUser = dto.getUser();
        Shop shop = dto.getShop();
        Address addressShop = dto.getAddress();
        HttpSession session = request.getSession(false);
        
        var userId = (Integer) session.getAttribute("userId");
        var user = userRepo.findById(userId);
      
        
        
        // Validate shop
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
        var phone = dataUser.getPhoneNumber().trim();
        if (phone == null || phone.isEmpty()) {
            errors.put("phone", "Phone is required");
        } else if (phone.length() < 10 || phone.length() > 10 ) {
        	errors.put("phone", "Invalid phone number (10)");
        }

        // Validate address
        if (addressShop == null || isBlank(addressShop.getProvinceId())) {
            errors.put("provinceId", "Province is required");
        }
        if (addressShop == null || isBlank(addressShop.getDistrictId())) {
            errors.put("districtId", "District is required");
        }
        if (addressShop == null || isBlank(addressShop.getWardCode())) {
            errors.put("wardCode", "Ward/Commune is required");
        }
        if (addressShop == null || isBlank(addressShop.getStreet())) {
            errors.put("street", "Street address is required");
        }

        if (!errors.isEmpty()) {
            return Map.of("success", false, "errors", errors);
        }

        // Save shop
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());
        shop.setUserId(userId); // Lấy từ user

        var strMessage = shopRepo.createShop(shop);
        if (Integer.parseInt(strMessage.get("rows").toString()) < 0) {
            return Map.of("success", false,
                          "errors", Map.of("global", strMessage.get("message").toString()));
        }

        // Save address
        try {
            ObjectMapper mapper = new ObjectMapper();
            addressShop.setId(UUID.randomUUID().toString());

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

            var shopAddress = new ShopAddress();
            shopAddress.setWarehouseAddress(addressShop);
            addresses.setShopAddresses(shopAddress);

            user.setAddresses(mapper.writeValueAsString(addresses));
            user.setUpdatedAt(LocalDateTime.now());
            user.setPhoneNumber(phone);
            String currentRole = user.getRoleId();

         // Tạo danh sách roles
            List<String> roles = new ArrayList<>();
            roles.add(currentRole);   // Buyer
            roles.add("SELLER");      // thêm Seller
    
            user.setRoleId(String.join(",", roles));

            var hasUser = userRepo.updateAddressShop(user);
            if (hasUser <= 0) {
                return Map.of("success", false,
                              "errors", Map.of("global", "Failed to add address!"));
            }
            
            session.setAttribute("roles", roles);
            return Map.of("success", true, "redirectUrl", "/seller");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false,
                          "errors", Map.of("global", "Unexpected error occurred"));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
