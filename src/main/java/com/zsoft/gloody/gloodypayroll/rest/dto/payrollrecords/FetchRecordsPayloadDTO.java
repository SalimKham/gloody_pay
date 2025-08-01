package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class FetchRecordsPayloadDTO {
    private PayrollRecordListFilter filter;
    private int pageSize;
    private int pageNumber;
    private  List<String> sort;
}
