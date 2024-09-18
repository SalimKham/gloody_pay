package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountingExportFilterDto {
    private String fileName;
    private LocalDate startMonth;
    private LocalDate endMonth;
    private String format;
    private boolean detailedByEmployee;
    private Map<String, List<PayslipSettingDto>> hrData;
}
