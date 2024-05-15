package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.enums.AbsenceType;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipAbsenceDto {
     private double absenceDays;
     private double cpDays;
}
