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

    ModelResponse toResponse(Model model);

    ModelSummary toSummary(Model model);

    ModelDetails toDetails(Model model);
}
