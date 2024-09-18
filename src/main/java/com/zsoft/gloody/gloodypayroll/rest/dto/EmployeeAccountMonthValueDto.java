package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
public class EmployeeAccountMonthValueDto {
    private Long account;
    private String employee;
    private Double value;
    public EmployeeAccountMonthValueDto(Long account, PayslipDetailDto payslipDetail, Double value ) {
        this.account = account;
        this.value =     BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (payslipDetail == null) {
            employee = "NOT_FOUND";
        } else {
            employee = payslipDetail.getFullName();
        }
    }
}
