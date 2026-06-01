package ma.project.echallenge.controller;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.TimeSlotRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TimeSlotResponse;
import ma.project.echallenge.service.TimeSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> createTimeSlot(
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.createTimeSlot(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAllTimeSlots() {
        return ResponseEntity.ok(timeSlotService.getAllTimeSlots());
    }

    // ✅ ENDPOINT SPÉCIFIQUE EN PREMIER
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableTimeSlots(
            @RequestParam Long testId) {
        return ResponseEntity.ok(timeSlotService.getAvailableTimeSlots(testId));
    }

    // ✅ ENDPOINT GÉNÉRIQUE EN DERNIER
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> getTimeSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> updateTimeSlot(
            @PathVariable Long id,
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.updateTimeSlot(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteTimeSlot(@PathVariable Long id) {
        return ResponseEntity.ok(timeSlotService.deleteTimeSlot(id));
    }
}