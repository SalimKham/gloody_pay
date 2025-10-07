package com.zsoft.gloody.gloodypayroll.rest.dto.section;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionVariableDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.ArrayList;
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
    private Integer identifier;
    private String label;
    private String description;
    @Enumerated(EnumType.STRING)
    private SectionType type;
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
    @Builder.Default
    private List<SectionVariableDto> variables = new ArrayList<>();

    public boolean isSalary() {
        return type != null && type.equals(SectionType.SALARY);
    }

    public boolean isTotal() {
        return type != null && type.equals(SectionType.TOTAL);
    }

    public boolean isIntermediate() {
        return type != null && type.equals(SectionType.INTERMEDIATE);
    }

    public boolean isGain() {
        return type != null && type.equals(SectionType.GAIN);
    }

    public boolean isDeduction() {
        return type != null && type.equals(SectionType.DEDUCTION);
    }
}
