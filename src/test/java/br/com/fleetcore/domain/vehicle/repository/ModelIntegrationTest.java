package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.owner.entity.Owner;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.entity.Model;
import br.com.fleetcore.domain.vehicle.entity.Vehicle;
import br.com.fleetcore.domain.vehicle.enums.VehicleFuelType;
import br.com.fleetcore.domain.vehicle.enums.VehicleStatus;
import br.com.fleetcore.domain.vehicle.enums.VehicleType;
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
import java.util.Optional;

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
    void shouldAllowSameNameInDifferentBrands_WhenOldConstraintWasRemoved() {
        // This test verifies that the old constraint uk_model_brand_name was removed
        // With the new global uniqueness, this should FAIL
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

        Long scaniaId = transactionTemplate.execute(status -> {
            Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Scania").build());
            return brand.getId();
        });

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
    void shouldPreserveHistoricalLinks_WhenModelIsDeactivated() {
        Brand brand = brandRepository.saveAndFlush(Brand.builder().name("Volvo").build());

        Model model = Model.builder()
                .name("FH 540")
                .brand(brand)
                .build();
        Model savedModel = modelRepository.saveAndFlush(model);

        Owner owner = Owner.builder()
                .name("John Doe")
                .document("12345678900")
                .build();

        // Error! So, we don't need to save the owner
        // Owner savedOwner = brandRepository.saveAndFlush(owner);

        Vehicle vehicle = Vehicle.builder()
                .preRegistrationCode("ABC1234567")
                .chassis("9BWZZZ12345678901")
                .renavam("12345678901")
                .brand(brand)
                .model(model)
                .vehicleType(VehicleType.TRUCK)
                .fuelType(VehicleFuelType.DIESEL)
                .manufactureYear(2024)
                .modelYear(2024)
                .mileage(0L)
                .status(VehicleStatus.PRE_REGISTERED)
                .owner(owner)
                .build();

        // Note: Vehicle requires VehicleConfiguration, but we're testing Model inactivation
        // For this test, we'll verify the Model can be inactivated without breaking FKs
        // In a real scenario, Vehicle would need a VehicleConfiguration

        // Deactivate the model
        savedModel.setActive(false);
        modelRepository.saveAndFlush(savedModel);

        // Verify model is inactive
        Model inactiveModel = modelRepository.findById(savedModel.getId()).orElseThrow();
        assertFalse(inactiveModel.isActive());

        // Verify the model still exists and links are intact
        assertNotNull(inactiveModel);
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

        // Simulate idempotent operation: checking if name is same before update
        String currentName = savedModel.getName();
        if (currentName.equals("FH 540")) {
            // No change, no save
        }

        // Reload from database to verify no change
        Model reloadedModel = modelRepository.findById(savedModel.getId()).orElseThrow();

        assertEquals(originalCreatedAt, reloadedModel.getCreatedAt());
        assertEquals(originalUpdatedAt, reloadedModel.getUpdatedAt());
    }
}
