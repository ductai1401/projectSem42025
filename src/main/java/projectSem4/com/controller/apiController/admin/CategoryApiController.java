package projectSem4.com.controller.apiController.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import projectSem4.com.model.entities.Category;
import projectSem4.com.service.admin.CategoryService;

@RestController
@RequestMapping("/api/category")
public class CategoryApiController {
	@Autowired
    private CategoryService categoryService;

    /* ========== PAGING + SEARCH ========== */
    @GetMapping("/")
    public Map<String, Object> getCategory(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "5") int size,
                                           @RequestParam(defaultValue = "") String keyword) {
        List<Category> categories = categoryService.findPaged(page, size, keyword);
        int total = categoryService.countTotal(keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        return response;
    }

    /* ========== ROOTS ========== */
    @GetMapping("/roots")
    public List<Map<String, Object>> apiRoots() {
        return categoryService.getAll().stream()
                .filter(c -> c.getParentCategory() == null)
                .map(this::toSimpleMap)
                .collect(Collectors.toList());
    }

    /* ========== CHILDREN ========== */
    @GetMapping("/children")
    public List<Map<String, Object>> apiChildren(@RequestParam("parentId") Integer parentId) {
        return categoryService.getAll().stream()
                .filter(c -> Objects.equals(c.getParentCategory(), parentId))
                .map(this::toSimpleMap)
                .collect(Collectors.toList());
    }

    /* ========== SEARCH ========== */
    @GetMapping("/search")
    public List<Map<String, Object>> apiSearch(@RequestParam("keyword") String keyword) {
        return categoryService.findPaged(1, 100, keyword).stream()
                .map(this::toSimpleMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toSimpleMap(Category c) {
        Map<String, Object> m = new HashMap<>();
        m.put("categoryId", c.getCategoryId());
        m.put("categoryName", c.getCategoryName());
        m.put("categoryOption", c.getCategoryOption());
        return m;
    }
}
