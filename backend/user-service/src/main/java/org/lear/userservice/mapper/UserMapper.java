package org.lear.userservice.mapper;

import org.lear.userservice.dtos.UserDto;
import org.lear.userservice.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;

@Mapper(componentModel = "spring")

public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);


    @Mapping(source = "role.roleName", target = "roleName")
    UserDto userToUserDto(User user);

}
