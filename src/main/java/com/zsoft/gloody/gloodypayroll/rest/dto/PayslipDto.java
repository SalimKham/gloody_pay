package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipDto {
    @JsonProperty(value = "elements")
    private List<PayslipElementDto> elements;
}
