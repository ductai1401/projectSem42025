package projectSem4.com.controller.apiController.seller;

import java.net.http.HttpRequest;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.model.repositories.UserRepository;
import projectSem4.com.service.JwtProvider;
import projectSem4.com.service.seller.ProfileService;

@RestController
@RequestMapping("/api/seller")
public class ProfileApiController {

    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private JwtProvider jwtService;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private ShopRepository shopRepo;
    

    @PostMapping("/updateAddress")
    public Map<String, Object> updateAddressApi(@RequestBody Address newAddress, HttpServletRequest req) throws Exception {
        // giả sử bạn có service giải mã JWT
        Integer userId = (Integer) req.getAttribute("userId");
        Integer shopId = shopRepo.getShopIdByUserId(userId.intValue()); // lấy shopId từ DB
        String oldAddressJson = userRepo.findById(userId.intValue()).getAddresses();

        return profileService.updateAddress(newAddress, userId.intValue(), shopId, oldAddressJson);
    }
   
    
    @PostMapping("/updateInfo")
    public Map<String, Object> updateInfoShop(@RequestBody Map<String, Object> body, HttpServletRequest req) throws Exception{
    	Integer userId = (Integer) req.getAttribute("userId");
    	Integer shopId = shopRepo.getShopIdByUserId(userId.intValue());
    	var shop = (Shop) body.get("shop");
    	var phone = (String) body.get("phone");
    	shop.setShopId(shopId);
    	return profileService.updateInfoShop(userId.intValue(), shop, phone);
    }
}
