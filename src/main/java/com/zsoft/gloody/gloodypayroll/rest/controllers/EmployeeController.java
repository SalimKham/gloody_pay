package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.services.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    private final EmployeeService service;

    @PostMapping
    public ResponseEntity<Void> saveEmployee(@RequestBody EmployeeDto payload) {
        service.save(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("bulk")
    public ResponseEntity<Void> createEmployees(@RequestBody  List<EmployeeDto> payload) {
        service.saveBulk(payload);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/bulk")
    public ResponseEntity<Void> updateEmployee(@RequestBody List<EmployeeDto> payload) {
        service.saveBulk(payload);
        return ResponseEntity.ok().build();
    }

    public record UpdateEmployeeFieldPayload(EmployeeDto dto, List<String> fields) {}

}
