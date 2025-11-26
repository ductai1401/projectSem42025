package projectSem4.com.service.client;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectSem4.com.controller.webController.admin.ProductController;
import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.ProductVariant;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;

@Service
public class HomeService {

    private final ProductController productController;

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository productVariantRepository;

    HomeService(ProductController productController) {
        this.productController = productController;
    }

    public List<Product> getLatest(int limit) {
        List<Product> all = productRepository.findAll();
        all.sort(Comparator.comparing(Product::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        return all.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Product> getBestSellers(int limit) {
        // Nếu có sold_count thì lấy top
        // return productRepository.findTopBestSellers(limit);
        return getLatest(limit); // fallback
    }

    public List<Product> getOnSale(int limit) {
        // Nếu có cờ on_sale thì gọi repo
        return getLatest(limit); // fallback
    }

    public Map<Integer, Double> buildMinPriceMap(List<Product>... lists) {
        Map<Integer, Double> map = new HashMap<>();
        Set<Integer> productIds = Arrays.stream(lists)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Product::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Integer pid : productIds) {
            List<ProductVariant> variants = productVariantRepository.findByProductId(pid);
            Optional<Double> minVarPrice = variants == null ? Optional.empty()
                    : variants.stream().map(ProductVariant::getPrice).filter(Objects::nonNull).min(Double::compareTo);

            map.put(pid, minVarPrice.orElse(null));
        }
        return map;
    }
}