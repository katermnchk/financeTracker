package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.UserProfileDTO;
import org.example.financetracker.entity.User;
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

import java.security.Principal;

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

        User user = userDetails.getUser();
        logger.info("Пользователь нашел: {}", user.getUsername());

        model.addAttribute("profile", userService.getUserProfile(user));
        model.addAttribute("username", user.getUsername());

        return "profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfileDTO profileDTO,
                                BindingResult bindingResult, Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile";
        }

        String username = principal.getName();
        boolean isUpdated = userService.updateUserProfile(username, profileDTO);

        if (isUpdated) {
            redirectAttributes.addAttribute("success", true);
        } else {
            redirectAttributes.addAttribute("error", "Не удалось обновить профиль");
        }

        return "redirect:/profile";
    }
}
