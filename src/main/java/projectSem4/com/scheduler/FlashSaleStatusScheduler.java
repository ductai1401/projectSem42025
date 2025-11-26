package projectSem4.com.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import projectSem4.com.service.admin.FlashSaleService;

@Component
public class FlashSaleStatusScheduler {

    @Autowired
    private FlashSaleService flashSaleService;

    /**
     * Tự động cập nhật status=2 cho FlashSales và FlashSaleItems đã hết hạn.
     * Chạy mỗi 5 phút (múi giờ Việt Nam).
     */
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Ho_Chi_Minh")
    public void markExpiredFlashSales() {
        try {
            var result = flashSaleService.refreshExpiredStatusesNow();
            int sales = (int) result.getOrDefault("expiredSalesUpdated", 0);
            int items = (int) result.getOrDefault("expiredItemsUpdated", 0);
            if (sales > 0 || items > 0) {
                System.out.println("[Scheduler] ✅ FlashSales hết hạn: " + sales + " | Items: " + items);
            }
        } catch (Exception e) {
            System.err.println("[Scheduler] ❌ Lỗi cập nhật FlashSale hết hạn: " + e.getMessage());
        }
    }
}
