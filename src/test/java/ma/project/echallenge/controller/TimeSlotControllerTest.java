package ma.project.echallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.project.echallenge.dto.request.TimeSlotRequest;
import ma.project.echallenge.dto.response.ApiResponse;
import ma.project.echallenge.dto.response.TimeSlotResponse;
import ma.project.echallenge.service.TimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class TimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeSlotService timeSlotService;

    private TimeSlotRequest validTimeSlotRequest;
    private TimeSlotResponse timeSlotResponse1;
    private TimeSlotResponse timeSlotResponse2;

    @BeforeEach
    void setUp() {
        // Setup valid time slot request
        validTimeSlotRequest = new TimeSlotRequest();
        validTimeSlotRequest.setTestId(1L);
        validTimeSlotRequest.setStartTime(LocalDateTime.of(2026, 1, 15, 9, 0));
        validTimeSlotRequest.setEndTime(LocalDateTime.of(2026, 1, 15, 12, 0));
        validTimeSlotRequest.setMaxCandidates(30);

        // Setup time slot responses
        timeSlotResponse1 = new TimeSlotResponse();
        timeSlotResponse1.setId(1L);
        timeSlotResponse1.setTestId(1L);
        timeSlotResponse1.setStartTime(LocalDateTime.of(2026, 1, 15, 9, 0));
        timeSlotResponse1.setEndTime(LocalDateTime.of(2026, 1, 15, 12, 0));

        timeSlotResponse2 = new TimeSlotResponse();
        timeSlotResponse2.setId(2L);
        timeSlotResponse2.setTestId(1L);
        timeSlotResponse2.setStartTime(LocalDateTime.of(2026, 1, 16, 14, 0));
        timeSlotResponse2.setEndTime(LocalDateTime.of(2026, 1, 16, 17, 0));
    }

    // ==================== CREATE TIME SLOT TESTS ====================

    @Test
    void createTimeSlot_ShouldReturnCreatedTimeSlot_WhenRequestValid() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire créé avec succès", timeSlotResponse1);
        when(timeSlotService.createTimeSlot(any(TimeSlotRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.testId").value(1));

        verify(timeSlotService, times(1)).createTimeSlot(any(TimeSlotRequest.class));
    }

    @Test
    void createTimeSlot_ShouldReturn400_WhenTestIdIsNull() throws Exception {
        validTimeSlotRequest.setTestId(null);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).createTimeSlot(any());
    }

    @Test
    void createTimeSlot_ShouldReturn400_WhenStartTimeIsNull() throws Exception {
        validTimeSlotRequest.setStartTime(null);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).createTimeSlot(any());
    }

    @Test
    void createTimeSlot_ShouldReturn400_WhenEndTimeIsNull() throws Exception {
        validTimeSlotRequest.setEndTime(null);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).createTimeSlot(any());
    }

    @Test
    void createTimeSlot_ShouldReturn400_WhenMaxCandidatesIsZero() throws Exception {
        validTimeSlotRequest.setMaxCandidates(0);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).createTimeSlot(any());
    }

    @Test
    void createTimeSlot_ShouldReturn400_WhenMaxCandidatesIsNegative() throws Exception {
        validTimeSlotRequest.setMaxCandidates(-5);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).createTimeSlot(any());
    }

    @Test
    void createTimeSlot_ShouldAcceptNullMaxCandidates() throws Exception {
        validTimeSlotRequest.setMaxCandidates(null);
        ApiResponse response = ApiResponse.success("Créneau horaire créé avec succès", timeSlotResponse1);
        when(timeSlotService.createTimeSlot(any(TimeSlotRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isOk());

        verify(timeSlotService, times(1)).createTimeSlot(any());
    }

    // ==================== GET ALL TIME SLOTS TESTS ====================

    @Test
    void getAllTimeSlots_ShouldReturnListOfTimeSlots() throws Exception {
        List<TimeSlotResponse> timeSlots = Arrays.asList(timeSlotResponse1, timeSlotResponse2);
        ApiResponse response = ApiResponse.success("Créneaux horaires récupérés avec succès", timeSlots);
        when(timeSlotService.getAllTimeSlots()).thenReturn(response);

        mockMvc.perform(get("/api/timeslots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(timeSlotService, times(1)).getAllTimeSlots();
    }

    @Test
    void getAllTimeSlots_ShouldReturnEmptyList_WhenNoTimeSlots() throws Exception {
        ApiResponse response = ApiResponse.success("Créneaux horaires récupérés avec succès", Arrays.asList());
        when(timeSlotService.getAllTimeSlots()).thenReturn(response);

        mockMvc.perform(get("/api/timeslots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(timeSlotService, times(1)).getAllTimeSlots();
    }

    // ==================== GET AVAILABLE TIME SLOTS TESTS ====================

    @Test
    void getAvailableTimeSlots_ShouldReturnAvailableSlots_WhenTestIdProvided() throws Exception {
        List<TimeSlotResponse> availableSlots = Arrays.asList(timeSlotResponse1);
        ApiResponse response = ApiResponse.success("Créneaux disponibles récupérés", availableSlots);
        when(timeSlotService.getAvailableTimeSlots(1L)).thenReturn(response);

        mockMvc.perform(get("/api/timeslots/available")
                        .param("testId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(timeSlotService, times(1)).getAvailableTimeSlots(1L);
    }

    @Test
    void getAvailableTimeSlots_ShouldCallServiceWithCorrectTestId() throws Exception {
        ApiResponse response = ApiResponse.success("Créneaux disponibles récupérés", Arrays.asList());
        when(timeSlotService.getAvailableTimeSlots(2L)).thenReturn(response);

        mockMvc.perform(get("/api/timeslots/available")
                        .param("testId", "2"))
                .andExpect(status().isOk());

        verify(timeSlotService, times(1)).getAvailableTimeSlots(2L);
    }

    // ==================== GET TIME SLOT BY ID TESTS ====================

    @Test
    void getTimeSlotById_ShouldReturnTimeSlot_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire récupéré avec succès", timeSlotResponse1);
        when(timeSlotService.getTimeSlotById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/timeslots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.testId").value(1));

        verify(timeSlotService, times(1)).getTimeSlotById(1L);
    }

    @Test
    void getTimeSlotById_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire récupéré avec succès", timeSlotResponse2);
        when(timeSlotService.getTimeSlotById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/timeslots/2"))
                .andExpect(status().isOk());

        verify(timeSlotService, times(1)).getTimeSlotById(2L);
    }

    // ==================== UPDATE TIME SLOT TESTS ====================

    @Test
    void updateTimeSlot_ShouldReturnUpdatedTimeSlot_WhenRequestValid() throws Exception {
        TimeSlotResponse updatedTimeSlot = new TimeSlotResponse();
        updatedTimeSlot.setId(1L);

        ApiResponse response = ApiResponse.success("Créneau horaire mis à jour avec succès", updatedTimeSlot);
        when(timeSlotService.updateTimeSlot(eq(1L), any(TimeSlotRequest.class))).thenReturn(response);

        validTimeSlotRequest.setMaxCandidates(50);

        mockMvc.perform(put("/api/timeslots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(timeSlotService, times(1)).updateTimeSlot(eq(1L), any(TimeSlotRequest.class));
    }

    @Test
    void updateTimeSlot_ShouldReturn400_WhenTestIdIsNull() throws Exception {
        validTimeSlotRequest.setTestId(null);

        mockMvc.perform(put("/api/timeslots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).updateTimeSlot(anyLong(), any());
    }

    @Test
    void updateTimeSlot_ShouldReturn400_WhenStartTimeIsNull() throws Exception {
        validTimeSlotRequest.setStartTime(null);

        mockMvc.perform(put("/api/timeslots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).updateTimeSlot(anyLong(), any());
    }

    @Test
    void updateTimeSlot_ShouldReturn400_WhenMaxCandidatesIsNegative() throws Exception {
        validTimeSlotRequest.setMaxCandidates(-10);

        mockMvc.perform(put("/api/timeslots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isBadRequest());

        verify(timeSlotService, never()).updateTimeSlot(anyLong(), any());
    }

    @Test
    void updateTimeSlot_ShouldCallServiceWithCorrectIdAndData() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire mis à jour avec succès", timeSlotResponse1);
        when(timeSlotService.updateTimeSlot(eq(1L), any(TimeSlotRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/timeslots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeSlotRequest)))
                .andExpect(status().isOk());

        verify(timeSlotService).updateTimeSlot(eq(1L), argThat(request ->
                request.getTestId().equals(1L) &&
                        request.getMaxCandidates().equals(30)
        ));
    }

    // ==================== DELETE TIME SLOT TESTS ====================

    @Test
    void deleteTimeSlot_ShouldReturnSuccess_WhenIdExists() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire supprimé avec succès", null);
        when(timeSlotService.deleteTimeSlot(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/timeslots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(timeSlotService, times(1)).deleteTimeSlot(1L);
    }

    @Test
    void deleteTimeSlot_ShouldCallServiceWithCorrectId() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire supprimé avec succès", null);
        when(timeSlotService.deleteTimeSlot(2L)).thenReturn(response);

        mockMvc.perform(delete("/api/timeslots/2"))
                .andExpect(status().isOk());

        verify(timeSlotService, times(1)).deleteTimeSlot(2L);
    }

    @Test
    void deleteTimeSlot_ShouldHandleMultipleDeleteRequests() throws Exception {
        ApiResponse response = ApiResponse.success("Créneau horaire supprimé avec succès", null);
        when(timeSlotService.deleteTimeSlot(anyLong())).thenReturn(response);

        mockMvc.perform(delete("/api/timeslots/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/timeslots/2"))
                .andExpect(status().isOk());

        verify(timeSlotService, times(1)).deleteTimeSlot(1L);
        verify(timeSlotService, times(1)).deleteTimeSlot(2L);
    }
}