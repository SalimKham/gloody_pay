package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        private Double wd1;
        private Double wd2;
        private Double gross1;
        private Double gross2;
        private Double wd3;
        private Double wd4;
        private Double gross3;
        private Double gross4;
        private Double grossYear;
    }