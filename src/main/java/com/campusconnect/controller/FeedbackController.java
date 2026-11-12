package com.campusconnect.controller;

import com.campusconnect.dto.FeedbackDto;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.Feedback;
import com.campusconnect.entity.User;
import com.campusconnect.service.EventService;
import com.campusconnect.service.FeedbackService;
import com.campusconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final EventService eventService;
    private final UserService userService;

    public FeedbackController(FeedbackService feedbackService,
                              EventService eventService,
                              UserService userService) {
        this.feedbackService = feedbackService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/feedback/submit/{eventId}")
    public String showFeedbackForm(@PathVariable("eventId") Long eventId, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (feedbackService.hasUserSubmitted(eventId, user.getId())) {
            redirectAttributes.addFlashAttribute("warningMessage", "You have already submitted feedback for this event.");
            return "redirect:/events/" + eventId;
        }

        Event event = eventService.getEventById(eventId);
        FeedbackDto dto = new FeedbackDto();
        dto.setEventId(eventId);

        model.addAttribute("event", event);
        model.addAttribute("feedbackDto", dto);
        return "feedback/submit";
    }

    @PostMapping("/feedback/submit")
    public String submitFeedback(@Valid @ModelAttribute("feedbackDto") FeedbackDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            Event event = eventService.getEventById(dto.getEventId());
            model.addAttribute("event", event);
            return "feedback/submit";
        }

        try {
            feedbackService.submitFeedback(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your valuable feedback!");
            return "redirect:/events/" + dto.getEventId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/events/" + dto.getEventId();
        }
    }

    @GetMapping("/admin/feedback")
    public String adminFeedback(@RequestParam(value = "eventId", required = false) Long eventId, Model model) {
        List<Event> events = eventService.getAllEvents();
        List<Feedback> feedbacks = (eventId != null)
                ? feedbackService.getEventFeedbacks(eventId)
                : feedbackService.getAllFeedbacks();

        Double avgRating = (eventId != null)
                ? feedbackService.getAverageRating(eventId)
                : feedbackService.getGlobalAverageRating();

        model.addAttribute("events", events);
        model.addAttribute("selectedEventId", eventId);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("averageRating", avgRating);

        return "admin/feedback";
    }
}

