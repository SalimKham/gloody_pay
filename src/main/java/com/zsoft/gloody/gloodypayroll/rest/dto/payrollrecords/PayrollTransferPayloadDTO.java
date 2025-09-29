package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;


import com.zsoft.gloody.gloodypayroll.models.enums.PayTransferType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public final class PayrollTransferPayloadDTO {
    private LocalDate month;
    private Set<Long> employeeIds;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PayTransferType type = PayTransferType.BANK;
}
