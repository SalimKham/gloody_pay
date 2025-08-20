package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class LegalLedgerPayloadDTO {
    private LocalDate month;
    Set<Long> employeeIds;
}
