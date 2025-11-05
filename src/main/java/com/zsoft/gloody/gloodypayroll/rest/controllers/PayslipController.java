package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.*;
import com.zsoft.gloody.gloodypayroll.services.PayslipCloseService;
import com.zsoft.gloody.gloodypayroll.services.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payslip")
public class PayslipController {
    private final PayslipService payslipService;
    private final PayslipCloseService closeService;
    private final PayslipCloseService payslipCloseService;

    @PostMapping("/print")
    public ResponseEntity<PayslipPrintDTO> print(@RequestBody PayslipCalculatePayload payload) {
        PayslipPrintDTO result = payslipService.print(payload);
        return ResponseEntity.ok().body(result);
    }


    @PostMapping("/calculate")
    public ResponseEntity<CalculatedPayslip> calculate(
            @RequestBody PayslipCalculatePayload payload
    ) {
        try {
            CalculatedPayslip result = payslipService.calculate(payload, null, true);
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



    public record CalculatedPayslip(PayslipSettingDto template, Set<PayslipElementDto> calculatedElements) {
    }

    @PostMapping("/close/{month}")
    public ResponseEntity<Set<Long>> calculate(
            @RequestBody Set<Long> resourceIds,
            @PathVariable LocalDate month
    ) {
        return ResponseEntity.ok().body( closeService.closePayslips(resourceIds, month ));
    }

    @GetMapping("/re_open/{month}")
    public ResponseEntity<Void> reOpenPayslip(
            @PathVariable LocalDate month,
            @RequestParam(name = "resource_id") Long resourceId,
            @RequestParam(name = "contract_id") Long contractId,
            @RequestParam(name = "template_id", required = false) Long templateId
    ) {
        closeService.reOpenPayslip( month, resourceId, contractId, templateId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/close/fetch_closed/{month}")
    public ResponseEntity<Set<Long>> getCloseEmployeeIds(
            @PathVariable LocalDate month
    ) {
        return ResponseEntity.ok().body(payslipService.getClosedPayslips(month));
    }

    @GetMapping("/close/fetch_closed_result/{month}")
    public ResponseEntity<PayslipCloseResultDto> getPayslipCloseResults(
            @PathVariable LocalDate month
    ) {
        return ResponseEntity.ok().body(payslipCloseService.getPayslipCloseResult(month));
    }
}
