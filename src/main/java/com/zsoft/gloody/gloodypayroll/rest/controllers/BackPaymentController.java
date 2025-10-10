package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.BackPaymentDto;
import com.zsoft.gloody.gloodypayroll.services.BackPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/back_payments")
public class BackPaymentController {
    private final BackPaymentService service;
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody BackPaymentDto backPayment) {
        service.saveBackPayment(backPayment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteBackPayment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{month}")
    public ResponseEntity<List<BackPaymentDto>> save(@PathVariable LocalDate month) {
        return ResponseEntity.ok(service.getBackPayments(month));
    }
}
