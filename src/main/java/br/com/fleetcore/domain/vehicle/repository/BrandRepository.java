package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Brand> findAllByActive(boolean active, Pageable pageable);
}