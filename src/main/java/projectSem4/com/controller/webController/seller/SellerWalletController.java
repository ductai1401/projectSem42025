package projectSem4.com.controller.webController.seller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import projectSem4.com.dto.WalletStatsDTO;
import projectSem4.com.model.entities.PlatformEarning;
import projectSem4.com.model.entities.Shop;
import projectSem4.com.model.entities.Wallet;
import projectSem4.com.model.repositories.UserRepository;
import projectSem4.com.service.PlatformEarningService;
import projectSem4.com.service.WalletService;
import projectSem4.com.service.admin.ShopService;
import projectSem4.com.service.admin.UserService;

@Controller
@RequestMapping("seller/wallet")

public class SellerWalletController {
	@Autowired
	private ShopService shopService;
	
	@Autowired
	private WalletService walletService;
	
	@Autowired
	private PlatformEarningService pEService;
	@Autowired
	private UserService userService; 
	@GetMapping("")
    public String walletDashboard(
    		@RequestParam(name = "startDate",required = false) String startDate,
    	    @RequestParam(name = "endDate",required = false) String endDate,
    	    @RequestParam(name = "page", defaultValue = "0") int page,
        HttpSession session,
        Model model
    ) {
        // Lấy shopID từ session (giả sử đã login)
        Integer shopID = (Integer) session.getAttribute("shopId");
        if (shopID == null) {
            return "redirect:/login";
        }
        int size = 10;
     // 2. Convert String → LocalDate (an toàn)
        LocalDate start = (startDate == null || startDate.isBlank())
                ? null
                : LocalDate.parse(startDate);

        LocalDate end = (endDate == null || endDate.isBlank())
                ? null
                : LocalDate.parse(endDate);
     // 3. Convert LocalDate → java.sql.Date (nếu SP yêu cầu)
        java.sql.Date sDate = (start != null) ? java.sql.Date.valueOf(start) : null;
        java.sql.Date eDate = (end != null) ? java.sql.Date.valueOf(end) : null;

        // 4. Lấy thông tin ví
        Wallet wallet = walletService.getShopWallet(shopID);
        model.addAttribute("wallet", wallet);

        // 5. Lấy earnings
        var earningsPage = pEService.getByShopFillter(shopID, sDate, eDate, page, size);
        model.addAllAttributes(earningsPage);

        // 6. Lấy thống kê ví
        LocalDateTime startDT = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime endDT = (end != null) ? end.atTime(23, 59, 59) : null;

        WalletStatsDTO stats = walletService.getWalletStats(shopID, startDT, endDT);
        model.addAttribute("stats", stats);
     // Giữ lại param trên UI
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "seller/wallet/index";
    }
    
    @PostMapping("/withdraw")
    public String withdraw(
        @RequestParam BigDecimal amount,
        @RequestParam String bankCode,
        @RequestParam String accountNumber,
        @RequestParam String accountName,
        @RequestParam(required = false) String note,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Integer shopID = (Integer) session.getAttribute("shopID");
            if (shopID == null) {
                return "redirect:/login";
            }
            
            walletService.processWithdraw(shopID, amount, bankCode, 
                                         accountNumber, accountName, note);
            
            redirectAttributes.addFlashAttribute("success", 
                "Yêu cầu rút tiền đã được gửi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/seller/wallet";
    }
    

    /**
     * Xem chi tiết giao dịch
     */
//    @GetMapping("/transaction/{id}")
//    public String viewTransactionDetail(@PathVariable Long id, 
//                                       @AuthenticationPrincipal User currentUser,
//                                       Model model) {
//        
//        Transaction transaction = transactionService.findById(id);
//        if (transaction == null) {
//            return "redirect:/seller/wallet";
//        }
//        
//        // Kiểm tra quyền truy cập
//        Shop shop = shopService.findByUserId(currentUser.getUserID());
//        Wallet wallet = walletService.getWalletByOwner("SHOP", shop.getShopID());
//        
//        if (!transaction.getWalletID().equals(wallet.getWalletID())) {
//            return "redirect:/seller/wallet";
//        }
//        
//        model.addAttribute("transaction", transaction);
//        return "seller/transaction-detail";
//    }
//    
//    /**
//     * API: Lấy số dư ví hiện tại
//     */
//    @GetMapping("/balance")
//    @ResponseBody
//    public BigDecimal getWalletBalance(@AuthenticationPrincipal User currentUser) {
//        Shop shop = shopService.findByUserId(currentUser.getUserID());
//        Wallet wallet = walletService.getWalletByOwner("SHOP", shop.getShopID());
//        return wallet != null ? wallet.getBalance() : BigDecimal.ZERO;
//    }
    
    /**
     * Xem tất cả giao dịch với phân trang
     */
//    @GetMapping("/transactions")
//    public String viewAllTransactions(@AuthenticationPrincipal User currentUser,
//                                     @RequestParam(defaultValue = "0") int page,
//                                     @RequestParam(defaultValue = "20") int size,
//                                     @RequestParam(required = false) String type,
//                                     @RequestParam(required = false) String startDate,
//                                     @RequestParam(required = false) String endDate,
//                                     Model model) {
//        
//        Shop shop = shopService.findByUserId(currentUser.getUserID());
//        Wallet wallet = walletService.getWalletByOwner("SHOP", shop.getShopID());
//        
//        // Lấy danh sách giao dịch có phân trang
//        Page<Transaction> transactionPage = transactionService.getTransactionsPaginated(
//            wallet.getWalletID(), type, startDate, endDate, page, size);
//        
//        model.addAttribute("transactions", transactionPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", transactionPage.getTotalPages());
//        model.addAttribute("totalItems", transactionPage.getTotalElements());
//        model.addAttribute("wallet", wallet);
//        
//        return "seller/transactions";
//    }
    
    /**
     * Xuất báo cáo giao dịch
     */
//    @GetMapping("/export")
//    public void exportTransactions(@AuthenticationPrincipal User currentUser,
//                                   @RequestParam(required = false) String startDate,
//                                   @RequestParam(required = false) String endDate,
//                                   @RequestParam(defaultValue = "excel") String format,
//                                   HttpServletResponse response) {
//        
//        try {
//            Shop shop = shopService.findByUserId(currentUser.getUserID());
//            Wallet wallet = walletService.getWalletByOwner("SHOP", shop.getShopID());
//            
//            List<Transaction> transactions = transactionService.getFilteredTransactions(
//                wallet.getWalletID(), null, startDate, endDate);
//            
//            if ("excel".equals(format)) {
//                exportToExcel(transactions, response);
//            } else if ("pdf".equals(format)) {
//                exportToPDF(transactions, response);
//            }
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
//    private void exportToExcel(List<Transaction> transactions, HttpServletResponse response) 
//            throws IOException {
//        response.setContentType("application/vnd.ms-excel");
//        response.setHeader("Content-Disposition", 
//            "attachment; filename=transactions_" + LocalDateTime.now().format(
//                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
//        
//        // Logic xuất Excel
//        // Sử dụng Apache POI hoặc thư viện tương tự
//    }
//    
//    private void exportToPDF(List<Transaction> transactions, HttpServletResponse response) 
//            throws IOException {
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", 
//            "attachment; filename=transactions_" + LocalDateTime.now().format(
//                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
//        
//        // Logic xuất PDF
//        // Sử dụng iText hoặc thư viện tương tự
//    }
}
