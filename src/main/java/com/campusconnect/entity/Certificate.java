package com.campusconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "certificates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cert_number", columnNames = {"certificate_number"}),
        @UniqueConstraint(name = "uk_reg_cert", columnNames = {"registration_id"})
    },
    indexes = {
        @Index(name = "idx_cert_event", columnList = "event_id"),
        @Index(name = "idx_cert_user", columnList = "user_id"),
        @Index(name = "idx_cert_token", columnList = "verification_token", unique = true)
    }
)
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 60)
    private String certificateNumber;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registration_id", nullable = false)
    private EventRegistration eventRegistration;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 40)
    private CertificateType certificateType = CertificateType.PARTICIPATION;

    @Column(name = "student_name", nullable = false, length = 120)
    private String studentName;

    @Column(name = "college_name", nullable = false, length = 180)
    private String collegeName;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "authorized_signatory", length = 120)
    private String authorizedSignatory = "Dean Student Affairs, CGC Mohali";

    @Column(name = "verification_token", nullable = false, unique = true, length = 100)
    private String verificationToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Certificate() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.issueDate == null) {
            this.issueDate = LocalDate.now();
        }
    }

    public String getTypeDisplayName() {
        return switch (certificateType) {
            case PARTICIPATION -> "Certificate of Participation";
            case WINNER -> "Certificate of Excellence (Winner)";
            case RUNNER_UP -> "Certificate of Merit (Runner-Up)";
            case ACHIEVEMENT -> "Certificate of Achievement";
        };
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public EventRegistration getEventRegistration() {
        return eventRegistration;
    }

    public void setEventRegistration(EventRegistration eventRegistration) {
        this.eventRegistration = eventRegistration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getAuthorizedSignatory() {
        return authorizedSignatory;
    }

    public void setAuthorizedSignatory(String authorizedSignatory) {
        this.authorizedSignatory = authorizedSignatory;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

