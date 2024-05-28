package com.zsoft.gloody.gloodypayroll.rest.controllers;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.services.SectionsConfigService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/categories")
    public ResponseEntity<List<SectionCategoryDto>> saveCategory(@RequestBody SectionCategoryDto category) {
        return sectionsConfigService.saveCategory(category)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/categories/{code}")
    public ResponseEntity<List<SectionCategoryDto>> saveCategory(@PathVariable(name = "code") String code) {
        return sectionsConfigService.deleteCategory(code)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }


}
