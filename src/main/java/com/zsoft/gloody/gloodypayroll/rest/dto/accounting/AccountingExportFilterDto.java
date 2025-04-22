package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import lombok.*;

import java.time.LocalDate;

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
}
