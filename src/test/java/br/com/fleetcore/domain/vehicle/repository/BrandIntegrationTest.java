package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Brand;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BrandIntegrationTest {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldPersistBrandInPostgres() {
        Brand brandSaved = brandRepository.saveAndFlush(
                Brand.builder().name("Ferrari").build()
        );

        assertNotNull(brandSaved);
        assertNotNull(brandSaved.getId());
        assertEquals("Ferrari", brandSaved.getName());
        assertTrue(brandSaved.isActive());

        Optional<Brand> brandFound = brandRepository.findById(brandSaved.getId());

        assertTrue(brandFound.isPresent());
        assertEquals(brandSaved.getName(), brandFound.get().getName());
        assertEquals(brandSaved.isActive(), brandFound.get().isActive());
    }

    @Test
    void shouldRejectDuplicateBrandNameIgnoreCase() {
        Brand firstBrand = Brand.builder().name("Lexus").build();
        brandRepository.saveAndFlush(firstBrand);

        Brand secondBrand = Brand.builder().name("LeXuS").build();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> brandRepository.saveAndFlush(secondBrand)
        );

        ConstraintViolationException exceptionCause =
                assertInstanceOf(ConstraintViolationException.class, exception.getCause());

        assertEquals("uk_brand_name_lower", exceptionCause.getConstraintName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectConcurrentUpdate_whenBrandVersionIsOutdated() {
        Long firstBrandId = transactionTemplate.execute(status -> {
            Brand savedBrand = brandRepository.saveAndFlush(
                    Brand.builder().name("BYD").build()
            );

            assertNotNull(savedBrand);
            assertNotNull(savedBrand.getId());

            return savedBrand.getId();
        });

        try {
            Brand userA = transactionTemplate.execute(status ->
                    brandRepository.findById(firstBrandId).orElseThrow());

            Brand userB = transactionTemplate.execute(status ->
                    brandRepository.findById(firstBrandId).orElseThrow());

            assertNotNull(userA);
            assertNotNull(userB);
            assertNotSame(userA, userB);

            assertEquals(0L, userA.getVersion());
            assertEquals(0L, userB.getVersion());

            Brand updatedUserA = transactionTemplate.execute(status -> {
                userA.setName("BYD Motors");
                return brandRepository.saveAndFlush(userA);
            });

            assertEquals(1L, updatedUserA.getVersion());

            userB.setName("BYD Brazil");

            assertThrows(
                    ObjectOptimisticLockingFailureException.class,
                    () -> transactionTemplate.executeWithoutResult(status ->
                            brandRepository.saveAndFlush(userB))
            );
        } finally {
            transactionTemplate.executeWithoutResult(status ->
                    brandRepository.deleteById(firstBrandId));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectConcurrentCreation_whenBrandNameAlreadyExists() {
        Brand firstBrand = Brand.builder().name("BYD").build();
        Brand secondBrand = Brand.builder().name("byd").build();

        Long firstBrandId = transactionTemplate.execute(status ->
                brandRepository.saveAndFlush(firstBrand).getId());

        assertNotNull(firstBrandId);

        try {
            DataIntegrityViolationException exception = assertThrows(
                    DataIntegrityViolationException.class,
                    () -> transactionTemplate.execute(status ->
                            brandRepository.saveAndFlush(secondBrand))
            );

            ConstraintViolationException cause =
                    assertInstanceOf(ConstraintViolationException.class, exception.getCause());

            assertEquals("uk_brand_name_lower", cause.getConstraintName());
        } finally {
            transactionTemplate.executeWithoutResult(status ->
                    brandRepository.deleteById(firstBrandId));
        }
    }
}