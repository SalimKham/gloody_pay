package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public  class PayrollElementTotal implements Serializable {
        private double brut;
        private double ret_ss;
        private double brut_impos;
        private  double irg;
        private double irg10;
        private double net_payer;
    }