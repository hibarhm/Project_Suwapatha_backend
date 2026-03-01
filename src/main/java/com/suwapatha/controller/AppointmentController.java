package com.suwapatha.controller;

import com.suwapatha.dto.AppointmentResponse;
import com.suwapatha.dto.BookAppointmentRequest;
import com.suwapatha.entity.User;
import com.suwapatha.repository.UserRepository;
import com.suwapatha.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    /*Book a new OPD appointment */
    @PostMapping
    public ResponseEntity<AppointmentResponse> book(
            Authentication authentication,
            @Valid @RequestBody BookAppointmentRequest request) {

        User user = getUser(authentication);
        AppointmentResponse response = appointmentService.bookAppointment(
                user.getId(), user.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    /*get all appointments for the logged-in patient */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAll(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(appointmentService.getMyAppointments(user.getId()));
    }

    /* Get the current active (BOOKED) appointment — for the queue status card */
    @GetMapping("/active")
    public ResponseEntity<AppointmentResponse> getActive(Authentication authentication) {
        User user = getUser(authentication);
        AppointmentResponse active = appointmentService.getActiveAppointment(user.getId());
        if (active == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(active);
    }

    /*Cancel an appointment */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            Authentication authentication,
            @PathVariable String id) {

        User user = getUser(authentication);
        appointmentService.cancelAppointment(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
