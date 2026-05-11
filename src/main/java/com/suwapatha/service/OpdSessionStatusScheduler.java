package com.suwapatha.service;

import com.suwapatha.entity.OpdSession;
import com.suwapatha.repository.OpdSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpdSessionStatusScheduler {

    private final OpdSessionRepository sessionRepository;

    /**
     * Runs every 15 minutes to mark sessions as COMPLETED.
     * Threshold:
     * 1. Date is in the past.
     * 2. Date is today and time is >= 12:00 PM.
     */
    @Scheduled(fixedDelay = 900_000) // 15 minutes
    public void markSessionsAsCompleted() {
        log.info("Running OPD Session Status Scheduler to mark sessions as COMPLETED...");

        String todayStr = LocalDate.now().toString();
        LocalTime nowTime = LocalTime.now();

        List<OpdSession> activeSessions = sessionRepository.findByStatusIn(List.of("OPEN", "FULL"));

        for (OpdSession session : activeSessions) {
            boolean shouldComplete = false;

            // 1. If date is in the past
            if (session.getDate().compareTo(todayStr) < 0) {
                shouldComplete = true;
            }
            // 2. If date is today and time is >= 12:00 PM
            else if (session.getDate().equals(todayStr) && !nowTime.isBefore(LocalTime.NOON)) {
                shouldComplete = true;
            }

            if (shouldComplete) {
                log.info("Marking session {} as COMPLETED (Date: {}, StartTime: {})",
                        session.getId(), session.getDate(), session.getStartTime());
                session.setStatus("COMPLETED");
                sessionRepository.save(session);
            }
        }
    }
}
