package com.campusconnect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FeedbackDto {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer overallRating = 5;

    @NotNull(message = "Organization rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer organizationRating = 5;

    @NotNull(message = "Venue rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer venueRating = 5;

    @NotNull(message = "Coordination rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer coordinationRating = 5;

    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;

    public FeedbackDto() {}

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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
}

