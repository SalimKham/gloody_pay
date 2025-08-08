package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public  class PayrollElementItem implements Serializable{
         private String month;
    private BigDecimal brut = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
    private BigDecimal ret_ss  = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
    private BigDecimal brut_impos  = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
    private BigDecimal irg  =new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
    private BigDecimal irg10  = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
    private BigDecimal net_payer =  new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
    }


