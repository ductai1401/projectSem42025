package projectSem4.com.model.repositories;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.Wallet;
import projectSem4.com.service.TokenDeviceService;
import projectSem4.com.model.entities.Wallet;

@Repository
public class WalletRepository {

    private final TokenDeviceService tokenDeviceService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

    WalletRepository(TokenDeviceService tokenDeviceService) {
        this.tokenDeviceService = tokenDeviceService;
    }

	@PostConstruct
	public void logSingletonInstance() {
		System.out.println("✅ Wallet initialized. hashCode: " + System.identityHashCode(this));
	}

	// RowMapper cho Wallet
	private RowMapper<Wallet> rowMapperForWallet = new RowMapper<Wallet>() {
		@Override
		public Wallet mapRow(ResultSet rs, int rowNum) throws SQLException {
			Integer WalletID = rs.getInt("WalletID");
			String OwnerType = rs.getString("OwnerType");
			Integer OwnerID = rs.getInt("OwnerID");
			BigDecimal Balance = rs.getBigDecimal("Balance");
			String Currency = rs.getString("Currency");
			Integer status = rs.getInt("Status");
			LocalDateTime LastUpdated = rs.getTimestamp("LastUpdated").toLocalDateTime();

			// Map dữ liệu và tạo đối tượng Wallet
			Wallet Wallet = new Wallet(WalletID,OwnerType,OwnerID,Balance,Currency,LastUpdated,
					status
					);
			
			return Wallet;
		}
	};
	
	// Find by ID
    public Wallet findById(int walletId) {
    	try {
    		 String sql = "SELECT * FROM Wallets WHERE WalletID = ?";
    	        List<Wallet> result = jdbcTemplate.query(sql, rowMapperForWallet, walletId);
    	        return result.isEmpty() ? result.get(0) : null;
		} catch (Exception e) {
			return null;
		}
       
    }

    // Find by Owner
    public Wallet findByOwner(String ownerType, int ownerId) {
    	try {
    		  String sql = "SELECT * FROM Wallets WHERE OwnerType = ? AND OwnerID = ?";
    		  var r = jdbcTemplate.query(sql, rowMapperForWallet, ownerType, ownerId);
    	        return r.isEmpty() ? null : r.get(0); 
		} catch (Exception e) {
			return null;
		}
      
    }

    // Update Status
    public int updateStatus(int walletId, String status) {
    	try {
    		String sql = "UPDATE Wallets SET Status = ?, LastUpdated = SYSDATETIME() WHERE WalletID = ?";
            return jdbcTemplate.update(sql, status, walletId);
		} catch (Exception e) {
			return 0;
		}
        
    }

    // Delete
    public int delete(int walletId) {
    	try {
    		String sql = "DELETE FROM Wallets WHERE WalletID = ?";
            return jdbcTemplate.update(sql, walletId);
		} catch (Exception e) {
			return 0;
		}
        
    }

    // Get all wallets
    public List<Wallet> findAll() {
    	try {
    		  String sql = "SELECT * FROM Wallets";
    	        return jdbcTemplate.query(sql, rowMapperForWallet);
		} catch (Exception e) {
			return null;
		}
      
    }
 // 🔹 Lấy ví theo loại và ID chủ sở hữu
    public Wallet findByOwner(String ownerType, long ownerId) {
        String sql = "SELECT * FROM Wallets WHERE OwnerType = ? AND OwnerID = ?";
        List<Wallet> wallets = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Wallet.class), ownerType, ownerId);
        return wallets.isEmpty() ? null : wallets.get(0);
    }

    // 🔹 Cập nhật số dư ví
    public int updateBalance(long walletId, BigDecimal amount) {
        String sql = "UPDATE Wallets SET Balance = Balance + ?, LastUpdated = SYSDATETIME() WHERE WalletID = ?";
        return jdbcTemplate.update(sql, amount, walletId);
    }

    // 🔹 Cập nhật trạng thái ví (1: hoạt động, 0: khóa)
    public int updateStatus(long walletId, int status) {
        String sql = "UPDATE Wallets SET Status = ?, LastUpdated = SYSDATETIME() WHERE WalletID = ?";
        return jdbcTemplate.update(sql, status, walletId);
    }

    // 🔹 Lấy thông tin ví theo ID
    public Wallet findById(long walletId) {
        String sql = "SELECT * FROM Wallets WHERE WalletID = ?";
        List<Wallet> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Wallet.class), walletId);
        return list.isEmpty() ? null : list.get(0);
    }
 // ✅ Tạo ví mới
    public Wallet create(Wallet wallet) {
        String sql = """
            INSERT INTO Wallets (OwnerType, OwnerID, Balance, Currency, Status, LastUpdated)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, wallet.getOwnerType(), wallet.getOwnerId(),
                wallet.getBalance(), wallet.getCurrency(),
                wallet.getStatus(), Timestamp.valueOf(wallet.getLastUpdated()));
        return findByOwner(wallet.getOwnerType(), wallet.getOwnerId());
    }

    // ✅ Cập nhật ví
    public int save(Wallet wallet) {
        String sql = """
            UPDATE Wallets 
            SET Balance = ?, Status = ?, LastUpdated = ? 
            WHERE WalletID = ?
        """;
        return jdbcTemplate.update(sql,
                wallet.getBalance(),
                wallet.getStatus(),
                Timestamp.valueOf(wallet.getLastUpdated()),
                wallet.getWalletId());
    }

    // ✅ Cập nhật số dư nhanh
    public int updateBalance(int walletId, BigDecimal delta) {
        String sql = """
            UPDATE Wallets 
            SET Balance = Balance + ?, LastUpdated = GETDATE()
            WHERE WalletID = ?
        """;
        return jdbcTemplate.update(sql, delta, walletId);
    }

    // ✅ Kiểm tra tồn tại
    public boolean existsByOwner(String ownerType, int ownerId) {
        String sql = "SELECT COUNT(*) FROM Wallets WHERE OwnerType=? AND OwnerID=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ownerType, ownerId);
        return count != null && count > 0;
    }

    // 🔹 Tạo yêu cầu rút tiền
//    public int createWithdrawRequest(WithdrawRequest request) {
//        String sql = """
//            INSERT INTO WithdrawRequests (WalletID, Amount, BankCode, AccountNumber, AccountName, Note, Status, CreatedAt)
//            VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
//        """;
//        return jdbcTemplate.update(sql, request.getWalletID(), request.getAmount(),
//                request.getBankCode(), request.getAccountNumber(), request.getAccountName(),
//                request.getNote(), request.getStatus());
//    }
//
//    // 🔹 Lấy danh sách yêu cầu rút tiền của ví
//    public List<WithdrawRequest> findWithdrawRequests(long walletId) {
//        String sql = "SELECT * FROM WithdrawRequests WHERE WalletID = ? ORDER BY CreatedAt DESC";
//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(WithdrawRequest.class), walletId);
//    }
}
