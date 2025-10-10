package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.models.enums.SummaryDataType;

import java.time.LocalDate;

public record HRMonthlySummaryDTO(Long id, Long resourceId, Long contractId,
SummaryDataType type, LocalDate month, String variable, double value, boolean empty, String label) {
}
