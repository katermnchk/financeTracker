package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.UserProfileDTO;
import org.example.financetracker.security.CustomUserDetails;
import org.example.financetracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Entering showProfile method");

        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        model.addAttribute("profile", userService.getUserProfile(userDetails.getUser()));
        model.addAttribute("username", userDetails.getUsername());

        return "profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfileDTO profileDTO,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.info("Entering updateProfile method");

        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        model.addAttribute("username", username);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors: {}", bindingResult.getAllErrors());
            return "profile";
        }

        try {
            boolean isUpdated = userService.updateUserProfile(username, profileDTO);
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("success", true);
                // Перезагружаем обновлённые данные
                model.addAttribute("profile", userService.getUserProfile(userDetails.getUser()));
                return "redirect:/profile";
            } else {
                model.addAttribute("error", "Не удалось обновить профиль");
                return "profile";
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error updating profile: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }
}