package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords.PayrollElementPayloadDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public  class PayrollElement implements Serializable {
        private PayrollElementPayloadDTO employee;
        private String logo;
        private int year;
        private String printDate;
        private List<PayrollElementItem> items;
       private PayrollElementTotal total;
    }