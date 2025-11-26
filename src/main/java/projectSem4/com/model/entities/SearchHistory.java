package projectSem4.com.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SearchHistory {

	private Integer searchId;
	private String keyWord;
	private String filters;
	private String ResultSnapshot;
	private Integer searchCount;
	private BigDecimal totalRevenue;
	private String reason;
	private LocalDateTime createAt;

	public Integer getSearchId() {
		return searchId;
	}

	public void setSearchId(Integer searchId) {
		this.searchId = searchId;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public String getResultSnapshot() {
		return ResultSnapshot;
	}

	public void setResultSnapshot(String resultSnapshot) {
		ResultSnapshot = resultSnapshot;
	}

	public Integer getSearchCount() {
		return searchCount;
	}

	public void setSearchCount(Integer searchCount) {
		this.searchCount = searchCount;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(BigDecimal totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDateTime getCreateAt() {
		return createAt;
	}

	public void setCreateAt(LocalDateTime createAt) {
		this.createAt = createAt;
	}

	public SearchHistory(Integer searchId, String keyWord, String filters, String resultSnapshot, Integer searchCount,
			BigDecimal totalRevenue, String reason, LocalDateTime createAt) {
		super();
		this.searchId = searchId;
		this.keyWord = keyWord;
		this.filters = filters;
		ResultSnapshot = resultSnapshot;
		this.searchCount = searchCount;
		this.totalRevenue = totalRevenue;
		this.reason = reason;
		this.createAt = createAt;
	}

	public SearchHistory() {
		super();
		// TODO Auto-generated constructor stub
	}

}
