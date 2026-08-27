package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
}
