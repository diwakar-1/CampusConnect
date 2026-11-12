package com.campusconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_status", columnList = "status"),
    @Index(name = "idx_event_visibility", columnList = "visibility"),
    @Index(name = "idx_event_published", columnList = "published")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(name = "custom_venue_details", length = 200)
    private String customVenueDetails;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "registration_start_date")
    private LocalDateTime registrationStartDate;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity = 100;

    @Column(name = "current_registrations", nullable = false)
    private Integer currentRegistrations = 0;

    @Column(name = "registration_fee", nullable = false)
    private Double registrationFee = 0.0;

    @Column(name = "coordinator_name", length = 120)
    private String coordinatorName;

    @Column(name = "coordinator_email", length = 150)
    private String coordinatorEmail;

    @Column(name = "coordinator_phone", length = 30)
    private String coordinatorPhone;

    @Column(name = "eligibility_requirements", length = 500)
    private String eligibilityRequirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventVisibility visibility = EventVisibility.ALL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status = EventStatus.UPCOMING;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "allow_team_registration", nullable = false)
    private boolean allowTeamRegistration = false;

    @Column(name = "min_team_size")
    private Integer minTeamSize = 1;

    @Column(name = "max_team_size")
    private Integer maxTeamSize = 4;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Event() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.currentRegistrations == null) {
            this.currentRegistrations = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRegistrationOpen() {
        if (!published || status == EventStatus.CANCELLED || status == EventStatus.COMPLETED) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (registrationStartDate != null && now.isBefore(registrationStartDate)) {
            return false;
        }
        return registrationDeadline != null && now.isBefore(registrationDeadline);
    }

    public boolean isFull() {
        return maxCapacity != null && currentRegistrations != null && currentRegistrations >= maxCapacity;
    }

    public int getAvailableSpots() {
        if (maxCapacity == null) return 0;
        int current = currentRegistrations != null ? currentRegistrations : 0;
        return Math.max(0, maxCapacity - current);
    }

    public String getVenueDisplayName() {
        if (venue != null) {
            return venue.getDisplayName();
        }
        return (customVenueDetails != null && !customVenueDetails.isBlank()) ? customVenueDetails : "TBA";
    }

    public boolean isEligibleFor(User user) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        if (visibility == EventVisibility.ALL) return true;
        if (visibility == EventVisibility.INTERNAL_ONLY && user.isInternalStudent()) return true;
        if (visibility == EventVisibility.EXTERNAL_ONLY && user.isExternalStudent()) return true;
        return false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public String getCustomVenueDetails() {
        return customVenueDetails;
    }

    public void setCustomVenueDetails(String customVenueDetails) {
        this.customVenueDetails = customVenueDetails;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(LocalDateTime registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentRegistrations() {
        return currentRegistrations;
    }

    public void setCurrentRegistrations(Integer currentRegistrations) {
        this.currentRegistrations = currentRegistrations;
    }

    public Double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
    }

    public String getCoordinatorName() {
        return coordinatorName;
    }

    public void setCoordinatorName(String coordinatorName) {
        this.coordinatorName = coordinatorName;
    }

    public String getCoordinatorEmail() {
        return coordinatorEmail;
    }

    public void setCoordinatorEmail(String coordinatorEmail) {
        this.coordinatorEmail = coordinatorEmail;
    }

    public String getCoordinatorPhone() {
        return coordinatorPhone;
    }

    public void setCoordinatorPhone(String coordinatorPhone) {
        this.coordinatorPhone = coordinatorPhone;
    }

    public String getEligibilityRequirements() {
        return eligibilityRequirements;
    }

    public void setEligibilityRequirements(String eligibilityRequirements) {
        this.eligibilityRequirements = eligibilityRequirements;
    }

    public EventVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(EventVisibility visibility) {
        this.visibility = visibility;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isAllowTeamRegistration() {
        return allowTeamRegistration;
    }

    public void setAllowTeamRegistration(boolean allowTeamRegistration) {
        this.allowTeamRegistration = allowTeamRegistration;
    }

    public Integer getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(Integer minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

