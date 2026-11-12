package com.campusconnect.repository;

import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventCategory;
import com.campusconnect.entity.EventStatus;
import com.campusconnect.entity.EventVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByPublishedTrueOrderByStartDateAsc();

    List<Event> findByPublishedTrueAndStatus(EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.published = true AND (e.visibility = 'ALL' OR e.visibility = :visibility) ORDER BY e.startDate ASC")
    List<Event> findPublishedByVisibility(@Param("visibility") EventVisibility visibility);

    @Query("SELECT e FROM Event e WHERE e.published = true AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:categoryId IS NULL OR e.category.id = :categoryId) AND (:status IS NULL OR e.status = :status) ORDER BY e.startDate ASC")
    List<Event> searchEvents(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("status") EventStatus status
    );

    @Query("SELECT e FROM Event e WHERE e.published = true AND e.startDate >= :now ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);

    long countByStatus(EventStatus status);

    @Query("SELECT e.category.name, COUNT(e) FROM Event e GROUP BY e.category.name")
    List<Object[]> countEventsByCategory();

    @Query("SELECT e FROM Event e ORDER BY e.currentRegistrations DESC LIMIT 5")
    List<Event> findTopPopularEvents();
}

