package com.campusconnect.config;

import com.campusconnect.entity.*;
import com.campusconnect.repository.*;
import com.campusconnect.util.QrCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CollegeRepository collegeRepository;
    private final EventCategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final EventScheduleRepository scheduleRepository;
    private final EventRegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final CertificateRepository certificateRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           CollegeRepository collegeRepository,
                           EventCategoryRepository categoryRepository,
                           VenueRepository venueRepository,
                           EventRepository eventRepository,
                           EventScheduleRepository scheduleRepository,
                           EventRegistrationRepository registrationRepository,
                           AttendanceRepository attendanceRepository,
                           CertificateRepository certificateRepository,
                           FeedbackRepository feedbackRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.collegeRepository = collegeRepository;
        this.categoryRepository = categoryRepository;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.scheduleRepository = scheduleRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.certificateRepository = certificateRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            logger.info("Database already initialized. Skipping seed data.");
            return;
        }

        logger.info("Seeding initial data for CampusConnect (CGC University Mohali)...");

        // 1. Roles
        Role adminRole = roleRepository.save(new Role(UserRole.ROLE_ADMIN, "College Administrator"));
        Role internalRole = roleRepository.save(new Role(UserRole.ROLE_INTERNAL_STUDENT, "Internal College Student (CGC Mohali)"));
        Role externalRole = roleRepository.save(new Role(UserRole.ROLE_EXTERNAL_STUDENT, "External College Student"));

        // 2. Colleges
        College cgc = collegeRepository.save(new College("CGC University Mohali", "CGC", "Mohali", "Punjab", true));
        College thapar = collegeRepository.save(new College("Thapar Institute of Engineering and Technology", "TIET", "Patiala", "Punjab", false));
        College chitkara = collegeRepository.save(new College("Chitkara University", "CU-PB", "Rajpura", "Punjab", false));
        College pec = collegeRepository.save(new College("Punjab Engineering College", "PEC", "Chandigarh", "Chandigarh", false));

        // 3. Admin User
        User admin = new User();
        admin.setFullName("Dr. Harpreet Singh");
        admin.setEmail("admin@cgc.edu.in");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setPhone("9876543210");
        admin.setDepartment("Student Welfare Department");
        admin.setCollege(cgc);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRoles(Collections.singleton(adminRole));
        userRepository.save(admin);

        // 4. Internal Student (Rahul Sharma)
        User internalStudent = new User();
        internalStudent.setFullName("Rahul Sharma");
        internalStudent.setEmail("rahul.cse@cgc.edu.in");
        internalStudent.setPassword(passwordEncoder.encode("Student@123"));
        internalStudent.setPhone("9812345678");
        internalStudent.setRollNumber("2023CSE1042");
        internalStudent.setDepartment("Computer Science & Engineering");
        internalStudent.setSemester("6th Semester");
        internalStudent.setCollege(cgc);
        internalStudent.setStatus(UserStatus.ACTIVE);
        internalStudent.setRoles(Collections.singleton(internalRole));
        User savedInternal = userRepository.save(internalStudent);

        // 5. Approved External Student (Priya Verma)
        User approvedExternal = new User();
        approvedExternal.setFullName("Priya Verma");
        approvedExternal.setEmail("priya.sharma@thapar.edu");
        approvedExternal.setPassword(passwordEncoder.encode("Student@123"));
        approvedExternal.setPhone("9876501234");
        approvedExternal.setRollNumber("TIET-2023-CS55");
        approvedExternal.setCollege(thapar);
        approvedExternal.setExternalCollegeName("Thapar Institute of Engineering and Technology");
        approvedExternal.setDepartment("Computer Engineering");
        approvedExternal.setStatus(UserStatus.ACTIVE);
        approvedExternal.setApprovedAt(LocalDateTime.now().minusDays(3));
        approvedExternal.setApprovalNotes("Verified student ID card");
        approvedExternal.setRoles(Collections.singleton(externalRole));
        User savedExternal = userRepository.save(approvedExternal);

        // 6. Pending External Student (Aman Verma)
        User pendingExternal = new User();
        pendingExternal.setFullName("Aman Verma");
        pendingExternal.setEmail("aman.verma@chitkara.edu.in");
        pendingExternal.setPassword(passwordEncoder.encode("Student@123"));
        pendingExternal.setPhone("9823456789");
        pendingExternal.setRollNumber("CU2023-IT19");
        pendingExternal.setCollege(chitkara);
        pendingExternal.setExternalCollegeName("Chitkara University");
        pendingExternal.setDepartment("Information Technology");
        pendingExternal.setStatus(UserStatus.PENDING_APPROVAL);
        pendingExternal.setRoles(Collections.singleton(externalRole));
        userRepository.save(pendingExternal);

        // 7. Event Categories
        EventCategory catTech = categoryRepository.save(new EventCategory("Technical & Hackathons", "TECH", "Coding, hackathons, robotics and technical symposiums", "bi-code-slash", "#6366f1"));
        EventCategory catCultural = categoryRepository.save(new EventCategory("Cultural & Arts", "CULTURE", "Music, dance, theatre, fashion shows and fine arts", "bi-palette", "#ec4899"));
        EventCategory catSports = categoryRepository.save(new EventCategory("Sports & Athletics", "SPORTS", "Cricket, football, badminton and athletics", "bi-trophy", "#10b981"));
        EventCategory catWorkshops = categoryRepository.save(new EventCategory("Workshops & Seminars", "WORKSHOP", "Skill masterclasses, guest lectures and tech tracks", "bi-mortarboard", "#3b82f6"));
        EventCategory catQuiz = categoryRepository.save(new EventCategory("Management & Quizzes", "MGMT", "Business case study, quizzes and startup pitch deck", "bi-lightbulb", "#f59e0b"));

        // 8. Venues
        Venue mainAuditorium = venueRepository.save(new Venue("CGC Main Auditorium", "Admin Block A", "Auditorium Hall", 800, "High-end AV system, Dual 4K Projectors, Stage Lighting, AC"));
        Venue seminarHall = venueRepository.save(new Venue("Seminar Hall Block 3", "Engineering Block 3", "Room 301", 200, "Podium, Mic setup, Smartboard, AC"));
        Venue techLab = venueRepository.save(new Venue("Tech Lab Alpha", "CSE Block 2", "Lab 204", 120, "120 High-spec workstations, Gigabit LAN, Power backup"));
        Venue sportsComplex = venueRepository.save(new Venue("University Sports Arena", "Sports Pavilion", "Ground 1", 1200, "Floodlights, Grandstand, Pavilion, Medical Booth"));

        // 9. Sample Events
        LocalDateTime now = LocalDateTime.now();

        // Event 1: Hackathon (Upcoming, Team, Both)
        Event hackathon = new Event();
        hackathon.setTitle("CGC National Hackathon 2026: CodeSpark");
        hackathon.setDescription("Join Northern India's biggest 36-hour inter-college hackathon at CGC University Mohali! Build game-changing solutions in AI, FinTech, CleanTech, and Smart Governance. Cash prizes worth ₹2,50,000, mentor sessions with top tech leaders, and direct interview opportunities.");
        hackathon.setBannerImageUrl("https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=1200&auto=format&fit=crop&q=80");
        hackathon.setCategory(catTech);
        hackathon.setVenue(techLab);
        hackathon.setStartDate(now.plusDays(10).withHour(9).withMinute(0));
        hackathon.setEndDate(now.plusDays(12).withHour(18).withMinute(0));
        hackathon.setRegistrationStartDate(now.minusDays(5));
        hackathon.setRegistrationDeadline(now.plusDays(8).withHour(23).withMinute(59));
        hackathon.setMaxCapacity(150);
        hackathon.setCurrentRegistrations(1);
        hackathon.setRegistrationFee(0.0);
        hackathon.setCoordinatorName("Er. Ramanpreet Kaur");
        hackathon.setCoordinatorEmail("hackathon@cgc.edu.in");
        hackathon.setCoordinatorPhone("9876541122");
        hackathon.setEligibilityRequirements("Open to undergraduate & postgraduate students with valid college ID");
        hackathon.setVisibility(EventVisibility.ALL);
        hackathon.setStatus(EventStatus.UPCOMING);
        hackathon.setPublished(true);
        hackathon.setAllowTeamRegistration(true);
        hackathon.setMinTeamSize(2);
        hackathon.setMaxTeamSize(4);
        Event savedHackathon = eventRepository.save(hackathon);

        // Schedule for Hackathon
        EventSchedule s1 = new EventSchedule();
        s1.setEvent(savedHackathon);
        s1.setSessionTitle("Opening Ceremony & Problem Statement Reveal");
        s1.setSessionDescription("Welcome address by Dean Student Affairs followed by domain reveal.");
        s1.setStartTime(savedHackathon.getStartDate());
        s1.setEndTime(savedHackathon.getStartDate().plusHours(2));
        s1.setVenue(mainAuditorium);
        s1.setCoordinatorName("Er. Ramanpreet Kaur");
        scheduleRepository.save(s1);

        EventSchedule s2 = new EventSchedule();
        s2.setEvent(savedHackathon);
        s2.setSessionTitle("Hackathon Sprint Phase 1");
        s2.setSessionDescription("36-hour non-stop development sprint with mentor checkpoints.");
        s2.setStartTime(savedHackathon.getStartDate().plusHours(2));
        s2.setEndTime(savedHackathon.getStartDate().plusHours(24));
        s2.setVenue(techLab);
        s2.setCoordinatorName("Tech Support Team");
        scheduleRepository.save(s2);

        // Event 2: Cultural Fest (Upcoming, Individual, Both)
        Event cultural = new Event();
        cultural.setTitle("Tarang 2026 – Annual Inter-College Cultural Fest");
        cultural.setDescription("Experience three electrifying days of artistic expression, musical performances, fashion showcase, and literary events. Witness top celebrity artist nights and compete for the prestigious CGC Trophy.");
        cultural.setBannerImageUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&auto=format&fit=crop&q=80");
        cultural.setCategory(catCultural);
        cultural.setVenue(mainAuditorium);
        cultural.setStartDate(now.plusDays(20).withHour(10).withMinute(0));
        cultural.setEndDate(now.plusDays(22).withHour(22).withMinute(0));
        cultural.setRegistrationStartDate(now.minusDays(2));
        cultural.setRegistrationDeadline(now.plusDays(18).withHour(23).withMinute(59));
        cultural.setMaxCapacity(500);
        cultural.setCurrentRegistrations(1);
        cultural.setRegistrationFee(150.0);
        cultural.setCoordinatorName("Prof. Simran Deep Singh");
        cultural.setCoordinatorEmail("tarang@cgc.edu.in");
        cultural.setCoordinatorPhone("9876543344");
        cultural.setEligibilityRequirements("All registered college students with valid identity card");
        cultural.setVisibility(EventVisibility.ALL);
        cultural.setStatus(EventStatus.UPCOMING);
        cultural.setPublished(true);
        cultural.setAllowTeamRegistration(false);
        Event savedCultural = eventRepository.save(cultural);

        // Event 3: Completed Tech Workshop (for demonstrating attendance, certificates, feedback)
        Event workshop = new Event();
        workshop.setTitle("Hands-on Masterclass: Generative AI & Cloud Architecture");
        workshop.setDescription("An intensive full-day masterclass covering LLMs, LangChain, vector databases, and scalable deployment on modern cloud platforms. Participants deployed real-world RAG applications.");
        workshop.setBannerImageUrl("https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=1200&auto=format&fit=crop&q=80");
        workshop.setCategory(catWorkshops);
        workshop.setVenue(seminarHall);
        workshop.setStartDate(now.minusDays(5).withHour(9).withMinute(30));
        workshop.setEndDate(now.minusDays(5).withHour(17).withMinute(0));
        workshop.setRegistrationStartDate(now.minusDays(20));
        workshop.setRegistrationDeadline(now.minusDays(6).withHour(23).withMinute(59));
        workshop.setMaxCapacity(80);
        workshop.setCurrentRegistrations(1);
        workshop.setRegistrationFee(0.0);
        workshop.setCoordinatorName("Dr. Vikas Sharma");
        workshop.setCoordinatorEmail("genai.workshop@cgc.edu.in");
        workshop.setCoordinatorPhone("9812300998");
        workshop.setEligibilityRequirements("Students with basic programming knowledge");
        workshop.setVisibility(EventVisibility.INTERNAL_ONLY);
        workshop.setStatus(EventStatus.COMPLETED);
        workshop.setPublished(true);
        workshop.setAllowTeamRegistration(false);
        Event savedWorkshop = eventRepository.save(workshop);

        // 10. Sample Registration for Rahul in Completed Workshop
        String regNum = "CC-2026-003-8821";
        String qrPayload = "CC-PASS:" + regNum + ":" + savedWorkshop.getId() + ":" + savedInternal.getId();
        String qrCode = QrCodeGenerator.generateQrCodeBase64(qrPayload, 260, 260);

        EventRegistration workshopReg = new EventRegistration();
        workshopReg.setEvent(savedWorkshop);
        workshopReg.setUser(savedInternal);
        workshopReg.setRegistrationNumber(regNum);
        workshopReg.setRegistrationType(RegistrationType.INDIVIDUAL);
        workshopReg.setStatus(RegistrationStatus.CONFIRMED);
        workshopReg.setQrCodeData(qrCode);
        EventRegistration savedReg = registrationRepository.save(workshopReg);

        // 11. Attendance for Rahul
        Attendance att = new Attendance(
                savedReg,
                savedWorkshop,
                savedInternal,
                AttendanceStatus.PRESENT,
                admin,
                "QR_SCAN"
        );
        att.setMarkedAt(savedWorkshop.getStartDate().plusMinutes(15));
        att.setNotes("Verified at registration desk");
        attendanceRepository.save(att);

        // 12. Certificate for Rahul
        Certificate cert = new Certificate();
        cert.setCertificateNumber("CGC-CERT-2026-10492");
        cert.setEventRegistration(savedReg);
        cert.setUser(savedInternal);
        cert.setEvent(savedWorkshop);
        cert.setCertificateType(CertificateType.PARTICIPATION);
        cert.setStudentName(savedInternal.getFullName());
        cert.setCollegeName("CGC University Mohali");
        cert.setIssueDate(LocalDate.now().minusDays(4));
        cert.setAuthorizedSignatory("Dean Student Affairs, CGC Mohali");
        cert.setVerificationToken("CERT-CGC-AI-8821");
        certificateRepository.save(cert);

        // 13. Feedback from Rahul
        Feedback fb = new Feedback();
        fb.setEvent(savedWorkshop);
        fb.setUser(savedInternal);
        fb.setOverallRating(5);
        fb.setOrganizationRating(5);
        fb.setVenueRating(4);
        fb.setCoordinationRating(5);
        fb.setComments("The masterclass was very practical and informative! Learned how to deploy real AI agents.");
        feedbackRepository.save(fb);

        // 14. Registration for Rahul in upcoming Hackathon
        String hackRegNum = "CC-2026-001-3012";
        String hackQr = QrCodeGenerator.generateQrCodeBase64("CC-PASS:" + hackRegNum + ":" + savedHackathon.getId() + ":" + savedInternal.getId(), 260, 260);
        EventRegistration hackReg = new EventRegistration();
        hackReg.setEvent(savedHackathon);
        hackReg.setUser(savedInternal);
        hackReg.setRegistrationNumber(hackRegNum);
        hackReg.setRegistrationType(RegistrationType.INDIVIDUAL);
        hackReg.setStatus(RegistrationStatus.CONFIRMED);
        hackReg.setQrCodeData(hackQr);
        registrationRepository.save(hackReg);

        // 15. Notifications
        notificationRepository.save(new Notification(
                savedInternal,
                "Welcome to CampusConnect!",
                "Your CGC University student account has been successfully configured. Explore events and register!",
                "/events",
                "SUCCESS"
        ));

        notificationRepository.save(new Notification(
                savedInternal,
                "Certificate Available",
                "Your Participation Certificate for 'Hands-on Masterclass: Generative AI & Cloud Architecture' is now ready for download.",
                "/student/certificates",
                "CERTIFICATE"
        ));

        notificationRepository.save(new Notification(
                null,
                "Campus Announcement: CGC CodeSpark Hackathon 2026",
                "Registrations are officially open for CodeSpark 2026! Cash prizes worth ₹2.5 Lakhs. Form your teams today.",
                "/events/" + savedHackathon.getId(),
                "ANNOUNCEMENT"
        ));

        logger.info("Sample seed data created successfully!");
        logger.info("ADMIN: admin@cgc.edu.in | Password: Admin@123");
        logger.info("INTERNAL STUDENT: rahul.cse@cgc.edu.in | Password: Student@123");
        logger.info("APPROVED EXTERNAL: priya.sharma@thapar.edu | Password: Student@123");
        logger.info("PENDING EXTERNAL: aman.verma@chitkara.edu.in | Password: Student@123");
    }
}

