package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.CompanyDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.services.CompanyService;
import com.zsoft.gloody.gloodypayroll.services.HRMonthlyDataService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService service;

    @PostMapping
    public ResponseEntity<Void> saveCompany(@RequestBody CompanyDto payload) {
        service.save(payload);
        return ResponseEntity.ok().build();
    }

}
