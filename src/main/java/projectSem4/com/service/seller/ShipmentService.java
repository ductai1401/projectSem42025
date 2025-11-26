package projectSem4.com.service.seller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import projectSem4.com.model.entities.Payment;
import projectSem4.com.model.entities.RefundRequest;
import projectSem4.com.model.entities.Shipment;
import projectSem4.com.model.enums.ShipmentStatus;
import projectSem4.com.model.repositories.OrderItemRepository;
import projectSem4.com.model.repositories.OrderRepository;
import projectSem4.com.model.repositories.RefundRequestRepository;
import projectSem4.com.model.repositories.ShipmentRepository;
import projectSem4.com.model.repositories.ShipperRepository;
import projectSem4.com.model.repositories.ShopRepository;
import projectSem4.com.service.RefundService;

@Service
public class ShipmentService {
	@Autowired
    private ShipmentRepository shipmentRepo;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private ShipperRepository shipperRepo;
    
    @Autowired
    private OrderRepository orderRepo;
    
    @Autowired
    private RefundRequestRepository rRRepo;
    
    @Autowired
    private RefundService refundService;
    
    /**
     * Lấy danh sách shipment cho seller
     */
//    public List<ShipmentDTO> getAllShipmentsForSeller(Integer status, String keyword) {
//        return shipmentRepository.findAllForSeller(status, keyword);
//    }
//    
    

    /**
     * Đếm thống kê (ví dụ dùng trong dashboard)
     */
//    public long countTotalForSeller() {
//        return shipmentRepository.countAll();
//    }
//
//    public long countInTransit() {
//        return shipmentRepository.countByStatus("IN_TRANSIT");
//    }
//
//    public long countCompleted() {
//        return shipmentRepository.countByStatus("COMPLETED");
//    }
//
//    public long countCancelled() {
//        return shipmentRepository.countByStatus("CANCELLED");
//    }

    /**
     * Lấy chi tiết shipment
     */
//    public Shipment getShipmentDetail(String shipmentId) {
//        Shipment shipment = shipmentRepo.findById(shipmentId);
//        if (shipment == null) {
//            throw new RuntimeException("Shipment not found");
//        }
//
//        ShipmentDetailDTO dto = new ShipmentDetailDTO();
//        dto.setShipment(shipment);
//        dto.setShop(shopRepository.findById(shipment.getShopId()));
//        dto.setOrderItems(orderItemRepository.findByShipmentId(shipmentId));
//        dto.setHistory(logisticRepository.getLogs(shipmentId));
//        return dto;
//    }
    public Shipment getShipmentDetailByShipper(int shipperId) {
        Shipment shipment = shipmentRepo.findByIdShipper(shipperId);
        if (shipment == null) {
        	shipment = null;
        } 
        return shipment;
    }

    /**
     * Seller xác nhận chuẩn bị hàng
     */
    public boolean prepareShipment(String shipmentId, String notes) {
        Shipment shipment = shipmentRepo.findById(shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Shipment not found");
        }

        // Kiểm tra trạng thái hợp lệ
        if (!(shipment.getStatus() == 0)) {
            throw new RuntimeException("Shipment cannot be prepared from status: " + shipment.getStatus());
        }

        // Cập nhật trạng thái
        shipment.setStatus(1);
        shipment.setNotes(notes);
        shipmentRepo.update(shipment);


        return true;
    }
    
    /**
     * Shipper accept shipment
     */
    
    public boolean acceptShipment(String shipmentId, int shipperId) {
    	
    	
        Shipment shipment = shipmentRepo.findById(shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Shipment not found");
        }
        var shipper = shipperRepo.findById(shipperId);
        if(shipper == null) {
        	 throw new RuntimeException("Shipper not found");
        }
        if(!shipper.isAvailable()) {
       	 throw new RuntimeException("You need to complete the current order in order to receive the next one.");
       }
        // Kiểm tra trạng thái hợp lệ
        if (!(shipment.getStatus() == 1)) {
            throw new RuntimeException("Shipment cannot be handed over from status: " + ShipmentStatus.fromValue(shipment.getStatus()).getLabel());
        }

        // Cập nhật trạng thái
        shipment.setStatus(ShipmentStatus.ASSIGNED.getValue());
        shipment.setAssignedAt(LocalDateTime.now());
        shipment.setShipperID(shipperId);
        var a = shipmentRepo.accept(shipment);

        return true;
    }

