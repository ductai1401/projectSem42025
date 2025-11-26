// projectSem4.com.controller.client.AccountController
package projectSem4.com.controller.webController.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectSem4.com.dto.ChangePasswordRequest;
import projectSem4.com.dto.ProfileUpdateRequest;
import projectSem4.com.model.entities.Address;
import projectSem4.com.model.entities.Addresses;
import projectSem4.com.model.entities.Shipment;
import projectSem4.com.model.entities.ShipmentHistory;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.enums.OrderStatus;
import projectSem4.com.model.modelViews.OrderView;
import projectSem4.com.model.repositories.UserRepository;
import projectSem4.com.service.client.CheckoutService;
import projectSem4.com.service.client.OrderService;
import projectSem4.com.service.seller.ShipmentService;
import projectSem4.com.service.shipper.ShipperService;

import java.awt.Checkbox;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserRepository userRepository;
    
    @Autowired
    private CheckoutService checkService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ShipmentService shipmentService;
    
    @Autowired
    private ShipperService shipperService;

    // Nên chuyển sang cấu hình trong application.properties/yml
    private static final String UPLOAD_DIR = "D:/uploads/";

    public AccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/"})
    public String myAccount(Model model, HttpServletRequest request) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";

        if (!model.containsAttribute("profileForm")) {
            ProfileUpdateRequest pf = new ProfileUpdateRequest();
            pf.setFullName(nz(me.getFullName()));
            pf.setPhoneNumber(nz(me.getPhoneNumber()));
            pf.setAddresses(nz(me.getAddresses()));
            model.addAttribute("profileForm", pf);
            List<Address> addresses = checkService.extractAddresses(me);
            
            model.addAttribute("addresses" , addresses);
            
        }
        if (!model.containsAttribute("pwdForm")) {
            model.addAttribute("pwdForm", new ChangePasswordRequest());
        }
        model.addAttribute("me", me);
        return "client/common/my-account";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileUpdateRequest form,
                                BindingResult br,
                                HttpServletRequest request,
                                RedirectAttributes ra) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.profileForm", br);
            ra.addFlashAttribute("profileForm", form);
            ra.addFlashAttribute("error", "Update profile failed. Please check the form.");
            return "redirect:/account#account-info";
        }

        me.setFullName(trimOrNull(form.getFullName()));
        me.setPhoneNumber(trimOrNull(form.getPhoneNumber()));
        me.setAddresses(trimOrNull(form.getAddresses()));
        me.setUpdatedAt(LocalDateTime.now());

        userRepository.updateUser(me);
        ra.addFlashAttribute("success", "Profile updated successfully.");
        return "redirect:/account#account-info";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("pwdForm") ChangePasswordRequest form,
                                 BindingResult br,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";

        if (!br.hasErrors()) {
            // so khớp password cũ dùng jBCrypt
            if (!BCrypt.checkpw(nz(form.getCurrentPassword()), nz(me.getPasswordHash()))) {
                br.rejectValue("currentPassword", "pwd.current.invalid", "Current password is incorrect");
            }
            // xác nhận khớp new/confirm
            if (!Objects.equals(nz(form.getNewPassword()), nz(form.getConfirmPassword()))) {
                br.rejectValue("confirmPassword", "pwd.confirm.mismatch", "Confirm password does not match");
            }
            // không cho new == current
            if (Objects.equals(nz(form.getNewPassword()), nz(form.getCurrentPassword()))) {
                br.rejectValue("newPassword", "pwd.new.same", "New password must be different from current password");
            }
        }

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pwdForm", br);
            ra.addFlashAttribute("pwdForm", form);
            ra.addFlashAttribute("error", "Change password failed. Please fix the errors below.");
            return "redirect:/account#download";
        }

        // hash mật khẩu mới với jBCrypt
        String newHash = BCrypt.hashpw(form.getNewPassword(), BCrypt.gensalt(12));
        userRepository.updatePassword(me.getUserId(), newHash);

        ra.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/account#download";
    }
    @GetMapping("/address/{id}")
    @ResponseBody
    public Address getById(@PathVariable("id") String id,
                           HttpServletRequest request,
                           HttpSession session) {
        User me = getCurrentUser(request);
        try {
            List<Address> lAddress = checkService.extractAddresses(me);

            return lAddress.stream()
                           .filter(a -> id.equals(a.getId()))
                           .findFirst()
                           .orElse(null);

        } catch (Exception e) {
            return null;
        }
    }
    
    @PostMapping("/address/update")
    public String updateAddress(@RequestParam("addressesJson") String addressesJson,
                                HttpServletRequest request,
                                RedirectAttributes ra) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";

        if (addressesJson == null || addressesJson.isBlank()) {
            ra.addFlashAttribute("error", "Address update failed: empty data.");
            return "redirect:/account#address-edit";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Address addressUpdate = mapper.readValue(addressesJson, Address.class);

            // Lấy danh sách địa chỉ hiện có
            List<Address> addressCus = checkService.extractAddresses(me);

            boolean found = false;
            for (Address addr : addressCus) {
                if (addr.getId().equals(addressUpdate.getId())) {
                    // Cập nhật fields
                    addr.setStreet(addressUpdate.getStreet());
                    addr.setWardCode(addressUpdate.getWardCode());
                    addr.setWardName(addressUpdate.getWardName());
                    addr.setDistrictId(addressUpdate.getDistrictId());
                    addr.setDistrictName(addressUpdate.getDistrictName());
                    addr.setProvinceId(addressUpdate.getProvinceId());
                    addr.setProvinceName(addressUpdate.getProvinceName());
                    addr.setPhone(addressUpdate.getPhone());
                    found = true;
                }
                // Nếu đang set default => reset các cái khác
                if (addressUpdate.isDefault()) {
                    addr.setDefault(false);
                }
            }

            if (!found) {
                ra.addFlashAttribute("error", "Address not found to update.");
                return "redirect:/account#address-edit";
            }

            // Set default cho địa chỉ mới update nếu cần
            if (addressUpdate.isDefault()) {
                addressCus.stream()
                        .filter(a -> a.getId().equals(addressUpdate.getId()))
                        .forEach(a -> a.setDefault(true));
            }

            // Save lại JSON
            Addresses wrapper = mapper.readValue(me.getAddresses(), Addresses.class);
            wrapper.setCustomerAddresses(addressCus);
            String json = mapper.writeValueAsString(wrapper);

            me.setAddresses(json);
            me.setUpdatedAt(LocalDateTime.now());
            int rows = userRepository.updateAddressCus(me);

            if (rows == 1) {
                ra.addFlashAttribute("success", "Address updated successfully.");
            } else {
                ra.addFlashAttribute("error", "Address update failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Error updating address: " + e.getMessage());
        }

        return "redirect:/account#address-edit";
    }
  

    @PostMapping("/address")
    public String saveAddress(@RequestParam("addressesJson") String addressesJson,
                              HttpServletRequest request,
                              RedirectAttributes ra) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";
        
        if(addressesJson.isEmpty() || "".equals(addressesJson) ) {
        	 ra.addFlashAttribute("success", "Address updated failed.");
             return "redirect:/account#address-edit";
        }
        try {
        	 ObjectMapper mapper = new ObjectMapper();
             Address addressNew = mapper.readValue(addressesJson, Address.class);
             List<Address> addressCus = checkService.extractAddresses(me);
             if(addressNew.isDefault()) {
            	 addressCus.forEach(addr -> addr.setDefault(false));
             }
             addressNew.setId(UUID.randomUUID().toString());
             addressCus.add(addressNew);
             var addresses = mapper.readValue(me.getAddresses(), Addresses.class);
             addresses.setCustomerAddresses(addressCus);
             
            
             // Lưu JSON thẳng vào cột addresses
             String json = mapper.writeValueAsString(addresses);
             me.setAddresses(json);
             me.setUpdatedAt(LocalDateTime.now());
             var a = userRepository.updateAddressCus(me);
             if(a == 1) {
            	 ra.addFlashAttribute("success", "Address updated.");
                 return "redirect:/account#address-edit";
             } 
             ra.addFlashAttribute("error", "Address updated failed.");
 	        return "redirect:/account#address-edit";
		} catch (Exception e) {
			e.printStackTrace();
			ra.addFlashAttribute("error", "Address updated failed.");
	        return "redirect:/account#address-edit";
		}
       

        
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";

        // Kiểm tra rỗng
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select an image to upload.");
            return "redirect:/account#account-info";
        }

        // Kiểm tra content type an toàn
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            ra.addFlashAttribute("error", "Invalid image file type.");
            return "redirect:/account#account-info";
        }

        // Chỉ cho phép 1 số đuôi
        String ext = getFileExt(file.getOriginalFilename());
        if (!isAllowedImageExt(ext)) {
            ra.addFlashAttribute("error", "Only JPG, JPEG, PNG, WEBP are allowed.");
            return "redirect:/account#account-info";
        }

        // Kích thước
        long size = file.getSize();
        if (size > 10 * 1024 * 1024L) { // 10MB
            ra.addFlashAttribute("error", "Image size exceeds 10MB.");
            return "redirect:/account#account-info";
        }

        // Tạo thư mục nếu chưa có
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Could not create upload directory.");
            return "redirect:/account#account-info";
        }

        // Tạo tên file ngẫu nhiên để tránh trùng
        String newName = UUID.randomUUID().toString().replace("-", "") + ext.toLowerCase();

        // Lưu file
        Path target = Paths.get(UPLOAD_DIR, newName).normalize();
        try {
            // Ngăn path traversal
            if (!target.toAbsolutePath().startsWith(Paths.get(UPLOAD_DIR).toAbsolutePath())) {
                ra.addFlashAttribute("error", "Invalid file name.");
                return "redirect:/account#account-info";
            }
            file.transferTo(target.toFile());
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Error saving image: " + e.getMessage());
            return "redirect:/account#account-info";
        }

        // Xoá ảnh cũ nếu có
        deleteQuietly(me.getImage());

        // Cập nhật DB
        me.setImage(newName);
        me.setUpdatedAt(LocalDateTime.now());
        userRepository.updateImage(me.getUserId(), newName);

        ra.addFlashAttribute("success", "Avatar updated successfully.");
        return "redirect:/account#account-info";
    }

    @PostMapping("/avatar/delete")
    public String deleteAvatar(HttpServletRequest request, RedirectAttributes ra) {
        User me = getCurrentUser(request);
        if (me == null) return "redirect:/login";

        // Xoá file vật lý
        deleteQuietly(me.getImage());

        // Xoá tham chiếu trong DB
        me.setImage(null);
        me.setUpdatedAt(LocalDateTime.now());
        userRepository.updateUser(me);

        ra.addFlashAttribute("success", "Avatar deleted successfully.");
        return "redirect:/account#account-info";
    }
    @GetMapping("/orders/list")
    public String getOrders(@RequestParam("status") String status,
    	    @RequestParam("page") int page,
    	    Model model,
    	    HttpSession session) {
    	var userId = (Integer) session.getAttribute("userId");
        int pageSize = 10; // mỗi lần load 10 đơn
        Map<String, Object> data = orderService.getAllByStatusUserId(userId, status, page, pageSize);

        List<OrderView> orders = (List<OrderView>) data.get("orders");
        Integer totalPages = (Integer) data.get("totalPages");
        Integer totalRows = (Integer) data.get("totalRows");

        // gửi về frontend cho AJAX
        Map<String, Object> response = Map.of(
                "orders", orders,
                "totalPages", totalPages,
                "totalRows", totalRows
        );
        model.addAttribute("orders", orders);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRows", totalRows);
        return "client/order/_orderCart :: orderListFragment"; // trả về fragment HTML
    }
    @GetMapping("/orders/{orderId}/tracking")
    public String trackOrder(@PathVariable(name = "orderId") String orderId,
    	    Model model,
    	    HttpSession session) {
    	var order = orderService.getOrderItemByOrderId(orderId);
    	var shipment = shipmentService.getByOrderId(orderId);
    	if(shipment != null) {
    		if(shipment.getShipperID() != null) {
    			var shipper = shipperService.getById(shipment.getShipperID());
        		if(shipper != null) {
        			model.addAttribute("shipper", shipper);
        		}
        		var history = shipperService.getShipmentHistoryByIdShipment(shipment.getShipmentID());
        		if(!history.isEmpty()) {
        			model.addAttribute("historyShipment", history);
        		}
    		}
    		model.addAttribute("shipment", shipment);
    		
    	}
    	var statusText = OrderStatus.fromCode(order.getStatus());
    	model.addAttribute("statusText", statusText);
        model.addAttribute("order", order);
        if(order.getDeliveryAddressJson() != null) {
        	var addr = order.getDeliveryAddressJson();
        	var strAddress = addr.getStreet() + ", " + addr.getWardName() + ", " 
        	+ addr.getDistrictName() + ", " + addr.getProvinceName();
        	model.addAttribute("address", strAddress);
        }
        
        return "client/order/trackOrder"; // trả về fragment HTML
    }
    @GetMapping("/orders/detail/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetail(@PathVariable("orderId") String orderId,
                                            HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        var order = orderService.getOrderItemByOrderId(orderId);
        
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Không tìm thấy đơn hàng");
        }

        if (order.getBuyerId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Không phải đơn hàng của bạn");
        }

        // Trả về đúng cấu trúc cho modal
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("items", order.getItems()); // chắc chắn là List<Item> hoặc map
        data.put("discounts", order.getDiscounts() != null ? order.getDiscounts() : Collections.emptyList());

        return ResponseEntity.ok(data);
    }
    
    

    // ===== helpers =====
    private User getCurrentUser(HttpServletRequest request) {
        // key session phải trùng với lúc login bạn set
        Integer uid = (Integer) request.getSession().getAttribute("userId");
        return uid == null ? null : userRepository.findById(uid);
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // ===== helpers dành cho ảnh =====
    private static String getFileExt(String filename) {
        if (filename == null) return "";
        String fn = filename.trim();
        int dot = fn.lastIndexOf('.');
        return (dot >= 0) ? fn.substring(dot).toLowerCase() : "";
    }

    private static boolean isAllowedImageExt(String ext) {
        return ".jpg".equalsIgnoreCase(ext)
            || ".jpeg".equalsIgnoreCase(ext)
            || ".png".equalsIgnoreCase(ext)
            || ".webp".equalsIgnoreCase(ext);
    }

    private void deleteQuietly(String fileName) {
        try {
            if (fileName == null || fileName.isBlank()) return;
            Path p = Paths.get(UPLOAD_DIR, fileName).normalize();
            if (p.toAbsolutePath().startsWith(Paths.get(UPLOAD_DIR).toAbsolutePath())) {
                Files.deleteIfExists(p);
            }
        } catch (Exception ignore) {}
    }
}
