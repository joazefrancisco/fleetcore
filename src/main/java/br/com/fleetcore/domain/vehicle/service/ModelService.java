package br.com.fleetcore.domain.vehicle.service;

import br.com.fleetcore.domain.vehicle.dto.CreateModelRequest;
import br.com.fleetcore.domain.vehicle.dto.ModelDetails;
import br.com.fleetcore.domain.vehicle.dto.ModelResponse;
import br.com.fleetcore.domain.vehicle.dto.ModelSummary;
import br.com.fleetcore.domain.vehicle.dto.UpdateModelRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.entity.Model;
import br.com.fleetcore.domain.vehicle.enums.ModelStatusFilter;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import br.com.fleetcore.domain.vehicle.exception.ModelAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.ModelInactiveException;
import br.com.fleetcore.domain.vehicle.exception.ModelNotFoundException;
import br.com.fleetcore.domain.vehicle.mapper.ModelMapper;
import br.com.fleetcore.domain.vehicle.repository.BrandRepository;
import br.com.fleetcore.domain.vehicle.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final BrandRepository brandRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ModelResponse create(CreateModelRequest request) {
        Brand brand = findBrandByIdOrThrow(request.brandId());

        if (!brand.isActive()) {
            throw new BrandInactiveException("Brand inactive");
        }

        String normalizedName = request.name().trim();

        if (modelRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ModelAlreadyExistsException("Model already exists");
        }

        Model model = modelMapper.toEntity(request);
        model.setName(normalizedName);
        model.setBrand(brand);

        Model savedModel = modelRepository.save(model);

        return modelMapper.toResponse(savedModel);
    }

    @Transactional(readOnly = true)
    public Page<ModelSummary> findAll(ModelStatusFilter status, Long brandId, Pageable pageable) {
        ModelStatusFilter filter = status == null
                ? ModelStatusFilter.ACTIVE
                : status;

        Page<Model> models;

        if (brandId != null) {
            models = switch (filter) {
                case ACTIVE -> modelRepository.findByBrandIdAndActive(brandId, true, pageable);
                case INACTIVE -> modelRepository.findByBrandIdAndActive(brandId, false, pageable);
                case ALL -> modelRepository.findByBrandId(brandId, pageable);
            };
        } else {
            models = switch (filter) {
                case ACTIVE -> modelRepository.findAllByActive(true, pageable);
                case INACTIVE -> modelRepository.findAllByActive(false, pageable);
                case ALL -> modelRepository.findAll(pageable);
            };
        }

        return models.map(modelMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public ModelDetails findById(Long id) {
        Model model = findByIdOrThrow(id);
        return modelMapper.toDetails(model);
    }

    @Transactional
    public ModelDetails update(Long id, UpdateModelRequest request) {
        Model model = findByIdOrThrow(id);

        if (!model.isActive()) {
            throw new ModelInactiveException("Model inactive");
        }

        String normalizedName = request.name().trim();
        String currentName = model.getName().trim();

        if (normalizedName.equals(currentName)) {
            return modelMapper.toDetails(model);
        }

        if (modelRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new ModelAlreadyExistsException("Model already exists");
        }

        model.setName(normalizedName);

        // saveAndFlush is necessary for @PreUpdate to populate updatedAt
        // before mapping to the response DTO
        Model savedModel = modelRepository.saveAndFlush(model);

        return modelMapper.toDetails(savedModel);
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        Model model = findByIdOrThrow(id);

        if (model.isActive() == active) {
            return;
        }

        model.setActive(active);
    }

    protected Model findByIdOrThrow(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Model not found"));
    }

    protected Brand findBrandByIdOrThrow(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found"));
    }
}
