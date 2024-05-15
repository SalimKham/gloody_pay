package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddressDto {


    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "address_line1")
    private String addressLine1;
    @JsonProperty(value = "address_line2")
    private String addressLine2;
    @JsonProperty(value = "city")
    private String city;
    @JsonProperty(value = "state")
    private String state;
    @JsonProperty(value = "postal_code")
    private String postalCode;
    @JsonProperty(value = "country_code")
    private String countryCode;
}
