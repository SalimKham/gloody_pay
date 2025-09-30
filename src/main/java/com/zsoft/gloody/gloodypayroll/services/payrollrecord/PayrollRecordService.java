package com.zsoft.gloody.gloodypayroll.services.payrollrecord;


import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.entities.PayrollRecord;
import com.zsoft.gloody.gloodypayroll.models.entities.User;
import com.zsoft.gloody.gloodypayroll.models.enums.PayTransferType;
import com.zsoft.gloody.gloodypayroll.models.enums.PayrollRecordType;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayrollRecordRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords.*;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayrollRecordMapper;
import com.zsoft.gloody.gloodypayroll.services.filestore.FileStoreProperties;
import com.zsoft.gloody.gloodypayroll.services.filestore.FileStoreService;
import com.zsoft.gloody.gloodypayroll.services.payrollrecord.payloads.DasIndividualPayloadDTO;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PayrollRecordService {
    private final PayrollRecordRepository payrollRecordRepository;
    private final LegalLedgerService legalLedgerService;
    private final FileStoreProperties fileStoreProperties;
    private final DASDetailsService dasDetailsService;
    private final FileStoreService fileStoreService;
    private final PayrollRecordMapper payrollRecordMapper;

    public static final Map<String, String> contentTypeToExtensionMap = new HashMap<>(5) {
        {
            put("image/png", "png");
            put("image/jpeg", "jpeg");
            put("image/jpg", "jpg");
            put("text/plain", "txt");
            put("text/html", "html");
            put("application/pdf", "pdf");
            put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
            put("application/msword", "doc");
            put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        }
    };
    private final DasIndividualService dasIndividualService;
    private final DasHeaderService dasHeaderService;
    private final PayrollElementsService payrollElementsService;
    private final Form301BISService form301BISService;
    private final PayrollJournalService payrollJournalService;
    private final PayrollTransferService payrollTransferService;

    @Async("payrollRecordsAsyncExecutor")
    public void generateDasHeader(int year, String affiliateName, String activitySector, String affiliateAddress) {
        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.DAS_HEADER);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.DAS_HEADER.name().toLowerCase();
            String contentType = "text/plain";
            String fileName = String.format("DAS_Entête-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            String content = dasHeaderService.generate(year, affiliateName, activitySector, affiliateAddress);
            saveFile(PayrollRecordType.DAS_HEADER, Objects.requireNonNullElse(content, "").getBytes(StandardCharsets.UTF_8), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }

    }

    @Async("payrollRecordsAsyncExecutor")
    public void generateLegalLedger(LegalLedgerPayloadDTO payload) {

        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.LEGAL_LEDGER);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.LEGAL_LEDGER.name().toLowerCase();
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            String fileName = String.format("Journal-légal-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            byte[] content = legalLedgerService.generate(payload.getMonth(), payload.getEmployeeIds());
            saveFile(PayrollRecordType.LEGAL_LEDGER, Objects.requireNonNullElse(content, new byte[]{}), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }
    }


    @Async("payrollRecordsAsyncExecutor")
    public void generate301BIS(Form301BISPayloadDTO payload) {

        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.FORM_301BIS);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.FORM_301BIS.name().toLowerCase();
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            String fileName = String.format("form301bis-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            byte[] content = form301BISService.generate(payload.getYear(), payload.getEmployeeIds());
            saveFile(PayrollRecordType.FORM_301BIS, Objects.requireNonNullElse(content, new byte[]{}), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }
    }

    @Async("payrollRecordsAsyncExecutor")
    public void generatePayrollJournal(PayrollJournalPayloadDTO payload) {

        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.PAYROLL_JOURNAL);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.PAYROLL_JOURNAL.name().toLowerCase();
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            String fileName = String.format("journal-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            byte[] content = payrollJournalService.generate(SecurityUtils.getCurrentCompanyId(), payload.getMonth(), payload.getEmployeeIds());
            saveFile(PayrollRecordType.PAYROLL_JOURNAL, Objects.requireNonNullElse(content, new byte[]{}), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }
    }

    @Async("payrollRecordsAsyncExecutor")
    public void generatePayrollTransfer(PayrollTransferPayloadDTO payload) {

        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.PAYROLL_TRANSFER);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.PAYROLL_TRANSFER.name().toLowerCase();
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            String fileName = String.format("%s-%s", Objects.equals(payload.getType(), PayTransferType.POSTAL) ? "virements CCP" : "virements bancaires", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            byte[] content = payrollTransferService.generate(payload.getMonth(), payload.getType(), payload.getEmployeeIds());
            saveFile(PayrollRecordType.PAYROLL_TRANSFER, Objects.requireNonNullElse(content, new byte[]{}), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }
    }

    @Async("payrollRecordsAsyncExecutor")
    public void generatePayrollElements(PayrollElementPayloadDTO payload) {

        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.PAYROLL_ELEMENTS);
        payrollRecord = payrollRecordRepository.save(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.PAYROLL_ELEMENTS.name().toLowerCase();
            String contentType = "application/pdf";
            String fileName = String.format("Relevé des émoluments-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            byte[] content = payrollElementsService.generate(payload);
            saveFile(PayrollRecordType.PAYROLL_ELEMENTS, Objects.requireNonNullElse(content, new byte[]{}), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }
    }

    @Async("payrollRecordsAsyncExecutor")
    public void generateDasDetails(int year) {
        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.DAS_DETAILS);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.DAS_DETAILS.name().toLowerCase();
            String contentType = "text/plain";
            String fileName = String.format("DAS-detail-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            StringBuffer content = dasDetailsService.generate(year);
            saveFile(PayrollRecordType.DAS_DETAILS, Objects.requireNonNullElse(content, "").toString().getBytes(StandardCharsets.UTF_8), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }

    }

    @Async("payrollRecordsAsyncExecutor")
    public void generateDasIndividual(DasIndividualPayloadDTO payload) {
        PayrollRecord payrollRecord = init(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUser().get().getId());
        payrollRecord.setType(PayrollRecordType.DAS_INDIVIDUAL);
        payrollRecord = payrollRecordRepository.saveAndFlush(payrollRecord);
        try {
            String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + PayrollRecordType.DAS_INDIVIDUAL.name().toLowerCase();
            String contentType = "application/pdf";
            String fileName = String.format("DAS-individuel-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            fileName = getFileName(fileName, contentType);
            payrollRecord.setFileName(fileName);
            byte[] content = dasIndividualService.generate(payload);
            saveFile(PayrollRecordType.DAS_INDIVIDUAL, Objects.requireNonNullElse(content, new byte[]{}), contentType, fileName);
            payrollRecord.setFilePath(path);
            payrollRecord.setFileContentType(contentType);
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.DONE);
        } catch (Exception e) {
            payrollRecord.setStatus(PayrollRecord.GeneratedFileStatus.FAILED);
        } finally {
            payrollRecordRepository.save(payrollRecord);
        }

    }


    private static PayrollRecord init(Long companyId, Long userId) {
        PayrollRecord generatedRecord = new PayrollRecord();
        generatedRecord.setCompany(Company.builder().id(companyId).build());
        generatedRecord.setGeneratedBy(userId);
        generatedRecord.setGeneratedAt(Instant.now());
        generatedRecord.setFileName("");
        generatedRecord.setFilePath("");
        generatedRecord.setStatus(PayrollRecord.GeneratedFileStatus.IN_PROGRESS);
        return generatedRecord;
    }

    private String getFileName(String fileName, String contentType) {
        String extension = contentTypeToExtensionMap.get(contentType.toLowerCase());
        return String.format("%s.%s", fileName, extension != null ? extension : "");
    }

    public void saveFile(PayrollRecordType type, byte[] content, String contentType, String fileName) {
        if (content == null) {
            return;
        }
        String path = fileStoreProperties.getPayRecordPathPrefix() + "/" + type.name().toLowerCase();
        if (path != null) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", contentType);
            metadata.put("Content-Length", String.valueOf(content.length));
            fileStoreService.save(path, fileName, Optional.of(metadata), new ByteArrayInputStream(content));
        }
    }

    public Page<PayrollRecordDTO> getPayrollRecordFilesForCurrentUser(FetchRecordsPayloadDTO payload) {
        Optional<User> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return Page.empty();
        }
        PayrollRecordListFilter filter = payload.getFilter();
        Pageable pageable = PageRequest.of(payload.getPageNumber(), payload.getPageSize(), parseSort(payload.getSort()));
        Specification<PayrollRecord> recordFileSpecification = Specification.where((root, query, cb) -> {
            query.distinct(true);
            List<Predicate> filterPredicates = new ArrayList<>();
            if (filter.statuses() != null && !filter.statuses().isEmpty()) {
                filterPredicates.add(root.get("status").as(String.class).in(filter.statuses()));
            }
            if (filter.types() != null && !filter.types().isEmpty()) {
                filterPredicates.add(root.get("type").as(String.class).in(filter.types()));
            }
            return cb.and(
                    cb.equal(root.get("generatedBy"), currentUser.get().getId()),
                    cb.and(filterPredicates.toArray(new Predicate[0])));
        });
        return payrollRecordRepository.findAll(recordFileSpecification, pageable).map(payrollRecordMapper::toDto);
    }

    public byte[] downloadRecordFile(Long fileId) {
        return payrollRecordRepository.findById(fileId).map(record -> {
            byte[] file = fileStoreService.download(record.getFilePath(), record.getFileName());
            return file;
        }).orElse(null);
    }

    public void deleteRecordFile(long id) {
        Optional<User> currentUser = SecurityUtils.getCurrentUser();
        payrollRecordRepository.findById(id).ifPresent(record -> {
            if (Objects.equals(record.getGeneratedBy(), currentUser.get().getId())) {
                if (record.getFilePath() != null && !record.getFilePath().isEmpty()) {
                    fileStoreService.delete(record.getFilePath(), record.getFileName());
                }
                payrollRecordRepository.deleteById(id);
            }
        });
    }

    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sort.stream()
                .map(s -> {
                    String[] parts = s.split(",");
                    return parts.length == 2
                            ? new Sort.Order(Sort.Direction.fromString(parts[1]), parts[0])
                            : new Sort.Order(Sort.Direction.ASC, parts[0]);
                }).toList();

        return Sort.by(orders);
    }
}
