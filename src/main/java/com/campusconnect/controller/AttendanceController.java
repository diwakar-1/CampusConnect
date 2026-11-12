package com.campusconnect.controller;

import com.campusconnect.dto.AttendanceScanDto;
import com.campusconnect.entity.Attendance;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.User;
import com.campusconnect.service.AttendanceService;
import com.campusconnect.service.EventService;
import com.campusconnect.service.RegistrationService;
import com.campusconnect.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;

    public AttendanceController(AttendanceService attendanceService,
                                EventService eventService,
                                RegistrationService registrationService,
                                UserService userService) {
        this.attendanceService = attendanceService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
    }

    @GetMapping("/admin/attendance")
    public String attendanceDashboard(@RequestParam(value = "eventId", required = false) Long eventId,
                                      Model model) {
        List<Event> events = eventService.getAllEvents();
        Event selectedEvent = null;
        List<EventRegistration> registrations = List.of();
        List<Attendance> attendances = List.of();

        if (eventId != null) {
            selectedEvent = eventService.getEventById(eventId);
            registrations = registrationService.getEventRegistrations(eventId);
            attendances = attendanceService.getAttendanceByEvent(eventId);
        } else if (!events.isEmpty()) {
            selectedEvent = events.get(0);
            eventId = selectedEvent.getId();
            registrations = registrationService.getEventRegistrations(eventId);
            attendances = attendanceService.getAttendanceByEvent(eventId);
        }

        model.addAttribute("events", events);
        model.addAttribute("selectedEvent", selectedEvent);
        model.addAttribute("selectedEventId", eventId);
        model.addAttribute("registrations", registrations);
        model.addAttribute("attendances", attendances);
        model.addAttribute("scanDto", new AttendanceScanDto("", eventId != null ? eventId : 0L));

        return "admin/attendance";
    }

    @PostMapping("/admin/attendance/scan")
    public String processScan(@ModelAttribute("scanDto") AttendanceScanDto dto,
                              RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser();
        try {
            Attendance att = attendanceService.markAttendance(dto, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Attendance marked PRESENT for " + att.getUser().getFullName() + " (" + att.getEventRegistration().getRegistrationNumber() + ")!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/attendance?eventId=" + dto.getEventId();
    }

    @PostMapping("/admin/attendance/manual/{regId}")
    public String markManual(@PathVariable("regId") Long regId,
                             @RequestParam("eventId") Long eventId,
                             RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser();
        try {
            Attendance att = attendanceService.markAttendanceManual(regId, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Attendance manually marked for " + att.getUser().getFullName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/attendance?eventId=" + eventId;
    }

    @PostMapping("/api/attendance/scan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiScan(@RequestBody AttendanceScanDto dto) {
        Map<String, Object> response = new HashMap<>();
        User admin = userService.getCurrentUser();
        try {
            Attendance att = attendanceService.markAttendance(dto, admin);
            response.put("success", true);
            response.put("message", "Attendance marked successfully");
            response.put("studentName", att.getUser().getFullName());
            response.put("rollNumber", att.getUser().getRollNumber());
            response.put("registrationNumber", att.getEventRegistration().getRegistrationNumber());
            response.put("markedAt", att.getMarkedAt().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

