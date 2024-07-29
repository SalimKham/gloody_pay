package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.LabelSettingDto;
import com.zsoft.gloody.gloodypayroll.services.LabelService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/labels")
public class LabelSettingController {
    private final LabelService labelService;

    @GetMapping
    public ResponseEntity<Page<LabelSettingDto>> findAll(Pageable pageable,  @RequestParam(value = "query", required = false) String query) {
        Page<LabelSettingDto> result = labelService.findAll(pageable, query);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("bulk")
    public ResponseEntity<Page<LabelSettingDto>> saveBulk(@RequestBody List<LabelSettingDto> variables, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        Page<LabelSettingDto> result = labelService.save(variables, pageable, query);
        return ResponseEntity.ok().body(result);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Page<LabelSettingDto>> deleteById(@PathVariable(name = "id") Long variableId, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        Page<LabelSettingDto> result = labelService.delete(variableId, pageable, query);
        return ResponseEntity.ok().body(result);
    }

}
