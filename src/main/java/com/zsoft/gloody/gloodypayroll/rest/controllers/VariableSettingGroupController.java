package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingGroupDto;
import com.zsoft.gloody.gloodypayroll.services.VariableSettingGroupService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/variable_group")
public class VariableSettingGroupController {
    private final VariableSettingGroupService service;

    @GetMapping
    public ResponseEntity<Page<VariableSettingGroupDto>> findAll(Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        Page<VariableSettingGroupDto> result = service.findAll(pageable, query);
        return ResponseEntity.ok().body(result);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable(name = "id") Long variableId) {
        service.delete(variableId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody VariableSettingGroupDto variableSettingGroupDto) {
       service.save(variableSettingGroupDto);
        return ResponseEntity.ok().build();
    }

}
