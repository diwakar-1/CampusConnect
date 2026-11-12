package com.campusconnect.controller;

import com.campusconnect.dto.RegistrationDto;
import com.campusconnect.entity.*;
import com.campusconnect.exception.DuplicateRegistrationException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final ScheduleService scheduleService;
    private final FeedbackService feedbackService;
    private final UserService userService;

    public EventController(EventService eventService,
                           RegistrationService registrationService,
                           ScheduleService scheduleService,
                           FeedbackService feedbackService,
                           UserService userService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.scheduleService = scheduleService;
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

    @GetMapping
    public String listEvents(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "category", required = false) Long categoryId,
                             @RequestParam(value = "status", required = false) EventStatus status,
                             Model model) {
        List<Event> events;
        if (keyword != null || categoryId != null || status != null) {
            events = eventService.searchEvents(keyword, categoryId, status);
        } else {
            User user = userService.getCurrentUser();
            events = eventService.getEventsForUser(user);
        }

        model.addAttribute("events", events);
        model.addAttribute("categories", eventService.getAllCategories());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedStatus", status);

        return "events/list";
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable("id") Long id, Model model) {
        Event event = eventService.getEventById(id);
        List<EventSchedule> schedules = scheduleService.getSchedulesByEventId(id);
        List<Feedback> feedbacks = feedbackService.getEventFeedbacks(id);
        Double avgRating = feedbackService.getAverageRating(id);

        User currentUser = userService.getCurrentUser();
        boolean isRegistered = false;
        boolean hasSubmittedFeedback = false;
        EventRegistration userRegistration = null;

        if (currentUser != null) {
            isRegistered = registrationService.isUserRegistered(id, currentUser.getId());
            hasSubmittedFeedback = feedbackService.hasUserSubmitted(id, currentUser.getId());
            if (isRegistered) {
                userRegistration = registrationService.getUserRegistrations(currentUser.getId()).stream()
                        .filter(r -> r.getEvent().getId().equals(id) && r.getStatus() != RegistrationStatus.CANCELLED)
                        .findFirst().orElse(null);
            }
        }

        model.addAttribute("event", event);
        model.addAttribute("schedules", schedules);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("averageRating", avgRating);
        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("userRegistration", userRegistration);
        model.addAttribute("hasSubmittedFeedback", hasSubmittedFeedback);

        return "events/details";
    }

    @PostMapping("/{id}/register")
    public String registerIndividual(@PathVariable("id") Long id,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            EventRegistration reg = registrationService.registerIndividual(currentUser, id);
            redirectAttributes.addFlashAttribute("successMessage", "Registered successfully for " + reg.getEvent().getTitle() + "!");
            return "redirect:/events/registration-success/" + reg.getId();
        } catch (DuplicateRegistrationException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/events/" + id;
        }
    }

    @GetMapping("/{id}/team-register")
    public String showTeamRegisterForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Event event = eventService.getEventById(id);
        if (!event.isAllowTeamRegistration()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Team registration is not supported for this event.");
            return "redirect:/events/" + id;
        }

        if (registrationService.isUserRegistered(id, currentUser.getId())) {
            redirectAttributes.addFlashAttribute("warningMessage", "You are already registered for this event.");
            return "redirect:/events/" + id;
        }

        RegistrationDto dto = new RegistrationDto();
        dto.setEventId(id);
        dto.setRegistrationType(RegistrationType.TEAM);

        model.addAttribute("event", event);
        model.addAttribute("registrationDto", dto);
        return "events/team-register";
    }

    @PostMapping("/{id}/team-register")
    public String processTeamRegister(@PathVariable("id") Long id,
                                      @ModelAttribute("registrationDto") RegistrationDto dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        dto.setEventId(id);
        dto.setRegistrationType(RegistrationType.TEAM);

        try {
            EventRegistration reg = registrationService.registerTeam(currentUser, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Team registered successfully!");
            return "redirect:/events/registration-success/" + reg.getId();
        } catch (Exception e) {
            Event event = eventService.getEventById(id);
            model.addAttribute("event", event);
            model.addAttribute("errorMessage", e.getMessage());
            return "events/team-register";
        }
    }

    @GetMapping("/registration-success/{regId}")
    public String registrationSuccess(@PathVariable("regId") Long regId, Model model) {
        EventRegistration registration = registrationService.getRegistrationById(regId);
        User currentUser = userService.getCurrentUser();

        if (currentUser == null || (!currentUser.isAdmin() && !registration.getUser().getId().equals(currentUser.getId()))) {
            return "redirect:/events";
        }

        model.addAttribute("registration", registration);
        return "events/registration-success";
    }
}

