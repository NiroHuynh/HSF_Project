package com.hsf_project.controller.admin;

import com.hsf_project.dto.admin.*;
import com.hsf_project.entity.*;
import com.hsf_project.entity.enums.AgeRating;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.repository.*;
import com.hsf_project.repository.auth.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final MovieRepository movies;
    private final PromotionRepository promotions;
    private final ComboRepository combos;
    private final UserRepository users;
    private final CinemaRepository cinemas;
    private final RoleRepository roles;

    public AdminController(MovieRepository movies, PromotionRepository promotions, ComboRepository combos,
                           UserRepository users, CinemaRepository cinemas, RoleRepository roles) {
        this.movies = movies;
        this.promotions = promotions;
        this.combos = combos;
        this.users = users;
        this.cinemas = cinemas;
        this.roles = roles;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(@RequestParam(required = false) Integer cinemaId, Model model) {
        var cinemaList = cinemas.findByIsDeletedFalseOrderByNameAsc();
        var counts = new LinkedHashMap<Integer, Long>();
        cinemaList.forEach(c -> counts.put(c.getId(), users.countCustomersByCinema(c.getId())));
        model.addAttribute("totalCustomers", users.countActiveCustomers());
        model.addAttribute("cinemaCounts", counts);
        model.addAttribute("selectedCinemaId", cinemaId);
        model.addAttribute("selectedCustomerCount", cinemaId == null ? users.countActiveCustomers() : users.countCustomersByCinema(cinemaId));
        model.addAttribute("cinemas", cinemaList);
        model.addAttribute("movies", movies.findByIsDeletedFalseOrderByIdDesc());
        model.addAttribute("promotions", promotions.findByIsDeletedFalseOrderByIdDesc());
        model.addAttribute("combos", combos.findByIsDeletedFalseOrderByIdDesc());
        model.addAttribute("accounts", users.findByIsDeletedFalseOrderByIdDesc());
        model.addAttribute("ageRatings", AgeRating.values());
        model.addAttribute("movieStatuses", MovieStatus.values());
        addForm(model, "movieForm", new MovieAdminForm());
        addForm(model, "voucherForm", new VoucherAdminForm());
        addForm(model, "comboForm", new ComboAdminForm());
        addForm(model, "accountForm", new AccountAdminForm());
        return "admin/dashboard";
    }

    private void addForm(Model model, String name, Object form) {
        if (!model.containsAttribute(name)) model.addAttribute(name, form);
    }

    @PostMapping("/movies/save")
    public String saveMovie(@Valid @ModelAttribute MovieAdminForm form, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return invalid("movieForm", form, result, "Phim", ra);
        Movie m = form.getId() == null ? new Movie() : movies.findById(form.getId()).orElse(new Movie());
        m.setTitle(form.getTitle().trim()); m.setDescription(form.getDescription());
        m.setDurationMinutes(form.getDurationMinutes()); m.setDirector(form.getDirector()); m.setCast(form.getCast());
        m.setReleaseDate(form.getReleaseDate()); m.setPosterUrl(form.getPosterUrl());
        m.setAgeRating(form.getAgeRating()); m.setStatus(form.getStatus());
        if (m.getId() == null) { m.setAverageRating(BigDecimal.ZERO); m.setCreatedAt(LocalDateTime.now()); }
        m.setDeleted(false); movies.save(m);
        return success("Đã lưu phim", ra);
    }

    @PostMapping("/movies/{id}/delete")
    public String deleteMovie(@PathVariable Integer id, RedirectAttributes ra) {
        movies.findById(id).ifPresent(m -> { m.setDeleted(true); movies.save(m); });
        return success("Đã xóa phim", ra);
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@Valid @ModelAttribute VoucherAdminForm form, BindingResult result, RedirectAttributes ra) {
        if (form.getId() == null && form.getStartDate() != null && form.getStartDate().isBefore(LocalDateTime.now())) {
            result.rejectValue("startDate", "past", "Thời gian bắt đầu không được nằm trong quá khứ");
        }
        if (form.getStartDate() != null && form.getEndDate() != null && !form.getEndDate().isAfter(form.getStartDate())) {
            result.rejectValue("endDate", "range", "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if ("PERCENT".equals(form.getDiscountType()) && form.getDiscountValue() != null
                && form.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            result.rejectValue("discountValue", "percent", "Phần trăm giảm không được vượt quá 100");
        }
        var sameCode = form.getCode() == null ? null : promotions.findByCodeIgnoreCaseAndIsDeletedFalse(form.getCode()).orElse(null);
        if (sameCode != null && !sameCode.getId().equals(form.getId())) result.rejectValue("code", "duplicate", "Mã voucher đã tồn tại");
        if (result.hasErrors()) return invalid("voucherForm", form, result, "Voucher", ra);
        Promotion p = form.getId() == null ? new Promotion() : promotions.findById(form.getId()).orElse(new Promotion());
        p.setCode(form.getCode().trim().toUpperCase()); p.setName(form.getName().trim()); p.setDescription(form.getDescription());
        p.setDiscountType(form.getDiscountType()); p.setDiscountValue(form.getDiscountValue());
        p.setStartDate(form.getStartDate()); p.setEndDate(form.getEndDate()); p.setUsageLimit(form.getUsageLimit());
        if (p.getUsedCount() == null) p.setUsedCount(0);
        p.setStatus(form.getStatus()); p.setIsDeleted(false); promotions.save(p);
        return success("Đã lưu voucher", ra);
    }

    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes ra) {
        promotions.findById(id).ifPresent(p -> { p.setIsDeleted(true); promotions.save(p); });
        return success("Đã xóa voucher", ra);
    }

    @PostMapping("/combos/save")
    public String saveCombo(@Valid @ModelAttribute ComboAdminForm form, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return invalid("comboForm", form, result, "Combo", ra);
        Combo c = form.getId() == null ? new Combo() : combos.findById(form.getId()).orElse(new Combo());
        c.setName(form.getName().trim()); c.setDescription(form.getDescription()); c.setPrice(form.getPrice());
        c.setQuantity(form.getQuantity()); c.setStatus(form.getStatus()); c.setIsDeleted(false); combos.save(c);
        return success("Đã lưu combo", ra);
    }

    @PostMapping("/combos/{id}/delete")
    public String deleteCombo(@PathVariable Long id, RedirectAttributes ra) {
        combos.findById(id).ifPresent(c -> { c.setIsDeleted(true); combos.save(c); });
        return success("Đã xóa combo", ra);
    }

    @PostMapping("/accounts/save")
    public String saveAccount(@Valid @ModelAttribute AccountAdminForm form, BindingResult result, RedirectAttributes ra) {
        if (form.getEmail() != null && users.findByEmailAndIsDeletedFalse(form.getEmail()).isPresent()) {
            result.rejectValue("email", "duplicate", "Email đã tồn tại");
        }
        if ("MANAGER".equals(form.getRoleName()) && form.getCinemaId() == null) {
            result.rejectValue("cinemaId", "required", "Manager phải được gán một chi nhánh");
        }
        if (result.hasErrors()) return invalid("accountForm", form, result, "Tài khoản", ra);
        User u = new User();
        u.setLastName(form.getLastName().trim()); u.setFirstName(form.getFirstName().trim());
        u.setEmail(form.getEmail().trim().toLowerCase()); u.setPhoneNumber(form.getPhoneNumber());
        u.setPassword(form.getPassword());
        var role = roles.findByRoleNameIgnoreCaseAndIsDeletedFalse(form.getRoleName());
        if (role.isEmpty() && "MANAGER".equals(form.getRoleName())) {
            role = roles.findByRoleNameIgnoreCaseAndIsDeletedFalse("STAFF");
        }
        u.setRole(role.orElseThrow(() -> new IllegalStateException("Không tìm thấy role " + form.getRoleName())));
        u.setCinema(form.getCinemaId() == null ? null : cinemas.findById(form.getCinemaId()).orElse(null));
        u.setStatus("ACTIVE"); u.setDeleted(false); u.setCreatedAt(LocalDateTime.now()); users.save(u);
        return success("Đã thêm tài khoản", ra);
    }

    private String invalid(String key, Object form, BindingResult result, String section, RedirectAttributes ra) {
        ra.addFlashAttribute(key, form);
        ra.addFlashAttribute("errors", result.getAllErrors().stream()
                .map(e -> e.getDefaultMessage()).collect(Collectors.joining("; ")));
        ra.addFlashAttribute("openSection", section);
        return "redirect:/admin/dashboard";
    }

    private String success(String message, RedirectAttributes ra) {
        ra.addFlashAttribute("success", message);
        return "redirect:/admin/dashboard";
    }
}
