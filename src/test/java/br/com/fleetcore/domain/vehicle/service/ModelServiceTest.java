package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.CreateModelRequest;
import br.com.fleetcore.domain.vehicle.dto.ModelDetails;
import br.com.fleetcore.domain.vehicle.dto.ModelResponse;
import br.com.fleetcore.domain.vehicle.dto.ModelSummary;
import br.com.fleetcore.domain.vehicle.dto.UpdateModelRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.entity.Model;
import br.com.fleetcore.domain.vehicle.enums.ModelStatusFilter;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import br.com.fleetcore.domain.vehicle.exception.ModelAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.ModelInactiveException;
import br.com.fleetcore.domain.vehicle.exception.ModelNotFoundException;
import br.com.fleetcore.domain.vehicle.mapper.ModelMapper;
import br.com.fleetcore.domain.vehicle.repository.BrandRepository;
import br.com.fleetcore.domain.vehicle.repository.ModelRepository;
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
public class ModelServiceTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ModelService modelService;

    // ==================== CRIAÇÃO ====================

    @Test
    void create_ShouldCreateModel_WhenRequestIsValid() {
        // Arrange
        CreateModelRequest request = new CreateModelRequest("FH 540", 1L);
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();
        
        Model savedModel = createModel(1L, "FH 540", brand, true);
        
        ModelResponse response = new ModelResponse(
                1L,
                "FH 540",
                1L,
                "Volvo",
                true
        );
        
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(modelRepository.existsByNameIgnoreCase("FH 540")).thenReturn(false);
        when(modelMapper.toEntity(request)).thenReturn(model);
        when(modelRepository.save(model)).thenReturn(savedModel);
        when(modelMapper.toResponse(savedModel)).thenReturn(response);
        
        // Act
        ModelResponse result = modelService.create(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("FH 540", result.name());
        assertEquals(1L, result.brandId());
        assertEquals("Volvo", result.brandName());
        assertTrue(result.active());
        
        verify(brandRepository).findById(1L);
        verify(modelRepository).existsByNameIgnoreCase("FH 540");
        verify(modelMapper).toEntity(request);
        verify(modelRepository).save(model);
        verify(modelMapper).toResponse(savedModel);
    }

    @Test
    void create_ShouldNormalizeExternalSpaces_WhenNameHasLeadingAndTrailingSpaces() {
        // Arrange
        CreateModelRequest request = new CreateModelRequest("  FH 540  ", 1L);
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = Model.builder()
                .name("FH 540") // Nome deve ser normalizado
                .brand(brand)
                .build();
        
        Model savedModel = createModel(1L, "FH 540", brand, true);
        
        ModelResponse response = new ModelResponse(
                1L,
                "FH 540",
                1L,
                "Volvo",
                true
        );
        
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(modelRepository.existsByNameIgnoreCase("FH 540")).thenReturn(false);
        when(modelMapper.toEntity(request)).thenReturn(model);
        when(modelRepository.save(model)).thenReturn(savedModel);
        when(modelMapper.toResponse(savedModel)).thenReturn(response);
        
        // Act
        ModelResponse result = modelService.create(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("FH 540", result.name());
        
        verify(modelRepository).existsByNameIgnoreCase("FH 540"); // Nome normalizado
    }

    @Test
    void create_ShouldThrowModelAlreadyExistsException_WhenNameAlreadyExistsInAnotherBrand() {
        // Arrange
        CreateModelRequest request = new CreateModelRequest("FH 540", 2L);
        
        Brand brand = createBrand(2L, "Scania", true);
        
        when(brandRepository.findById(2L)).thenReturn(Optional.of(brand));
        when(modelRepository.existsByNameIgnoreCase("FH 540")).thenReturn(true);
        
        // Act & Assert
        assertThrows(ModelAlreadyExistsException.class, 
                () -> modelService.create(request));
        
        verify(brandRepository).findById(2L);
        verify(modelRepository).existsByNameIgnoreCase("FH 540");
        verify(modelMapper, never()).toEntity(any());
        verify(modelRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBrandNotFoundException_WhenBrandDoesNotExist() {
        // Arrange
        CreateModelRequest request = new CreateModelRequest("FH 540", 999L);
        
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BrandNotFoundException.class, 
                () -> modelService.create(request));
        
        verify(brandRepository).findById(999L);
        verify(modelRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    void create_ShouldThrowBrandInactiveException_WhenBrandIsInactive() {
        // Arrange
        CreateModelRequest request = new CreateModelRequest("FH 540", 1L);
        
        Brand brand = createBrand(1L, "Volvo", false);
        
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        
        // Act & Assert
        assertThrows(BrandInactiveException.class, 
                () -> modelService.create(request));
        
        verify(brandRepository).findById(1L);
        verify(modelRepository, never()).existsByNameIgnoreCase(anyString());
        verify(modelRepository, never()).save(any());
    }

    @Test
    void create_ShouldNotPersist_WhenValidationFails() {
        // Arrange
        CreateModelRequest request = new CreateModelRequest("FH 540", 999L);
        
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BrandNotFoundException.class, 
                () -> modelService.create(request));
        
        verify(modelRepository, never()).save(any());
    }

    // ==================== CONSULTA E LISTAGEM ====================

    @Test
    void findById_ShouldReturnModelDetails_WhenModelIsActive() {
        // Arrange
        Long modelId = 1L;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        
        ModelDetails modelDetails = new ModelDetails(
                modelId,
                "FH 540",
                1L,
                "Volvo",
                true,
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelMapper.toDetails(model)).thenReturn(modelDetails);
        
        // Act
        ModelDetails result = modelService.findById(modelId);
        
        // Assert
        assertNotNull(result);
        assertEquals(modelId, result.id());
        assertEquals("FH 540", result.name());
        assertTrue(result.active());
        
        verify(modelRepository).findById(modelId);
        verify(modelMapper).toDetails(model);
    }

    @Test
    void findById_ShouldReturnModelDetails_WhenModelIsInactive() {
        // Arrange
        Long modelId = 1L;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, false);
        
        ModelDetails modelDetails = new ModelDetails(
                modelId,
                "FH 540",
                1L,
                "Volvo",
                false,
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelMapper.toDetails(model)).thenReturn(modelDetails);
        
        // Act
        ModelDetails result = modelService.findById(modelId);
        
        // Assert
        assertNotNull(result);
        assertEquals(modelId, result.id());
        assertFalse(result.active());
        
        verify(modelRepository).findById(modelId);
        verify(modelMapper).toDetails(model);
    }

    @Test
    void findById_ShouldThrowModelNotFoundException_WhenModelDoesNotExist() {
        // Arrange
        Long modelId = 999L;
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ModelNotFoundException.class, 
                () -> modelService.findById(modelId));
        
        verify(modelRepository).findById(modelId);
        verify(modelMapper, never()).toDetails(any());
    }

    @Test
    void findAll_ShouldReturnActiveModelsByDefault_WhenStatusIsNull() {
        // Arrange
        ModelStatusFilter status = null;
        Long brandId = null;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(1L, "FH 540", brand, true);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Model> modelPage = new PageImpl<>(List.of(model), pageable, 1);
        
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        
        when(modelRepository.findAllByActive(true, pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(model)).thenReturn(modelSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("FH 540", result.getContent().getFirst().name());
        
        verify(modelRepository).findAllByActive(true, pageable);
        verify(modelMapper).toSummary(model);
    }

    @Test
    void findAll_ShouldReturnActiveModels_WhenStatusIsActive() {
        // Arrange
        ModelStatusFilter status = ModelStatusFilter.ACTIVE;
        Long brandId = null;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(1L, "FH 540", brand, true);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Model> modelPage = new PageImpl<>(List.of(model), pageable, 1);
        
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        
        when(modelRepository.findAllByActive(true, pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(model)).thenReturn(modelSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        verify(modelRepository).findAllByActive(true, pageable);
        verify(modelMapper).toSummary(model);
    }

    @Test
    void findAll_ShouldReturnInactiveModels_WhenStatusIsInactive() {
        // Arrange
        ModelStatusFilter status = ModelStatusFilter.INACTIVE;
        Long brandId = null;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(1L, "FH 540", brand, false);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Model> modelPage = new PageImpl<>(List.of(model), pageable, 1);
        
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        
        when(modelRepository.findAllByActive(false, pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(model)).thenReturn(modelSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        verify(modelRepository).findAllByActive(false, pageable);
        verify(modelMapper).toSummary(model);
    }

    @Test
    void findAll_ShouldReturnAllModels_WhenStatusIsAll() {
        // Arrange
        ModelStatusFilter status = ModelStatusFilter.ALL;
        Long brandId = null;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model activeModel = createModel(1L, "FH 540", brand, true);
        Model inactiveModel = createModel(2L, "FH 542", brand, false);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Model> modelPage = new PageImpl<>(List.of(activeModel, inactiveModel), pageable, 2);
        
        ModelSummary activeSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        ModelSummary inactiveSummary = new ModelSummary(2L, "FH 542", 1L, "Volvo");
        
        when(modelRepository.findAll(pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(activeModel)).thenReturn(activeSummary);
        when(modelMapper.toSummary(inactiveModel)).thenReturn(inactiveSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        
        verify(modelRepository).findAll(pageable);
        verify(modelMapper).toSummary(activeModel);
        verify(modelMapper).toSummary(inactiveModel);
    }

    @Test
    void findAll_ShouldFilterByBrand_WhenBrandIdIsProvided() {
        // Arrange
        ModelStatusFilter status = ModelStatusFilter.ACTIVE;
        Long brandId = 1L;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(1L, "FH 540", brand, true);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Model> modelPage = new PageImpl<>(List.of(model), pageable, 1);
        
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        
        when(modelRepository.findByBrandIdAndActive(1L, true, pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(model)).thenReturn(modelSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        verify(modelRepository).findByBrandIdAndActive(1L, true, pageable);
        verify(modelMapper).toSummary(model);
    }

    @Test
    void findAll_ShouldCombineStatusAndBrand_WhenBothFiltersAreProvided() {
        // Arrange
        ModelStatusFilter status = ModelStatusFilter.ACTIVE;
        Long brandId = 1L;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(1L, "FH 540", brand, true);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Model> modelPage = new PageImpl<>(List.of(model), pageable, 1);
        
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        
        when(modelRepository.findByBrandIdAndActive(1L, true, pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(model)).thenReturn(modelSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        verify(modelRepository).findByBrandIdAndActive(1L, true, pageable);
    }

    @Test
    void findAll_ShouldRespectPagination() {
        // Arrange
        ModelStatusFilter status = ModelStatusFilter.ACTIVE;
        Long brandId = null;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(1L, "FH 540", brand, true);
        
        Pageable pageable = PageRequest.of(1, 10);
        Page<Model> modelPage = new PageImpl<>(List.of(model), pageable, 11);
        
        ModelSummary modelSummary = new ModelSummary(1L, "FH 540", 1L, "Volvo");
        
        when(modelRepository.findAllByActive(true, pageable)).thenReturn(modelPage);
        when(modelMapper.toSummary(model)).thenReturn(modelSummary);
        
        // Act
        Page<ModelSummary> result = modelService.findAll(status, brandId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(11, result.getTotalElements());
        assertEquals(1, result.getNumber());
        
        verify(modelRepository).findAllByActive(true, pageable);
    }

    // ==================== ATUALIZAÇÃO ====================

    @Test
    void update_ShouldUpdateModelName_WhenModelIsActiveAndNameIsAvailable() {
        // Arrange
        Long modelId = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 542");
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        Model updatedModel = createModel(modelId, "FH 542", brand, true);
        
        ModelDetails modelDetails = new ModelDetails(
                modelId,
                "FH 542",
                1L,
                "Volvo",
                true,
                updatedModel.getCreatedAt(),
                updatedModel.getUpdatedAt()
        );
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.existsByNameIgnoreCaseAndIdNot("FH 542", modelId)).thenReturn(false);
        when(modelRepository.saveAndFlush(model)).thenReturn(updatedModel);
        when(modelMapper.toDetails(updatedModel)).thenReturn(modelDetails);
        
        // Act
        ModelDetails result = modelService.update(modelId, request);
        
        // Assert
        assertNotNull(result);
        assertEquals("FH 542", result.name());
        
        verify(modelRepository).findById(modelId);
        verify(modelRepository).existsByNameIgnoreCaseAndIdNot("FH 542", modelId);
        verify(modelRepository).saveAndFlush(model);
        verify(modelMapper).toDetails(updatedModel);
    }

    @Test
    void update_ShouldThrowModelInactiveException_WhenModelIsInactive() {
        // Arrange
        Long modelId = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 542");
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, false);
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        
        // Act & Assert
        assertThrows(ModelInactiveException.class, 
                () -> modelService.update(modelId, request));
        
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_ShouldThrowModelAlreadyExistsException_WhenNameAlreadyExistsGlobally() {
        // Arrange
        Long modelId = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 542");
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelRepository.existsByNameIgnoreCaseAndIdNot("FH 542", modelId)).thenReturn(true);
        
        // Act & Assert
        assertThrows(ModelAlreadyExistsException.class, 
                () -> modelService.update(modelId, request));
        
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_ShouldNotCallSave_WhenNormalizedNameHasNotChanged() {
        // Arrange
        Long modelId = 1L;
        UpdateModelRequest request = new UpdateModelRequest("  FH 540  "); // Espaços externos
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        
        ModelDetails modelDetails = new ModelDetails(
                modelId,
                "FH 540",
                1L,
                "Volvo",
                true,
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelMapper.toDetails(model)).thenReturn(modelDetails);
        
        // Act
        ModelDetails result = modelService.update(modelId, request);
        
        // Assert
        assertNotNull(result);
        assertEquals("FH 540", result.name());
        
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_ShouldNotCallSave_WhenNameIsExactlyTheSame() {
        // Arrange
        Long modelId = 1L;
        UpdateModelRequest request = new UpdateModelRequest("FH 540");
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        
        ModelDetails modelDetails = new ModelDetails(
                modelId,
                "FH 540",
                1L,
                "Volvo",
                true,
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(modelMapper.toDetails(model)).thenReturn(modelDetails);
        
        // Act
        ModelDetails result = modelService.update(modelId, request);
        
        // Assert
        assertNotNull(result);
        assertEquals("FH 540", result.name());
        
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).saveAndFlush(any());
    }

    // ==================== STATUS ====================

    @Test
    void updateStatus_ShouldActivateModel_WhenModelIsInactive() {
        // Arrange
        Long modelId = 1L;
        boolean active = true;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, false);
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        
        // Act
        modelService.updateStatus(modelId, active);
        
        // Assert
        assertTrue(model.isActive());
        verify(modelRepository).findById(modelId);
    }

    @Test
    void updateStatus_ShouldDeactivateModel_WhenModelIsActive() {
        // Arrange
        Long modelId = 1L;
        boolean active = false;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        
        // Act
        modelService.updateStatus(modelId, active);
        
        // Assert
        assertFalse(model.isActive());
        verify(modelRepository).findById(modelId);
    }

    @Test
    void updateStatus_ShouldNotCallSave_WhenStatusIsAlreadyActive() {
        // Arrange
        Long modelId = 1L;
        boolean active = true;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, true);
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        
        // Act
        modelService.updateStatus(modelId, active);
        
        // Assert
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).save(any());
    }

    @Test
    void updateStatus_ShouldNotCallSave_WhenStatusIsAlreadyInactive() {
        // Arrange
        Long modelId = 1L;
        boolean active = false;
        
        Brand brand = createBrand(1L, "Volvo", true);
        Model model = createModel(modelId, "FH 540", brand, false);
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        
        // Act
        modelService.updateStatus(modelId, active);
        
        // Assert
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).save(any());
    }

    @Test
    void updateStatus_ShouldThrowModelNotFoundException_WhenModelDoesNotExist() {
        // Arrange
        Long modelId = 999L;
        boolean active = true;
        
        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ModelNotFoundException.class, 
                () -> modelService.updateStatus(modelId, active));
        
        verify(modelRepository).findById(modelId);
        verify(modelRepository, never()).save(any());
    }

    // ==================== HELPER METHODS ====================

    private Brand createBrand(Long id, String name, boolean active) {
        return Brand.builder()
                .id(id)
                .name(name)
                .active(active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Model createModel(Long id, String name, Brand brand, boolean active) {
        return Model.builder()
                .id(id)
                .name(name)
                .brand(brand)
                .active(active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
