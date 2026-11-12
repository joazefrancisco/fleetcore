package br.com.fleetcore.domain.vehicle.mapper;

import br.com.fleetcore.domain.vehicle.dto.ModelDetails;
import br.com.fleetcore.domain.vehicle.dto.ModelResponse;
import br.com.fleetcore.domain.vehicle.dto.ModelSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateModelRequest;
import br.com.fleetcore.domain.vehicle.entity.Model;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "brand", ignore = true)
    Model toEntity(CreateModelRequest request);

    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    ModelResponse toResponse(Model model);

    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    ModelSummary toSummary(Model model);

    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    ModelDetails toDetails(Model model);
}
