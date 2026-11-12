package com.campusconnect.util;

import com.campusconnect.entity.EventRegistration;
import com.campusconnect.entity.Feedback;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CsvExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CsvExportUtil() {}

    public static String exportRegistrationsCsv(List<EventRegistration> registrations) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Registration ID,Event Title,Student Name,Email,Phone,Roll Number,College,Type,Status,Registered At");

        for (EventRegistration r : registrations) {
            String college = r.getUser().getEffectiveCollegeName();
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                escapeCsv(r.getRegistrationNumber()),
                escapeCsv(r.getEvent().getTitle()),
                escapeCsv(r.getUser().getFullName()),
                escapeCsv(r.getUser().getEmail()),
                escapeCsv(r.getUser().getPhone() != null ? r.getUser().getPhone() : "N/A"),
                escapeCsv(r.getUser().getRollNumber() != null ? r.getUser().getRollNumber() : "N/A"),
                escapeCsv(college),
                escapeCsv(r.getRegistrationType().name()),
                escapeCsv(r.getStatus().name()),
                escapeCsv(r.getRegisteredAt() != null ? r.getRegisteredAt().format(DATE_FORMATTER) : "")
            );
        }

        return sw.toString();
    }

    public static String exportFeedbackCsv(List<Feedback> feedbacks) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Feedback ID,Event Title,Student Name,Email,Overall,Organization,Venue,Coordination,Comments,Submitted At");

        for (Feedback f : feedbacks) {
            pw.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%d\",\"%d\",\"%d\",\"%d\",\"%s\",\"%s\"%n",
                f.getId(),
                escapeCsv(f.getEvent().getTitle()),
                escapeCsv(f.getUser().getFullName()),
                escapeCsv(f.getUser().getEmail()),
                f.getOverallRating(),
                f.getOrganizationRating(),
                f.getVenueRating(),
                f.getCoordinationRating(),
                escapeCsv(f.getComments() != null ? f.getComments() : ""),
                escapeCsv(f.getSubmittedAt() != null ? f.getSubmittedAt().format(DATE_FORMATTER) : "")
            );
        }

        return sw.toString();
    }

    private static String escapeCsv(String data) {
        if (data == null) {
            return "";
        }
        return data.replace("\"", "\"\"");
    }
}

