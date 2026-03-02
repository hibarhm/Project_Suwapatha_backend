package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardResponse {

    private Stats stats;
    private List<AppointmentResponse> upcomingAppointments;
    private List<Notification> notifications;
    private List<VisitData> patientVisitsData;
    private List<DayConsultation> consultationsByDay;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int totalPatientsToday;
        private int consultationsThisWeek;
        private int averageWaitTime;
        private int changeFromYesterday;
        private int changeFromLastWeek;
        private int changeFromLastMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        private String id;
        private String type; // message, cancelled, lab, reminder
        private String title;
        private String time;
        private String icon; // mail, calendar, flask
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitData {
        private String month;
        private int visits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayConsultation {
        private String day;
        private int count;
    }
}
