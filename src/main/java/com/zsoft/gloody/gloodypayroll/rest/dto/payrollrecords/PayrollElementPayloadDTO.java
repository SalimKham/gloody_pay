package com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public final class PayrollElementPayloadDTO {
    private Long id;
    private String identification;
    private String lastName;
    private String firstName;
    private String jobTitle;
    private String socialSecurity;
    private LocalDate startMonth;
    private LocalDate endMonth;
    private int year;
    private String birthDate;
    private String hireDate;
    private String gender;
    private String maritalStatus;
    public int nbrOfChildren;
    private String contractNature;

}
