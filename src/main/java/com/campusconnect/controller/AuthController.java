package com.campusconnect.controller;

import com.campusconnect.dto.ExternalStudentRegisterDto;
import com.campusconnect.dto.InternalStudentRegisterDto;
import com.campusconnect.exception.DuplicateRegistrationException;
import com.campusconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (userService.getCurrentUser() != null) {
            if (userService.getCurrentUser().isAdmin()) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/student/dashboard";
        }

        if (error != null) {
            model.addAttribute("loginError", "Invalid email or password, or your account may be pending approval.");
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register/internal")
    public String showInternalRegisterForm(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new InternalStudentRegisterDto());
        }
        return "auth/register-internal";
    }

    @PostMapping("/register/internal")
    public String processInternalRegistration(@Valid @ModelAttribute("registerDto") InternalStudentRegisterDto dto,
                                              BindingResult bindingResult,
                                              RedirectAttributes redirectAttributes) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerDto", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register-internal";
        }

        try {
            userService.registerInternalStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! You can now log in with your CGC credentials.");
            return "redirect:/auth/login";
        } catch (DuplicateRegistrationException e) {
            bindingResult.rejectValue("email", "error.registerDto", e.getMessage());
            return "auth/register-internal";
        }
    }

    @GetMapping("/register/external")
    public String showExternalRegisterForm(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new ExternalStudentRegisterDto());
        }
        return "auth/register-external";
    }

    @PostMapping("/register/external")
    public String processExternalRegistration(@Valid @ModelAttribute("registerDto") ExternalStudentRegisterDto dto,
                                              BindingResult bindingResult,
                                              RedirectAttributes redirectAttributes) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerDto", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register-external";
        }

        try {
            userService.registerExternalStudent(dto);
            return "redirect:/auth/approval-pending?email=" + dto.getEmail();
        } catch (DuplicateRegistrationException e) {
            bindingResult.rejectValue("email", "error.registerDto", e.getMessage());
            return "auth/register-external";
        }
    }

    @GetMapping("/approval-pending")
    public String showPendingApprovalPage(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("registeredEmail", email != null ? email : "your registered email");
        return "auth/approval-pending";
    }
}

