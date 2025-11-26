package projectSem4.com.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import projectSem4.com.model.entities.Order;
import projectSem4.com.model.entities.Payment;
import projectSem4.com.model.entities.PaymentOrder;
import projectSem4.com.model.repositories.OrderRepository;
import projectSem4.com.model.repositories.PaymentOrderRepository;
import projectSem4.com.model.repositories.PaymentRepository;

@Service
public class PaymentService {
	@Autowired
	private PaymentRepository paymentRepo;
	
	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private VNPayService vnService;
	
	@Autowired
	private PaymentOrderRepository pORepo;
	
	@Transactional
    public String createOrReusePaymentUrl(String orderId)  {
        Order order = orderRepo.getOrderById(orderId);
        
        try {
        	if(order == null) {
            	throw new IllegalArgumentException("Order không tồn tại");
            }
                    

            // Nếu order đã thanh toán thì không cho tạo link nữa
            if (order.getStatus() == 1) {
                throw new IllegalStateException("Đơn đã thanh toán thành công");
            }
            if (order.getStatus() == 4) {
                throw new IllegalStateException("Đơn đã bị hủy");
            }

            // Lấy payment mới nhất
            Payment lastPayment = paymentRepo.findTopByOrderIdOrderByCreatedAtDesc(orderId);

            if (lastPayment != null) {
                switch (lastPayment.getStatus()) {
                    case 0:
                        // Nếu link cũ chưa hết hạn thì dùng lại
                        if (lastPayment.getExpiredAt().isAfter(LocalDateTime.now())) {
                            return lastPayment.getPaymentUrl();
                        } else {
                            lastPayment.setStatus(3);
                            paymentRepo.updateStatus(lastPayment);
                        }
                        break;
                    case 1:
                        throw new IllegalStateException("Order đã thanh toán thành công");
                    case 2:
                    case 3:
                        // cho phép tạo mới
                        break;
                }
            }
        	// Tạo Payment mới
            Payment newPayment = new Payment();
            newPayment.setPaymentMethod("VNPAY");
            newPayment.setPaymentAmount(order.getTotalAmount());
            newPayment.setStatus(-1);
            newPayment.setCreatedAt(LocalDateTime.now());
            var transac = "PAY" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
            newPayment.setTransactionCode(transac);
            var a = paymentRepo.create(newPayment);
            if(a != 0) {
            	PaymentOrder pO = new PaymentOrder();
            	pO.setOrderId(orderId);
            	pO.setPaymentId(a);
            	pORepo.save(pO);
            } else {
            	throw new IllegalArgumentException("Tao url thanh toan that bai do mot so ly do, vui long thu lai sau");
            }
         // Gọi VNPay service tạo URL
		    String paymentUrl = vnService.createPaymentUrl(newPayment.getPaymentAmount(), "Thanh toán đơn #" + orderId, transac);
		    newPayment.setPaymentUrl(paymentUrl);
		    newPayment.setExpiredAt(LocalDateTime.now().plusMinutes(15)); 
		    newPayment.setStatus(0);
	        paymentRepo.updateUrl(newPayment);

	        return paymentUrl;
            
		} catch (Exception e) {
			throw new IllegalArgumentException("Tao thanh that bai do mot so ly do, vui long thu lai sau");
		}
       
    }
	
}
