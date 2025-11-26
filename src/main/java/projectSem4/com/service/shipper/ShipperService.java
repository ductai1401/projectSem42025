package projectSem4.com.service.shipper;

import java.util.List;
import java.util.Optional;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectSem4.com.model.entities.ShipmentHistory;
import projectSem4.com.model.entities.Shipper;
import projectSem4.com.model.repositories.ShipmentHistoryRepository;
import projectSem4.com.model.repositories.ShipperRepository;
import projectSem4.com.model.repositories.UserRepository;

@Service
public class ShipperService {
	@Autowired
	private ShipperRepository shipperRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ShipmentHistoryRepository sHRope;
	
	public List<Shipper> getAllShipper() {
		return shipperRepo.findAll();
	}
	
	public Shipper getByIdUser(int idUser) {
		return shipperRepo.findByIdUser(idUser);
	}
	public Shipper getById(int shipperID) {
		return shipperRepo.findById(shipperID);
	}
	
	public List<ShipmentHistory> getShipmentHistoryByIdShipment(String shipmentID) {
		return sHRope.findByShipment(shipmentID);
	}
}
