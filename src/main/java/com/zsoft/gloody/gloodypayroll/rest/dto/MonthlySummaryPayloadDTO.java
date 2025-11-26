package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MonthlySummaryPayloadDTO {
    LocalDate month;
    Set<Long> employees;
    Set<String> labels;
}