    /**
     * Seller bàn giao hàng cho shipper
     */
    @Transactional
    public void updateStatusByShipper(Shipment data) {
        // =============================
        // VALIDATION
        // =============================
        
        // Tìm shipment
        Shipment shipment = shipmentRepo.findById(data.getShipmentID());
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found: " + data.getShipmentID());
        }
        
        // Tìm shipper
        var shipper = shipperRepo.findById(data.getShipperID());
        if (shipper == null) {
            throw new IllegalArgumentException("Shipper not found: " + data.getShipperID());
        }
        
        // Kiểm tra quyền sở hữu
        if (shipper.getShipperID() != shipment.getShipperID()) {
            throw new IllegalStateException("This shipment does not belong to shipper: " + data.getShipperID());
        }
        
        // Kiểm tra trạng thái có thay đổi không
        if (data.getStatus() == shipment.getStatus()) {
            throw new IllegalStateException("Status is already " + data.getStatus() + ", no update needed");
        }
        
        // Lấy description của status
        String description;
        try {
            description = ShipmentStatus.fromValue(data.getStatus()).getDescription();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid shipment status: " + data.getStatus(), e);
        }
        
        // =============================
        // XỬ LÝ THEO STATUS
        // =============================
        
        switch (data.getStatus()) {
            case 3 -> handleStatusPickedUp(shipment, description);
            case 4 -> handleStatusDelivered(shipment, description);
            case 5 -> handleStatusInTransit(shipment, description);
            case 6 -> handleStatusReturned(shipment, description);
            default -> throw new IllegalArgumentException("Unsupported status transition: " + data.getStatus());
        }
    }

    // =============================
    // HANDLER METHODS
    // =============================

    /**
     * Status 3: Đã lấy hàng (PICKED_UP)
     * Chỉ chuyển từ status 2 -> 3
     */
    private void handleStatusPickedUp(Shipment shipment, String description) {
        if (shipment.getStatus() != 2) {
            throw new IllegalStateException(
                "Cannot transition from status " + shipment.getStatus() + " to 3 (PICKED_UP)"
            );
        }
        
        // Cập nhật status shipment
        shipment.setStatus(3);
        boolean updated = shipmentRepo.updateStatus(shipment, description);
        if (!updated) {
            throw new RuntimeException("Failed to update shipment status to PICKED_UP");
        }
        
        // Cập nhật order hoặc refund
        if (shipment.getOrderID() != null) {
            // Đơn hàng thông thường
            int result = orderRepo.updateStatusInven(3, shipment.getOrderID(), null);
            if (result <= 0) {
                throw new RuntimeException("Failed to update order status for order: " + shipment.getOrderID());
            }
        } else if (shipment.getRefundID() != null) {
            // Đơn hoàn trả
            boolean result = rRRepo.updateStatusByBuyer(shipment.getRefundID(), "RETURNING");
            if (!result) {
                throw new RuntimeException("Failed to update refund status to RETURNING for refund: " + shipment.getRefundID());
            }
        } else {
            throw new IllegalStateException("Shipment has neither OrderID nor RefundID");
        }
    }

    /**
     * Status 4: Đã giao hàng (DELIVERED)
     * Chỉ chuyển từ status 3 -> 4
     */
    private void handleStatusDelivered(Shipment shipment, String description) {
        if (shipment.getStatus() != 3) {
            throw new IllegalStateException(
                "Cannot transition from status " + shipment.getStatus() + " to 4 (DELIVERED)"
            );
        }
        
        // Cập nhật status shipment
        shipment.setStatus(4);
        boolean updated = shipmentRepo.updateStatus(shipment, description);
        if (!updated) {
            throw new RuntimeException("Failed to update shipment status to DELIVERED");
        }
        
        // Cập nhật order hoặc refund
        if (shipment.getOrderID() != null) {
            // Đơn hàng thông thường - giao thành công
            handleOrderDelivered(shipment.getOrderID());
        } else if (shipment.getRefundID() != null) {
            // Đơn hoàn trả - shop nhận được hàng
            handleRefundDelivered(shipment.getRefundID());
        } else {
            throw new IllegalStateException("Shipment has neither OrderID nor RefundID");
        }
    }

    /**
     * Status 5: Đang vận chuyển (IN_TRANSIT)
     * Chỉ chuyển từ status 3 -> 5
     */
    private void handleStatusInTransit(Shipment shipment, String description) {
        if (shipment.getStatus() != 3) {
            throw new IllegalStateException(
                "Cannot transition from status " + shipment.getStatus() + " to 5 (IN_TRANSIT)"
            );
        }
        
        // Cập nhật status shipment
        shipment.setStatus(5);
        boolean updated = shipmentRepo.updateStatus(shipment, description);
        if (!updated) {
            throw new RuntimeException("Failed to update shipment status to IN_TRANSIT");
        }
    }

    /**
     * Status 6: Đã hoàn trả (RETURNED)
     * Chỉ chuyển từ status 5 -> 6
     */
    private void handleStatusReturned(Shipment shipment, String description) {
        if (shipment.getStatus() != 5) {
            throw new IllegalStateException(
                "Cannot transition from status " + shipment.getStatus() + " to 6 (RETURNED)"
            );
        }
        
        // Cập nhật status shipment
        shipment.setStatus(6);
        boolean updated = shipmentRepo.updateStatus(shipment, description);
        if (!updated) {
            throw new RuntimeException("Failed to update shipment status to RETURNED");
        }
    }

    // =============================
    // HELPER METHODS
    // =============================

    /**
     * Xử lý khi đơn hàng được giao thành công
     */
    private void handleOrderDelivered(String orderId) {
        int result = orderRepo.updateStatusInven(5, orderId, null);
        if (result <= 0) {
            throw new RuntimeException("Failed to update order status to DELIVERED for order: " + orderId);
        }
    }

    /**
     * Xử lý khi shop nhận được hàng hoàn trả
     */
    private void handleRefundDelivered(int refundId) {
        // Lấy thông tin refund
        var refund = rRRepo.getRefundById(refundId);
        if (refund == null) {
            throw new IllegalStateException("Refund not found: " + refundId);
        }
        
        // Xác định status tiếp theo dựa trên phương thức hoàn tiền
        String newStatus;
        if ("VNPAY".equals(refund.getRefundMethod())) {
            newStatus = "PROCESSING_REFUND";
        } else if ("COD".equals(refund.getRefundMethod())) {
            newStatus = "REFUNDED";
        } else {
            throw new IllegalStateException("Unknown refund method: " + refund.getRefundMethod());
        }
        
        // Cập nhật status refund
        boolean updated = rRRepo.updateStatusByBuyer(refundId, newStatus);
        if (!updated) {
            throw new RuntimeException("Failed to update refund status to " + newStatus + " for refund: " + refundId);
        }
        
        // Xử lý sau khi refund (tính lại discount, inventory, etc.)
        var refundRequest = new RefundRequest();
        refundRequest.setRefundType(refund.getRefundType());
        refundRequest.setRefundId(refund.getRefundId());
        refundRequest.setOrderId(refund.getOrderId());
        
        refundService.handleAfterRefunded(refundRequest);
    }
    
    public List<Shipment> getAllByStatusPending() {
    	return shipmentRepo.findAllStatusPending();
    }
    
    public Shipment getByOrderId(String orderId) {
    	if(orderId == "") {
    		return null;
    	}
    	
    	return shipmentRepo.findByOrderId(orderId);
    } 
    
}
