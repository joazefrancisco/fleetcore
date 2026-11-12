package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Model> findAllByActive(boolean active, Pageable pageable);

    Page<Model> findByBrandIdAndActive(Long brandId, boolean active, Pageable pageable);

    Page<Model> findByBrandId(Long brandId, Pageable pageable);
}
