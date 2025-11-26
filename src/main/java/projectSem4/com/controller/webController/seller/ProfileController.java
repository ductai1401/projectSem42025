package projectSem4.com.controller.webController.seller;



import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.service.seller.ProfileService;



@Controller
@RequestMapping("/seller")
public class ProfileController {
	
	@Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    public String index(Model model, HttpServletRequest request) {
    	
    	
    	
        var data = profileService.loadProfile(request);
        if(data == null) {
        	return "redirect:/login";
        }
        
        if(data.get("address").equals("")) {
        	model.addAttribute("address", new Address());
        }else {
        	model.addAttribute("address", data.get("address"));
        }
        
        
        model.addAttribute("shopProfile", data.get("shopProfile"));
        model.addAttribute("addressStr", data.get("addressStr"));
        
        return "seller/profile";
    }
    
    @PostMapping("/updateAddress")
    @ResponseBody
    public Map<String, Object> updateAddress(@RequestBody Address newAddress, HttpServletRequest request, HttpSession session) {
    	Integer userId = (Integer) session.getAttribute("userId");
        Integer shopId = (Integer) session.getAttribute("shopId");
        String oldAddressJson = (String) session.getAttribute("addressSS");
        
        return profileService.updateAddress(newAddress, userId, shopId, oldAddressJson);
    }
    
    @PostMapping("/updateInfo")
    @ResponseBody
    public Map<String, Object> updateInfoShop(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpSession session) {
    	Integer userId = (Integer) session.getAttribute("userId");
        Integer shopId = (Integer) session.getAttribute("shopId");
        
    	var phone = (String) body.get("phone");
    	Map<String, Object> shopMap = (Map<String, Object>) body.get("shop");
        Shop shop = new Shop();
        shop.setShopId(shopId);
        shop.setShopName((String) shopMap.get("shopName"));
        shop.setDescription((String) shopMap.get("description"));
        shop.setUserId(userId);
        shop.setUpdatedAt(LocalDateTime.now());
        return profileService.updateInfoShop(userId, shop, phone);
    }
}
