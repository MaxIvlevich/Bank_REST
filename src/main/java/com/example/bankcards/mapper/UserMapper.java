package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.UserDetailResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(source = "enabled", target = "isActive")
    UserResponseDto toUserResponseDto(User user);


    /**
     * Converts a User entity to a detailed UserDetailResponse,
     * including profile and card information.
     */
    @Mapping(source = "userProfile", target = "profile")
    @Mapping(source = "enabled", target = "isActive")
    UserDetailResponse toUserDetailResponse(User user);

    @Mapping(source = "user.userProfile", target = "profile")
    @Mapping(source = "user.enabled", target = "isActive")
    @Mapping(source = "cards", target = "cards")
    UserDetailResponse toUserDetailResponse(User user, List<Card> cards);

}
