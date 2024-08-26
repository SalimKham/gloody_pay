package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.services.AccountingSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/monthly-logs")
    public ResponseEntity<AccountingSettingDTO> getAccountingMonthlyTotals( @RequestParam(name = "month") LocalDate date) {
        Optional<AccountingSettingDTO> accountingSettingDTO = accountingSettingService.getAccountingMonthlyTotals(date);
        return accountingSettingDTO.map(response -> ResponseEntity.ok().body(response)).orElse(null);
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
