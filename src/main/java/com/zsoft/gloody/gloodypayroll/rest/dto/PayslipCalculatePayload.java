package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PayslipCalculatePayload {
    private Long resourceId;
    private Long contractId;
    LocalDate month;
    private double holidaysCount;
    private PayslipSettingDto tempTemplate;
    private boolean reCalculate;
    private BackPaymentDto backPayment;
    private boolean adjustBackPayments;



    public void setValues(Long resourceId, Long contractId, LocalDate month) {
        this.resourceId = resourceId;
        this.contractId = contractId;
        this.month = month;
    }
}