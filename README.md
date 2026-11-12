# CampusConnect – College Event Management & Registration System
**Built for CGC University Mohali**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green.svg)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3.3-purple.svg)](https://getbootstrap.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#)

CampusConnect is a modern, enterprise-grade, full-stack event management and registration web application designed and built for **CGC University Mohali**. The platform connects college administrators, faculty event coordinators, internal CGC scholars, and external university participants through three dedicated role-based portals.

---

## 🚀 Key Modules & Capabilities

### 1. Multi-Role Authentication & Access Control
- **College Administrator (`ROLE_ADMIN`)**: Comprehensive analytics dashboard, event lifecycle management, external application review, venue scheduling conflict detection, QR attendance check-in desk, bulk PDF certificate engine, feedback analysis, and CSV export.
- **Internal CGC Student (`ROLE_INTERNAL_STUDENT`)**: Automatic verification for CGC University Mohali students, 1-click individual and team event registrations, personalized schedules, dynamic QR passes, attendance history, downloadable certificates, and 5-star event reviews.
- **External College Student (`ROLE_EXTERNAL_STUDENT`)**: Dedicated inter-college registration portal requiring administrator identity approval before system access, enabling participation in hackathons, athletic tournaments, and cultural fests.

### 2. Event Management Engine
- Full CRUD for events with title, rich description, poster banner, category, venue, start/end dates, registration opening and deadline.
- **Capacity & Waitlist**: Tracks registration limits in real-time. If an event reaches capacity, participants are waitlisted; if a confirmed participant cancels, waitlisted applicants are automatically promoted.
- **Audience Visibility**: Public inter-college events (`ALL`), campus internal only (`INTERNAL_ONLY`), or external competitions only (`EXTERNAL_ONLY`).
- **Team Registration**: Support for teams with team name, leader, custom member roster (min and max team size restrictions).

### 3. Venue Scheduling & Conflict Detection
- Multi-session scheduling per event (keynotes, coding rounds, workshops).
- **Intelligent Conflict Detection**: Prevents double-booking overlapping sessions at the same campus venue.

### 4. Zero-Paper QR Code Attendance
- Generates an encrypted Base64 QR pass for every confirmed registration.
- Web-based attendance scanning console supporting handheld barcode/QR scanner guns, webcam live scan, and manual quick-check.
- Enforces single check-in policy, preventing duplicate attendance marking and proxy entries.

### 5. Automated PDF Certificate Generation & Verification
- High-resolution landscape PDF certificate generation powered by **OpenPDF**.
- Features gold and indigo decorative borders, CGC University Mohali header, recipient name, event details, issue date, signature lines, and embedded QR code.
- **Public Verification Endpoint** (`/certificates/verify/{token}`): Allows employers, colleges, and students to authenticate certificate validity online.

### 6. Participant Feedback & 5-Star Rating System
- Detailed evaluations covering Overall Experience, Organization, Venue & Facilities, and Coordinator Support.
- Admin analytics dashboard showing average satisfaction ratings and student commentary with CSV export.

### 7. Notification & Email System
- In-app notification center with real-time unread badge counts and dismissal.
- Asynchronous email dispatching for confirmations, approval statuses, reminders, and certificate availability.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend Core** | Java 21, Spring Boot 3.3.5, Spring MVC, Spring Security 6, Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0 (with connection pooling) & H2 in-memory dev fallback |
| **Frontend** | HTML5, CSS3, JavaScript (ES6), Thymeleaf 3, Bootstrap 5.3.3, Bootstrap Icons |
| **Design Style** | Premium Dark Theme, Glassmorphism, Linear/Vercel/Stripe SaaS Aesthetic |
| **Libraries** | ZXing 3.5.3 (QR Generation), OpenPDF 2.0.3 (PDF Certificates), Chart.js 4.4 (Analytics) |
| **Build Tool** | Apache Maven 3.9+ |

---

## 📂 Project Architecture

```
Campus Connect/
├── pom.xml                                    # Maven configuration & dependencies
├── README.md                                  # Project overview and specifications
├── setup-guide.md                             # Step-by-step installation instructions
├── schema.sql                                 # Complete MySQL 8 DDL database schema
├── data.sql                                   # Initial SQL seed data script
├── src/
│   ├── main/
│   │   ├── java/com/campusconnect/
│   │   │   ├── CampusConnectApplication.java  # Main application entry point
│   │   │   ├── config/                        # SecurityConfig, WebMvcConfig, DataInitializer
│   │   │   ├── controller/                    # 10 MVC & REST Controllers
│   │   │   ├── dto/                           # Form transfer objects & validation
│   │   │   ├── entity/                        # 14 JPA entities & 7 enums
│   │   │   ├── exception/                     # Custom exceptions & GlobalExceptionHandler
│   │   │   ├── repository/                    # Spring Data JPA repositories
│   │   │   ├── security/                      # CustomUserDetails, Service, SuccessHandler
│   │   │   ├── service/                       # 10 Core Business services
│   │   │   └── util/                          # QrCodeGenerator, PdfCertificateGenerator, CsvExport
│   │   └── resources/
│   │       ├── application.yml                # Main configuration
│   │       ├── application-mysql.yml          # MySQL 8 datasource profile
│   │       ├── application-h2.yml             # Embedded H2 datasource profile
│   │       ├── static/
│   │       │   ├── css/theme.css              # Premium dark theme stylesheet
│   │       │   ├── js/main.js                 # UI interactions & clipboard
│   │       │   ├── js/qr-scanner.js           # QR scanner console script
│   │       │   ├── js/charts.js               # Chart.js analytics graphs
│   │       │   └── images/cgc-logo.svg        # CGC University emblem & logo
│   │       └── templates/                     # 35 Thymeleaf HTML templates
```

---

## ⚡ Quick Start

### 1. Database Setup
```sql
CREATE DATABASE IF NOT EXISTS campus_connect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure Credentials
Update your MySQL username and password in `src/main/resources/application-mysql.yml` if necessary.

### 3. Run Application
```powershell
mvn spring-boot:run
```

Open your browser at: **`http://localhost:8080`**

### 4. Test Accounts

| Portal | Email | Password | Role |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@cgc.edu.in` | `Admin@123` | Full Admin |
| **CGC Student** | `rahul.cse@cgc.edu.in` | `Student@123` | Internal Student |
| **External Student** | `priya.sharma@thapar.edu` | `Student@123` | Approved External |
| **Pending Student** | `aman.verma@chitkara.edu.in` | `Student@123` | Awaiting Approval |

---

## 📄 License
Developed for academic excellence and campus event management at **CGC University Mohali**. All rights reserved.

