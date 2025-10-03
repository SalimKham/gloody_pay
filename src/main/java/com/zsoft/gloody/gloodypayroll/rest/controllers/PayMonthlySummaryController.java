package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.MonthlySummaryDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.MonthlySummaryPayloadDTO;
import com.zsoft.gloody.gloodypayroll.services.PayMonthlySummaryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/monthly_summary")
public class PayMonthlySummaryController {
    private final PayMonthlySummaryService service;
    @PostMapping
    public ResponseEntity<List<MonthlySummaryDto>> getMonthlySummary(@RequestBody MonthlySummaryPayloadDTO payload, Pageable pageable) {
        return ResponseEntity.ok().body(service.getMonthlySummary(payload, pageable));
    }

}
