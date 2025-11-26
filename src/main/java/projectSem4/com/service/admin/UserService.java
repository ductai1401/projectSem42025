package projectSem4.com.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import projectSem4.com.model.entities.User;
import projectSem4.com.model.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getBuyers(String keyword, int page, int size) {
        // Xử lý dữ liệu trước khi trả ra
        List<User> buyers = userRepository.findByRolePaged("CUSTOMER", keyword, page, size);

        return buyers;
    }

    public int countBuyers(String keyword) {
        return userRepository.countByRole("CUSTOMER", keyword);
    }

    public List<User> getSellers(String keyword, int page, int size) {
        List<User> sellers = userRepository.findByRolePaged("SELLER", keyword, page, size);

        return sellers;
    }

    public int countSellers(String keyword) {
        return userRepository.countByRole("SELLER", keyword);
    }

    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    public void toggleStatus(int id) {
        userRepository.toggleStatus(id);
    }

    public void deleteUser(int id) {
        userRepository.deleteUser(id);
    }
    
//    public String getAddress(int idUser) {
//    	return userRepository.
//    }
}
