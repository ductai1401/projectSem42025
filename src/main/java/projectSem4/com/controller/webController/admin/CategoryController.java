package projectSem4.com.controller.webController.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projectSem4.com.model.entities.Category;
import projectSem4.com.service.admin.CategoryService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /* ========================= PAGE: LIST ========================= */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "expandParent", required = false) Integer expandParent) {
        List<Category> categories = categoryService.getAll();

        // Map id -> name
        Map<Integer, String> nameById = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));

        // Map parentId -> count children
        Map<Integer, Long> childrenCount = categories.stream()
                .filter(c -> c.getParentCategory() != null)
                .collect(Collectors.groupingBy(Category::getParentCategory, Collectors.counting()));

        model.addAttribute("categories", categories);
        model.addAttribute("nameById", nameById);
        model.addAttribute("childrenCount", childrenCount);

        model.addAttribute("totalPages", 1);
        model.addAttribute("currentPage", 1);
        model.addAttribute("expandParent", expandParent);
        return "admin/category/listCate";
    }

    /* ========================= CREATE ========================= */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "admin/category/createCate";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam("categoryName") String categoryName,
                                 @RequestParam(name = "parentID", required = false) Integer parentID,
                                 @RequestParam(name = "description", required = false) String description,
                                 @RequestParam(name = "sortOrder", defaultValue = "0") Integer sortOrder,
                                 @RequestParam(name = "status", defaultValue = "1") Integer status,
                                 @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                 @RequestParam(name = "categoryOption", required = false) String categoryOptionJson,
                                 RedirectAttributes ra,
                                 Model model) {
        Map<String, String> fieldErrors = new HashMap<>();
        String name = (categoryName == null ? "" : categoryName.trim().replaceAll("\\s+", " "));

        if (name.isEmpty()) fieldErrors.put("categoryName", "Tên danh mục là bắt buộc.");
        else if (name.length() > 255) fieldErrors.put("categoryName", "Tên danh mục tối đa 255 ký tự.");
        if (description != null && description.length() > 2000)
            fieldErrors.put("description", "Mô tả tối đa 2000 ký tự.");
        if (sortOrder < 0) fieldErrors.put("sortOrder", "Sort Order phải ≥ 0.");
        if (status != 0 && status != 1) fieldErrors.put("status", "Trạng thái không hợp lệ.");

        String prettyJson = null;
        if (categoryOptionJson != null && !categoryOptionJson.trim().isEmpty()) {
            try {
                JsonNode node = MAPPER.readTree(categoryOptionJson);
                prettyJson = MAPPER.writeValueAsString(node);
            } catch (Exception ex) {
                fieldErrors.put("categoryOption", "CategoryOption phải là JSON hợp lệ.");
            }
        }

        if (!fieldErrors.isEmpty()) {
            model.addAttribute("fieldErrors", fieldErrors);
            Map<String, Object> form = new HashMap<>();
            form.put("categoryName", categoryName);
            form.put("description", description);
            form.put("sortOrder", sortOrder);
            form.put("status", status);
            form.put("parentID", parentID);
            form.put("categoryOption", categoryOptionJson);
            model.addAttribute("form", form);
            model.addAttribute("categories", categoryService.getAll());
            return "admin/category/createCate";
        }

        Category c = new Category();
        c.setCategoryName(name);
        c.setDescription(description);
        c.setSortOrder(sortOrder);
        c.setStatus(status);
        c.setParentCategory((parentID == null || parentID == 0) ? null : parentID);
        c.setCategoryOption(prettyJson);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());

        try {
            categoryService.create(c, imageFile);
            ra.addFlashAttribute("success", "Tạo danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi tạo: " + e.getMessage());
            return "redirect:/category/create";
        }

        return "redirect:/category/";
    }

    /* ========================= EDIT ========================= */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") int id, Model model, RedirectAttributes ra) {
        Category category = categoryService.getById(id);
        if (category == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/category/";
        }
        model.addAttribute("category", category);
        model.addAttribute("categories", categoryService.getAll());
        return "admin/category/editCate";
    }

    @PostMapping("/edit")
    public String updateCategory(@ModelAttribute Category category,
                                 @RequestParam(name = "parentID", required = false) Integer parentID,
                                 @RequestParam(name = "categoryOption", required = false) String categoryOptionJson,
                                 @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                 RedirectAttributes ra) {
        try {
            if (categoryOptionJson != null && !categoryOptionJson.trim().isEmpty()) {
                try {
                    JsonNode node = MAPPER.readTree(categoryOptionJson);
                    category.setCategoryOption(MAPPER.writeValueAsString(node));
                } catch (Exception ex) {
                    ra.addFlashAttribute("error", "CategoryOption phải là JSON hợp lệ.");
                    return "redirect:/category/edit/" + category.getCategoryId();
                }
            } else {
                category.setCategoryOption(null);
            }

            category.setUpdatedAt(LocalDateTime.now());
            categoryService.update(category, parentID, imageFile);

            ra.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + ex.getMessage());
            return "redirect:/category/edit/" + category.getCategoryId();
        }
        return "redirect:/category/";
    }

    /* ========================= DELETE ========================= */
    @PostMapping("/delete")
    public String deleteCategory(@RequestParam("id") int id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Xóa danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/category/";
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Đã xảy ra lỗi: " + ex.getMessage());
        return "redirect:/category/";
    }
}
