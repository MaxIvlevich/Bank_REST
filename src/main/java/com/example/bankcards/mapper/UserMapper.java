package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    UserResponseDto toUserResponseDto(User user);

}
