package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class Form301BISPayloadDTO {
    private int year;
    private Set<Long> employeeIds;
}
