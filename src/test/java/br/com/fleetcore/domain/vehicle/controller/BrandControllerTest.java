package br.com.fleetcore.domain.vehicle.controller;

import br.com.fleetcore.domain.vehicle.dto.BrandDetails;
import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateBrandRequest;
import br.com.fleetcore.domain.vehicle.exception.BrandAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import br.com.fleetcore.domain.vehicle.service.BrandService;
import org.springframework.data.domain.Page;
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


@WebMvcTest(BrandController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandService brandService;

    @Test
    void create_ShouldReturnCreated_WhenRequestIsValid() throws Exception {

        CreateBrandRequest request = new CreateBrandRequest("Volvo");

        BrandResponse response = new BrandResponse(1L, "Volvo", true);

        when(brandService.create(request))
                .thenReturn(response);

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Volvo"))
                .andExpect(jsonPath("$.active").value(true)
                );

        verify(brandService).create(request);
    }

    @Test
    void create_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {

        CreateBrandRequest request = new CreateBrandRequest("");

        mockMvc.perform(post("/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/brands"));

        verify(brandService, never()).create(request);
    }

    @Test
    void findAll_ShouldReturnOk_WhenRequestIsValid() throws Exception {

        Boolean active = null;

        Pageable pageable = PageRequest.of(0, 10);

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        Page<BrandSummary> brandSummaryPage = new PageImpl<>(List.of(brandSummary), pageable, 1);

        when(brandService.findAll(eq(active), any(Pageable.class)))
                .thenReturn(brandSummaryPage);

        mockMvc.perform(get("/brands")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Volvo"));

        verify(brandService).findAll(active, pageable);
    }

    @Test
    void findAll_ShouldReturnActiveBrands_WhenActiveIsTrue() throws Exception {

        boolean active = true;

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        Pageable pageable = PageRequest.of(0, 20);

        Page<BrandSummary> brandPage = new PageImpl<>(List.of(brandSummary), pageable, 1);

        when(brandService.findAll(active, pageable))
                .thenReturn(brandPage);

        mockMvc.perform(get("/brands")
                                .param("active", "true")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Volvo"));

        verify(brandService).findAll(active, pageable);
    }

    @Test
    void findAll_ShouldReturnInactiveBrands_WhenActiveIsFalse() throws Exception {

        boolean active = false;

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        Pageable pageable = PageRequest.of(0, 20);

        Page<BrandSummary> brandPage = new PageImpl<>(List.of(brandSummary), pageable, 1);

        when(brandService.findAll(active, pageable))
                .thenReturn(brandPage);

        mockMvc.perform(get("/brands")
                        .param("active", "false")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Volvo"));

        verify(brandService).findAll(active, pageable);
    }

    @Test
    void findById_ShouldReturnOk_WhenRequestIsValid() throws Exception {

        Long id = 1L;

        BrandDetails brandDetails = new BrandDetails(id,
                "Volvo",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(brandService.findById(id))
                .thenReturn(brandDetails);

        mockMvc.perform(get("/brands/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Volvo"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        verify(brandService).findById(id);
    }


    @Test
    void findById_ShouldReturnNotFound_WhenBrandIsNotFound() throws Exception {

        Long id = 2L;

        when(brandService.findById(id))
                .thenThrow(new BrandNotFoundException("Brand not found"));

        mockMvc.perform(get("/brands/{id}", id)
                .accept(MediaType.APPLICATION_JSON)

        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("BRAND_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Brand not found"))
                .andExpect(jsonPath("$.path").value("/brands/2"));

        verify(brandService).findById(id);
    }

    @Test
    void update_ShouldReturnOk_WhenRequestIsValid() throws Exception {

        Long id = 1L;
        UpdateBrandRequest request = new UpdateBrandRequest("Volvo Updated");

        BrandDetails brandDetails = new BrandDetails(id,
                "Volvo Updated",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(brandService.update(id, request))
                .thenReturn(brandDetails);

        mockMvc.perform(put("/brands/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Volvo Updated"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        verify(brandService).update(id, request);
    }

    @Test
    void update_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {

        Long id = 1L;
        UpdateBrandRequest request = new UpdateBrandRequest("");

        mockMvc.perform(put("/brands/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/brands/1"));

        verify(brandService, never()).update(id, request);
    }

    @Test
    void update_ShouldReturnNotFound_WhenBrandIsNotFound() throws Exception {

        Long id = 2L;
        UpdateBrandRequest request = new UpdateBrandRequest("Volvo");

        when(brandService.update(id, request))
                .thenThrow(new BrandNotFoundException("Brand not found"));

        mockMvc.perform(put("/brands/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("BRAND_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Brand not found"))
                .andExpect(jsonPath("$.path").value("/brands/2"));

        verify(brandService).update(id, request);
    }

    @Test
    void update_ShouldReturnBrandInactive_WhenBrandIsInactive() throws  Exception {

        Long id = 1L;
        UpdateBrandRequest request = new UpdateBrandRequest("Volvo");

        when(brandService.update(id, request))
                .thenThrow(new BrandInactiveException("Brand inactive"));

        mockMvc.perform(put("/brands/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("BRAND_INACTIVE"))
                .andExpect(jsonPath("$.message").value("Brand inactive"))
                .andExpect(jsonPath("$.path").value("/brands/1"));

        verify(brandService).update(id, request);
    }

    @Test
    void update_ShouldReturnConflict_WhenBrandAlreadyExists() throws  Exception {

        Long id = 1L;
        UpdateBrandRequest request = new UpdateBrandRequest("Volvo");

        when(brandService.update(id, request))
                .thenThrow(new BrandAlreadyExistsException("Brand already exists"));

        mockMvc.perform(put("/brands/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("BRAND_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Brand already exists"))
                .andExpect(jsonPath("$.path").value("/brands/1"));

        verify(brandService).update(id, request);
    }

    @Test
    void updateStatus_ShouldReturnNoContent_WhenRequestIsValid() throws Exception {

        Long id = 1L;
        boolean active = true;

        mockMvc.perform(patch("/brands/{id}/status", id)
                .param("active", "true"))

                .andExpect(status().isNoContent());

        verify(brandService).updateStatus(id, active);
    }

    @Test
    void updateStatus_ShouldReturnNoContent_WhenDeactivatingBrand() throws Exception {

        Long id = 1L;
        boolean active = false;

        mockMvc.perform(patch("/brands/{id}/status", id)
                        .param("active", "false"))

                .andExpect(status().isNoContent());

        verify(brandService).updateStatus(id, active);
    }

    @Test
    void updateStatus_ShouldReturnNoContent_WhenBrandIsNotFound() throws Exception {

        Long id = 2L;
        boolean active = true;

        doThrow(new BrandNotFoundException("Brand not found"))
                .when(brandService)
                .updateStatus(id, active);

        mockMvc.perform(patch("/brands/{id}/status", id)
                        .param("active", "true")
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("BRAND_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Brand not found"))
                .andExpect(jsonPath("$.path").value("/brands/2/status"));

        verify(brandService).updateStatus(id, active);
    }

    @Test
    void updateStatus_ShouldReturnBadRequest_WhenActiveIsInvalid() throws Exception {

        Long id = 1L;
        String active = "abc";

        mockMvc.perform(patch("/brands/{id}/status", id)
                        .param("active", active)
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid parameter value"))
                .andExpect(jsonPath("$.path").value("/brands/1/status"));

        verify(brandService, never()).updateStatus(anyLong(), anyBoolean());
    }
}

