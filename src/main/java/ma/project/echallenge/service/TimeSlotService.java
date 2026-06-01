package ma.project.echallenge.service;

import lombok.RequiredArgsConstructor;
import ma.project.echallenge.dto.request.TimeSlotRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TimeSlotResponse;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.entity.TimeSlot;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TestRepository testRepository;

    @Transactional
    public ApiResponse<TimeSlotResponse> createTimeSlot(TimeSlotRequest request) {
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("L'heure de début doit être avant l'heure de fin");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("L'heure de début doit être dans le futur");
        }

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setTest(test);
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setBooked(false);

        TimeSlot saved = timeSlotRepository.save(timeSlot);

        return ApiResponse.success("Créneau créé avec succès", mapToResponse(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TimeSlotResponse>> getAllTimeSlots() {
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();
        List<TimeSlotResponse> responses = timeSlots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Créneaux récupérés", responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TimeSlotResponse>> getAvailableTimeSlots(Long testId) {
        List<TimeSlot> timeSlots = timeSlotRepository.findByTestIdAndBookedFalse(testId);
        List<TimeSlotResponse> responses = timeSlots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Créneaux disponibles récupérés", responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<TimeSlotResponse> getTimeSlotById(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau non trouvé"));

        return ApiResponse.success("Créneau récupéré", mapToResponse(timeSlot));
    }

    @Transactional
    public ApiResponse<TimeSlotResponse> updateTimeSlot(Long id, TimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau non trouvé"));

        if (timeSlot.isBooked()) {
            throw new BadRequestException("Impossible de modifier un créneau déjà réservé");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("L'heure de début doit être avant l'heure de fin");
        }

        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());

        TimeSlot updated = timeSlotRepository.save(timeSlot);

        return ApiResponse.success("Créneau mis à jour", mapToResponse(updated));
    }

    @Transactional
    public ApiResponse<String> deleteTimeSlot(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau non trouvé"));

        if (timeSlot.isBooked()) {
            throw new BadRequestException("Impossible de supprimer un créneau déjà réservé");
        }

        timeSlotRepository.delete(timeSlot);

        return ApiResponse.success("Créneau supprimé", null);
    }

    private TimeSlotResponse mapToResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.isBooked(),
                timeSlot.getTest().getId(),
                timeSlot.getTest().getTitle()
        );
    }
}