package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.BrandDetails;
import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateBrandRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional()
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

    @Transactional(readOnly = true)
    public Page<BrandSummary> findAll(Boolean active, Pageable pageable) {

        Page<Brand> brands;

        if (active == null) {
            brands = brandRepository.findAll(pageable);
        } else {
            brands = brandRepository.findAllByActive(active, pageable);
        }

        return brands.map(brand ->
                new BrandSummary(
                        brand.getId(),
                        brand.getName()
                )
        );
    }

    @Transactional(readOnly = true)
    public BrandDetails findById(Long id) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Brand not found")
                );

        return new BrandDetails(
                brand.getId(),
                brand.getName(),
                brand.isActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }

    public BrandDetails update(Long id, UpdateBrandRequest request) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Brand not found")
                );

        if (!brand.getName().equalsIgnoreCase(request.name())
                && brandRepository.existsByNameIgnoreCase(request.name())) {

            throw new IllegalArgumentException("Brand already exists");
        }

        brand.setName(request.name());

        Brand updatedBrand = brandRepository.save(brand);

        return new BrandDetails(
                updatedBrand.getId(),
                updatedBrand.getName(),
                updatedBrand.isActive(),
                updatedBrand.getCreatedAt(),
                updatedBrand.getUpdatedAt()
        );
    }

    public void updateStatus(Long id, boolean active) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Brand not found")
                );

        brand.setActive(active);

        brandRepository.save(brand);
    }
}