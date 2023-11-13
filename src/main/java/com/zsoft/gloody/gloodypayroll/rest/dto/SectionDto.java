package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SectionDto {
    @JsonProperty(value = "code")
    private String code;
    @JsonProperty(value = "label")
    private String label;
    @JsonProperty(value = "description")
    private String description;
    @JsonProperty(value = "type")
    private String type;
    @JsonProperty(value = "base_formula")
    private String baseFormula;
    @JsonProperty(value = "rate_percent")
    private boolean ratePercent;
    @JsonProperty(value = "rate_formula")
    private String rateFormula;
    @JsonProperty(value = "value_formula")
    private String valueFormula;
    @JsonProperty(value = "category")
    private String category;
}
