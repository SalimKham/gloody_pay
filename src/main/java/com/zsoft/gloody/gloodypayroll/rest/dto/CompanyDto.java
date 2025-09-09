package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class CompanyDto {
    private Long id;
    private String name;
    private String membershipNumber;
    private String activitySector;
    private AddressDto address;
}
