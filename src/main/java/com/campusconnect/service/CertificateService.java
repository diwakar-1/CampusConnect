package com.campusconnect.service;

import com.campusconnect.entity.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AttendanceRepository;
import com.campusconnect.repository.CertificateRepository;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.util.PdfCertificateGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final EventRegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    @Value("${campusconnect.app.base-url:http://localhost:8080}")
    private String baseUrl;

    public CertificateService(CertificateRepository certificateRepository,
                              EventRegistrationRepository registrationRepository,
                              AttendanceRepository attendanceRepository,
                              NotificationService notificationService) {
        this.certificateRepository = certificateRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationService = notificationService;
    }

    public List<Certificate> getUserCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIssueDateDesc(userId);
    }

    public List<Certificate> getEventCertificates(Long eventId) {
        return certificateRepository.findByEventId(eventId);
    }

    public Certificate getCertificateById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }

    public Certificate getCertificateByToken(String token) {
        return certificateRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("No valid certificate matches verification code: " + token));
    }

    public Certificate getCertificateByNumber(String number) {
        return certificateRepository.findByCertificateNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("No certificate found with ID: " + number));
    }

    @Transactional
    public Certificate issueCertificate(Long registrationId, CertificateType type, String signatory) {
        EventRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + registrationId));

        if (certificateRepository.existsByEventRegistrationId(registrationId)) {
            return certificateRepository.findByEventRegistrationId(registrationId).get();
        }

        String certNumber = String.format("CGC-CERT-%d-%05d", LocalDate.now().getYear(), 10000 + (int)(Math.random() * 90000));
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        Certificate cert = new Certificate();
        cert.setCertificateNumber(certNumber);
        cert.setEventRegistration(reg);
        cert.setUser(reg.getUser());
        cert.setEvent(reg.getEvent());
        cert.setCertificateType(type != null ? type : CertificateType.PARTICIPATION);
        cert.setStudentName(reg.getUser().getFullName());
        cert.setCollegeName(reg.getUser().getEffectiveCollegeName());
        cert.setIssueDate(LocalDate.now());
        cert.setAuthorizedSignatory(signatory != null && !signatory.isBlank() ? signatory : "Dean Student Affairs, CGC Mohali");
        cert.setVerificationToken(token);

        Certificate saved = certificateRepository.save(cert);

        notificationService.notifyUser(
                reg.getUser(),
                "Certificate Issued!",
                "Your " + saved.getTypeDisplayName() + " for '" + reg.getEvent().getTitle() + "' is now ready for download.",
                "/student/certificates",
                "CERTIFICATE"
        );

        return saved;
    }

    @Transactional
    public List<Certificate> bulkIssueCertificates(Long eventId, CertificateType type, String signatory) {
        List<Attendance> attendances = attendanceRepository.findByEventId(eventId);
        List<Certificate> generated = new ArrayList<>();

        for (Attendance att : attendances) {
            EventRegistration reg = att.getEventRegistration();
            if (reg != null && !certificateRepository.existsByEventRegistrationId(reg.getId())) {
                Certificate cert = issueCertificate(reg.getId(), type, signatory);
                generated.add(cert);
            }
        }
        return generated;
    }

    public byte[] generatePdf(Long certificateId) {
        Certificate cert = getCertificateById(certificateId);
        return PdfCertificateGenerator.generateCertificatePdf(cert, baseUrl);
    }

    public long countTotalCertificates() {
        return certificateRepository.count();
    }
}

