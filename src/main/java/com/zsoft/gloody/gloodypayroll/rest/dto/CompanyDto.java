package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.models.entities.Address;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
public class CompanyDto {
    private String name;
    private AddressDto address;
}
