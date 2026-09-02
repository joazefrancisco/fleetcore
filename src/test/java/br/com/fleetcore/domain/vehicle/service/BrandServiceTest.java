package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.mapper.BrandMapper;
import br.com.fleetcore.domain.vehicle.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandService brandService;

    @Test
    void create_ShouldCreateBrand_WhenNameIsAvailable() {

        CreateBrandRequest request = new CreateBrandRequest("Volvo");

        Brand brand = Brand.builder()
                .name("Volvo")
                .build();

        Brand savedBrand = Brand.builder()
                .id(1L)
                .name("Volvo")
                .active(true)
                .build();

        BrandResponse response = new BrandResponse(
                1L,
                "Volvo",
                true
        );

        when(brandRepository.existsByNameIgnoreCase("Volvo"))
                .thenReturn(false);

        when(brandMapper.toEntity(request))
                .thenReturn(brand);

        when(brandRepository.save(brand))
                .thenReturn(savedBrand);

        when(brandMapper.toResponse(savedBrand))
                .thenReturn(response);

        // Act
        BrandResponse result = brandService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Volvo", result.name());
        verify(brandRepository).existsByNameIgnoreCase("Volvo");
        verify(brandMapper).toEntity(request);
        verify(brandRepository).save(brand);
        verify(brandMapper).toResponse(savedBrand);
    }
}
