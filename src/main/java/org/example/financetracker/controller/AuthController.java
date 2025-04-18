package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.UserRegistrationDTO;
import org.example.financetracker.entity.User;
import org.example.financetracker.service.CustomUserDetailsService;
import org.example.financetracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistrationDTO", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userRegistrationDTO") UserRegistrationDTO dto,
            BindingResult result,
            Model model) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Пароли не совпадают");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            userService.registerUser(user);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }


    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}