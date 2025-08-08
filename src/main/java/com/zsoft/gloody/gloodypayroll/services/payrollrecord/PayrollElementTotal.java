package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public  class PayrollElementTotal implements Serializable {
        private BigDecimal brut = new BigDecimal(0);
        private BigDecimal ret_ss = new BigDecimal(0);
        private BigDecimal brut_impos = new BigDecimal(0);
        private  BigDecimal irg = new BigDecimal(0);
        private BigDecimal irg10  = new BigDecimal(0);
        private BigDecimal net_payer = new BigDecimal(0);
    }