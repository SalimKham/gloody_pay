package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionListFilter;
import com.zsoft.gloody.gloodypayroll.services.SectionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/sections")
public class SectionController {
    private final SectionService sectionService;


    @PostMapping("/find_all")
    public ResponseEntity<List<SectionDto>> findAll(@RequestBody SectionListFilter filter) {
        List<SectionDto> result = sectionService.findAll(filter);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{code}")
    public ResponseEntity<SectionDto> findById(@PathVariable("code") String code) {
        return sectionService.findByCode(code)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exists_by_identifier/{identifier}")
    public ResponseEntity<Boolean> existsByIdentifier(@PathVariable("identifier") Integer identifier) {
        return  ResponseEntity.ok().body(sectionService.existByIdentifier(identifier));
    }

    @GetMapping("/next_identifier")
    public ResponseEntity<Integer> existsByIdentifier() {
        return  ResponseEntity.ok().body(sectionService.getNextIdentifier());
    }

    @PostMapping
    public ResponseEntity<List<SectionDto>> create(@Valid @RequestBody SectionDto dto) {
        List<SectionDto> result = sectionService.create(dto);
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping
    public ResponseEntity<List<SectionDto>> update(@Valid @RequestBody SectionDto dto) {
        if (dto.getCode() == null || this.sectionService.notExistsById(dto.getCode())) {
            return ResponseEntity.notFound().build();
        }
       List<SectionDto> result = sectionService.update(dto);
        if (result != null) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.badRequest().build();
    }


    @DeleteMapping("/{code}")
    public ResponseEntity<List<SectionDto>> deleteById(@PathVariable("code") String code) {
        if (code == null || this.sectionService.notExistsById(code)) {
            return ResponseEntity.notFound().build();
        }
        boolean isRemoved = sectionService.deleteById(code);
        if (!isRemoved) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(sectionService.findAll());
    }
}
