package projectSem4.com.controller.admin;

import org.springframework.stereotype.Controller;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projectSem4.com.model.entities.Role;
import projectSem4.com.model.repositories.RoleRepository;

import java.util.List;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Danh sách các role
    @GetMapping({"", "/"})
    public String listRoles(Model model,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {

        List<Role> roles = roleRepository.findByRolePaged(keyword, page, size);
        int total = roleRepository.getTotalRoles();

        model.addAttribute("roles", roles);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("total", total);
        model.addAttribute("size", size);

        return "admin/roles/listRole";
    }

    // Tạo mới role
    @GetMapping("/create")
    public String createRoleForm(Model model) {
        model.addAttribute("role", new Role());
        return "admin/roles/createRole";
    }

    @PostMapping("/create")
    public String createRole(@ModelAttribute Role role) throws UnsupportedEncodingException {
        String result = roleRepository.createRole(role);
        String encodedMessage = URLEncoder.encode(result, "UTF-8");
        return "redirect:/admin/roles?message=" + encodedMessage;
    }

    // Sửa role
    @GetMapping("/{id}/edit")
    public String editRoleForm(@PathVariable int id, Model model) {
        Role role = roleRepository.findById(id);
        model.addAttribute("role", role);
        return "admin/roles/editRole";
    }

    @PostMapping("/{id}/edit")
    public String editRole(@PathVariable int id, @ModelAttribute Role role) throws UnsupportedEncodingException {
        role.setRoleId(id);
        String result = roleRepository.updateRole(role);
        // Mã hóa thông điệp kết quả
        String encodedMessage = URLEncoder.encode(result, "UTF-8");
        return "redirect:/admin/roles?message=" + encodedMessage;
    }

    @PostMapping("/{id}/delete")
    public String deleteRole(@PathVariable int id) throws UnsupportedEncodingException {
        String result = roleRepository.deleteRole(id);
        // Mã hóa thông điệp kết quả
        String encodedMessage = URLEncoder.encode(result, "UTF-8");
        return "redirect:/admin/roles?message=" + encodedMessage;
    }
    
    // Tìm kiếm role theo tên
    @GetMapping("/search")
    public String searchRoles(@RequestParam String keyword, Model model) {
        List<Role> roles = roleRepository.searchRoles(keyword);
        model.addAttribute("roles", roles);
        model.addAttribute("keyword", keyword);
        return "admin/roles/listRole";
    }
}
