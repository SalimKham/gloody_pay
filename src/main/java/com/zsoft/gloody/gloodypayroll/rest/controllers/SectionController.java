package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import com.zsoft.gloody.gloodypayroll.services.SectionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/sections")
public class SectionController {
    private final SectionService sectionService;

    @GetMapping
    public ResponseEntity<List<SectionDto>> findAll() {
        List<SectionDto> result = sectionService.findAll();
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Page<SectionDto>> findAllByPage(Pageable pageable) {
        Page<SectionDto> result = sectionService.findAllByPage(pageable);
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
    public ResponseEntity<SectionDto> create(@Valid @RequestBody SectionDto dto) {
        SectionDto result = sectionService.create(dto);
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping
    public ResponseEntity<SectionDto> update(@Valid @RequestBody SectionDto dto) {
        if (dto.getCode() == null || this.sectionService.notExistsById(dto.getCode())) {
            return ResponseEntity.notFound().build();
        }
        SectionDto result = sectionService.update(dto);
        if (result != null) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{code}")
    public ResponseEntity<SectionDto> updateFields(
            @PathVariable("code") String code,
            @RequestBody SectionDto dto,
            @RequestParam(value = "fields") List<String> fields
    ) {
        if (code == null || this.sectionService.notExistsById(code)) {
            return ResponseEntity.notFound().build();
        }
        return sectionService.updateFields(code, dto, fields)
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteById(@PathVariable("code") String code) {
        if (code == null || this.sectionService.notExistsById(code)) {
            return ResponseEntity.notFound().build();
        }
        boolean isRemoved = sectionService.deleteById(code);
        if (!isRemoved) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
