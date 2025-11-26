
package projectSem4.com.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import projectSem4.com.dto.WalletStatsDTO;
import projectSem4.com.model.entities.PlatformEarning;
import projectSem4.com.model.entities.Wallet;
import projectSem4.com.model.repositories.PlatformEarningRepository;
import projectSem4.com.model.repositories.WalletRepository;

@Service
public class WalletService {
	private final WalletRepository walletRepository;
	
	@Autowired
	private PlatformEarningRepository earningRepository; 

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

 // Lấy thông tin ví của shop
    public Wallet getShopWallet(Integer shopID) {
    	var w = walletRepository.findByOwner("SHOP", shopID.longValue());
    	if(w == null) {
    		w = createDefaultWallet(shopID);
    	}
        return w;

    }
    
    // Tạo ví mặc định nếu chưa có
    private Wallet createDefaultWallet(Integer shopID) {
        Wallet wallet = new Wallet();
        wallet.setOwnerType("SHOP");
        wallet.setOwnerId(shopID);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("VND");
        wallet.setStatus(1);
        wallet.setLastUpdated(LocalDateTime.now());
        return walletRepository.create(wallet);
    }
    
    public Page<PlatformEarning> getEarnings(Integer shopID, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		int offset = page * size;
		
		List<PlatformEarning> earnings;
		int totalRecords;
		
		if (startDate != null && endDate != null) {
		// Có lọc theo ngày
		earnings = earningRepository.findEarningsByShop(shopID, startDate, endDate, offset, size);
		totalRecords = earningRepository.countEarningsByShop(shopID, startDate, endDate);
		} else {
		// Không lọc theo ngày
		earnings = earningRepository.findEarningsByShop(shopID, offset, size);
		totalRecords = earningRepository.countEarningsByShop(shopID);
		}
		
		return new PageImpl<>(earnings, pageable, totalRecords);
		}
    
    // Tính toán thống kê
    public WalletStatsDTO getWalletStats(Integer shopID, LocalDateTime startDate, 
                                         LocalDateTime endDate) {
        BigDecimal totalBase;
        BigDecimal totalCommission;
        BigDecimal totalNetIncome;
        
        if (startDate != null && endDate != null) {
            totalBase = earningRepository.sumBaseAmountByShopIDAndDateRange(
                shopID, startDate, endDate
            );
            totalCommission = earningRepository.sumCommissionByShopIDAndDateRange(
                shopID, startDate, endDate
            );
            totalNetIncome = earningRepository.sumShopNetIncomeByShopIDAndDateRange(
                shopID, startDate, endDate
            );
        } else {
            totalBase = earningRepository.sumBaseAmountByShopID(shopID);
            totalCommission = earningRepository.sumCommissionByShopID(shopID);
            totalNetIncome = earningRepository.sumShopNetIncomeByShopID(shopID);
        }
        
        Integer totalOrders = earningRepository.countOrdersByShopID(shopID);
        var wSDTO = new WalletStatsDTO();
       
        wSDTO.setTotalBaseAmount(totalBase != null ? totalBase : BigDecimal.ZERO);
        wSDTO.setTotalCommission(totalCommission != null ? totalCommission : BigDecimal.ZERO);
        wSDTO.setTotalShopNetIncome(totalNetIncome != null ? totalNetIncome : BigDecimal.ZERO);
        wSDTO.setTotalOrders(totalOrders != null ? totalOrders : 0);
        		
         return wSDTO;
    }
    
    // Xử lý rút tiền
    public boolean processWithdraw(Integer shopID, BigDecimal amount, String bankCode,
                                  String accountNumber, String accountName, String note) {
        Wallet wallet = getShopWallet(shopID);
        
        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ");
        }
        
        if (amount.compareTo(new BigDecimal("50000")) < 0) {
            throw new IllegalArgumentException("Số tiền rút tối thiểu 50,000đ");
        }
        
        // Trừ số dư
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setLastUpdated(LocalDateTime.now());
        walletRepository.save(wallet);
        
        // TODO: Tạo bản ghi rút tiền, gửi request đến hệ thống thanh toán
        
        return true;
    }
}
