package com.zsoft.gloody.gloodypayroll.rest.controllers;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.services.SectionsConfigService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/payslip-config")
public class PayslipConfigController {
    private final SectionsConfigService sectionsConfigService;

    @GetMapping("/{companyId}")
    public ResponseEntity<List<SectionCategoryDto>> findByCompanyId(@PathVariable("companyId") Long companyId) {
        return sectionsConfigService.getByCompany(companyId)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/init/{companyId}")
    public ResponseEntity<Void> initDefaultConfiguration(@PathVariable("companyId") Long companyId) {
        sectionsConfigService.initCompanyDefaultConfiguration(Collections.singletonList(companyId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/init")
    public ResponseEntity<Void> initDefaultConfiguration(@RequestBody List<Long> companies) {
        sectionsConfigService.initCompanyDefaultConfiguration(companies);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/categories")
    public ResponseEntity<List<SectionCategoryDto>> saveCategory(@RequestBody SectionCategoryDto category) {
        return sectionsConfigService.saveCategory(category)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<List<SectionCategoryDto>> saveCategory(@PathVariable(name = "id") Long id) {
        return sectionsConfigService.deleteCategory(id)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }


}
