package com.campusconnect.repository;

import com.campusconnect.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    Optional<Certificate> findByVerificationToken(String verificationToken);

    Optional<Certificate> findByEventRegistrationId(Long registrationId);

    boolean existsByEventRegistrationId(Long registrationId);

    List<Certificate> findByUserIdOrderByIssueDateDesc(Long userId);

    List<Certificate> findByEventId(Long eventId);

    long countByEventId(Long eventId);
}

