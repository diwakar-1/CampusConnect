package com.campusconnect.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        logger.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(DuplicateRegistrationException.class)
    public String handleDuplicateRegistration(DuplicateRegistrationException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Duplicate registration: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/events";
    }

    @ExceptionHandler(EventFullException.class)
    public String handleEventFull(EventFullException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Event full: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("warningMessage", ex.getMessage());
        return "redirect:/events";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("Unexpected error occurred", ex);
        model.addAttribute("errorTitle", "An Unexpected Error Occurred");
        model.addAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "Please contact system administrator.");
        return "error/500";
    }
}

