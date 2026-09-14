package br.com.fleetcore.domain.vehicle.controller;

import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(status().isBadRequest());

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
}

