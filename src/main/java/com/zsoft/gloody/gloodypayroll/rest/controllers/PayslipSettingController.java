package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.PayslipSettingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/payslip-setting")
public class PayslipSettingController {
    private final PayslipSettingService payslipSettingService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<PayslipSettingDto> findById(@PathVariable("employeeId") Long employeeId) {
        return payslipSettingService.findByEmployeeId(employeeId)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<PayslipSettingDto> findAll(
            @RequestBody PayslipSettingDto dto
    ) {
        PayslipSettingDto result = payslipSettingService.update(dto);
        return ResponseEntity.ok().body(result);
    }
}
