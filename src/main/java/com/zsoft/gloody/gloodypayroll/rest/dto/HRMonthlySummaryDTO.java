package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.models.enums.SummaryDataType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

public record HRMonthlySummaryDTO(Long id, Long resourceId, Long contractId, @Enumerated(EnumType.STRING)
SummaryDataType type, LocalDate month, String variable, double value, boolean empty) {
}
