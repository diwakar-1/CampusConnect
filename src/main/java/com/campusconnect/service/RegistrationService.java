package com.campusconnect.service;

import com.campusconnect.dto.RegistrationDto;
import com.campusconnect.dto.TeamMemberDto;
import com.campusconnect.entity.*;
import com.campusconnect.exception.DuplicateRegistrationException;
import com.campusconnect.exception.EventFullException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.TeamRepository;
import com.campusconnect.util.CsvExportUtil;
import com.campusconnect.util.QrCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    public RegistrationService(EventRegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               TeamRepository teamRepository,
                               NotificationService notificationService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
    }

    public List<EventRegistration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserIdOrderByRegisteredAtDesc(userId);
    }

    public List<EventRegistration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventIdOrderByRegisteredAtDesc(eventId);
    }

    public List<EventRegistration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public EventRegistration getRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));
    }

    public EventRegistration getRegistrationByNumber(String regNumber) {
        return registrationRepository.findByRegistrationNumber(regNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with code: " + regNumber));
    }

    public boolean isUserRegistered(Long eventId, Long userId) {
        return registrationRepository.existsByEventIdAndUserIdAndStatusNot(eventId, userId, RegistrationStatus.CANCELLED);
    }

    @Transactional
    public EventRegistration registerIndividual(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        validateRegistrationPreconditions(user, event);

        // Check if already registered
        if (isUserRegistered(eventId, user.getId())) {
            throw new DuplicateRegistrationException("You are already registered for this event.");
        }

        RegistrationStatus status = RegistrationStatus.CONFIRMED;
        if (event.isFull()) {
            status = RegistrationStatus.WAITLISTED;
        } else {
            event.setCurrentRegistrations(event.getCurrentRegistrations() + 1);
            eventRepository.save(event);
        }

        String regNumber = generateRegistrationNumber(event.getId());
        String qrPayload = "CC-PASS:" + regNumber + ":" + event.getId() + ":" + user.getId();
        String qrCodeBase64 = QrCodeGenerator.generateQrCodeBase64(qrPayload, 260, 260);

        EventRegistration reg = new EventRegistration();
        reg.setEvent(event);
        reg.setUser(user);
        reg.setRegistrationNumber(regNumber);
        reg.setRegistrationType(RegistrationType.INDIVIDUAL);
        reg.setStatus(status);
        reg.setQrCodeData(qrCodeBase64);

        EventRegistration saved = registrationRepository.save(reg);

        String title = (status == RegistrationStatus.CONFIRMED) ? "Registration Confirmed!" : "Placed on Waitlist";
        String message = (status == RegistrationStatus.CONFIRMED)
                ? "You have successfully registered for " + event.getTitle() + ". Your pass ID is " + regNumber + "."
                : "The event is currently full. You have been placed on the priority waitlist (" + regNumber + ").";

        notificationService.notifyUser(user, title, message, "/student/registrations", "SUCCESS");

        return saved;
    }

    @Transactional
    public EventRegistration registerTeam(User leader, RegistrationDto dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        if (!event.isAllowTeamRegistration()) {
            throw new IllegalArgumentException("Team registration is not enabled for this event.");
        }

        validateRegistrationPreconditions(leader, event);

        if (isUserRegistered(event.getId(), leader.getId())) {
            throw new DuplicateRegistrationException("You are already registered for this event.");
        }

        if (dto.getTeamName() == null || dto.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("Team name is required for team registration.");
        }

        List<TeamMemberDto> members = dto.getTeamMembers();
        int totalMembers = (members != null ? members.size() : 0) + 1; // leader + members

        if (event.getMinTeamSize() != null && totalMembers < event.getMinTeamSize()) {
            throw new IllegalArgumentException("Team must have at least " + event.getMinTeamSize() + " members.");
        }
        if (event.getMaxTeamSize() != null && totalMembers > event.getMaxTeamSize()) {
            throw new IllegalArgumentException("Team cannot exceed " + event.getMaxTeamSize() + " members.");
        }

        RegistrationStatus status = RegistrationStatus.CONFIRMED;
        if (event.isFull()) {
            status = RegistrationStatus.WAITLISTED;
        } else {
            event.setCurrentRegistrations(event.getCurrentRegistrations() + 1);
            eventRepository.save(event);
        }

        // Build Team
        Team team = new Team(dto.getTeamName().trim(), event, leader);
        if (members != null) {
            for (TeamMemberDto m : members) {
                if (m.getFullName() != null && !m.getFullName().isBlank()) {
                    TeamMember member = new TeamMember(
                            m.getFullName().trim(),
                            m.getEmail().trim(),
                            m.getRollNumber(),
                            m.getCollegeName() != null ? m.getCollegeName() : leader.getEffectiveCollegeName(),
                            m.getPhone()
                    );
                    team.addMember(member);
                }
            }
        }

        String regNumber = generateRegistrationNumber(event.getId());
        String qrPayload = "CC-PASS:" + regNumber + ":" + event.getId() + ":" + leader.getId() + ":TEAM";
        String qrCodeBase64 = QrCodeGenerator.generateQrCodeBase64(qrPayload, 260, 260);

        EventRegistration reg = new EventRegistration();
        reg.setEvent(event);
        reg.setUser(leader);
        reg.setRegistrationNumber(regNumber);
        reg.setRegistrationType(RegistrationType.TEAM);
        reg.setStatus(status);
        reg.setQrCodeData(qrCodeBase64);
        reg.setTeam(team);

        EventRegistration saved = registrationRepository.save(reg);

        String title = (status == RegistrationStatus.CONFIRMED) ? "Team Registered: " + team.getTeamName() : "Team Waitlisted";
        String message = "Your team '" + team.getTeamName() + "' has been registered for " + event.getTitle() + ". Registration ID: " + regNumber;
        notificationService.notifyUser(leader, title, message, "/student/registrations", "SUCCESS");

        return saved;
    }

    @Transactional
    public void cancelRegistration(Long registrationId, User user) {
        EventRegistration reg = getRegistrationById(registrationId);

        if (!user.isAdmin() && !reg.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to cancel this registration.");
        }

        if (reg.getStatus() == RegistrationStatus.CANCELLED) {
            return;
        }

        boolean wasConfirmed = reg.getStatus() == RegistrationStatus.CONFIRMED;
        reg.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(reg);

        Event event = reg.getEvent();

        if (wasConfirmed && event.getCurrentRegistrations() > 0) {
            event.setCurrentRegistrations(event.getCurrentRegistrations() - 1);
            eventRepository.save(event);

            // Automatic waitlist promotion!
            List<EventRegistration> waitlist = registrationRepository.findWaitlistedForEvent(event.getId());
            if (!waitlist.isEmpty()) {
                EventRegistration promoted = waitlist.get(0);
                promoted.setStatus(RegistrationStatus.CONFIRMED);
                event.setCurrentRegistrations(event.getCurrentRegistrations() + 1);
                eventRepository.save(event);
                registrationRepository.save(promoted);

                notificationService.notifyUser(
                        promoted.getUser(),
                        "Waitlist Promotion: Registration Confirmed!",
                        "A spot opened up for " + event.getTitle() + "! Your registration is now CONFIRMED. Check your dashboard for your pass.",
                        "/student/registrations",
                        "SUCCESS"
                );
            }
        }

        notificationService.notifyUser(
                reg.getUser(),
                "Registration Cancelled",
                "Your registration for " + event.getTitle() + " has been cancelled.",
                "/student/registrations",
                "INFO"
        );
    }

    private void validateRegistrationPreconditions(User user, Event event) {
        if (!event.isPublished()) {
            throw new IllegalArgumentException("This event is not published.");
        }
        if (!event.isRegistrationOpen()) {
            throw new IllegalArgumentException("Registration for this event is closed or deadline has passed.");
        }
        if (!event.isEligibleFor(user)) {
            throw new IllegalArgumentException("You are not eligible for this event based on participant type restrictions.");
        }
    }

    private String generateRegistrationNumber(Long eventId) {
        int randomSuffix = 1000 + (int)(Math.random() * 9000);
        return String.format("CC-%d-%03d-%d", LocalDateTime.now().getYear(), eventId, randomSuffix);
    }

    public String exportRegistrationsCsv(Long eventId) {
        List<EventRegistration> list = (eventId != null)
                ? registrationRepository.findByEventIdOrderByRegisteredAtDesc(eventId)
                : registrationRepository.findAll();
        return CsvExportUtil.exportRegistrationsCsv(list);
    }

    public long countTotalRegistrations() {
        return registrationRepository.countByStatus(RegistrationStatus.CONFIRMED);
    }
}

