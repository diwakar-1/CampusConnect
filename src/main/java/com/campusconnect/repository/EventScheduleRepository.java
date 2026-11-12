package com.campusconnect.repository;

import com.campusconnect.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    List<EventSchedule> findByEventIdOrderByStartTimeAsc(Long eventId);

    @Query("SELECT s FROM EventSchedule s WHERE s.venue.id = :venueId AND (:excludeId IS NULL OR s.id <> :excludeId) AND s.startTime < :endTime AND s.endTime > :startTime")
    List<EventSchedule> findVenueConflicts(
        @Param("venueId") Long venueId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );

    @Query("SELECT s FROM EventSchedule s JOIN EventRegistration r ON r.event = s.event WHERE r.user.id = :userId AND r.status = 'CONFIRMED' ORDER BY s.startTime ASC")
    List<EventSchedule> findSchedulesForRegisteredUser(@Param("userId") Long userId);
}

