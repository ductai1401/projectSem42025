package projectSem4.com.controller.apiController.seller;

import java.net.http.HttpRequest;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import projectSem4.com.dto.RegisterSalesDTO;
import projectSem4.com.service.seller.RegisterSellingService;

@RestController
@RequestMapping("/api/registerSeller")
public class RegisterSellingApiController {

    private final RegisterSellingService registerSellingService;

    public RegisterSellingApiController(RegisterSellingService registerSellingService) {
        this.registerSellingService = registerSellingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShop(@RequestBody RegisterSalesDTO dto, HttpServletRequest request) {
        Map<String, Object> result = registerSellingService.createShop(dto, request);

        if (!(boolean) result.get("success")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
