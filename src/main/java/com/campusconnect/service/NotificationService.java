package com.campusconnect.service;

import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Notification notifyUser(User user, String title, String message, String linkUrl, String type) {
        Notification notification = new Notification(user, title, message, linkUrl, type);
        Notification saved = notificationRepository.save(notification);

        // Also trigger email notification
        if (user != null && user.getEmail() != null) {
            String emailBody = String.format("Hello %s,%n%n%s%n%nView details at CampusConnect: %s%n%nBest regards,%nCGC University Mohali Event Team",
                    user.getFullName(), message, linkUrl != null ? linkUrl : "http://localhost:8080");
            emailService.sendEmail(user.getEmail(), "CampusConnect Notification: " + title, emailBody);
        }
        return saved;
    }

    @Transactional
    public void broadcastAnnouncement(String title, String message, String linkUrl) {
        Notification notification = new Notification(null, title, message, linkUrl, "ANNOUNCEMENT");
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findForUser(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadForUser(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findForUser(userId);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(list);
    }
}

