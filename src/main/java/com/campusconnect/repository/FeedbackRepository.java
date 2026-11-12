package com.campusconnect.repository;

import com.campusconnect.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<Feedback> findByEventIdOrderBySubmittedAtDesc(Long eventId);

    List<Feedback> findByUserIdOrderBySubmittedAtDesc(Long userId);

    @Query("SELECT AVG(f.overallRating) FROM Feedback f WHERE f.event.id = :eventId")
    Double getAverageOverallRatingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT AVG(f.overallRating) FROM Feedback f")
    Double getGlobalAverageOverallRating();

    @Query("SELECT AVG(f.overallRating), AVG(f.organizationRating), AVG(f.venueRating), AVG(f.coordinationRating) FROM Feedback f WHERE f.event.id = :eventId")
    List<Object[]> getRatingAveragesByEventId(@Param("eventId") Long eventId);
}

