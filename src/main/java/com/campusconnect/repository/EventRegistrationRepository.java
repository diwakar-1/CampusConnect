package com.campusconnect.repository;

import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByRegistrationNumber(String registrationNumber);

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserIdAndStatusNot(Long eventId, Long userId, RegistrationStatus status);

    List<EventRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId);

    List<EventRegistration> findByEventIdOrderByRegisteredAtDesc(Long eventId);

    List<EventRegistration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    long countByStatus(RegistrationStatus status);

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'WAITLISTED' ORDER BY r.registeredAt ASC")
    List<EventRegistration> findWaitlistedForEvent(@Param("eventId") Long eventId);

    @Query("SELECT r.event.title, COUNT(r) FROM EventRegistration r WHERE r.status = 'CONFIRMED' GROUP BY r.event.title ORDER BY COUNT(r) DESC LIMIT 6")
    List<Object[]> countRegistrationsPerEvent();

    @Query("SELECT DATE_FORMAT(r.registeredAt, '%Y-%m'), COUNT(r) FROM EventRegistration r GROUP BY DATE_FORMAT(r.registeredAt, '%Y-%m') ORDER BY DATE_FORMAT(r.registeredAt, '%Y-%m') ASC")
    List<Object[]> countRegistrationsByMonth();
}

