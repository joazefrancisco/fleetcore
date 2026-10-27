package br.com.fleetcore.domain.vehicle.controller;

import br.com.fleetcore.domain.vehicle.dto.ModelDetails;
import br.com.fleetcore.domain.vehicle.dto.ModelResponse;
import br.com.fleetcore.domain.vehicle.dto.ModelSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateModelRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateModelRequest;
import br.com.fleetcore.domain.vehicle.enums.ModelStatusFilter;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import br.com.fleetcore.domain.vehicle.exception.ModelAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.ModelInactiveException;
import br.com.fleetcore.domain.vehicle.exception.ModelNotFoundException;
import br.com.fleetcore.domain.vehicle.service.ModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModelController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ModelService modelService;

    @Test
    void create_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        CreateModelRequest request = new CreateModelRequest("FH 540", 1L);
        ModelResponse response = new ModelResponse(1L, "FH 540", 1L, "Volvo", true);

        when(modelService.create(request)).thenReturn(response);

        mockMvc.perform(post("/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("FH 540"))
                .andExpect(jsonPath("$.brandId").value(1L))
                .andExpect(jsonPath("$.brandName").value("Volvo"))
                .andExpect(jsonPath("$.active").value(true));

        verify(modelService).create(request);
    }

    @Test
    void create_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        CreateModelRequest request = new CreateModelRequest("", 1L);

        mockMvc.perform(post("/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/models"));

        verify(modelService, never()).create(any());
    }

    @Test
    void findAll_ShouldReturnActiveModelsByDefault_WhenStatusIsNotProvided() throws Exception {
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        Pageable pageable = PageRequest.of(0, 20);
        var modelPage = new PageImpl<>(List.of(modelSummary), pageable, 1);

        when(modelService.findAll(null, null, pageable)).thenReturn(modelPage);

        mockMvc.perform(get("/models")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("FH 540"));

        verify(modelService).findAll(null, null, pageable);
    }

    @Test
    void findAll_ShouldReturnActiveModels_WhenStatusIsActive() throws Exception {
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        Pageable pageable = PageRequest.of(0, 20);
        var modelPage = new PageImpl<>(List.of(modelSummary), pageable, 1);

        when(modelService.findAll(ModelStatusFilter.ACTIVE, null, pageable)).thenReturn(modelPage);

        mockMvc.perform(get("/models")
                        .param("status", "ACTIVE")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("FH 540"));

        verify(modelService).findAll(ModelStatusFilter.ACTIVE, null, pageable);
    }

    @Test
    void findAll_ShouldReturnInactiveModels_WhenStatusIsInactive() throws Exception {
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        Pageable pageable = PageRequest.of(0, 20);
        var modelPage = new PageImpl<>(List.of(modelSummary), pageable, 1);

        when(modelService.findAll(ModelStatusFilter.INACTIVE, null, pageable)).thenReturn(modelPage);

        mockMvc.perform(get("/models")
                        .param("status", "INACTIVE")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("FH 540"));

        verify(modelService).findAll(ModelStatusFilter.INACTIVE, null, pageable);
    }

    @Test
    void findAll_ShouldReturnAllModels_WhenStatusIsAll() throws Exception {
        ModelSummary volvo = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        ModelSummary scania = new ModelSummary(2L, "P 410", 2L, "Scania");
        Pageable pageable = PageRequest.of(0, 20);
        var modelPage = new PageImpl<>(List.of(volvo, scania), pageable, 2);

        when(modelService.findAll(ModelStatusFilter.ALL, null, pageable)).thenReturn(modelPage);

        mockMvc.perform(get("/models")
                        .param("status", "ALL")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("FH 540"))
                .andExpect(jsonPath("$.content[1].name").value("P 410"));

        verify(modelService).findAll(ModelStatusFilter.ALL, null, pageable);
    }

    @Test
    void findAll_ShouldReturnModelsByBrand_WhenBrandIdIsProvided() throws Exception {
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        Pageable pageable = PageRequest.of(0, 20);
        var modelPage = new PageImpl<>(List.of(modelSummary), pageable, 1);

        when(modelService.findAll(null, 1L, pageable)).thenReturn(modelPage);

        mockMvc.perform(get("/models")
                        .param("brandId", "1")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        verify(modelService).findAll(null, 1L, pageable);
    }

    @Test
    void findAll_ShouldReturnBadRequest_WhenStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/models")
                        .param("status", "INVALID")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid parameter value"))
                .andExpect(jsonPath("$.path").value("/models"));

        verify(modelService, never()).findAll(any(), any(), any());
    }

    @Test
    void findById_ShouldReturnOk_WhenModelIsActive() throws Exception {
        Long id = 1L;
        ModelDetails modelDetails = new ModelDetails(
                id,
                "FH 540",
                1L,
                "Volvo",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(modelService.findById(id)).thenReturn(modelDetails);

        mockMvc.perform(get("/models/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("FH 540"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        verify(modelService).findById(id);
    }

    @Test
    void findById_ShouldReturnOk_WhenModelIsInactive() throws Exception {
        Long id = 1L;
        ModelDetails modelDetails = new ModelDetails(
                id,
                "FH 540",
                1L,
                "Volvo",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(modelService.findById(id)).thenReturn(modelDetails);

        mockMvc.perform(get("/models/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("FH 540"))
                .andExpect(jsonPath("$.active").value(false));

        verify(modelService).findById(id);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenModelIsNotFound() throws Exception {
        Long id = 2L;

        when(modelService.findById(id))
                .thenThrow(new ModelNotFoundException("Model not found"));

        mockMvc.perform(get("/models/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("MODEL_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Model not found"))
                .andExpect(jsonPath("$.path").value("/models/2"));

        verify(modelService).findById(id);
    }

    @Test
    void update_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Long id = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 542");

        ModelDetails modelDetails = new ModelDetails(
                id,
                "FH 542",
                1L,
                "Volvo",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(modelService.update(id, request)).thenReturn(modelDetails);

        mockMvc.perform(put("/models/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("FH 542"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        verify(modelService).update(id, request);
    }

    @Test
    void update_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        Long id = 1L;
        UpdateModelRequest request = new UpdateModelRequest("");

        mockMvc.perform(put("/models/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/models/1"));

        verify(modelService, never()).update(anyLong(), any());
    }

    @Test
    void update_ShouldReturnNotFound_WhenModelIsNotFound() throws Exception {
        Long id = 2L;
        UpdateModelRequest request = new UpdateModelRequest("FH 540");

        when(modelService.update(id, request))
                .thenThrow(new ModelNotFoundException("Model not found"));

        mockMvc.perform(put("/models/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("MODEL_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Model not found"))
                .andExpect(jsonPath("$.path").value("/models/2"));

        verify(modelService).update(id, request);
    }

    @Test
    void update_ShouldReturnConflict_WhenModelIsInactive() throws Exception {
        Long id = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 540");

        when(modelService.update(id, request))
                .thenThrow(new ModelInactiveException("Model inactive"));

        mockMvc.perform(put("/models/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("MODEL_INACTIVE"))
                .andExpect(jsonPath("$.message").value("Model inactive"))
                .andExpect(jsonPath("$.path").value("/models/1"));

        verify(modelService).update(id, request);
    }

    @Test
    void update_ShouldReturnConflict_WhenNameAlreadyExists() throws Exception {
        Long id = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 540");

        when(modelService.update(id, request))
                .thenThrow(new ModelAlreadyExistsException("Model already exists"));

        mockMvc.perform(put("/models/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("MODEL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Model already exists"))
                .andExpect(jsonPath("$.path").value("/models/1"));

        verify(modelService).update(id, request);
    }

    @Test
    void updateStatus_ShouldReturnNoContent_WhenActivatingModel() throws Exception {
        Long id = 1L;
        boolean active = true;

        mockMvc.perform(patch("/models/{id}/status", id)
                        .param("active", "true")
                )
                .andExpect(status().isNoContent());

        verify(modelService).updateStatus(id, active);
    }

    @Test
    void updateStatus_ShouldReturnNoContent_WhenDeactivatingModel() throws Exception {
        Long id = 1L;
        boolean active = false;

        mockMvc.perform(patch("/models/{id}/status", id)
                        .param("active", "false")
                )
                .andExpect(status().isNoContent());

        verify(modelService).updateStatus(id, active);
    }

    @Test
    void updateStatus_ShouldReturnNotFound_WhenModelIsNotFound() throws Exception {
        Long id = 2L;
        boolean active = true;

        doThrow(new ModelNotFoundException("Model not found"))
                .when(modelService)
                .updateStatus(id, active);

        mockMvc.perform(patch("/models/{id}/status", id)
                        .param("active", "true")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("MODEL_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Model not found"))
                .andExpect(jsonPath("$.path").value("/models/2/status"));

        verify(modelService).updateStatus(id, active);
    }

    @Test
    void updateStatus_ShouldReturnBadRequest_WhenActiveIsInvalid() throws Exception {
        Long id = 1L;
        String active = "abc";

        mockMvc.perform(patch("/models/{id}/status", id)
                        .param("active", active)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid parameter value"))
                .andExpect(jsonPath("$.path").value("/models/1/status"));

        verify(modelService, never()).updateStatus(anyLong(), anyBoolean());
    }

    @Test
    void handleGenericException_ShouldReturnServerError_WhenUnexpectedExceptionOccurs() throws Exception {
        Long id = 1L;

        when(modelService.findById(id))
                .thenThrow(new NullPointerException());

        mockMvc.perform(get("/models/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/models/1"));

        verify(modelService).findById(id);
    }
}
