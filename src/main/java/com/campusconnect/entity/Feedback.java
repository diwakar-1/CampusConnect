package com.campusconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_feedback_event_user", columnNames = {"event_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_feedback_event", columnList = "event_id")
    }
)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Column(name = "organization_rating", nullable = false)
    private Integer organizationRating;

    @Column(name = "venue_rating", nullable = false)
    private Integer venueRating;

    @Column(name = "coordination_rating", nullable = false)
    private Integer coordinationRating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    public Feedback() {}

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    public double getAverageRating() {
        int sum = (overallRating != null ? overallRating : 0)
                + (organizationRating != null ? organizationRating : 0)
                + (venueRating != null ? venueRating : 0)
                + (coordinationRating != null ? coordinationRating : 0);
        return sum / 4.0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getOrganizationRating() {
        return organizationRating;
    }

    public void setOrganizationRating(Integer organizationRating) {
        this.organizationRating = organizationRating;
    }

    public Integer getVenueRating() {
        return venueRating;
    }

    public void setVenueRating(Integer venueRating) {
        this.venueRating = venueRating;
    }

    public Integer getCoordinationRating() {
        return coordinationRating;
    }

    public void setCoordinationRating(Integer coordinationRating) {
        this.coordinationRating = coordinationRating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}

