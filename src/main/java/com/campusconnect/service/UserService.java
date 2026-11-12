package com.campusconnect.service;

import com.campusconnect.dto.ExternalStudentRegisterDto;
import com.campusconnect.dto.InternalStudentRegisterDto;
import com.campusconnect.entity.*;
import com.campusconnect.exception.DuplicateRegistrationException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.CollegeRepository;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CollegeRepository collegeRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.collegeRepository = collegeRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public User registerInternalStudent(InternalStudentRegisterDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateRegistrationException("An account with email " + email + " already exists.");
        }

        Role role = roleRepository.findByName(UserRole.ROLE_INTERNAL_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_INTERNAL_STUDENT, "Internal College Student")));

        College cgc = collegeRepository.findByNameIgnoreCase("CGC University Mohali")
                .orElseGet(() -> collegeRepository.save(new College("CGC University Mohali", "CGC", "Mohali", "Punjab", true)));

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRollNumber(dto.getRollNumber().trim());
        user.setDepartment(dto.getDepartment().trim());
        user.setSemester(dto.getSemester().trim());
        user.setPhone(dto.getPhone());
        user.setCollege(cgc);
        user.setStatus(UserStatus.ACTIVE); // Auto-active for verified internal students
        user.setRoles(Collections.singleton(role));

        User saved = userRepository.save(user);

        notificationService.notifyUser(
                saved,
                "Welcome to CampusConnect!",
                "Your CGC University student account has been successfully created. Explore upcoming events and register today!",
                "/events",
                "SUCCESS"
        );

        return saved;
    }

    @Transactional
    public User registerExternalStudent(ExternalStudentRegisterDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateRegistrationException("An account with email " + email + " already exists.");
        }

        Role role = roleRepository.findByName(UserRole.ROLE_EXTERNAL_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_EXTERNAL_STUDENT, "External College Student")));

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setExternalCollegeName(dto.getCollegeName().trim());
        user.setRollNumber(dto.getStudentId().trim());
        user.setDepartment(dto.getDepartment());
        user.setPhone(dto.getPhone().trim());
        user.setStatus(UserStatus.PENDING_APPROVAL); // Requires admin approval
        user.setRoles(Collections.singleton(role));

        User saved = userRepository.save(user);

        // Notify user about pending status
        notificationService.notifyUser(
                saved,
                "Application Received - Pending Approval",
                "Thank you for registering on CampusConnect for CGC University Mohali events. Your application is under review by the administrator.",
                "/auth/login",
                "INFO"
        );

        return saved;
    }

    @Transactional
    public void approveExternalStudent(Long userId, String notes) {
        User user = getById(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovalNotes(notes != null ? notes : "Approved by administrator");
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Application Approved!",
                "Congratulations! Your external student account on CampusConnect has been approved. You can now log in and register for inter-college events.",
                "/auth/login",
                "SUCCESS"
        );
    }

    @Transactional
    public void rejectExternalStudent(Long userId, String notes) {
        User user = getById(userId);
        user.setStatus(UserStatus.REJECTED);
        user.setApprovalNotes(notes != null ? notes : "Application declined by administrator");
        userRepository.save(user);

        notificationService.notifyUser(
                user,
                "Application Status Update",
                "Your registration for CampusConnect could not be approved at this time. Reason: " + user.getApprovalNotes(),
                "/auth/login",
                "WARNING"
        );
    }

    public List<User> getPendingExternalStudents() {
        return userRepository.findByRoleAndStatus(UserRole.ROLE_EXTERNAL_STUDENT, UserStatus.PENDING_APPROVAL);
    }

    public List<User> getAllStudents() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateProfile(Long userId, String fullName, String phone, String department, String semester) {
        User user = getById(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setDepartment(department);
        user.setSemester(semester);
        userRepository.save(user);
    }

    public long countTotalUsers() {
        return userRepository.count();
    }

    public long countInternalStudents() {
        return userRepository.countByRole(UserRole.ROLE_INTERNAL_STUDENT);
    }

    public long countExternalStudents() {
        return userRepository.countByRole(UserRole.ROLE_EXTERNAL_STUDENT);
    }

    public long countPendingApprovals() {
        return userRepository.countByStatus(UserStatus.PENDING_APPROVAL);
    }
}

