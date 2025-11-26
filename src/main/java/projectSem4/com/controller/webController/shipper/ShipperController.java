package projectSem4.com.controller.webController.shipper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import projectSem4.com.model.entities.Shipment;
import projectSem4.com.service.RefundService;
import projectSem4.com.service.admin.ShopService;
import projectSem4.com.service.client.OrderService;
import projectSem4.com.service.seller.ShipmentService;
import projectSem4.com.service.shipper.ShipperService;

@Controller
@RequestMapping("shipper")
public class ShipperController {
	
	@Autowired
	private ShipmentService shipmentService;
	
	@Autowired
	private ShipperService shipperService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private ShopService shopService;
	
	@Autowired
	private RefundService refundService;
	
	
	@GetMapping("/shipments")
	public String getAllShipment(Model model, HttpSession session) {
		try {
			var shipperId = (Integer) session.getAttribute("shipperId");
        	if(shipperId == null) {
        		model.addAttribute("error", "Shipper not found");
                return "/login";
        	}
        	var shipper = shipperService.getById(shipperId);
        	if(shipper != null) {
            	model.addAttribute("shipper", shipper);
        	} else {
        		model.addAttribute("error", "Shipper not found");
                return "/login";
        	}
        	var shipments = shipmentService.getAllByStatusPending();
        	if(shipments != null) {
        		model.addAttribute("availableShipments", shipments);
        	}
        	
        	
        	return "shipper/shipment_list";
		} catch (Exception e) {
			return "/login";
		}
	    
	}
	@GetMapping("/my_order")
	public String getShipment(Model model, HttpSession session) {
		try {
			var shipperId = (Integer) session.getAttribute("shipperId");
        	if(shipperId == null) {
        		model.addAttribute("error", "Shipper not found");
                return "/login";
        	}
        	var shipper = shipperService.getById(shipperId);
        	if(shipper != null) {
            	model.addAttribute("shipper", shipper);
        	} else {
        		model.addAttribute("error", "Shipper not found");
                return "redirect:/login";
        	}
        	var currentShipment = shipmentService.getShipmentDetailByShipper(shipperId);
        	if(currentShipment != null) {
        		model.addAttribute("currentShipment", currentShipment);
        	} else {
        		return "redirect:/shipper/shipments";
        	}
        	
        	if(!(currentShipment.getOrderID() == null)) {
        		var order = orderService.getOrderItemByShipmentId(currentShipment.getShipmentID());
            	if(order != null) {
            		model.addAttribute("order",order);
            		
            	}
            	var shop = shopService.getShopById(order.getShopId());
            	if(shop != null) {
            		model.addAttribute("shop",shop);
            		
            	}
        	} else {
        		var refund = refundService.getRefundViewByID(currentShipment.getRefundID());
        		if(refund != null) {
            		model.addAttribute("refund",refund);
            		
            	}
            	var shop = shopService.getShopById(refund.getShopId());
            	if(shop != null) {
            		model.addAttribute("shop",shop);
            		
            	}
            	var refundItem = refundService.getRefundItemViewByRefundId(refund.getRefundId());
            	
            	if(refundItem != null) {
            		model.addAttribute("rItems",refundItem);
            		
            	}
        	}
        	
        	
        	var sh = shipperService.getShipmentHistoryByIdShipment(currentShipment.getShipmentID());
        	if(sh != null) {
        		model.addAttribute("shipmentHistory",sh);
        		
        	}
        	var textSt = getStatusText(currentShipment.getStatus());
        	var classSt = getStatusClass(currentShipment.getStatus());
        	model.addAttribute("statusclass", classSt);
        	model.addAttribute("statusText", textSt);
        	
        	return "shipper/my_order";
		} catch (Exception e) {
			return "redirect:/login";
		}
	    
	}

	
	/**
     * POST /shipper/shipments/{id}/accept
     * Shipper nhan don hang
     */
    @PostMapping("shipments/{id}/accept")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptShipment(
            @PathVariable("id") String shipmentId,
            
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer shipperId = (Integer) session.getAttribute("shipperId");
            if (shipperId == null) {
                response.put("status", "ERROR");
                response.put("message", "Shipper not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            boolean success = shipmentService.acceptShipment(shipmentId, shipperId);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Shipment accepted successfully");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Failed to confirm shipment: " + shipmentId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Exception: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * POST /seller/shipments/{id}/handover
     * Seller xác nhận bàn giao hàng cho shipper
     */
    @PostMapping("shipments/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatusByShipper(
            @PathVariable("id") String shipmentId,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        
        Map<String, Object> res = new HashMap<>();
        
        try {
            // =============================
            // VALIDATION
            // =============================
            
            // Kiểm tra shipperId trong session
            var shipperId = (Integer) session.getAttribute("shipperId");
            if (shipperId == null) {
                res.put("success", false);
                res.put("message", "Shipper not found in session. Please login again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }
            
            // Kiểm tra request body
            if (body == null || !body.containsKey("status")) {
                res.put("success", false);
                res.put("message", "Status is required");
                return ResponseEntity.badRequest().body(res);
            }
            
            // Parse status
            Integer status;
            try {
                status = Integer.parseInt(body.get("status").toString());
            } catch (NumberFormatException e) {
                res.put("success", false);
                res.put("message", "Invalid status format");
                return ResponseEntity.badRequest().body(res);
            }
            
            if (status == null || status == 0) {
                res.put("success", false);
                res.put("message", "Status is required and cannot be 0");
                return ResponseEntity.badRequest().body(res);
            }
            
            // =============================
            // CALL SERVICE
            // =============================
            
            var shipment = new Shipment();
            shipment.setShipperID(shipperId);
            shipment.setStatus(status);
            shipment.setShipmentID(shipmentId);
            
            // Service sẽ throw exception nếu có lỗi
            shipmentService.updateStatusByShipper(shipment);
            
            // Success response
            res.put("success", true);
            res.put("message", "Shipment status updated successfully");
            return ResponseEntity.ok(res);
            
        } catch (IllegalArgumentException e) {
            // Validation errors (shipment not found, shipper not found, etc.)
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
            
        } catch (IllegalStateException e) {
            // Business logic errors (invalid state transition, ownership, etc.)
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
            
        } catch (RuntimeException e) {
            // Database/system errors
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "Failed to update shipment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
            
        } catch (Exception e) {
            // Unexpected errors
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "System error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }
    
 // Helper methods
    private String getStatusClass(int status) {
        switch (status) {
            case 2: return "bg-info";
            case 3: return "bg-warning";
            case 4: return "bg-primary";
            case 5: return "bg-primary";
            case 6: return "bg-success";
            case 7: return "bg-danger";
            default: return "bg-secondary";
        }
    }

    private String getStatusText(int status) {
        switch (status) {
            case 2: return "Shipper Assigned";
            case 3: return "Picking Up";
            case 4: return "Picked Up";
            case 5: return "Delivering";
            case 6: return "Delivered";
            case 7: return "Failed";
            case 8: return "Cancelled";
            default: return "Unknown";
        }
    }

}
