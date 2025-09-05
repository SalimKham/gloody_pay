package com.zsoft.gloody.gloodypayroll.services.payrollrecord.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public  class DasIndividualPayloadDTO {
        private  int year;
        private Long resourceId;
        private String affiliateAddress;
        private String activitySector;
        private String memberShipNumber;
    }