package ma.project.echallenge.service;

import ma.project.echallenge.dto.request.TimeSlotRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TimeSlotResponse;
import ma.project.echallenge.entity.Test;
import ma.project.echallenge.entity.TimeSlot;
import ma.project.echallenge.exception.BadRequestException;
import ma.project.echallenge.exception.ResourceNotFoundException;
import ma.project.echallenge.repository.TestRepository;
import ma.project.echallenge.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private Test test;
    private TimeSlot timeSlot;
    private TimeSlotRequest timeSlotRequest;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        test = new Test();
        test.setId(1L);
        test.setTitle("Java Test");
        test.setDuration(60);

        futureStart = LocalDateTime.now().plusDays(1);
        futureEnd = futureStart.plusHours(2);

        timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setTest(test);
        timeSlot.setStartTime(futureStart);
        timeSlot.setEndTime(futureEnd);
        timeSlot.setBooked(false);

        timeSlotRequest = new TimeSlotRequest();
        timeSlotRequest.setTestId(1L);
        timeSlotRequest.setStartTime(futureStart);
        timeSlotRequest.setEndTime(futureEnd);
    }

    // ==================== CREATE TESTS ====================

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        ApiResponse<TimeSlotResponse> response = timeSlotService.createTimeSlot(timeSlotRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Créneau créé avec succès", response.getMessage());
        assertNotNull(response.getData());
        assertFalse(response.getData().getBooked());

        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldThrowException_WhenTestNotFound() {
        // Arrange
        when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> timeSlotService.createTimeSlot(timeSlotRequest)
        );

        assertEquals("Test non trouvé", exception.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldThrowException_WhenStartTimeAfterEndTime() {
        // Arrange
        timeSlotRequest.setStartTime(futureEnd);
        timeSlotRequest.setEndTime(futureStart);
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timeSlotService.createTimeSlot(timeSlotRequest)
        );

        assertEquals("L'heure de début doit être avant l'heure de fin", exception.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldThrowException_WhenStartTimeInPast() {
        // Arrange
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        timeSlotRequest.setStartTime(pastTime);
        timeSlotRequest.setEndTime(pastTime.plusHours(1));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timeSlotService.createTimeSlot(timeSlotRequest)
        );

        assertEquals("L'heure de début doit être dans le futur", exception.getMessage());

        verify(testRepository, times(1)).findById(1L);
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldSetBookedToFalse() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenAnswer(invocation -> {
            TimeSlot saved = invocation.getArgument(0);
            assertFalse(saved.isBooked());
            return saved;
        });

        // Act
        timeSlotService.createTimeSlot(timeSlotRequest);

        // Assert
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldHandleShortDuration() {
        // Arrange
        timeSlotRequest.setEndTime(futureStart.plusMinutes(15));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        ApiResponse<TimeSlotResponse> response = timeSlotService.createTimeSlot(timeSlotRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @org.junit.jupiter.api.Test
    void createTimeSlot_ShouldHandleLongDuration() {
        // Arrange
        timeSlotRequest.setEndTime(futureStart.plusHours(8));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        ApiResponse<TimeSlotResponse> response = timeSlotService.createTimeSlot(timeSlotRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    // ==================== READ TESTS ====================

    @org.junit.jupiter.api.Test
    void getAllTimeSlots_ShouldReturnList_WhenTimeSlotsExist() {
        // Arrange
        TimeSlot slot2 = new TimeSlot();
        slot2.setId(2L);
        slot2.setTest(test);
        slot2.setStartTime(futureStart.plusDays(1));
        slot2.setEndTime(futureEnd.plusDays(1));
        slot2.setBooked(false);

        when(timeSlotRepository.findAll()).thenReturn(Arrays.asList(timeSlot, slot2));

        // Act
        ApiResponse<List<TimeSlotResponse>> response = timeSlotService.getAllTimeSlots();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Créneaux récupérés", response.getMessage());
        assertEquals(2, response.getData().size());

        verify(timeSlotRepository, times(1)).findAll();
    }

    @org.junit.jupiter.api.Test
    void getAllTimeSlots_ShouldReturnEmptyList_WhenNoTimeSlots() {
        // Arrange
        when(timeSlotRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<TimeSlotResponse>> response = timeSlotService.getAllTimeSlots();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());

        verify(timeSlotRepository, times(1)).findAll();
    }

    @org.junit.jupiter.api.Test
    void getAvailableTimeSlots_ShouldReturnOnlyUnbookedSlots() {
        // Arrange
        TimeSlot bookedSlot = new TimeSlot();
        bookedSlot.setId(2L);
        bookedSlot.setTest(test);
        bookedSlot.setBooked(true);

        when(timeSlotRepository.findByTestIdAndBookedFalse(1L))
                .thenReturn(Collections.singletonList(timeSlot));

        // Act
        ApiResponse<List<TimeSlotResponse>> response = timeSlotService.getAvailableTimeSlots(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Créneaux disponibles récupérés", response.getMessage());
        assertEquals(1, response.getData().size());
        assertFalse(response.getData().get(0).getBooked());

        verify(timeSlotRepository, times(1)).findByTestIdAndBookedFalse(1L);
    }

    @org.junit.jupiter.api.Test
    void getAvailableTimeSlots_ShouldReturnEmptyList_WhenAllBooked() {
        // Arrange
        when(timeSlotRepository.findByTestIdAndBookedFalse(1L))
                .thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<TimeSlotResponse>> response = timeSlotService.getAvailableTimeSlots(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @org.junit.jupiter.api.Test
    void getTimeSlotById_ShouldReturnTimeSlot_WhenExists() {
        // Arrange
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act
        ApiResponse<TimeSlotResponse> response = timeSlotService.getTimeSlotById(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Créneau récupéré", response.getMessage());
        assertEquals(1L, response.getData().getTestId());
        assertEquals("Java Test", response.getData().getTestTitle());

        verify(timeSlotRepository, times(1)).findById(1L);
    }

    @org.junit.jupiter.api.Test
    void getTimeSlotById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> timeSlotService.getTimeSlotById(999L)
        );

        assertEquals("Créneau non trouvé", exception.getMessage());

        verify(timeSlotRepository, times(1)).findById(999L);
    }

    // ==================== UPDATE TESTS ====================

    @org.junit.jupiter.api.Test
    void updateTimeSlot_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        LocalDateTime newStart = futureStart.plusDays(1);
        LocalDateTime newEnd = futureEnd.plusDays(1);

        timeSlotRequest.setStartTime(newStart);
        timeSlotRequest.setEndTime(newEnd);

        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        // Act
        ApiResponse<TimeSlotResponse> response = timeSlotService.updateTimeSlot(1L, timeSlotRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Créneau mis à jour", response.getMessage());

        verify(timeSlotRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void updateTimeSlot_ShouldThrowException_WhenTimeSlotNotFound() {
        // Arrange
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> timeSlotService.updateTimeSlot(999L, timeSlotRequest)
        );

        assertEquals("Créneau non trouvé", exception.getMessage());

        verify(timeSlotRepository, times(1)).findById(999L);
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void updateTimeSlot_ShouldThrowException_WhenTimeSlotIsBooked() {
        // Arrange
        timeSlot.setBooked(true);
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timeSlotService.updateTimeSlot(1L, timeSlotRequest)
        );

        assertEquals("Impossible de modifier un créneau déjà réservé", exception.getMessage());

        verify(timeSlotRepository, times(1)).findById(1L);
        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void updateTimeSlot_ShouldThrowException_WhenStartTimeAfterEndTime() {
        // Arrange
        timeSlotRequest.setStartTime(futureEnd);
        timeSlotRequest.setEndTime(futureStart);
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timeSlotService.updateTimeSlot(1L, timeSlotRequest)
        );

        assertEquals("L'heure de début doit être avant l'heure de fin", exception.getMessage());

        verify(timeSlotRepository, never()).save(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void updateTimeSlot_ShouldUpdateTimes() {
        // Arrange
        LocalDateTime newStart = futureStart.plusDays(2);
        LocalDateTime newEnd = futureEnd.plusDays(2);

        timeSlotRequest.setStartTime(newStart);
        timeSlotRequest.setEndTime(newEnd);

        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenAnswer(invocation -> {
            TimeSlot updated = invocation.getArgument(0);
            assertEquals(newStart, updated.getStartTime());
            assertEquals(newEnd, updated.getEndTime());
            return updated;
        });

        // Act
        timeSlotService.updateTimeSlot(1L, timeSlotRequest);

        // Assert
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }

    // ==================== DELETE TESTS ====================

    @org.junit.jupiter.api.Test
    void deleteTimeSlot_ShouldReturnSuccess_WhenExists() {
        // Arrange
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        doNothing().when(timeSlotRepository).delete(any(TimeSlot.class));

        // Act
        ApiResponse<String> response = timeSlotService.deleteTimeSlot(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Créneau supprimé", response.getMessage());

        verify(timeSlotRepository, times(1)).findById(1L);
        verify(timeSlotRepository, times(1)).delete(timeSlot);
    }

    @org.junit.jupiter.api.Test
    void deleteTimeSlot_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> timeSlotService.deleteTimeSlot(999L)
        );

        assertEquals("Créneau non trouvé", exception.getMessage());

        verify(timeSlotRepository, times(1)).findById(999L);
        verify(timeSlotRepository, never()).delete(any(TimeSlot.class));
    }

    @org.junit.jupiter.api.Test
    void deleteTimeSlot_ShouldThrowException_WhenTimeSlotIsBooked() {
        // Arrange
        timeSlot.setBooked(true);
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timeSlotService.deleteTimeSlot(1L)
        );

        assertEquals("Impossible de supprimer un créneau déjà réservé", exception.getMessage());

        verify(timeSlotRepository, times(1)).findById(1L);
        verify(timeSlotRepository, never()).delete(any(TimeSlot.class));
    }

    // ==================== EDGE CASES ====================

    @org.junit.jupiter.api.Test
    void getAvailableTimeSlots_ShouldReturnMultipleSlots() {
        // Arrange
        TimeSlot slot2 = new TimeSlot();
        slot2.setId(2L);
        slot2.setTest(test);
        slot2.setBooked(false);

        TimeSlot slot3 = new TimeSlot();
        slot3.setId(3L);
        slot3.setTest(test);
        slot3.setBooked(false);

        when(timeSlotRepository.findByTestIdAndBookedFalse(1L))
                .thenReturn(Arrays.asList(timeSlot, slot2, slot3));

        // Act
        ApiResponse<List<TimeSlotResponse>> response = timeSlotService.getAvailableTimeSlots(1L);

        // Assert
        assertEquals(3, response.getData().size());
        response.getData().forEach(slot -> assertFalse(slot.getBooked()));
    }

    @org.junit.jupiter.api.Test
    void mapToResponse_ShouldIncludeAllFields() {
        // Arrange
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));

        // Act
        ApiResponse<TimeSlotResponse> response = timeSlotService.getTimeSlotById(1L);

        // Assert
        TimeSlotResponse data = response.getData();
        assertNotNull(data.getId());
        assertNotNull(data.getStartTime());
        assertNotNull(data.getEndTime());
        assertNotNull(data.getBooked());
        assertNotNull(data.getTestId());
        assertNotNull(data.getTestTitle());
    }
}