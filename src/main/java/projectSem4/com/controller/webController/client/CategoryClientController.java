package projectSem4.com.controller.webController.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import projectSem4.com.model.entities.Product;
import projectSem4.com.model.entities.ProductVariant;
import projectSem4.com.model.modelViews.CatNode;
import projectSem4.com.model.entities.Category;
import projectSem4.com.model.repositories.ProductRepository;
import projectSem4.com.model.repositories.ProductVariantRepository;
import projectSem4.com.model.repositories.CategoryRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client/category")
public class CategoryClientController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private CategoryRepository categoryRepository;

    @GetMapping("{id}")
    public String detail(@PathVariable("id") Integer categoryId,
                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                         @RequestParam(value = "size", defaultValue = "12") Integer size,
                         @RequestParam(value = "sort", required = false) String sort,
                         @RequestParam(value = "q",    required = false) String q,
                         Model model) {

        // Category hiện tại + id để template dùng
        Category current = categoryRepository.findById(categoryId);
        model.addAttribute("category", current);
        model.addAttribute("categoryId", categoryId);

        // Lấy toàn bộ category & map parent->children
        List<Category> allCats = categoryRepository.findAll();
        Map<Integer, List<Category>> byParent = new HashMap<>();
        for (Category c : allCats) {
            Integer parentId = (c.getParentCategory() == null) ? 0 : c.getParentCategory();
            byParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
        }

        // Cây danh mục (sidebar)
        List<CatNode> catTree = new ArrayList<>();
        for (Category root : byParent.getOrDefault(0, List.of())) {
            catTree.add(buildNode(root, byParent));
        }
        model.addAttribute("catTree", catTree);

        // Gom tất cả ID con (bao gồm chính nó)
        List<Integer> catIds = collectDescendantIds(categoryId, byParent);
        if (!catIds.contains(categoryId)) catIds.add(categoryId);

        // Tìm kiếm + phân trang + sort
        String keyword = (q == null) ? "" : q.trim();
        List<Product> products = productRepository.findByCategoryIdsAndKeywordPagedSorted(
                catIds, keyword, page, size, sort
        );
        int total = productRepository.countByCategoryIdsAndKeyword(catIds, keyword);

        // priceMap (min price theo variants)
        Map<Integer, Double> priceMap = buildMinPriceMap(products);
        model.addAttribute("priceMap", priceMap);

        // Đếm số product theo category để hiện (n) ở sidebar
        Map<Integer, Long> categoryCounts = new HashMap<>();
        for (Category c : allCats) {
            categoryCounts.put(c.getCategoryId(),
                    (long) productRepository.countByCategoryId(c.getCategoryId()));
        }
        model.addAttribute("categoryCounts", categoryCounts);

        // Breadcrumb
        model.addAttribute("breadcrumbChain", buildBreadcrumbChain(current, allCats));

        // Thông tin phân trang
        int totalPages = (int) Math.ceil(total / (double) size);
        int from = (total == 0) ? 0 : (page - 1) * size + 1;
        int to   = (total == 0) ? 0 : Math.min(page * size, total);

        model.addAttribute("products", products);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("q", q);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "client/category/categoryDetail";
    }

    private CatNode buildNode(Category c, Map<Integer, List<Category>> byParent) {
        CatNode node = new CatNode(c);
        List<CatNode> children = byParent.getOrDefault(c.getCategoryId(), List.of())
                .stream().map(ch -> buildNode(ch, byParent)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }

    private List<Integer> collectDescendantIds(Integer rootId, Map<Integer, List<Category>> byParent) {
        List<Integer> result = new ArrayList<>();
        for (Category c : byParent.getOrDefault(rootId, List.of())) {
            result.add(c.getCategoryId());
            result.addAll(collectDescendantIds(c.getCategoryId(), byParent));
        }
        return result;
    }

    private Map<Integer, Double> buildMinPriceMap(List<Product> products) {
        Map<Integer, Double> map = new HashMap<>();
        for (Product p : products) {
            List<ProductVariant> vars = productVariantRepository.findByProductId(p.getProductId());
            Double min = null;
            if (vars != null && !vars.isEmpty()) {
                min = vars.stream()
                        .map(ProductVariant::getPrice)
                        .filter(Objects::nonNull)
                        .min(Double::compareTo).orElse(null);
            }
            map.put(p.getProductId(), min);
        }
        return map;
    }

    private List<Category> buildBreadcrumbChain(Category current, List<Category> allCats) {
        if (current == null) return List.of();
        Map<Integer, Category> byId = allCats.stream()
                .collect(Collectors.toMap(Category::getCategoryId, x -> x));
        LinkedList<Category> chain = new LinkedList<>();
        Category cursor = current;
        while (cursor != null) {
            chain.addFirst(cursor);
            Integer parentId = cursor.getParentCategory();
            cursor = (parentId == null || parentId == 0) ? null : byId.get(parentId);
        }
        return chain;
    }
}
