package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingExportFilterDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.services.AccountingSettingService;
import com.zsoft.gloody.gloodypayroll.services.payrollrecord.PayrollRecordsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounting-setting")
public class AccountingSettingController {

    private final AccountingSettingService accountingSettingService;
    @GetMapping
    public ResponseEntity<AccountingSettingDTO> getOne() {
        Optional<AccountingSettingDTO> accountingSettingDTO = accountingSettingService.findOne();
        return accountingSettingDTO.map(response -> ResponseEntity.ok().body(response)).orElse(null);
    }

    @PostMapping("/monthly-logs")
    public ResponseEntity<AccountingSettingDTO> getAccountingMonthlyTotals( @RequestBody  AccountingExportFilterDto filter) {
        List<AccountingSettingDTO> result = accountingSettingService.getAccountingMonthlyTotals(filter);
        return ResponseEntity.ok().body(result != null && !result.isEmpty() ? result.get(0) : null);
    }

    @PostMapping("/monthly-logs/export")
    public ResponseEntity<byte[]> exportAccountingData(@RequestBody  AccountingExportFilterDto filter) {
        byte[] result  = accountingSettingService.exportAccountingData(filter);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" +  filter.getFileName() + "\"");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @GetMapping("/monthly-logs/export-plan")
    public ResponseEntity<byte[]> exportAccountingSetting() {
        String fileName = String.format("Accounting-Plan-%s", LocalDate.now().format(PayrollRecordsUtils.hyphenDateFormatter));
        byte[] result  = accountingSettingService.exportAccountingSetting(fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" +  fileName + "\"");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountingSettingService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<AccountingSettingDTO> save(@Valid @RequestBody AccountingSettingDTO accountingSettingDTO) {
            AccountingSettingDTO result = accountingSettingService.save(accountingSettingDTO);
            return ResponseEntity.ok().body(result);
    }
}
