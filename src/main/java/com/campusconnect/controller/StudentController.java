package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;
    private final CertificateService certificateService;
    private final EventService eventService;

    public StudentController(UserService userService,
                             RegistrationService registrationService,
                             ScheduleService scheduleService,
                             AttendanceService attendanceService,
                             CertificateService certificateService,
                             EventService eventService) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.scheduleService = scheduleService;
        this.attendanceService = attendanceService;
        this.certificateService = certificateService;
        this.eventService = eventService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = userService.getCurrentUser();
        List<EventRegistration> registrations = registrationService.getUserRegistrations(user.getId());
        List<EventSchedule> schedules = scheduleService.getSchedulesForUser(user.getId());
        List<Certificate> certificates = certificateService.getUserCertificates(user.getId());
        List<Attendance> attendances = attendanceService.getUserAttendance(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("registrations", registrations);
        model.addAttribute("recentRegistrations", registrations.size() > 5 ? registrations.subList(0, 5) : registrations);
        model.addAttribute("schedules", schedules);
        model.addAttribute("certificates", certificates);
        model.addAttribute("attendances", attendances);

        return "student/dashboard";
    }

    @GetMapping("/registrations")
    public String myRegistrations(Model model) {
        User user = userService.getCurrentUser();
        List<EventRegistration> list = registrationService.getUserRegistrations(user.getId());
        model.addAttribute("registrations", list);
        return "student/my-registrations";
    }

    @PostMapping("/registrations/cancel/{id}")
    public String cancelRegistration(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        try {
            registrationService.cancelRegistration(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/registrations";
    }

    @GetMapping("/schedules")
    public String mySchedules(Model model) {
        User user = userService.getCurrentUser();
        List<EventSchedule> schedules = scheduleService.getSchedulesForUser(user.getId());
        model.addAttribute("schedules", schedules);
        return "student/my-schedules";
    }

    @GetMapping("/attendance")
    public String myAttendance(Model model) {
        User user = userService.getCurrentUser();
        List<Attendance> list = attendanceService.getUserAttendance(user.getId());
        model.addAttribute("attendances", list);
        return "student/my-attendance";
    }

    @GetMapping("/certificates")
    public String myCertificates(Model model) {
        User user = userService.getCurrentUser();
        List<Certificate> list = certificateService.getUserCertificates(user.getId());
        model.addAttribute("certificates", list);
        return "student/my-certificates";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam(value = "department", required = false) String department,
                                @RequestParam(value = "semester", required = false) String semester,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        userService.updateProfile(user.getId(), fullName, phone, department, semester);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/student/profile";
    }
}

