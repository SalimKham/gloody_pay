package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class PayslipCalculatePayload {
    private Long resourceId;
    private Long contractId;
    LocalDate month;
    private double holidaysCount;
    private PayslipSettingDto tempTemplate;

    public PayslipCalculatePayload(Long resourceId, Long contractId, LocalDate month, double holidaysCount, PayslipSettingDto tempTemplate) {
        this.resourceId = resourceId;
        this.contractId = contractId;
        this.month = month;
        this.holidaysCount = holidaysCount;
        this.tempTemplate = tempTemplate;
    }

    public PayslipCalculatePayload() {
    }

    public void setValues(Long resourceId, Long contractId, LocalDate month) {
        this.resourceId = resourceId;
        this.contractId = contractId;
        this.month = month;
    }
}