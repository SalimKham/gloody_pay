package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.PayslipService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> print(@RequestBody PayslipSettingDto dto) {
        String result = payslipService.print(dto);
        return ResponseEntity.ok().body(result);
    }


    @PostMapping("/calculate")
    public ResponseEntity<Set<PayslipElementDto>> calculate(
            @RequestBody PayslipSettingDto dto
    ) {
        TreeSet<PayslipElementDto> result = payslipService.calculate(dto, Collections.singletonList(dto.getEmployee().getId()));
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/close")
    public ResponseEntity<List<Long>> calculate(
            @RequestBody List<PayslipSettingDto> dtos
    ) {
        return ResponseEntity.ok().body( payslipService.closePayslips(dtos));
    }
}
