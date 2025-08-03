package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserProfileDto;
import com.example.bankcards.dto.request.UpdateProfileRequest;
import com.example.bankcards.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface  UserProfileMapper {
    UserProfileDto toUserProfileDto(UserProfile userProfile);


    /**
     * Updates an existing UserProfile entity from an UpdateProfileRequest DTO.
     * Null fields in the DTO will be ignored, allowing for partial updates.
     *
     * @param request The DTO with new data.
     * @param profile The entity to be updated.
     */
    void updateProfileFromDto(UpdateProfileRequest request, @MappingTarget UserProfile profile);
}

