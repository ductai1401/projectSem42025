package projectSem4.com.service;

import java.sql.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectSem4.com.model.repositories.PlatformEarningRepository;

@Service
public class PlatformEarningService {
	@Autowired
	private PlatformEarningRepository pERepo;

	public Map<String , Object> getByShopFillter(Integer shopId, Date startDate, Date endDate, int pageIndex, int pageSize){
		if(shopId == null || shopId < 1) {
			return null;
		}
		return pERepo.findByShopIdAndDateRange(shopId, startDate, endDate, pageIndex, pageSize);
	}
}
