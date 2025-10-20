package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.services.MonthlyPayInitializer;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@RequestMapping("/api/variables_initializer")
public class MonthVariableInitializer {
    private final MonthlyPayInitializer service;

    @GetMapping("/{company_id}/{month}/{active_month}")
    public ResponseEntity<Void> initialize(@PathVariable(name = "company_id") Long companyId,
                                                               @PathVariable(name = "month") LocalDate month,
                                                               @PathVariable(name = "active_month") LocalDate activeMonth
                                                               ) {
      service.initializeMonthlyPay(companyId, month, activeMonth);
        return ResponseEntity.ok().build();
    }


}
