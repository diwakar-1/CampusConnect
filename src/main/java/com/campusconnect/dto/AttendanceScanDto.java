package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AttendanceScanDto {

    @NotBlank(message = "Registration code or QR code is required")
    private String qrCode;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    private String notes;

    public AttendanceScanDto() {}

    public AttendanceScanDto(String qrCode, Long eventId) {
        this.qrCode = qrCode;
        this.eventId = eventId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

