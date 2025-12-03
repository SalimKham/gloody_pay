package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.services.VariableService;
import com.zsoft.gloody.gloodypayroll.services.VariableSettingFilter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/variables")
public class VariableSettingController {
    private final VariableService variableService;

    @PostMapping
    public ResponseEntity<Page<VariableSettingDto>> findAll(Pageable pageable, @RequestBody VariableSettingFilter filter) {
        Page<VariableSettingDto> result = variableService.findAll(pageable, filter);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("bulk")
    public ResponseEntity<Page<VariableSettingDto>> saveBulk(@RequestBody List<VariableSettingDto> variables, Pageable pageable,  @RequestParam(value = "query", required = false) String query) {
        Page<VariableSettingDto> result = variableService.save(variables, pageable, query);
        return ResponseEntity.ok().body(result);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Page<VariableSettingDto>> deleteById(@PathVariable(name = "id") Long variableId, Pageable pageable,  @RequestParam(value = "query", required = false) String query) {
        Page<VariableSettingDto> result = variableService.delete(variableId, pageable, query);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/source/{source}")
    public ResponseEntity<Void> deleteBySource(@PathVariable String source) {
        variableService.delete(source);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/delete_sourced/{company_id}")
    public ResponseEntity<Void> deleteAllSourcedVariables(@PathVariable(name = "company_id") Long companyId) {
        variableService.deleteAllSourcedForCompany(companyId);
        return ResponseEntity.ok().build();
    }
}
