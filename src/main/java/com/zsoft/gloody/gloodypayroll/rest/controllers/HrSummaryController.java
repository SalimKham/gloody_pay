package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.services.HRAsyncService;
import com.zsoft.gloody.gloodypayroll.services.HRMonthlyDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/api/hr_summary_data")
public class HrSummaryController {
    private final HRMonthlyDataService service;
    private final HRAsyncService asyncService;

    @PostMapping()
    public ResponseEntity<Void> saveSummaryData(@RequestBody List<HRMonthlySummaryDTO> payload) {
        service.save(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contracts/{resource_id}/{month}")
    public ResponseEntity<List<Long>> getSummaryContracts(@PathVariable(name = "resource_id") Long resourceId,
                                                          @PathVariable(name = "month") LocalDate month

    ) {
        return ResponseEntity.ok().body(service.getMonthlyContractIds(resourceId, month));
    }

    @GetMapping("/monthly_contracts/{month}")
    public ResponseEntity<List<Long>> getMonthlyContracts(@PathVariable(name = "month") LocalDate month
    ) {
        return ResponseEntity.ok().body(service.getAllMonthlyContracts(month));
    }

    @PostMapping("/save_drafts/{month}")
    public ResponseEntity<Void> saveSummaryData(@RequestBody Set<Long> resourceIds,@PathVariable LocalDate month) {
        service.saveDrafts(resourceIds, month);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/delete_drafts/{month}")
    public ResponseEntity<Void> deleteDrafts(@RequestBody Set<Long> resourceIds,@PathVariable LocalDate month) {
        service.deleteDrafts(resourceIds, month);
        return ResponseEntity.ok().build();
    }

}
