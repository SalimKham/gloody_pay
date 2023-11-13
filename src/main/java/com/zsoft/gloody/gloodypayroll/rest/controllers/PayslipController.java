package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.PayslipService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;

@AllArgsConstructor
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
    public ResponseEntity<LinkedList<PayslipElementDto>> calculate(
            @RequestBody PayslipSettingDto dto
    ) {
        LinkedList<PayslipElementDto> result = payslipService.calculate(dto);
        return ResponseEntity.ok().body(result);
    }
}
