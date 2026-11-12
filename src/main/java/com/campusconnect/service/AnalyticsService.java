package com.campusconnect.service;

import com.campusconnect.entity.AttendanceStatus;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventStatus;
import com.campusconnect.entity.UserRole;
import com.campusconnect.entity.UserStatus;
import com.campusconnect.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeedbackRepository feedbackRepository;

    public AnalyticsService(UserRepository userRepository,
                            EventRepository eventRepository,
                            EventRegistrationRepository registrationRepository,
                            AttendanceRepository attendanceRepository,
                            FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long internalStudents = userRepository.countByRole(UserRole.ROLE_INTERNAL_STUDENT);
        long externalStudents = userRepository.countByRole(UserRole.ROLE_EXTERNAL_STUDENT);
        long pendingApprovals = userRepository.countByStatus(UserStatus.PENDING_APPROVAL);

        long totalEvents = eventRepository.count();
        long upcomingEvents = eventRepository.countByStatus(EventStatus.UPCOMING);
        long totalRegistrations = registrationRepository.count();
        long totalAttendance = attendanceRepository.countByStatus(AttendanceStatus.PRESENT);

        double attendanceRate = totalRegistrations > 0 ? ((double) totalAttendance / totalRegistrations) * 100.0 : 0.0;
        Double avgRating = feedbackRepository.getGlobalAverageOverallRating();

        stats.put("totalUsers", totalUsers);
        stats.put("internalStudents", internalStudents);
        stats.put("externalStudents", externalStudents);
        stats.put("pendingApprovals", pendingApprovals);
        stats.put("totalEvents", totalEvents);
        stats.put("upcomingEvents", upcomingEvents);
        stats.put("totalRegistrations", totalRegistrations);
        stats.put("totalAttendance", totalAttendance);
        stats.put("attendanceRate", Math.round(attendanceRate * 10.0) / 10.0);
        stats.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        return stats;
    }

    public Map<String, List<Object>> getEventRegistrationChartData() {
        List<Object[]> queryResults = registrationRepository.countRegistrationsPerEvent();
        List<Object> labels = new ArrayList<>();
        List<Object> counts = new ArrayList<>();

        for (Object[] row : queryResults) {
            labels.add(row[0] != null ? row[0].toString() : "Unknown");
            counts.add(row[1]);
        }

        Map<String, List<Object>> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", counts);
        return chartData;
    }

    public Map<String, List<Object>> getCategoryDistributionChartData() {
        List<Object[]> queryResults = eventRepository.countEventsByCategory();
        List<Object> labels = new ArrayList<>();
        List<Object> counts = new ArrayList<>();

        for (Object[] row : queryResults) {
            labels.add(row[0] != null ? row[0].toString() : "General");
            counts.add(row[1]);
        }

        Map<String, List<Object>> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", counts);
        return chartData;
    }

    public Map<String, List<Object>> getMonthlyRegistrationsChartData() {
        List<Object[]> queryResults = registrationRepository.countRegistrationsByMonth();
        List<Object> labels = new ArrayList<>();
        List<Object> counts = new ArrayList<>();

        for (Object[] row : queryResults) {
            labels.add(row[0] != null ? row[0].toString() : "");
            counts.add(row[1]);
        }

        Map<String, List<Object>> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", counts);
        return chartData;
    }
}

