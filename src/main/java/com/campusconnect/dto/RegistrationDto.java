package com.campusconnect.dto;

import com.campusconnect.entity.RegistrationType;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RegistrationDto {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    private RegistrationType registrationType = RegistrationType.INDIVIDUAL;

    private String teamName;

    private List<TeamMemberDto> teamMembers = new ArrayList<>();

    public RegistrationDto() {}

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public RegistrationType getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(RegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<TeamMemberDto> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<TeamMemberDto> teamMembers) {
        this.teamMembers = teamMembers;
    }
}

