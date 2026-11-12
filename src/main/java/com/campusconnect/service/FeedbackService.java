package com.campusconnect.service;

import com.campusconnect.dto.FeedbackDto;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.Feedback;
import com.campusconnect.entity.User;
import com.campusconnect.exception.DuplicateRegistrationException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.FeedbackRepository;
import com.campusconnect.util.CsvExportUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           EventRepository eventRepository,
                           NotificationService notificationService) {
        this.feedbackRepository = feedbackRepository;
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Feedback submitFeedback(User user, FeedbackDto dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        if (feedbackRepository.existsByEventIdAndUserId(event.getId(), user.getId())) {
            throw new DuplicateRegistrationException("You have already submitted feedback for this event.");
        }

        Feedback feedback = new Feedback();
        feedback.setEvent(event);
        feedback.setUser(user);
        feedback.setOverallRating(dto.getOverallRating());
        feedback.setOrganizationRating(dto.getOrganizationRating());
        feedback.setVenueRating(dto.getVenueRating());
        feedback.setCoordinationRating(dto.getCoordinationRating());
        feedback.setComments(dto.getComments());

        Feedback saved = feedbackRepository.save(feedback);

        notificationService.notifyUser(
                user,
                "Thank you for your feedback!",
                "Your review for '" + event.getTitle() + "' has been recorded. We appreciate your valuable suggestions.",
                "/events/" + event.getId(),
                "INFO"
        );

        return saved;
    }

    public boolean hasUserSubmitted(Long eventId, Long userId) {
        return feedbackRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public List<Feedback> getEventFeedbacks(Long eventId) {
        return feedbackRepository.findByEventIdOrderBySubmittedAtDesc(eventId);
    }

    public List<Feedback> getUserFeedbacks(Long userId) {
        return feedbackRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public Double getAverageRating(Long eventId) {
        Double avg = feedbackRepository.getAverageOverallRatingByEventId(eventId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public Double getGlobalAverageRating() {
        Double avg = feedbackRepository.getGlobalAverageOverallRating();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public String exportFeedbackCsv(Long eventId) {
        List<Feedback> list = (eventId != null)
                ? feedbackRepository.findByEventIdOrderBySubmittedAtDesc(eventId)
                : feedbackRepository.findAll();
        return CsvExportUtil.exportFeedbackCsv(list);
    }
}

