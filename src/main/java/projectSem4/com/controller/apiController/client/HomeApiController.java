package projectSem4.com.controller.apiController.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import projectSem4.com.model.entities.Product;
import projectSem4.com.service.client.HomeService;

@RestController
@RequestMapping("api/home")
public class HomeApiController {

    @Autowired private HomeService homeService;

    @GetMapping("/data")
    public Map<String, Object> getHomeData() {
        Map<String, Object> response = new HashMap<>();

        List<Product> arrivals    = homeService.getLatest(12);
        List<Product> bestSellers = homeService.getBestSellers(12);
        List<Product> onSale      = homeService.getOnSale(12);
        List<Product> suggestions = homeService.getLatest(24);

        Map<Integer, Double> priceMap = homeService.buildMinPriceMap(arrivals, bestSellers, onSale, suggestions);

        response.put("arrivals", arrivals);
        response.put("bestSellers", bestSellers);
        response.put("onSale", onSale);
        response.put("suggestions", suggestions);
        response.put("priceMap", priceMap);

        return response;
    }
}
