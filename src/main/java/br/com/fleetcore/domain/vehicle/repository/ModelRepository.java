package br.com.fleetcore.domain.vehicle.repository;

import br.com.fleetcore.domain.vehicle.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
}
