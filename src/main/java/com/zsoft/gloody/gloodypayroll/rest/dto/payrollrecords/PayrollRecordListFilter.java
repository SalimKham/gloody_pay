package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;

import java.util.List;

public record PayrollRecordListFilter(
        List<String> statuses,
        List<String> types) {
}