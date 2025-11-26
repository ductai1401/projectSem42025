package projectSem4.com.controller.webController.admin;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.service.admin.ShopService;


@Controller
@RequestMapping("admin/shop")
public class ShopController {
    
	@Autowired
    private ShopService shopService;

    // Hiển thị trang danh sách shop
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("repoHashCode", shopService.getRepoHashCode());
        return "admin/shop/listShop";
    }

    @GetMapping("/detail")
    public String shopDetail(Model model) {
        model.addAttribute("repoHashCode", shopService.getRepoHashCode());
        return "admin/shop/shopDetail";
    }

    @GetMapping("/productDetail")
    public String productDetail(Model model) {
        model.addAttribute("repoHashCode", shopService.getRepoHashCode());
        return "admin/shop/productDetail";
    }
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getAllByFillter(
    		@RequestParam(name = "page", defaultValue = "1") Integer page,
    		@RequestParam(name = "size" ,defaultValue = "10") Integer size,
    		@RequestParam(name = "status") Integer status,
    		@RequestParam(name = "search", required = false) String search,
    		@RequestParam(name = "rating", required = false) Double rating,
    		@RequestParam(name = "sort") String sort,
    		Model model) {
    	
    	 try {
             Map<String, Object> data = shopService.searchAndPagi(page, size, search, rating, status, sort);
             
             data.put("stats", status);
             return ResponseEntity.ok(data);
         } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
         }
    }
    @GetMapping("/shop_detail/{idShop}")
    public String getShopDetail(
    		@PathVariable(name = "idShop") Integer id,
    		Model model) {
    	
    	 try {
    		 if(id == null) {
 				return "admin/shop/shopDetail";
 			}
             var data = shopService.getShopDetail(id);
             if(data != null) {
            	model.addAllAttributes(data);
             }    
         } catch (Exception e) {
             e.printStackTrace();
             
         }
    	 return "admin/shop/shopDetail";
    }
    
    @PostMapping("/{idShop}/approved")
    @ResponseBody
    public ResponseEntity<?> approve(@PathVariable(name = "idShop") Integer id){
    	try {
			if(id == null) {
				return ResponseEntity.ok(Map.of("success" , false, "message", "id not found"));
			}
			var rs =  shopService.updateStatus(id, 2);
			return ResponseEntity.ok(rs);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return ResponseEntity.ok(Map.of("success" , false, "message", "The system is temporarily not operating"));
		}
    	
    	
    }
    @PostMapping("/{idShop}/block")
    @ResponseBody
    public ResponseEntity<?> block(@PathVariable(name = "idShop") Integer id){
    	try {
    		if(id == null) {
    			return ResponseEntity.ok(Map.of("success" , false, "message", "id not found"));
    		}
    		var rs =  shopService.updateStatus(id, 3);
    		return ResponseEntity.ok(rs);
    	} catch (Exception e) {
    		e.printStackTrace();
    		// TODO: handle exception
    		return ResponseEntity.ok(Map.of("success" , false, "message", "The system is temporarily not operating"));
    	}
    	
    	
    }
    @PostMapping("/{idShop}/inactive")
    @ResponseBody
    public ResponseEntity<?> inactive(@PathVariable(name = "idShop") Integer id){
    	try {
    		if(id == null) {
    			return ResponseEntity.ok(Map.of("success" , false, "message", "id not found"));
    		}
    		var rs =  shopService.updateStatus(id, 4);
    		return ResponseEntity.ok(rs);
    	} catch (Exception e) {
    		e.printStackTrace();
    		// TODO: handle exception
    		return ResponseEntity.ok(Map.of("success" , false, "message", "The system is temporarily not operating"));
    	}
    	
    	
    }
    
    
}