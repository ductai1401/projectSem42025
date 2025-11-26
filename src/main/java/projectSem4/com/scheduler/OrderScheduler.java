package projectSem4.com.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import projectSem4.com.model.repositories.OrderRepository;

@Component
public class OrderScheduler {
	@Autowired
    private OrderRepository orderRepository;

    // Chạy mỗi 10 phút
    @Scheduled(fixedRate = 600_000)
    public void autoCancelOrders() {
        int updated = orderRepository.autoCancelUnpaidOrders();
        if (updated > 0) {
            System.out.println("✅ Automatically canceled " + updated + " order more than 8 hours unpaid.");
        }
    }
}
