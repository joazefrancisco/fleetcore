
package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.dto.UpdateModelRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.entity.Model;
import br.com.fleetcore.domain.vehicle.service.ModelService;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ModelIntegrationTest {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelService modelService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistModelInPostgres() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        assertNotNull(savedModel);
        assertNotNull(savedModel.getId());
        assertEquals("FH 540", savedModel.getName());
        assertTrue(savedModel.isActive());
        assertEquals(brand.getId(), savedModel.getBrand().getId());
    }

    @Test
    void shouldRejectDuplicateModelNameIgnoreCase_Globally() {
        Brand volvo = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );
        Brand scania = brandRepository.saveAndFlush(
                Brand.builder().name("Scania").build()
        );

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();

        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("fh 540")
                .brand(scania)
                .build();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> modelRepository.saveAndFlush(secondModel)
        );

        ConstraintViolationException cause =
                assertInstanceOf(
                        ConstraintViolationException.class,
                        exception.getCause()
                );

        assertEquals("uk_model_name_lower", cause.getConstraintName());
    }

    @Test
    void shouldRejectDuplicateModelNameWithExternalSpaces_Globally() {
        Brand volvo = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );
        Brand scania = brandRepository.saveAndFlush(
                Brand.builder().name("Scania").build()
        );

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();

        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("  FH 540  ")
                .brand(scania)
                .build();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> modelRepository.saveAndFlush(secondModel)
        );

        ConstraintViolationException cause =
                assertInstanceOf(
                        ConstraintViolationException.class,
                        exception.getCause()
                );

        assertEquals("uk_model_name_lower", cause.getConstraintName());
    }

    @Test
    void shouldAllowDifferentNamesInSameBrand() {
        Brand volvo = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();

        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("FH 542")
                .brand(volvo)
                .build();

        Model savedSecondModel = modelRepository.saveAndFlush(secondModel);

        assertNotNull(savedSecondModel);
        assertEquals("FH 542", savedSecondModel.getName());
    }

    @Test
    void shouldRejectSameModelNameAcrossDifferentBrands() {
        Brand volvo = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );
        Brand scania = brandRepository.saveAndFlush(
                Brand.builder().name("Scania").build()
        );

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();

        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("FH 540")
                .brand(scania)
                .build();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> modelRepository.saveAndFlush(secondModel)
        );

        ConstraintViolationException cause =
                assertInstanceOf(
                        ConstraintViolationException.class,
                        exception.getCause()
                );

        assertEquals("uk_model_name_lower", cause.getConstraintName());
    }

    @Test
    void shouldPreserveInternalConsecutiveSpaces() {
        Brand volvo = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model model = Model.builder()
                .name("FH  540")
                .brand(volvo)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        assertNotNull(savedModel);
        assertEquals("FH  540", savedModel.getName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectConcurrentUpdate_whenModelVersionIsOutdated() {
        Long brandId = null;
        Long firstModelId = null;

        try {
            Long createdBrandId = transactionTemplate.execute(status -> {
                Brand brand = brandRepository.saveAndFlush(
                        Brand.builder().name("Volvo").build()
                );

                return brand.getId();
            });

            assertNotNull(createdBrandId);
            brandId = createdBrandId;

            final Long savedBrandId = createdBrandId;

            Long createdModelId = transactionTemplate.execute(status -> {
                Model savedModel = modelRepository.saveAndFlush(
                        Model.builder()
                                .name("FH 540")
                                .brand(
                                        brandRepository.findById(savedBrandId)
                                                .orElseThrow()
                                )
                                .build()
                );

                return savedModel.getId();
            });

            assertNotNull(createdModelId);
            firstModelId = createdModelId;

            final Long savedModelId = createdModelId;

            Model userA = transactionTemplate.execute(status ->
                    modelRepository.findById(savedModelId).orElseThrow()
            );

            Model userB = transactionTemplate.execute(status ->
                    modelRepository.findById(savedModelId).orElseThrow()
            );

            assertNotNull(userA);
            assertNotNull(userB);
            assertNotSame(userA, userB);

            assertEquals(0L, userA.getVersion());
            assertEquals(0L, userB.getVersion());

            Model updatedUserA = transactionTemplate.execute(status -> {
                userA.setName("FH 542");
                return modelRepository.saveAndFlush(userA);
            });

            assertNotNull(updatedUserA);
            assertEquals(1L, updatedUserA.getVersion());

            userB.setName("FH 543");

            assertThrows(
                    ObjectOptimisticLockingFailureException.class,
                    () -> transactionTemplate.executeWithoutResult(status ->
                            modelRepository.saveAndFlush(userB)
                    )
            );

        } finally {
            if (firstModelId != null) {
                Long modelIdToDelete = firstModelId;

                transactionTemplate.executeWithoutResult(status ->
                        modelRepository.deleteById(modelIdToDelete)
                );
            }

            if (brandId != null) {
                Long brandIdToDelete = brandId;

                transactionTemplate.executeWithoutResult(status ->
                        brandRepository.deleteById(brandIdToDelete)
                );
            }
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectConcurrentCreation_whenModelNameAlreadyExistsGlobally() {
        Long volvoId = null;
        Long scaniaId = null;
        Long firstModelId = null;

        try {
            Long createdVolvoId = transactionTemplate.execute(status -> {
                Brand brand = brandRepository.saveAndFlush(
                        Brand.builder().name("Volvo").build()
                );

                return brand.getId();
            });

            assertNotNull(createdVolvoId);
            volvoId = createdVolvoId;

            Long createdScaniaId = transactionTemplate.execute(status -> {
                Brand brand = brandRepository.saveAndFlush(
                        Brand.builder().name("Scania").build()
                );

                return brand.getId();
            });

            assertNotNull(createdScaniaId);
            scaniaId = createdScaniaId;

            final Long savedVolvoId = createdVolvoId;
            final Long savedScaniaId = createdScaniaId;

            Long createdModelId = transactionTemplate.execute(status -> {
                Model model = Model.builder()
                        .name("FH 540")
                        .brand(
                                brandRepository.findById(savedVolvoId)
                                        .orElseThrow()
                        )
                        .build();

                return modelRepository.saveAndFlush(model).getId();
            });

            assertNotNull(createdModelId);
            firstModelId = createdModelId;

            Model secondModel = Model.builder()
                    .name("fh 540")
                    .brand(
                            brandRepository.findById(savedScaniaId)
                                    .orElseThrow()
                    )
                    .build();

            DataIntegrityViolationException exception = assertThrows(
                    DataIntegrityViolationException.class,
                    () -> transactionTemplate.execute(status ->
                            modelRepository.saveAndFlush(secondModel)
                    )
            );

            ConstraintViolationException cause =
                    assertInstanceOf(
                            ConstraintViolationException.class,
                            exception.getCause()
                    );

            assertEquals("uk_model_name_lower", cause.getConstraintName());

        } finally {
            if (firstModelId != null) {
                Long modelIdToDelete = firstModelId;

                transactionTemplate.executeWithoutResult(status ->
                        modelRepository.deleteById(modelIdToDelete)
                );
            }

            if (volvoId != null) {
                Long brandIdToDelete = volvoId;

                transactionTemplate.executeWithoutResult(status ->
                        brandRepository.deleteById(brandIdToDelete)
                );
            }

            if (scaniaId != null) {
                Long brandIdToDelete = scaniaId;

                transactionTemplate.executeWithoutResult(status ->
                        brandRepository.deleteById(brandIdToDelete)
                );
            }
        }
    }

    @Test
    void shouldPersistAndDeactivateModel_WhenBrandExists() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        savedModel.setActive(false);
        modelRepository.saveAndFlush(savedModel);

        Model inactiveModel = modelRepository.findById(savedModel.getId())
                .orElseThrow();

        assertNotNull(inactiveModel);
        assertFalse(inactiveModel.isActive());
        assertEquals(brand.getId(), inactiveModel.getBrand().getId());
    }

    @Test
    void shouldPopulateCreatedAtAndUpdatedAt_OnCreation() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        assertNotNull(savedModel.getCreatedAt());
        assertNotNull(savedModel.getUpdatedAt());
        assertEquals(savedModel.getCreatedAt(), savedModel.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAt_OnRealUpdate() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        LocalDateTime originalCreatedAt = savedModel.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedModel.getUpdatedAt();

        savedModel.setUpdatedAt(originalUpdatedAt.minusSeconds(1));
        savedModel.setName("FH 542");

        Model updatedModel = modelRepository.saveAndFlush(savedModel);

        assertEquals(originalCreatedAt, updatedModel.getCreatedAt());
        assertNotEquals(originalUpdatedAt, updatedModel.getUpdatedAt());
        assertTrue(updatedModel.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void shouldNotChangeTimestampsOrVersion_WhenUpdatingWithSameName() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Volvo").build()
        );

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        LocalDateTime originalCreatedAt = savedModel.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedModel.getUpdatedAt();
        Long originalVersion = savedModel.getVersion();
        Long modelId = savedModel.getId();

        modelService.update(
                modelId,
                new UpdateModelRequest("FH 540")
        );

        entityManager.flush();
        entityManager.clear();

        Model reloadedModel = modelRepository.findById(modelId).orElseThrow();

        assertEquals(originalCreatedAt, reloadedModel.getCreatedAt());
        assertEquals(originalUpdatedAt, reloadedModel.getUpdatedAt());
        assertEquals(originalVersion, reloadedModel.getVersion());
        assertEquals("FH 540", reloadedModel.getName());
    }
}