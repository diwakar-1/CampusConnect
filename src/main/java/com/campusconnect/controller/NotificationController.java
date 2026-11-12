package com.campusconnect.controller;

import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.service.NotificationService;
import com.campusconnect.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String viewAllNotifications(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        List<Notification> list = notificationService.getUserNotifications(user.getId());
        model.addAttribute("notifications", list);
        return "notifications/index";
    }

    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead() {
        User user = userService.getCurrentUser();
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
        }
        return "redirect:/notifications";
    }
}

