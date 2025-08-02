package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.masking.CardMaskingUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper interface for converting between Card entity and its DTOs.
 * Uses MapStruct for implementation generation.
 */
@Mapper(componentModel = "spring")
public interface CardMapper {
    /**
     * Converts a Card entity to a CardResponse DTO.
     *
     * @param card The Card entity.
     * @return The corresponding CardResponse DTO.
     */
    @Mapping(source = "cardNumber", target = "maskedCardNumber", qualifiedByName = "maskCardNumber")
    CardResponse toCardResponse(Card card);

    /**
     * A custom mapping method to be used by MapStruct for masking the card number.
     * It uses the CardMaskingUtil to perform the masking.
     *
     * @param cardNumber The full card number.
     * @return The masked card number.
     */
    @Named("maskCardNumber")
    default String maskCardNumber(String cardNumber) {
        return CardMaskingUtil.maskCardNumber(cardNumber);
    }
}
