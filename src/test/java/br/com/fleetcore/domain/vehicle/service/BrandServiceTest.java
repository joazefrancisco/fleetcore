package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.*;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.enums.BrandStatusFilter;
import br.com.fleetcore.domain.vehicle.exception.BrandAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        Brand savedBrand = this.createBrand(1L, "Volvo", true);

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

        Brand brand = this.createBrand(1L, "Volvo", true);

        Brand updatedBrand = this.createBrand(1L, "Volvo Trucks", true);

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

        when(brandRepository.saveAndFlush(brand))
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

        verify(brandRepository).saveAndFlush(brand);
        verify(brandMapper).toDetails(updatedBrand);
    }

    @Test
    void updateStatus_ShouldDeactivateBrand_WhenActiveIsFalse() {

        Long brandId = 1L;

        boolean active = false;

        Brand brand = this.createBrand(1L, "Volvo", true);

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

        Brand brand = this.createBrand(1L, "Volvo", true);

        when(brandRepository.findById(brandId))
                .thenReturn(Optional.of(brand));

        brandService.updateStatus(brandId, active);

        verify(brandRepository).findById(brandId);
        verify(brandRepository, never()).save(brand);
    }

    @Test
    void findById_ShouldReturnBrandDetails_WhenBrandExists(){
        Long brandId = 1L;

        Brand brand = this.createBrand(1L, "Volvo", true);

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
    void findAll_ShouldReturnActiveBrandsByDefault_WhenStatusIsNull(){
        BrandStatusFilter status = null;

        boolean active = true;

        Brand brand = this.createBrand(1L, "Volvo", active);

        Pageable pageable = PageRequest.of(0, 1);

        Page<Brand> brandPage = new PageImpl<>(List.of(brand), pageable, 1);

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        when(brandRepository.findAllByActive(active, pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(brand))
                .thenReturn(brandSummary);

        Page<BrandSummary> result = brandService.findAll(status, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(brand.getId(), result.getContent().getFirst().id());
        assertEquals("Volvo", result.getContent().getFirst().name());

        verify(brandRepository).findAllByActive(active, pageable);
        verify(brandMapper).toSummary(brand);
    }

    @Test
    void findAll_ShouldReturnActiveBrands_WhenStatusIsActive(){

        BrandStatusFilter status = BrandStatusFilter.ACTIVE;

        boolean active = true;

        Brand brand = this.createBrand(1L, "Volvo", active);

        Pageable pageable = PageRequest.of(0, 1);

        Page<Brand> brandPage = new PageImpl<>(List.of(brand), pageable, 1);

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        when(brandRepository.findAllByActive(active, pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(brand))
                .thenReturn(brandSummary);

        Page<BrandSummary> result = brandService.findAll(status, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(brand.getId(), result.getContent().getFirst().id());
        assertEquals("Volvo", result.getContent().getFirst().name());

        verify(brandRepository).findAllByActive(active, pageable);
        verify(brandMapper).toSummary(brand);
    }

    @Test
    void findAll_ShouldReturnInactiveBrands_WhenStatusIsInactive(){
        BrandStatusFilter status = BrandStatusFilter.INACTIVE;

        boolean active = false;

        Brand brand = this.createBrand(1L, "Volvo", active);

        Pageable pageable = PageRequest.of(0, 1);

        BrandSummary brandSummary = new BrandSummary(1L, "Volvo");

        Page<Brand> brandPage = new PageImpl<>(List.of(brand), pageable, 1);

        when(brandRepository.findAllByActive(active, pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(brand))
                .thenReturn(brandSummary);

        Page<BrandSummary> result = brandService.findAll(status, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(brand.getId(), result.getContent().getFirst().id());
        assertEquals("Volvo", result.getContent().getFirst().name());

        verify(brandRepository).findAllByActive(active, pageable);
        verify(brandMapper).toSummary(brand);
    }

    @Test
    void findAll_ShouldReturnBrands_WhenStatusIsAll(){

        BrandStatusFilter status = BrandStatusFilter.ALL;

        Pageable pageable = PageRequest.of(0, 10);

        Brand volvo = this.createBrand(1L, "Volvo", true);

        Brand scania = this.createBrand(2L, "Scania", false);


        BrandSummary volvoSummary = new BrandSummary(1L, "Volvo");
        BrandSummary scaniaSummary = new BrandSummary(2L, "Scania");

        Page<Brand> brandPage = new PageImpl<>(List.of(volvo, scania), pageable, 2);

        when(brandRepository.findAll(pageable))
                .thenReturn(brandPage);

        when(brandMapper.toSummary(volvo))
                .thenReturn(volvoSummary);

        when(brandMapper.toSummary(scania))
                .thenReturn(scaniaSummary);


        Page<BrandSummary> result = brandService.findAll(status,  pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Volvo", result.getContent().get(0).name());
        assertEquals("Scania", result.getContent().get(1).name());

        verify(brandRepository).findAll(pageable);
        verify(brandMapper).toSummary(volvo);
        verify(brandMapper).toSummary(scania);
    }

    @Test
    void create_ShouldThrowException_WhenBrandAlready(){

        String brand = "Volvo";

        CreateBrandRequest request = new CreateBrandRequest(brand);

        when(brandRepository.existsByNameIgnoreCase(brand))
                .thenReturn(true);

        assertThrows(BrandAlreadyExistsException.class,
                () -> brandService.create(request));

        verify(brandRepository).existsByNameIgnoreCase(brand);
        verify(brandMapper, never()).toEntity(any());
        verify(brandRepository, never()).save(any());
    }

    @Test
    void findById_ShouldThrowBrandNotFoundException_WhenBrandDoesNotExist(){

        Long idBrand = 1L;

        when(brandRepository.findById(idBrand))
                .thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> brandService.findById(idBrand));

        verify(brandRepository).findById(idBrand);
        verify(brandMapper, never()).toDetails(any());
    }

    @Test
    void update_ShouldThrowBrandNotFoundException_WhenBrandDoesNotExist(){
        Long idBrand = 1L;

        UpdateBrandRequest request = new UpdateBrandRequest("Volvo");

        when(brandRepository.findById(idBrand))
                .thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> brandService.update(idBrand, request));

        verify(brandRepository).findById(any());
        verify(brandMapper, never()).toDetails(any());
    }

    @Test
    void update_ShouldThrowBrandInactiveException_WhenBrandIsInactive(){
        Long idBrand = 1L;

        UpdateBrandRequest request = new UpdateBrandRequest("Scania");

        Brand savedBrand = this.createBrand(idBrand, "Volvo", false);

        when(brandRepository.findById(idBrand))
                .thenReturn(Optional.of(savedBrand));

        assertThrows(BrandInactiveException.class,
                () -> brandService.update(idBrand, request));

        verify(brandRepository).findById(idBrand);
        verify(brandRepository, never()).saveAndFlush(any());
        verify(brandMapper, never()).toDetails(any());
    }

    @Test
    void update_ShouldReturnDetails_WhenBrandNameIsEqual(){
        Long idBrand = 1L;

        UpdateBrandRequest request = new UpdateBrandRequest("Volvo");

        Brand savedBrand = this.createBrand(idBrand, "Volvo", true);

        BrandDetails brandDetails = new BrandDetails(
                idBrand,
                "Volvo",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(brandRepository.findById(idBrand))
                .thenReturn(Optional.of(savedBrand));

        when(brandMapper.toDetails(savedBrand))
                .thenReturn(brandDetails);

        BrandDetails result = brandService.update(idBrand, request);

        assertNotNull(result);
        assertEquals(request.name(), result.name());

        verify(brandRepository).findById(idBrand);
        verify(brandMapper).toDetails(savedBrand);
        verify(brandRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_ShouldThrowBrandAlreadyExistsException_WhenNewNameAlreadyExists(){
        Long idBrand = 1L;

        UpdateBrandRequest request = new UpdateBrandRequest("Scania");

        Brand savedBrand = this.createBrand(idBrand, "Volvo", true);

        when(brandRepository.findById(idBrand))
                .thenReturn(Optional.of(savedBrand));

        when(brandRepository.existsByNameIgnoreCaseAndIdNot(request.name(), idBrand))
                .thenReturn(true);

        assertThrows(BrandAlreadyExistsException.class,
                () -> brandService.update(idBrand, request));

        verify(brandRepository).findById(idBrand);
        verify(brandRepository, never()).saveAndFlush(any());
        verify(brandMapper, never()).toDetails(any());
    }

    private Brand createBrand(Long id, String name, boolean active){
        return Brand.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }
}