package com.community.service.controller;

import com.community.service.entity.Activity;
import com.community.service.entity.Announcement;
import com.community.service.entity.DemandComment;
import com.community.service.entity.Product;
import com.community.service.entity.User;
import com.community.service.entity.VolunteerProfile;
import com.community.service.mapper.ActivityRepository;
import com.community.service.mapper.AnnouncementRepository;
import com.community.service.mapper.DemandCommentRepository;
import com.community.service.mapper.DemandParticipantRepository;
import com.community.service.mapper.DemandRepository;
import com.community.service.mapper.ProgressReportRepository;
import com.community.service.mapper.ProductRepository;
import com.community.service.mapper.RedemptionRepository;
import com.community.service.mapper.UserRepository;
import com.community.service.mapper.VolunteerProfileRepository;
import com.community.service.service.ActivityService;
import com.community.service.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AdminController {

    private final VolunteerProfileRepository volunteerProfileRepository;
    private final ActivityRepository activityRepository;
    private final DemandRepository demandRepository;
    private final DemandParticipantRepository demandParticipantRepository;
    private final ProgressReportRepository progressReportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DemandCommentRepository demandCommentRepository;
    private final AnnouncementService announcementService;
    private final RedemptionRepository redemptionRepository;
    private final ActivityService activityService;
    private final AnnouncementRepository announcementRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "管理后台");
        model.addAttribute("pendingVolunteers", volunteerProfileRepository.findByAuditStatus(VolunteerProfile.AuditStatus.PENDING));
        model.addAttribute("activities", activityRepository.findAll());
        model.addAttribute("demands", demandRepository.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("title", "用户管理");
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @Transactional
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "操作成功");
        return "redirect:/admin/users";
    }

    @Transactional
    @PostMapping("/users/{id}/points")
    public String updateUserPoints(@PathVariable Long id,
                                   @RequestParam Integer points,
                                   @RequestParam String reason,
                                   @AuthenticationPrincipal User admin,
                                   RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPoints(points);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "积分修改成功");
        return "redirect:/admin/users";
    }

    @PostMapping("/volunteers/{id}/approve")
    public String approveVolunteer(@PathVariable Long id) {
        VolunteerProfile profile = volunteerProfileRepository.findById(id).orElseThrow();
        profile.setAuditStatus(VolunteerProfile.AuditStatus.APPROVED);
        volunteerProfileRepository.save(profile);
        return "redirect:/admin?approved=true";
    }

    @PostMapping("/volunteers/{id}/reject")
    public String rejectVolunteer(@PathVariable Long id) {
        VolunteerProfile profile = volunteerProfileRepository.findById(id).orElseThrow();
        profile.setAuditStatus(VolunteerProfile.AuditStatus.REJECTED);
        volunteerProfileRepository.save(profile);
        return "redirect:/admin?rejected=true";
    }

    @GetMapping("/activities")
    public String activities(Model model) {
        model.addAttribute("title", "活动管理");
        model.addAttribute("activities", activityRepository.findAll());
        return "admin/activities";
    }

    @GetMapping("/activities/new")
    public String activityForm(Model model) {
        model.addAttribute("title", "创建活动");
        model.addAttribute("activity", new Activity());
        return "admin/activity-form";
    }

    @PostMapping("/activities")
    public String createActivity(@ModelAttribute Activity activity) {
        activity.setEnrolledNumber(0);
        activity.setStatus(Activity.ActivityStatus.PUBLISHED);
        activityRepository.save(activity);
        return "redirect:/admin/activities?created=true";
    }

    @GetMapping("/activities/{id}/edit")
    public String editActivity(@PathVariable Long id, Model model) {
        Activity activity = activityRepository.findById(id).orElseThrow();
        model.addAttribute("title", "编辑活动");
        model.addAttribute("activity", activity);
        return "admin/activity-form";
    }

    @Transactional
    @PostMapping("/activities/{id}")
    public String updateActivity(@PathVariable Long id, @ModelAttribute Activity activity) {
        Activity existing = activityRepository.findById(id).orElseThrow();
        existing.setTitle(activity.getTitle());
        existing.setDescription(activity.getDescription());
        existing.setLocation(activity.getLocation());
        existing.setStartTime(activity.getStartTime());
        existing.setEndTime(activity.getEndTime());
        existing.setRecruitNumber(activity.getRecruitNumber());
        existing.setRequiredSkills(activity.getRequiredSkills());
        existing.setBasePoints(activity.getBasePoints());
        activityRepository.save(existing);
        return "redirect:/admin/activities?updated=true";
    }

    @PostMapping("/activities/{id}/delete")
    public String deleteActivity(@PathVariable Long id) {
        activityRepository.deleteById(id);
        return "redirect:/admin/activities?deleted=true";
    }

    @GetMapping("/demands")
    public String demands(Model model) {
        model.addAttribute("title", "需求管理");
        model.addAttribute("demands", demandRepository.findAll());
        return "admin/demands";
    }

    @Transactional
    @PostMapping("/demands/{id}/status")
    public String updateDemandStatus(@PathVariable Long id, @RequestParam String status) {
        var demand = demandRepository.findById(id).orElseThrow();
        demand.setStatus(com.community.service.entity.Demand.DemandStatus.valueOf(status));
        demandRepository.save(demand);
        return "redirect:/admin/demands?updated=true";
    }

    @GetMapping("/comments")
    public String comments(Model model) {
        model.addAttribute("title", "评论管理");
        model.addAttribute("comments", demandCommentRepository.findAll());
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id) {
        demandCommentRepository.deleteById(id);
        return "redirect:/admin/comments?deleted=true";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "商品管理");
        model.addAttribute("products", productRepository.findAll());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String productForm(Model model) {
        model.addAttribute("title", "添加商品");
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product,
                               @RequestParam(value = "image", required = false) MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                String fileName = UUID.randomUUID().toString() + extension;
                Path uploadPath = Path.of(uploadDir, "products").toAbsolutePath().normalize();
                new File(uploadPath.toString()).mkdirs();
                Path filePath = uploadPath.resolve(fileName);
                file.transferTo(filePath.toFile());
                product.setImageUrl("/uploads/products/" + fileName);
            }
            product.setStatus(Product.ProductStatus.AVAILABLE);
            product.setStock(product.getStock() != null ? product.getStock() : 0);
            productRepository.save(product);
            return "redirect:/admin/products?created=true";
        } catch (Exception e) {
            return "redirect:/admin/products/new?error=" + e.getMessage();
        }
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElseThrow();
        model.addAttribute("title", "编辑商品");
        model.addAttribute("product", product);
        return "admin/product-form";
    }

    @Transactional
    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute Product product,
                                @RequestParam(value = "image", required = false) MultipartFile file) {
        Product existing = productRepository.findById(id).orElseThrow();
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setRequiredPoints(product.getRequiredPoints());
        existing.setStock(product.getStock());

        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                String fileName = UUID.randomUUID().toString() + extension;
                Path uploadPath = Path.of(uploadDir, "products").toAbsolutePath().normalize();
                new File(uploadPath.toString()).mkdirs();
                Path filePath = uploadPath.resolve(fileName);
                file.transferTo(filePath.toFile());
                existing.setImageUrl("/uploads/products/" + fileName);
            } catch (Exception e) {
            }
        }

        productRepository.save(existing);
        return "redirect:/admin/products?updated=true";
    }

    @Transactional
    @PostMapping("/products/{id}/restock")
    public String restockProduct(@PathVariable Long id, @RequestParam Integer quantity) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setStock(product.getStock() + quantity);
        if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
            product.setStatus(Product.ProductStatus.AVAILABLE);
        }
        productRepository.save(product);
        return "redirect:/admin/products?restocked=true";
    }

    @Transactional
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        redemptionRepository.deleteByProduct(product);
        productRepository.deleteById(id);
        return "redirect:/admin/products?deleted=true";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("title", "报表中心");
        return "admin/reports";
    }

    @GetMapping("/demands/{id}/edit")
    public String editDemand(@PathVariable Long id, Model model) {
        var demand = demandRepository.findById(id).orElseThrow();
        model.addAttribute("title", "编辑需求");
        model.addAttribute("demand", demand);
        return "admin/demand-form";
    }

    @Transactional
    @PostMapping("/demands/{id}")
    public String updateDemand(@PathVariable Long id, @ModelAttribute com.community.service.entity.Demand demand) {
        com.community.service.entity.Demand existing = demandRepository.findById(id).orElseThrow();
        existing.setTitle(demand.getTitle());
        existing.setContent(demand.getContent());
        existing.setType(demand.getType());
        existing.setUrgency(demand.getUrgency());
        existing.setExpectedTime(demand.getExpectedTime());
        demandRepository.save(existing);
        return "redirect:/admin/demands?updated=true";
    }

    @Transactional
    @PostMapping("/demands/{id}/delete")
    public String deleteDemand(@PathVariable Long id) {
        demandCommentRepository.deleteByDemandId(id);
        demandParticipantRepository.deleteByDemandId(id);
        var demand = demandRepository.findById(id).orElseThrow();
        progressReportRepository.deleteByDemand(demand);
        demandRepository.deleteById(id);
        return "redirect:/admin/demands?deleted=true";
    }

    @PostMapping("/demand-comments/{id}/delete")
    public String deleteDemandComment(@PathVariable Long id) {
        demandCommentRepository.deleteById(id);
        return "redirect:/admin/demands?commentDeleted=true";
    }

    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("title", "通知管理");
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        return "admin/announcements";
    }

    @GetMapping("/announcements/new")
    public String announcementForm(Model model) {
        model.addAttribute("title", "发布通知");
        return "admin/announcement-form";
    }

    @PostMapping("/announcements")
    public String createAnnouncement(@ModelAttribute Announcement announcement,
                                      @AuthenticationPrincipal User admin) {
        announcementService.createAnnouncement(announcement.getTitle(), announcement.getContent(), announcement.getPriority(), admin);
        return "redirect:/admin/announcements?created=true";
    }

    @GetMapping("/announcements/{id}/edit")
    public String editAnnouncement(@PathVariable Long id, Model model) {
        Announcement announcement = announcementRepository.findById(id).orElseThrow();
        model.addAttribute("title", "编辑通知");
        model.addAttribute("announcement", announcement);
        return "admin/announcement-form";
    }

    @Transactional
    @PostMapping("/announcements/{id}")
    public String updateAnnouncement(@PathVariable Long id, @ModelAttribute Announcement announcement) {
        Announcement existing = announcementRepository.findById(id).orElseThrow();
        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        existing.setPriority(announcement.getPriority());
        announcementRepository.save(existing);
        return "redirect:/admin/announcements?updated=true";
    }

    @Transactional
    @PostMapping("/announcements/{id}/toggle")
    public String toggleAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Announcement announcement = announcementRepository.findById(id).orElseThrow();
        announcement.setActive(!announcement.isActive());
        announcementRepository.save(announcement);
        redirectAttributes.addFlashAttribute("success", "状态已更新");
        return "redirect:/admin/announcements";
    }

    @Transactional
    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementRepository.deleteById(id);
        return "redirect:/admin/announcements?deleted=true";
    }
}
