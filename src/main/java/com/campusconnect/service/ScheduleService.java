package com.campusconnect.service;

import com.campusconnect.dto.EventScheduleDto;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventSchedule;
import com.campusconnect.entity.Venue;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.EventScheduleRepository;
import com.campusconnect.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleService {

    private final EventScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public ScheduleService(EventScheduleRepository scheduleRepository,
                           EventRepository eventRepository,
                           VenueRepository venueRepository) {
        this.scheduleRepository = scheduleRepository;
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventSchedule> getSchedulesByEventId(Long eventId) {
        return scheduleRepository.findByEventIdOrderByStartTimeAsc(eventId);
    }

    public List<EventSchedule> getSchedulesForUser(Long userId) {
        return scheduleRepository.findSchedulesForRegisteredUser(userId);
    }

    public EventSchedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
    }

    @Transactional
    public EventSchedule createSchedule(EventScheduleDto dto) {
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Session start time must be before end time.");
        }

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        Venue venue = null;
        if (dto.getVenueId() != null) {
            venue = venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + dto.getVenueId()));

            // Conflict detection for overlapping venue bookings!
            List<EventSchedule> conflicts = scheduleRepository.findVenueConflicts(venue.getId(), dto.getStartTime(), dto.getEndTime(), null);
            if (!conflicts.isEmpty()) {
                EventSchedule c = conflicts.get(0);
                throw new IllegalArgumentException("Venue Conflict: " + venue.getName() + " is already booked for '"
                        + c.getSessionTitle() + "' (" + c.getEvent().getTitle() + ") between "
                        + c.getStartTime().toLocalTime() + " and " + c.getEndTime().toLocalTime() + ".");
            }
        }

        EventSchedule schedule = new EventSchedule();
        schedule.setEvent(event);
        schedule.setSessionTitle(dto.getSessionTitle().trim());
        schedule.setSessionDescription(dto.getSessionDescription());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setVenue(venue);
        schedule.setCoordinatorName(dto.getCoordinatorName());

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public EventSchedule updateSchedule(Long id, EventScheduleDto dto) {
        EventSchedule schedule = getScheduleById(id);

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Session start time must be before end time.");
        }

        Venue venue = null;
        if (dto.getVenueId() != null) {
            venue = venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + dto.getVenueId()));

            // Conflict detection excluding current schedule id
            List<EventSchedule> conflicts = scheduleRepository.findVenueConflicts(venue.getId(), dto.getStartTime(), dto.getEndTime(), id);
            if (!conflicts.isEmpty()) {
                EventSchedule c = conflicts.get(0);
                throw new IllegalArgumentException("Venue Conflict: " + venue.getName() + " is already booked for '"
                        + c.getSessionTitle() + "' (" + c.getEvent().getTitle() + ") between "
                        + c.getStartTime().toLocalTime() + " and " + c.getEndTime().toLocalTime() + ".");
            }
        }

        schedule.setSessionTitle(dto.getSessionTitle().trim());
        schedule.setSessionDescription(dto.getSessionDescription());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setVenue(venue);
        schedule.setCoordinatorName(dto.getCoordinatorName());

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    @Transactional
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    @Transactional
    public Venue updateVenue(Long id, Venue updated) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        venue.setName(updated.getName());
        venue.setBuilding(updated.getBuilding());
        venue.setRoomNumber(updated.getRoomNumber());
        venue.setCapacity(updated.getCapacity());
        venue.setFacilities(updated.getFacilities());
        venue.setActive(updated.isActive());
        return venueRepository.save(venue);
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
    }
}

