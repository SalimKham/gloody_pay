package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Address;
import com.zsoft.gloody.gloodypayroll.rest.dto.AddressDto;
import org.mapstruct.Mapper;

@Mapper
public interface AddressMapper {
    AddressDto toDto(Address address);

    Address toEntity(AddressDto addressDto);

    default Long toId(Address address) {
        return (address == null) ? null : address.getId();
    }

    default Address fromId(Long id) {
        return (id == null) ? null : Address.builder().id(id).build();
    }
}


