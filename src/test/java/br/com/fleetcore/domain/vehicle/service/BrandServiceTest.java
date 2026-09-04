package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.*;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.mapper.BrandMapper;
import br.com.fleetcore.domain.vehicle.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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

    @Test
    void update_ShouldUpdateBrand_WhenNewNameIsAvailable() {

        Long brandId = 1L;

        UpdateBrandRequest request = new UpdateBrandRequest("Volvo Trucks");

        Brand brand = Brand.builder()
                .id(brandId)
                .name("Volvo")
                .active(true)
                .build();

        Brand updatedBrand = Brand.builder()
                .id(brandId)
                .name("Volvo Trucks")
                .active(true)
                .build();

        BrandDetails response = new BrandDetails(
                brandId,
                "Volvo Trucks",
                true,
                updatedBrand.getCreatedAt(),
                updatedBrand.getUpdatedAt()
        );

        when(brandRepository.findById(brandId))
                .thenReturn(Optional.of(brand));

        when(brandRepository.existsByNameIgnoreCaseAndIdNot(
                "Volvo Trucks",
                brandId
        )).thenReturn(false);

        when(brandRepository.save(brand))
                .thenReturn(updatedBrand);

        when(brandMapper.toDetails(updatedBrand))
                .thenReturn(response);

        BrandDetails result = brandService.update(brandId, request);

        assertNotNull(result);
        assertEquals(brandId, result.id());
        assertEquals("Volvo Trucks", result.name());

        verify(brandRepository).findById(brandId);
        verify(brandRepository).existsByNameIgnoreCaseAndIdNot(
                "Volvo Trucks",
                brandId
        );

        verify(brandRepository).save(brand);
        verify(brandMapper).toDetails(updatedBrand);
    }

    @Test
    void updateStatus_ShouldDeactivateBrand_WhenActiveIsFalse() {

        Long brandId = 1L;

        boolean active = false;

        Brand brand = Brand.builder()
                .id(brandId)
                .name("Volvo")
                .active(true)
                .build();

        when(brandRepository.findById(brandId))
                .thenReturn(Optional.of(brand));

        brandService.updateStatus(brandId, active);

        assertEquals(active, brand.isActive());
        verify(brandRepository).findById(brandId);
        verify(brandRepository).save(brand);
    }

    @Test
    void updateStatus_ShouldNotSave_WhenStatusIsAlreadyActive(){
        Long brandId = 1L;

        boolean active = true;

        Brand brand = Brand.builder()
                .id(brandId)
                .name("Volvo")
                .active(true)
                .build();

        when(brandRepository.findById(brandId))
                .thenReturn(Optional.of(brand));

        brandService.updateStatus(brandId, active);

        verify(brandRepository).findById(brandId);
        verify(brandRepository, never()).save(brand);
    }

    @Test
    void findById_ShouldReturnBrandDetails_WhenBrandExists(){
        Long brandId = 1L;

        Brand brand = Brand.builder()
                .id(brandId)
                .name("Volvo")
                .active(true)
                .build();

        BrandDetails brandDetails = new BrandDetails(
                brandId,
                "Volvo",
                true,
                brand.getCreatedAt(),
                brand.getUpdatedAt());

        when(brandRepository.findById(brandId))
                .thenReturn(Optional.of(brand));

        when(brandMapper.toDetails(brand))
                .thenReturn(brandDetails);

        BrandDetails result = brandService.findById(brandId);

        assertNotNull(result);
        assertEquals(brandId, result.id());
        assertEquals(brand.isActive(), result.active());
        assertEquals("Volvo", result.name());

        verify(brandRepository).findById(brandId);
        verify(brandMapper).toDetails(brand);
    }

    @Test
    void findAll_ShouldReturnBrands_WhenActiveIsNull(){

        Boolean active = null;

        Pageable pageable = PageRequest.of(0, 10);

        Brand volvo = Brand.builder()
                .id(1L)
                .name("Volvo")
                .active(true)
                .build();

        Brand scania = Brand.builder()
                .id(2L)
                .name("Scania")
                .active(false)
                .build();

        BrandSummary volvoSummary = new BrandSummary(1L, "Volvo");
        BrandSummary scaniaSummary = new BrandSummary(2L, "Scania");

        Page<Brand> brandPage = new PageImpl<>(List.of(volvo, scania), pageable, 2);

        when(brandRepository.findAll(pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(volvo))
                .thenReturn(volvoSummary);

        when(brandMapper.toSummary(scania))
                .thenReturn(scaniaSummary);


        Page<BrandSummary> result = brandService.findAll(active,  pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Volvo", result.getContent().get(0).name());
        assertEquals("Scania", result.getContent().get(1).name());

        verify(brandRepository).findAll(pageable);
        verify(brandMapper).toSummary(volvo);
        verify(brandMapper).toSummary(scania);
    }

    @Test
    void findAll_ShouldReturnActiveBrands_WhenActiveIsTrue(){
        boolean active = true;

        Brand brand = Brand.builder()
                .id(1L)
                .name("Volvo")
                .active(active)
                .build();

        Pageable pageable = PageRequest.of(0, 1);

        Page<Brand> brandPage = new PageImpl<>(List.of(brand), pageable, 1);

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        when(brandRepository.findAllByActive(active, pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(brand))
                .thenReturn(brandSummary);

        Page<BrandSummary> result = brandService.findAll(active, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(brand.getId(), result.getContent().getFirst().id());
        assertEquals("Volvo", result.getContent().getFirst().name());

        verify(brandRepository).findAllByActive(active, pageable);
        verify(brandMapper).toSummary(brand);
    }

    @Test
    void findAll_ShouldReturnInactiveBrands_WhenActiveIsFalse(){
        boolean active = false;

        Brand brand = Brand.builder()
                .id(1L)
                .name("Volvo")
                .active(active)
                .build();

        Pageable pageable = PageRequest.of(0, 1);

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        Page<Brand> brandPage = new PageImpl<>(List.of(brand), pageable, 1);

        when(brandRepository.findAllByActive(active, pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(brand))
                .thenReturn(brandSummary);

        Page<BrandSummary> result = brandService.findAll(active, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(brand.getId(), result.getContent().getFirst().id());
        assertEquals("Volvo", result.getContent().getFirst().name());

        verify(brandRepository).findAllByActive(active, pageable);
        verify(brandMapper).toSummary(brand);
    }
}
