package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandResponse create(CreateBrandRequest request) {

        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Brand already exists");
        }

        Brand brand = Brand.builder()
                .name(request.name())
                .build();

        Brand savedBrand = brandRepository.save(brand);
        return new BrandResponse(
                savedBrand.getId(),
                savedBrand.getName(),
                savedBrand.isActive()
        );
    }
}
