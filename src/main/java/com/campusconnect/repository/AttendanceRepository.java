package com.campusconnect.repository;

import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEventRegistrationId(Long registrationId);

    boolean existsByEventRegistrationId(Long registrationId);

    List<Attendance> findByEventId(Long eventId);

    List<Attendance> findByUserIdOrderByMarkedAtDesc(Long userId);

    long countByEventIdAndStatus(Long eventId, AttendanceStatus status);

    long countByStatus(AttendanceStatus status);
}

