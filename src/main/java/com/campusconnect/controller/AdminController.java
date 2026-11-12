package com.campusconnect.controller;

import com.campusconnect.dto.EventDto;
import com.campusconnect.dto.EventScheduleDto;
import com.campusconnect.entity.*;
import com.campusconnect.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AnalyticsService analyticsService;
    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;
    private final CertificateService certificateService;
    private final FeedbackService feedbackService;

    public AdminController(AnalyticsService analyticsService,
                           EventService eventService,
                           UserService userService,
                           RegistrationService registrationService,
                           ScheduleService scheduleService,
                           AttendanceService attendanceService,
                           CertificateService certificateService,
                           FeedbackService feedbackService) {
        this.analyticsService = analyticsService;
        this.eventService = eventService;
        this.userService = userService;
        this.registrationService = registrationService;
        this.scheduleService = scheduleService;
        this.attendanceService = attendanceService;
        this.certificateService = certificateService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = analyticsService.getAdminDashboardStats();
        List<User> pendingApprovals = userService.getPendingExternalStudents();
        List<Event> popularEvents = eventService.getTopPopularEvents();
        List<EventRegistration> recentRegistrations = registrationService.getAllRegistrations();

        model.addAttribute("stats", stats);
        model.addAttribute("pendingApprovals", pendingApprovals);
        model.addAttribute("popularEvents", popularEvents);
        model.addAttribute("recentRegistrations", recentRegistrations.size() > 6 ? recentRegistrations.subList(0, 6) : recentRegistrations);

        // Chart Data
        model.addAttribute("regChartData", analyticsService.getEventRegistrationChartData());
        model.addAttribute("catChartData", analyticsService.getCategoryDistributionChartData());

        return "admin/dashboard";
    }

    // --- EVENTS MANAGEMENT ---
    @GetMapping("/events")
    public String listEvents(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/events";
    }

    @GetMapping("/events/new")
    public String showCreateEventForm(Model model) {
        if (!model.containsAttribute("eventDto")) {
            model.addAttribute("eventDto", new EventDto());
        }
        model.addAttribute("categories", eventService.getAllCategories());
        model.addAttribute("venues", eventService.getActiveVenues());
        return "admin/event-form";
    }

    @PostMapping("/events/new")
    public String createEvent(@Valid @ModelAttribute("eventDto") EventDto eventDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (eventDto.getStartDate() != null && eventDto.getEndDate() != null && eventDto.getStartDate().isAfter(eventDto.getEndDate())) {
            bindingResult.rejectValue("endDate", "error.eventDto", "End date must be after start date");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", eventService.getAllCategories());
            model.addAttribute("venues", eventService.getActiveVenues());
            return "admin/event-form";
        }

        Event saved = eventService.createEvent(eventDto);
        redirectAttributes.addFlashAttribute("successMessage", "Event '" + saved.getTitle() + "' created successfully!");
        return "redirect:/admin/events";
    }

    @GetMapping("/events/edit/{id}")
    public String showEditEventForm(@PathVariable("id") Long id, Model model) {
        Event event = eventService.getEventById(id);
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setBannerImageUrl(event.getBannerImageUrl());
        dto.setCategoryId(event.getCategory().getId());
        dto.setVenueId(event.getVenue() != null ? event.getVenue().getId() : null);
        dto.setCustomVenueDetails(event.getCustomVenueDetails());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setRegistrationStartDate(event.getRegistrationStartDate());
        dto.setRegistrationDeadline(event.getRegistrationDeadline());
        dto.setMaxCapacity(event.getMaxCapacity());
        dto.setRegistrationFee(event.getRegistrationFee());
        dto.setCoordinatorName(event.getCoordinatorName());
        dto.setCoordinatorEmail(event.getCoordinatorEmail());
        dto.setCoordinatorPhone(event.getCoordinatorPhone());
        dto.setEligibilityRequirements(event.getEligibilityRequirements());
        dto.setVisibility(event.getVisibility());
        dto.setStatus(event.getStatus());
        dto.setPublished(event.isPublished());
        dto.setAllowTeamRegistration(event.isAllowTeamRegistration());
        dto.setMinTeamSize(event.getMinTeamSize());
        dto.setMaxTeamSize(event.getMaxTeamSize());

        model.addAttribute("eventDto", dto);
        model.addAttribute("categories", eventService.getAllCategories());
        model.addAttribute("venues", eventService.getActiveVenues());
        return "admin/event-form";
    }

    @PostMapping("/events/edit/{id}")
    public String updateEvent(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("eventDto") EventDto eventDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", eventService.getAllCategories());
            model.addAttribute("venues", eventService.getActiveVenues());
            return "admin/event-form";
        }

        Event updated = eventService.updateEvent(id, eventDto);
        redirectAttributes.addFlashAttribute("successMessage", "Event '" + updated.getTitle() + "' updated successfully!");
        return "redirect:/admin/events";
    }

    @PostMapping("/events/toggle-publish/{id}")
    public String togglePublish(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Event event = eventService.getEventById(id);
        if (event.isPublished()) {
            eventService.unpublishEvent(id);
            redirectAttributes.addFlashAttribute("infoMessage", "Event unpublished.");
        } else {
            eventService.publishEvent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Event published.");
        }
        return "redirect:/admin/events";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @GetMapping("/events/{id}")
    public String viewAdminEventDetails(@PathVariable("id") Long id, Model model) {
        Event event = eventService.getEventById(id);
        List<EventSchedule> schedules = scheduleService.getSchedulesByEventId(id);
        List<EventRegistration> registrations = registrationService.getEventRegistrations(id);
        List<Attendance> attendances = attendanceService.getAttendanceByEvent(id);
        List<Feedback> feedbacks = feedbackService.getEventFeedbacks(id);
        Double avgRating = feedbackService.getAverageRating(id);

        model.addAttribute("event", event);
        model.addAttribute("schedules", schedules);
        model.addAttribute("registrations", registrations);
        model.addAttribute("attendances", attendances);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("averageRating", avgRating);
        model.addAttribute("venues", eventService.getActiveVenues());
        model.addAttribute("newSchedule", new EventScheduleDto());

        return "admin/event-details";
    }

    // --- EXTERNAL STUDENT APPROVALS ---
    @GetMapping("/external-approvals")
    public String externalApprovals(Model model) {
        model.addAttribute("pendingList", userService.getPendingExternalStudents());
        return "admin/external-approvals";
    }

    @PostMapping("/external-approvals/{id}/approve")
    public String approveExternal(@PathVariable("id") Long id,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  RedirectAttributes redirectAttributes) {
        userService.approveExternalStudent(id, notes);
        redirectAttributes.addFlashAttribute("successMessage", "External student account approved. Access granted!");
        return "redirect:/admin/external-approvals";
    }

    @PostMapping("/external-approvals/{id}/reject")
    public String rejectExternal(@PathVariable("id") Long id,
                                 @RequestParam(value = "notes", required = false) String notes,
                                 RedirectAttributes redirectAttributes) {
        userService.rejectExternalStudent(id, notes);
        redirectAttributes.addFlashAttribute("infoMessage", "External student application rejected.");
        return "redirect:/admin/external-approvals";
    }

    // --- REGISTRATIONS ---
    @GetMapping("/registrations")
    public String allRegistrations(@RequestParam(value = "eventId", required = false) Long eventId, Model model) {
        List<EventRegistration> list = (eventId != null)
                ? registrationService.getEventRegistrations(eventId)
                : registrationService.getAllRegistrations();

        model.addAttribute("registrations", list);
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("selectedEventId", eventId);
        return "admin/registrations";
    }

    // --- VENUES ---
    @GetMapping("/venues")
    public String venues(Model model) {
        model.addAttribute("venues", eventService.getAllVenues());
        model.addAttribute("newVenue", new Venue());
        return "admin/venues";
    }

    @PostMapping("/venues/new")
    public String createVenue(@ModelAttribute("newVenue") Venue venue, RedirectAttributes redirectAttributes) {
        scheduleService.createVenue(venue);
        redirectAttributes.addFlashAttribute("successMessage", "Venue '" + venue.getName() + "' created successfully!");
        return "redirect:/admin/venues";
    }

    // --- SCHEDULE SESSIONS ---
    @PostMapping("/schedules/new")
    public String addScheduleSession(@ModelAttribute("newSchedule") EventScheduleDto dto,
                                     RedirectAttributes redirectAttributes) {
        try {
            scheduleService.createSchedule(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Session added to schedule successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/events/" + dto.getEventId();
    }

    @PostMapping("/schedules/delete/{id}")
    public String deleteScheduleSession(@PathVariable("id") Long id,
                                        @RequestParam("eventId") Long eventId,
                                        RedirectAttributes redirectAttributes) {
        scheduleService.deleteSchedule(id);
        redirectAttributes.addFlashAttribute("successMessage", "Session deleted.");
        return "redirect:/admin/events/" + eventId;
    }

    // --- STUDENTS DIRECTORY ---
    @GetMapping("/students")
    public String studentDirectory(Model model) {
        model.addAttribute("students", userService.getAllStudents());
        return "admin/students";
    }
}

