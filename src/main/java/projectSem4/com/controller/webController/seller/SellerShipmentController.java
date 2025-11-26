package projectSem4.com.controller.webController.seller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import projectSem4.com.model.entities.Shipment;
import projectSem4.com.service.seller.ShipmentService;

@Controller
@RequestMapping("seller/shipment")
public class SellerShipmentController {
	@Autowired
    private ShipmentService shipmentService;

    /**
     * GET /seller/shipments
     * Hiển thị danh sách vận chuyển của seller
     */
//    @GetMapping
//    public String getAllShipments(Model model,
//                                  @RequestParam(value = "status", required = false) Integer status,
//                                  @RequestParam(value = "keyword", required = false) String keyword) {
//        try {
//            model.addAttribute("shipments", shipmentService.getAllShipmentsForSeller(status, keyword));
//            model.addAttribute("totalShipments", shipmentService.countTotalForSeller());
//            model.addAttribute("inTransit", shipmentService.countInTransit());
//            model.addAttribute("completed", shipmentService.countCompleted());
//            model.addAttribute("cancelled", shipmentService.countCancelled());
//            return "seller/shipment/index";
//        } catch (Exception e) {
//            model.addAttribute("error", "Failed to load shipments: " + e.getMessage());
//            return "error/500";
//        }
//    }

    /**
     * GET /seller/shipments/{id}
     * Hiển thị chi tiết shipment (trang HTML)
     */
//    @GetMapping("/{id}")
//    public String getShipmentDetailPage(@PathVariable("id") String shipmentId, Model model) {
//        try {
//            ShipmentDetailDTO shipment = shipmentService.getShipmentDetail(shipmentId);
//            model.addAttribute("shipment", shipment);
//            model.addAttribute("shop", shipment.getShop());
//            model.addAttribute("orderItems", shipment.getOrderItems());
//            model.addAttribute("shipmentHistory", shipment.getHistory());
//            return "seller/shipment/detail";
//        } catch (Exception e) {
//            model.addAttribute("error", "Shipment not found: " + e.getMessage());
//            return "error/404";
//        }
//    }

    

    /**
     * GET /seller/shipments/{id}/detail
     * API JSON cho AJAX / API client
     */
	/*
	 * @GetMapping("/{id}/detail")
	 * 
	 * @ResponseBody
	 */
    /*public ResponseEntity<?> getShipmentDetailApi(@PathVariable("id") String shipmentId) {
        try {
            ShipmentDetailDTO shipment = shipmentService.getShipmentDetail(shipmentId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "shipment", shipment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }*/
}
