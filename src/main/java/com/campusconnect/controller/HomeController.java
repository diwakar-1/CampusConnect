package com.campusconnect.controller;

import com.campusconnect.entity.Event;
import com.campusconnect.service.AnalyticsService;
import com.campusconnect.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final EventService eventService;
    private final AnalyticsService analyticsService;

    public HomeController(EventService eventService, AnalyticsService analyticsService) {
        this.eventService = eventService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Event> upcomingEvents = eventService.getUpcomingEvents();
        model.addAttribute("upcomingEvents", upcomingEvents.size() > 6 ? upcomingEvents.subList(0, 6) : upcomingEvents);
        model.addAttribute("categories", eventService.getAllCategories());
        model.addAttribute("stats", analyticsService.getAdminDashboardStats());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}

