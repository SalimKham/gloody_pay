package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Getter
@Setter
public class EmployeeAccountMonthValueDto {
    private Long account;
    private Boolean detailedByEmployee;
    private String employee;
    private Double value;
    public EmployeeAccountMonthValueDto(Long account, PayslipDetailDto payslipDetail, Double value, Boolean detailedByEmployee ) {
        this.account = account;
        this.detailedByEmployee = Objects.requireNonNullElse(detailedByEmployee, false);
        this.value =     BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (payslipDetail == null) {
            employee = "NOT_FOUND";
        } else {
            employee = payslipDetail.getFullName();
        }
    }
}
