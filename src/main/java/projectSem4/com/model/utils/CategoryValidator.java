package projectSem4.com.model.utils;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import projectSem4.com.model.entities.Category;
import projectSem4.com.model.repositories.CategoryRepository;

import java.util.Locale;
import java.util.Objects;

@Component // singleton
public class CategoryValidator {

    private int maxNameLength = 255;
    private int maxDescLength = 2000;
    private int maxDepth = 0; // 0 = không giới hạn

    @Autowired
    private CategoryRepository repo;

    /* ==================== PUBLIC API ==================== */

    public ValidationResult validateCreate(String categoryName,
                                           Integer parentID,
                                           String description,
                                           Integer sortOrder,
                                           Integer status) {

        String name = normalizeName(categoryName);
        String desc = safeTrim(description);
        int sOrder = (sortOrder == null ? 0 : sortOrder);

        if (name.isEmpty()) return ValidationResult.error("Tên danh mục không được để trống");
        if (name.length() > maxNameLength) return ValidationResult.error("Tên danh mục quá dài");
        if (desc != null && desc.length() > maxDescLength) return ValidationResult.error("Mô tả quá dài");
        if (sOrder < 0) return ValidationResult.error("Sort Order phải ≥ 0");
        if (status == null || (status != 0 && status != 1)) return ValidationResult.error("Trạng thái không hợp lệ");

        Category parent = null;
        if (parentID != null && parentID != 0) {
            parent = repo.findById(parentID);
            if (parent == null) return ValidationResult.error("Danh mục cha không tồn tại");

            if (maxDepth > 0) {
                int depth = computeDepth(parent); // root=1
                if (depth + 1 > maxDepth) return ValidationResult.error("Vượt quá độ sâu danh mục cho phép");
            }

            // NEW: không được trùng với tên của cha/ancestor
            String normLower = normLower(name);
            if (nameEqualsAnyAncestor(normLower, parentID)) {
                return ValidationResult.error("Tên danh mục không được trùng với danh mục cha/nhánh cha.");
            }
        }

        // check trùng tên trong cùng cấp (siblings)
        String normLower = normLower(name);
        boolean dup = repo.existsSiblingName(
                parent == null ? null : parent.getCategoryId(),
                normLower,
                null
        );
        if (dup) return ValidationResult.error("Tên danh mục đã tồn tại trong cùng cấp");

        return ValidationResult.ok();
    }

    public ValidationResult validateUpdate(int currentId,
                                           String categoryName,
                                           Integer parentID,
                                           String description,
                                           Integer sortOrder,
                                           Integer status) {
        Category current = repo.findById(currentId);
        if (current == null) return ValidationResult.error("Danh mục không tồn tại");

        String name = normalizeName(categoryName);
        String desc = safeTrim(description);
        int sOrder = (sortOrder == null ? 0 : sortOrder);

        if (name.isEmpty()) return ValidationResult.error("Tên danh mục không được để trống");
        if (name.length() > maxNameLength) return ValidationResult.error("Tên danh mục quá dài");
        if (desc != null && desc.length() > maxDescLength) return ValidationResult.error("Mô tả quá dài");
        if (sOrder < 0) return ValidationResult.error("Sort Order phải ≥ 0");
        if (status == null || (status != 0 && status != 1)) return ValidationResult.error("Trạng thái không hợp lệ");

        Category parent = null;
        if (parentID != null && parentID != 0) {
            if (Objects.equals(parentID, currentId))
                return ValidationResult.error("Danh mục cha không thể là chính nó");

            parent = repo.findById(parentID);
            if (parent == null) return ValidationResult.error("Danh mục cha không tồn tại");

            if (isDescendant(parentID, currentId))
                return ValidationResult.error("Không thể đặt cha là chính nhánh con/cháu của danh mục này");

            if (maxDepth > 0) {
                int depth = computeDepth(parent);
                if (depth + 1 > maxDepth) return ValidationResult.error("Vượt quá độ sâu danh mục cho phép");
            }

            // NEW: không được trùng với tên của cha/ancestor
            String normLower = normLower(name);
            if (nameEqualsAnyAncestor(normLower, parentID)) {
                return ValidationResult.error("Tên danh mục không được trùng với danh mục cha/nhánh cha.");
            }
        }

        String normLower = normLower(name);
        boolean dup = repo.existsSiblingName(
                parent == null ? null : parent.getCategoryId(),
                normLower,
                currentId
        );
        if (dup) return ValidationResult.error("Tên danh mục đã tồn tại trong cùng cấp");

        return ValidationResult.ok();
    }

    public ValidationResult validateDelete(int id) {
        Category cat = repo.findById(id);
        if (cat == null) return ValidationResult.error("Danh mục không tồn tại");

        if (repo.hasChildren(id))
            return ValidationResult.error("Không thể xoá: danh mục còn danh mục con");

        return ValidationResult.ok();
    }

    /* ==================== HELPERS ==================== */

    private static String safeTrim(String s) { return s == null ? null : s.trim(); }

    private static String normalizeName(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    private static String normLower(String s){
        return normalizeName(s).toLowerCase(Locale.ROOT);
    }

    // root depth = 1
    private int computeDepth(Category node) {
        int depth = 1, guard = 0;
        Integer pid = node.getParentCategory();
        while (pid != null && guard++ < 1000) {
            Category p = repo.findById(pid);
            if (p == null) break;
            depth++; pid = p.getParentCategory();
        }
        return depth;
    }

    private boolean isDescendant(Integer candidateParentId, Integer nodeId) {
        if (candidateParentId == null || nodeId == null) return false;
        int guard = 0; Integer cur = candidateParentId;
        while (cur != null && guard++ < 1000) {
            if (Objects.equals(cur, nodeId)) return true;
            Category c = repo.findById(cur);
            if (c == null) return false;
            cur = c.getParentCategory();
        }
        return false;
    }

    // NEW: kiểm tra tên đã chuẩn hoá (lowercase) có trùng bất kỳ ancestor nào không
    private boolean nameEqualsAnyAncestor(String normLowerName, Integer parentId) {
        Integer cur = parentId;
        int guard = 0;
        while (cur != null && guard++ < 1000) {
            Category p = repo.findById(cur);
            if (p == null) break;
            String parentNorm = normLower(p.getCategoryName() == null ? "" : p.getCategoryName());
            if (normLowerName.equals(parentNorm)) return true;
            cur = p.getParentCategory();
        }
        return false;
    }

    /* ==================== OPTIONAL CONFIG ==================== */
    public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
    public void setMaxNameLength(int maxNameLength) { this.maxNameLength = maxNameLength; }
    public void setMaxDescLength(int maxDescLength) { this.maxDescLength = maxDescLength; }

    /* ==================== RESULT TYPE ==================== */
    public static final class ValidationResult {
        private final boolean ok;
        private final String message;
        private ValidationResult(boolean ok, String message) { this.ok = ok; this.message = message; }
        public static ValidationResult ok() { return new ValidationResult(true, null); }
        public static ValidationResult error(String msg) { return new ValidationResult(false, msg); }
        public boolean isOk() { return ok; }
        public String getMessage() { return message; }
    }
}
