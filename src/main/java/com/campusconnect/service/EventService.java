package com.campusconnect.service;

import com.campusconnect.dto.EventDto;
import com.campusconnect.entity.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.EventCategoryRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final NotificationService notificationService;

    public EventService(EventRepository eventRepository,
                        EventCategoryRepository categoryRepository,
                        VenueRepository venueRepository,
                        NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.venueRepository = venueRepository;
        this.notificationService = notificationService;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getPublishedEvents() {
        return eventRepository.findByPublishedTrueOrderByStartDateAsc();
    }

    public List<Event> getEventsForUser(User user) {
        if (user == null || user.isAdmin()) {
            return getPublishedEvents();
        }
        if (user.isInternalStudent()) {
            return eventRepository.findPublishedByVisibility(EventVisibility.INTERNAL_ONLY);
        }
        if (user.isExternalStudent()) {
            return eventRepository.findPublishedByVisibility(EventVisibility.EXTERNAL_ONLY);
        }
        return getPublishedEvents();
    }

    public List<Event> searchEvents(String keyword, Long categoryId, EventStatus status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return eventRepository.searchEvents(cleanKeyword, categoryId, status);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public List<EventCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public List<Venue> getActiveVenues() {
        return venueRepository.findByActiveTrue();
    }

    @Transactional
    public Event createEvent(EventDto dto) {
        EventCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Venue venue = null;
        if (dto.getVenueId() != null) {
            venue = venueRepository.findById(dto.getVenueId()).orElse(null);
        }

        Event event = new Event();
        mapDtoToEvent(dto, event, category, venue);

        Event saved = eventRepository.save(event);

        // Broadcast notification about new event
        notificationService.broadcastAnnouncement(
                "New Event Announced: " + saved.getTitle(),
                "A new event '" + saved.getTitle() + "' has been published. Registration deadline is " + saved.getRegistrationDeadline().toLocalDate() + ".",
                "/events/" + saved.getId()
        );

        return saved;
    }

    @Transactional
    public Event updateEvent(Long id, EventDto dto) {
        Event event = getEventById(id);
        EventCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Venue venue = null;
        if (dto.getVenueId() != null) {
            venue = venueRepository.findById(dto.getVenueId()).orElse(null);
        }

        mapDtoToEvent(dto, event, category, venue);
        return eventRepository.save(event);
    }

    private void mapDtoToEvent(EventDto dto, Event event, EventCategory category, Venue venue) {
        event.setTitle(dto.getTitle().trim());
        event.setDescription(dto.getDescription());
        event.setBannerImageUrl(dto.getBannerImageUrl());
        event.setCategory(category);
        event.setVenue(venue);
        event.setCustomVenueDetails(dto.getCustomVenueDetails());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setRegistrationStartDate(dto.getRegistrationStartDate());
        event.setRegistrationDeadline(dto.getRegistrationDeadline());
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setRegistrationFee(dto.getRegistrationFee() != null ? dto.getRegistrationFee() : 0.0);
        event.setCoordinatorName(dto.getCoordinatorName());
        event.setCoordinatorEmail(dto.getCoordinatorEmail());
        event.setCoordinatorPhone(dto.getCoordinatorPhone());
        event.setEligibilityRequirements(dto.getEligibilityRequirements());
        event.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : EventVisibility.ALL);
        if (dto.getStatus() != null) {
            event.setStatus(dto.getStatus());
        }
        event.setPublished(dto.isPublished());
        event.setAllowTeamRegistration(dto.isAllowTeamRegistration());
        event.setMinTeamSize(dto.getMinTeamSize() != null ? dto.getMinTeamSize() : 1);
        event.setMaxTeamSize(dto.getMaxTeamSize() != null ? dto.getMaxTeamSize() : 4);
    }

    @Transactional
    public void publishEvent(Long id) {
        Event event = getEventById(id);
        event.setPublished(true);
        eventRepository.save(event);
    }

    @Transactional
    public void unpublishEvent(Long id) {
        Event event = getEventById(id);
        event.setPublished(false);
        eventRepository.save(event);
    }

    @Transactional
    public void updateStatus(Long id, EventStatus status) {
        Event event = getEventById(id);
        event.setStatus(status);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    public List<Event> getTopPopularEvents() {
        return eventRepository.findTopPopularEvents();
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now());
    }

    public long countTotalEvents() {
        return eventRepository.count();
    }

    public long countUpcomingEvents() {
        return eventRepository.countByStatus(EventStatus.UPCOMING);
    }
}

