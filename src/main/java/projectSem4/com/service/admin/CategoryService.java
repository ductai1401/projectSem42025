package projectSem4.com.service.admin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import projectSem4.com.model.entities.Category;
import projectSem4.com.model.repositories.CategoryRepository;
import projectSem4.com.model.utils.CategoryValidator;

@Service
public class CategoryService {

    private static final String UPLOAD_DIR = "D:/uploads/category/";
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024; // 10MB

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private CategoryValidator categoryValidator;

    /* ========== Helpers ========== */
    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return "File rỗng.";
        if (file.getSize() > MAX_IMAGE_BYTES) return "Ảnh quá lớn (tối đa 10MB).";
        return null;
    }

    private String saveFile(MultipartFile file) throws IOException {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục: " + dir.getAbsolutePath());
        }
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        String fileName = UUID.randomUUID() + "_" + original.replaceAll("[\\s]+", "_");
        file.transferTo(new File(dir, fileName));
        return fileName; // chỉ lưu tên file
    }

    /* ========== CRUD ========== */
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    public Category getById(int id) {
        return categoryRepo.findById(id);
    }

    public void create(Category category, MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String err = validateFile(imageFile);
                if (err != null) throw new IllegalArgumentException(err);
                String savedName = saveFile(imageFile);
                category.setImage(savedName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage(), e);
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepo.createCategory(category);
    }

    public void update(Category category, Integer parentID, MultipartFile imageFile) {
        CategoryValidator.ValidationResult vr = categoryValidator.validateUpdate(
                category.getCategoryId(),
                category.getCategoryName(),
                parentID,
                category.getDescription(),
                category.getSortOrder(),
                category.getStatus()
        );
        if (!vr.isOk()) {
            throw new IllegalArgumentException(vr.getMessage());
        }

        // xử lý parent
        if (parentID == null || parentID == 0) {
            category.setParentCategory(null);
        } else {
            category.setParentCategory(parentID);
        }

        // xử lý ảnh (nếu có upload mới)
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String err = validateFile(imageFile);
                if (err != null) throw new IllegalArgumentException(err);
                String savedName = saveFile(imageFile);
                category.setImage(savedName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage(), e);
        }

        category.setUpdatedAt(LocalDateTime.now());
        categoryRepo.updateCategory(category);
    }

    public void delete(int id) {
        CategoryValidator.ValidationResult vr = categoryValidator.validateDelete(id);
        if (!vr.isOk()) {
            throw new IllegalArgumentException(vr.getMessage());
        }
        categoryRepo.deleteCategory(id);
    }

    public List<Category> findPaged(int page, int size, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return categoryRepo.findAllPaged(page, size);
        } else {
            return categoryRepo.searchCategories(keyword, page, size);
        }
    }

    public int countTotal(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return categoryRepo.getTotalCategories();
        } else {
            return categoryRepo.countSearchCategories(keyword);
        }
    }
}
