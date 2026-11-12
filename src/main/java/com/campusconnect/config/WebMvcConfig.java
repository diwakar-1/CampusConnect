package com.campusconnect.config;

import com.campusconnect.entity.User;
import com.campusconnect.service.NotificationService;
import com.campusconnect.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${campusconnect.app.upload-dir:uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + dir.getAbsolutePath() + File.separator);
    }

    @ControllerAdvice
    public static class GlobalModelAttributes {

        private final UserService userService;
        private final NotificationService notificationService;

        @Value("${campusconnect.college.name:CGC University Mohali}")
        private String collegeName;

        public GlobalModelAttributes(UserService userService, NotificationService notificationService) {
            this.userService = userService;
            this.notificationService = notificationService;
        }

        @ModelAttribute("currentUser")
        public User getCurrentUser() {
            return userService.getCurrentUser();
        }

        @ModelAttribute("unreadNotifCount")
        public long getUnreadNotifCount() {
            User user = userService.getCurrentUser();
            return user != null ? notificationService.getUnreadCount(user.getId()) : 0;
        }

        @ModelAttribute("cgcCollegeName")
        public String getCollegeName() {
            return collegeName;
        }
    }
}

