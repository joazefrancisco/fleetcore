package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Brand;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BrandIntegrationTest {

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void shouldPersistsBrandInPostgres() {

        Brand brand = Brand.builder()
                .name("Ferrari")
                .build();

        Brand brandSaved = brandRepository.saveAndFlush(brand);

        assertNotNull(brandSaved);
        assertNotNull(brand.getId());
        assertEquals(brand.getName(), brandSaved.getName());
        assertEquals(brand.isActive(), brandSaved.isActive());

        Optional<Brand> brandFound = brandRepository.findById(brand.getId());

        assertNotNull(brandFound);
        assertTrue(brandFound.isPresent());
        assertEquals(brand.getName(), brandFound.get().getName());
        assertEquals(brand.isActive(), brandFound.get().isActive());
    }

    @Test
    void shouldRejectDuplicateBrandNameIgnoreCase(){
        Brand firstBrand = Brand.builder()
                .name("Lexus")
                .build();

        Brand firstBrandSaved = brandRepository.saveAndFlush(firstBrand);

        assertNotNull(firstBrandSaved);
        assertNotNull(firstBrandSaved.getId());
        assertEquals(firstBrand.getName(), firstBrandSaved.getName());
        assertEquals(firstBrand.isActive(), firstBrandSaved.isActive());

        Brand secondBrand = Brand.builder()
                .name("LeXuS")
                .build();

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> brandRepository.saveAndFlush(secondBrand));

        ConstraintViolationException exceptionCause = (ConstraintViolationException) exception.getCause();

        assertNotNull(exception.getCause());
        assertInstanceOf(ConstraintViolationException.class, exceptionCause);
        assertEquals("uk_brand_name_lower", exceptionCause.getConstraintName());
    }

    @Test
    void shouldRejectConcurrentUpdate_whenBrandVersionIsOutdated() {
        Brand brand = Brand.builder()
                .name("BYD")
                .build();

        Brand savedBrand = brandRepository.saveAndFlush(brand);

        Brand firstBrand = brandRepository.findById(savedBrand.getId()).orElseThrow();
        Brand secondBrand = brandRepository.findById(savedBrand.getId()).orElseThrow();

        firstBrand.setName("BYD Motors");
        brandRepository.saveAndFlush(firstBrand);

        secondBrand.setName("BYD Brasil");

        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> brandRepository.saveAndFlush(secondBrand));
    }
}