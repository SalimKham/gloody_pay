package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.PayslipSettingService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@RequestMapping("/api/payslip-setting")
public class PayslipSettingController {
    private final PayslipSettingService payslipSettingService;

    @GetMapping("/{id}")
    public ResponseEntity<PayslipSettingDto> findById(@PathVariable("id") Long id, @RequestParam(name = "employee_id", required = false) Long employeeId) {
        return payslipSettingService.findById(id, employeeId)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by_month/{resource_id}")
    public ResponseEntity<PayslipSettingDto> findByResourceAndMonth(@PathVariable("resource_id") Long id, @RequestParam(name = "month") LocalDate date) {
        return payslipSettingService.findByEmployeeIdAndCurrentMonth(id, date)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.ok().body(null));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Page<PayslipSettingDto>> deleteTemplate(@PathVariable("id") Long id, Pageable pageable) {
        Page<PayslipSettingDto> result = payslipSettingService.deleteTemplate(id, pageable);
         return ResponseEntity.ok().body(result);
    }

    @GetMapping("templates/default_template")
    public ResponseEntity<PayslipSettingDto> findDefaultTemplate(@RequestParam(name = "employee_id", required = false) Long employeeId) {
        return payslipSettingService.findDefaultTemplate(employeeId)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("templates/mark_as_default/{id}")
    public ResponseEntity<PayslipSettingDto> markAsDefault(@PathVariable(name = "id") Long templateId) {
        return payslipSettingService.markAsDefault(templateId)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/templates")
    public ResponseEntity<Page<PayslipSettingDto>> findTemplates( @RequestParam(name = "query", required = false) String query,  Pageable pageable) {
        Page<PayslipSettingDto> result =  payslipSettingService.findAllTemplates(query, pageable);
        return  ResponseEntity.ok().body(result);
    }

    @PutMapping
    public ResponseEntity<PayslipSettingDto> update(
            @RequestBody PayslipSettingDto dto
    ) {
        PayslipSettingDto result = payslipSettingService.update(dto);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping
    public ResponseEntity<PayslipSettingDto> create(
            @RequestBody PayslipSettingDto dto
    ) {
        PayslipSettingDto result = payslipSettingService.save(dto);
        return ResponseEntity.ok().body(result);
    }
}
