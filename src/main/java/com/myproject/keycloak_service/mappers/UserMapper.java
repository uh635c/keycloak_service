package com.myproject.keycloak_service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.uh635c.dto.IndividualRequestDTO;
import ru.uh635c.dto.UserRegistrationDTO;
import ru.uh635c.entity.Status;

@Mapper(componentModel = "spring", imports = Status.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretKey", ignore = true)
    IndividualRequestDTO map(UserRegistrationDTO userRegistrationDTO);
}
