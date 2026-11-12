package com.campusconnect.service;

import com.campusconnect.dto.AttendanceScanDto;
import com.campusconnect.entity.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AttendanceRepository;
import com.campusconnect.repository.EventRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRegistrationRepository registrationRepository;
    private final NotificationService notificationService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EventRegistrationRepository registrationRepository,
                             NotificationService notificationService) {
        this.attendanceRepository = attendanceRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Attendance markAttendance(AttendanceScanDto dto, User markedBy) {
        String rawCode = dto.getQrCode().trim();
        String regNumber = parseRegistrationCode(rawCode);

        EventRegistration reg = registrationRepository.findByRegistrationNumber(regNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No registration found with code: " + regNumber));

        if (!reg.getEvent().getId().equals(dto.getEventId())) {
            throw new IllegalArgumentException("Invalid Pass: This registration pass is for '" + reg.getEvent().getTitle() + "', not the selected event.");
        }

        if (reg.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Registration status is " + reg.getStatus() + ". Only confirmed registrations can be marked present.");
        }

        if (attendanceRepository.existsByEventRegistrationId(reg.getId())) {
            throw new IllegalArgumentException("Attendance has ALREADY been marked for " + reg.getUser().getFullName() + " (" + regNumber + ").");
        }

        Attendance attendance = new Attendance(
                reg,
                reg.getEvent(),
                reg.getUser(),
                AttendanceStatus.PRESENT,
                markedBy,
                "QR_SCAN"
        );
        attendance.setNotes(dto.getNotes());

        Attendance saved = attendanceRepository.save(attendance);

        notificationService.notifyUser(
                reg.getUser(),
                "Attendance Verified",
                "Your attendance for " + reg.getEvent().getTitle() + " has been successfully verified on " + LocalDateTime.now().toLocalTime() + ".",
                "/student/attendance",
                "ATTENDANCE"
        );

        return saved;
    }

    @Transactional
    public Attendance markAttendanceManual(Long registrationId, User markedBy) {
        EventRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + registrationId));

        if (attendanceRepository.existsByEventRegistrationId(reg.getId())) {
            throw new IllegalArgumentException("Attendance has already been marked for this participant.");
        }

        Attendance attendance = new Attendance(
                reg,
                reg.getEvent(),
                reg.getUser(),
                AttendanceStatus.PRESENT,
                markedBy,
                "MANUAL"
        );

        Attendance saved = attendanceRepository.save(attendance);

        notificationService.notifyUser(
                reg.getUser(),
                "Attendance Verified",
                "Your attendance for " + reg.getEvent().getTitle() + " has been manually marked present.",
                "/student/attendance",
                "ATTENDANCE"
        );

        return saved;
    }

    private String parseRegistrationCode(String raw) {
        if (raw.startsWith("CC-PASS:")) {
            String[] parts = raw.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return raw;
    }

    public List<Attendance> getAttendanceByEvent(Long eventId) {
        return attendanceRepository.findByEventId(eventId);
    }

    public List<Attendance> getUserAttendance(Long userId) {
        return attendanceRepository.findByUserIdOrderByMarkedAtDesc(userId);
    }

    public boolean isAttended(Long registrationId) {
        return attendanceRepository.existsByEventRegistrationId(registrationId);
    }

    public long countTotalAttendance() {
        return attendanceRepository.countByStatus(AttendanceStatus.PRESENT);
    }
}

