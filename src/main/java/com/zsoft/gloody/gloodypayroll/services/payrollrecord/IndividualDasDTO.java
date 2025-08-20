package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public  class IndividualDasDTO {
        private String memberShip;
        private String affiliate;
        private String service;
        private String companyAddress;
        private int year;
        private String declarationType;
        private String socialSecurity;
        private String birthDate;
        private String range;
        private String fullName;
        private String wd1;
        private String wd2;
        private BigDecimal gross1;
        private BigDecimal gross2;
        private String wd3;
        private String wd4;
        private BigDecimal gross3;
        private BigDecimal gross4;
        private BigDecimal grossYear;
    }