package br.com.fleetcore.domain.vehicle.mapper;

import br.com.fleetcore.domain.vehicle.dto.BrandDetails;
import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    
    Brand toEntity(CreateBrandRequest request);

    BrandResponse toResponse(Brand brand);

    BrandSummary toSummary(Brand brand);

    BrandDetails toDetails(Brand brand);

}
