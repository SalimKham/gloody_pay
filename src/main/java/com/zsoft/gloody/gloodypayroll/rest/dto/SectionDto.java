package com.zsoft.gloody.gloodypayroll.rest.dto;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import lombok.*;

import java.util.Collections;
import java.util.List;

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
    private SectionNature nature = SectionNature.TAXABLE_CONTRIBUTORY;
    private SectionCategoryDto category;
    private List<String> labelKeys = Collections.emptyList();
    private Long company;
    private Boolean referenced;
    private AccountingSettingAccountDTO accounting;
    private List<SectionVariableDto> variables;
}
