package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.models.enums.PayrollRecordType;

import java.time.LocalDate;
import java.util.Set;

public record PayrollRecordFilter(PayrollRecordType type, LocalDate date, Set<Long> employeeIds, String affiliateName, String affiliateAddress) {
}
