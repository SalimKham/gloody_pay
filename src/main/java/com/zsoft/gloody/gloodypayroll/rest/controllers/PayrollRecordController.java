package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords.*;
import com.zsoft.gloody.gloodypayroll.services.payrollrecord.PayrollRecordService;
import com.zsoft.gloody.gloodypayroll.services.payrollrecord.payloads.DasIndividualPayloadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payslip/record")
public class PayrollRecordController {
    private final PayrollRecordService recordService;

    @PostMapping
    public ResponseEntity<Page<PayrollRecordDTO>> getPayRollRecordsForCurrentUser( @RequestBody  FetchRecordsPayloadDTO payload) {
        Page<PayrollRecordDTO> result = recordService.getPayrollRecordFilesForCurrentUser(payload);
        return  ResponseEntity.ok().body(result);
    }

    @PostMapping("generate/legal_ledger")
    public ResponseEntity<Void> generateLegalLedger(@RequestBody LegalLedgerPayloadDTO payload) {
        recordService.generateLegalLedger(payload);
        return  ResponseEntity.ok().build();
    }
    @PostMapping("generate/form_301bis")
    public ResponseEntity<Void> generate301BIS(@RequestBody Form301BISPayloadDTO payload) {
        recordService.generate301BIS(payload );
        return  ResponseEntity.ok().build();
    }

    @PostMapping("generate/payroll_journal")
    public ResponseEntity<Void> generatePayrollJournal(@RequestBody PayrollJournalPayloadDTO payload) {
        recordService.generatePayrollJournal(payload );
        return  ResponseEntity.ok().build();
    }

    @GetMapping("generate/das_details/{year}")
    public ResponseEntity<Void> generateDasDetails(@PathVariable int year) {
        recordService.generateDasDetails(year);
        return  ResponseEntity.ok().build();
    }

    @GetMapping("generate/das_header/{year}")
    public ResponseEntity<Void> generateDasHeader(@PathVariable int year, @RequestParam("activity_sector") String activitySector,  @RequestParam("address") String address) {
        recordService.generateDasHeader(year, activitySector, address);
        return  ResponseEntity.ok().build();
    }

    @PostMapping("generate/das_individual")
    public ResponseEntity<Void> generateDasIndividual(
            @RequestBody DasIndividualPayloadDTO payload
    ) {
        recordService.generateDasIndividual(payload);
        return  ResponseEntity.ok().build();
    }
    @PostMapping("generate/payroll_elements")
    public ResponseEntity<Void> generatePayrollElements( @RequestBody  PayrollElementPayloadDTO payload) {
        recordService.generatePayrollElements(payload);
        return  ResponseEntity.ok().build();
    }


    @GetMapping("/download/{record_id}")
    public ResponseEntity<byte[]> downloadPayrollRecordFile(@PathVariable(name = "record_id") Long recordId) {
        return ResponseEntity.ok().body(recordService.downloadRecordFile(recordId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecordFile(@PathVariable Long id) {
        recordService.deleteRecordFile(id);
        return  ResponseEntity.ok().build();
    }
}
