package com.zsoft.gloody.gloodypayroll.rest.dto;

import java.time.LocalDate;

public record HRMonthlySummaryDTO(Long id, Long resourceId, Long contractId, LocalDate month, String variable, double value) {
}
