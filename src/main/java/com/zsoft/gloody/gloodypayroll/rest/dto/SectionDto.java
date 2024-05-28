package com.zsoft.gloody.gloodypayroll.rest.dto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SectionDto {
    private Long id;
    private String code;
    private String label;
    private String description;
    private String type;
    private String baseFormula;
    private boolean ratePercent;
    private String rateFormula;
    private String valueFormula;
    private SectionCategoryDto category;
    private Long company;
}
