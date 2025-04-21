package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipDetailDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.PayslipService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payslip")
public class PayslipController {
    private final PayslipService payslipService;

    @PostMapping("/print")
    public ResponseEntity<String> print( @RequestBody PayslipCalculatePayload payload) {
        String result = payslipService.print(payload);
        return ResponseEntity.ok().body(result);
    }


    @PostMapping("/calculate")
    public ResponseEntity<CalculatedPayslip> calculate(
            @RequestBody PayslipCalculatePayload payload
    ) {
        try {
            CalculatedPayslip result = payslipService.calculate(payload, null, true, true);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;

    }

    @PostMapping("/payslip_details")
    public ResponseEntity<PayslipDetailDto> getPayslipDetails(
            @RequestBody PayslipCalculatePayload payload
    ) {
        PayslipDetailDto result = payslipService.getPayslipDetails(payload);
        return ResponseEntity.ok().body(result);
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PayslipCalculatePayload{
        private Long resourceId;
        private Long contractId; LocalDate month;
        private double holidaysCount;
        private PayslipSettingDto tempTemplate;
        public void setValues(Long resourceId, Long contractId, LocalDate month) {
            this.resourceId = resourceId;
            this.contractId = contractId;
            this.month = month;
        }
    }

    public record CalculatedPayslip(PayslipSettingDto template, Set<PayslipElementDto> calculatedElements){}

    @PostMapping("/close/{month}")
    public ResponseEntity<List<Long>> calculate(
            @RequestBody Set<Long> resourceIds,
            @PathVariable LocalDate month
    ) {
        return ResponseEntity.ok().body( payslipService.closePayslips(resourceIds, month ));
    }
}
