package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.entity.Model;
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
public class ModelIntegrationTest {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

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
        Brand volvo = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());
        Brand scania = brandRepository.saveAndFlush(Brand.builder().name("Scania").build());

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();
        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("fh 540")  // Same name, different brand, different case
                .brand(scania)
                .build();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> modelRepository.saveAndFlush(secondModel)
        );

        ConstraintViolationException exceptionCause =
                assertInstanceOf(ConstraintViolationException.class, exception.getCause());

        assertEquals("uk_model_name_lower", exceptionCause.getConstraintName());
    }

    @Test
    void shouldRejectDuplicateModelNameWithExternalSpaces_Globally() {
        Brand volvo = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());
        Brand scania = brandRepository.saveAndFlush(Brand.builder().name("Scania").build());

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();
        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("  FH 540  ")  // Same name with external spaces
                .brand(scania)
                .build();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> modelRepository.saveAndFlush(secondModel)
        );

        ConstraintViolationException exceptionCause =
                assertInstanceOf(ConstraintViolationException.class, exception.getCause());

        assertEquals("uk_model_name_lower", exceptionCause.getConstraintName());
    }

    @Test
    void shouldAllowDifferentNamesInSameBrand() {
        Brand volvo = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

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
        // This test verifies that global model-name uniqueness rejects
        // duplicate model names across different brands.
        Brand volvo = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());
        Brand scania = brandRepository.saveAndFlush(Brand.builder().name("Scania").build());

        Model firstModel = Model.builder()
                .name("FH 540")
                .brand(volvo)
                .build();
        modelRepository.saveAndFlush(firstModel);

        Model secondModel = Model.builder()
                .name("FH 540")  // Same name, different brand
                .brand(scania)
                .build();

        // With global uniqueness, this should fail
        assertThrows(
                DataIntegrityViolationException.class,
                () -> modelRepository.saveAndFlush(secondModel)
        );
    }

    @Test
    void shouldPreserveInternalConsecutiveSpaces() {
        Brand volvo = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

        Model model = Model.builder()
                .name("FH  540")  // Two consecutive spaces
                .brand(volvo)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        assertNotNull(savedModel);
        assertEquals("FH  540", savedModel.getName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectConcurrentUpdate_whenModelVersionIsOutdated() {
        Long brandId = transactionTemplate.execute(status -> {
            Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());
            return brand.getId();
        });
        assertNotNull(brandId, "brandId should not be null");

        Long firstModelId = transactionTemplate.execute(status -> {
            Model savedModel = modelRepository.saveAndFlush(
                    Model.builder()
                            .name("FH 540")
                            .brand(brandRepository.findById(brandId).orElseThrow())
                            .build()
            );

            assertNotNull(savedModel);
            assertNotNull(savedModel.getId());

            return savedModel.getId();
        });

        assertNotNull(firstModelId);

        try {
            Model userA = transactionTemplate.execute(status ->
                    modelRepository.findById(firstModelId).orElseThrow());

            Model userB = transactionTemplate.execute(status ->
                    modelRepository.findById(firstModelId).orElseThrow());

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
                            modelRepository.saveAndFlush(userB))
            );
        } finally {
            transactionTemplate.executeWithoutResult(status ->
                    modelRepository.deleteById(firstModelId));
            transactionTemplate.executeWithoutResult(status ->
                    brandRepository.deleteById(brandId));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectConcurrentCreation_whenModelNameAlreadyExistsGlobally() {
        Long volvoId = transactionTemplate.execute(status -> {
            Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());
            return brand.getId();
        });
        assertNotNull(volvoId, "volvoId should not be null");

        Long scaniaId = transactionTemplate.execute(status -> {
            Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Scania").build());
            return brand.getId();
        });
        assertNotNull(scaniaId, "scaniaId should not be null");

        Long firstModelId = transactionTemplate.execute(status -> {
            Model model = Model.builder()
                    .name("FH 540")
                    .brand(brandRepository.findById(volvoId).orElseThrow())
                    .build();
            return modelRepository.saveAndFlush(model).getId();
        });

        assertNotNull(firstModelId);

        try {
            Model secondModel = Model.builder()
                    .name("fh 540")  // Same name, different brand, different case
                    .brand(brandRepository.findById(scaniaId).orElseThrow())
                    .build();

            DataIntegrityViolationException exception = assertThrows(
                    DataIntegrityViolationException.class,
                    () -> transactionTemplate.execute(status ->
                            modelRepository.saveAndFlush(secondModel))
            );

            ConstraintViolationException cause =
                    assertInstanceOf(ConstraintViolationException.class, exception.getCause());

            assertEquals("uk_model_name_lower", cause.getConstraintName());
        } finally {
            transactionTemplate.executeWithoutResult(status ->
                    modelRepository.deleteById(firstModelId));
            transactionTemplate.executeWithoutResult(status ->
                    brandRepository.deleteById(volvoId));
            transactionTemplate.executeWithoutResult(status ->
                    brandRepository.deleteById(scaniaId));
        }
    }

    @Test
    void shouldPersistAndDeactivateModel_WhenBrandExists() {
        // Note: This test validates Model persistence and deactivation.
        // Validation of historical links with Vehicle is pending due to missing
        // OwnerRepository and VehicleConfiguration in the test infrastructure.

        Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();
        Model savedModel = modelRepository.saveAndFlush(model);

        // Deactivate the model
        savedModel.setActive(false);
        modelRepository.saveAndFlush(savedModel);

        // Verify model is inactive
        Model inactiveModel = modelRepository.findById(savedModel.getId()).orElseThrow();
        assertFalse(inactiveModel.isActive());

        // Verify the model still exists and maintains a link to brand
        assertEquals(brand.getId(), inactiveModel.getBrand().getId());
    }

    @Test
    void shouldPopulateCreatedAtAndUpdatedAt_OnCreation() {
        Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);

        assertNotNull(savedModel.getCreatedAt());
        assertNotNull(savedModel.getUpdatedAt());
        assertNotNull(savedModel.getCreatedAt());
        assertEquals(savedModel.getCreatedAt(), savedModel.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAt_OnRealUpdate() {
        Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);
        LocalDateTime originalCreatedAt = savedModel.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedModel.getUpdatedAt();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        savedModel.setName("FH 542");
        Model updatedModel = modelRepository.saveAndFlush(savedModel);

        assertEquals(originalCreatedAt, updatedModel.getCreatedAt());
        assertNotEquals(originalUpdatedAt, updatedModel.getUpdatedAt());
        assertTrue(updatedModel.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void shouldNotUpdateTimestamps_OnIdempotentOperationWithoutPersistence() {
        Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();

        Model savedModel = modelRepository.saveAndFlush(model);
        LocalDateTime originalCreatedAt = savedModel.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedModel.getUpdatedAt();

        // Simulate idempotent operation: no change, no save
        // Reload from database to verify no change
        Model reloadedModel = modelRepository.findById(savedModel.getId()).orElseThrow();

        assertEquals(originalCreatedAt, reloadedModel.getCreatedAt());
        assertEquals(originalUpdatedAt, reloadedModel.getUpdatedAt());
    }
}
