package projectSem4.com.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet {
	private Integer walletId; 
	private String ownerType;
	private Integer ownerId;
	private BigDecimal balance;
	private String currency;
	private LocalDateTime lastUpdated;
	private Integer status; 
	
	public Wallet() {}
	
	
	public Wallet(Integer walletId, String ownerType, Integer ownerId, BigDecimal balance, String currency,
			LocalDateTime lastUpdated, Integer status) {
		super();
		this.walletId = walletId;
		this.ownerType = ownerType;
		this.ownerId = ownerId;
		this.balance = balance;
		this.currency = currency;
		this.lastUpdated = lastUpdated;
		this.status = status;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Integer getWalletId() {
		return walletId;
	}
	public void setWalletId(Integer walletId) {
		this.walletId = walletId;
	}
	public String getOwnerType() {
		return ownerType;
	}
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	public Integer getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	@Override
	public String toString() {
		return "Wallet [walletId=" + walletId + ", ownerType=" + ownerType + ", ownerId=" + ownerId + ", balance="
				+ balance + ", currency=" + currency + ", lastUpdated=" + lastUpdated + "]";
	}
	
	
	
}
