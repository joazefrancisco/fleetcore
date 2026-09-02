package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.BrandDetails;
import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateBrandRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.exception.BrandAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import br.com.fleetcore.domain.vehicle.mapper.BrandMapper;
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
    private final BrandMapper brandMapper;

    @Transactional()
    public BrandResponse create(CreateBrandRequest request) {

        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw new BrandAlreadyExistsException("Brand already exists");
        }

        Brand brand = brandMapper.toEntity(request);

        Brand savedBrand = brandRepository.save(brand);

        return brandMapper.toResponse(savedBrand);
    }

    @Transactional(readOnly = true)
    public Page<BrandSummary> findAll(Boolean active, Pageable pageable) {

        Page<Brand> brands;

        if (active == null) {
            brands = brandRepository.findAll(pageable);
        } else {
            brands = brandRepository.findAllByActive(active, pageable);
        }

        return brands.map(brandMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public BrandDetails findById(Long id) {

        Brand brand = this.findByIdOrThrow(id);

        return brandMapper.toDetails(brand);
    }

    @Transactional
    public BrandDetails update(Long id, UpdateBrandRequest request) {

        Brand brand = this.findByIdOrThrow(id);

        if (!brand.isActive()) {
            throw new BrandInactiveException("Brand inactive");
        }

        if (brand.getName().equals(request.name())) {
            return brandMapper.toDetails(brand);
        }

        if (brandRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BrandAlreadyExistsException("Brand already exists");
        }

        brand.setName(request.name());

        Brand savedBrand = brandRepository.save(brand);

        return brandMapper.toDetails(savedBrand);
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {

        Brand brand = this.findByIdOrThrow(id);

        if (brand.isActive() == active){
            return;
        }

        brand.setActive(active);
        brandRepository.save(brand);
    }

    protected Brand findByIdOrThrow(Long id){
        return brandRepository.findById(id)
                .orElseThrow(() ->
                        new BrandNotFoundException("Brand not found")
                );
    }
}