package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import com.zsoft.gloody.gloodypayroll.models.repositories.AccountingSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingExportFilterDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Transactional
public class AccountingSettingService {
    private final AccountingSettingRepository accountingSettingRepository;
    private final AccountingSettingMapper accountingSettingMapper;
    private final MessageSource messageSource;

    public AccountingSettingDTO save(AccountingSettingDTO validationModelDTO) {
        AccountingSetting accountingSetting = accountingSettingMapper.toEntity(validationModelDTO);
        accountingSetting = accountingSettingRepository.save(accountingSetting);
        return accountingSettingMapper.toDto(accountingSetting);
    }

    @Transactional(readOnly = true)
    public Optional<AccountingSettingDTO> findOne() {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto);
    }


    @Transactional(readOnly = true)
    public Optional<AccountingSettingDTO> getAccountingMonthlyTotals(LocalDate month) {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto)
                .map(accountingSetting -> {
                    AtomicReference<Double> accountingSettingDebitTotal = new AtomicReference<>(0.0);
                    AtomicReference<Double> accountingSettingCreditTotal = new AtomicReference<>(0.0);
                    List<Object[]> accountingTotals =  accountingSettingRepository.getAccountingSectionsMonthlyTotal(month, SecurityUtils.getCurrentCompanyId());
                    Map<Long, Double> totals = accountingTotals == null ? new HashMap<>() : accountingTotals
                            .stream()
                            .collect(Collectors.toMap(objects -> (Long) objects[0], objects -> (Double)objects[1]));
                    if (accountingSetting.getGroups() != null) {
                       accountingSetting.getGroups().forEach(group -> {
                           AtomicReference<Double> debitTotal = new AtomicReference<>(0.0);
                           AtomicReference<Double> creditTotal = new AtomicReference<>(0.0);
                           if (group.getAccounts()!=null) {
                               group.getAccounts().forEach(account -> {
                                   Double accountMonthlyTotal = totals.getOrDefault(account.getId(), 0.0);
                                   account.setMonthlyTotal(accountMonthlyTotal);
                                   if (Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT)) {
                                       debitTotal.updateAndGet(v -> v + accountMonthlyTotal);
                                   } else {
                                       creditTotal.updateAndGet(v -> v + accountMonthlyTotal);
                                   }
                               });
                           }
                           group.setDebitMonthlyTotal(debitTotal.get());
                           group.setCreditMonthlyTotal(creditTotal.get());
                           accountingSettingDebitTotal.updateAndGet(v -> v + group.getDebitMonthlyTotal());
                           accountingSettingCreditTotal.updateAndGet(v -> v + group.getCreditMonthlyTotal());
                       });
                    }
                    accountingSetting.setCreditMonthlyTotal(accountingSettingCreditTotal.get());
                    accountingSetting.setDebitMonthlyTotal(accountingSettingDebitTotal.get());
                    return accountingSetting;
                });
    }

    @Transactional(readOnly = true)
    public Page<AccountingSettingDTO> findAll(Pageable pageable) {
        return accountingSettingRepository.findAll(pageable)
                .map(accountingSettingMapper::toDto);
    }

    public void delete(Long id) {
        accountingSettingRepository.deleteById(id);
    }

    public byte[] exportAccountingData(AccountingExportFilterDto filter) {

        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("dd/MM/yyyy");
        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet(filter.getFileName());
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 8000);
        sheet.setColumnWidth(3, 8000);
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        AtomicInteger currentIndex = new AtomicInteger(0);
        // create header
        Row headerRow = sheet.createRow(currentIndex.getAndIncrement());
        String dateColumn = messageSource.getMessage("accounting.export.columns.date", null, null);
        headerRow.createCell(0).setCellValue(dateColumn);
        String accountNbr = messageSource.getMessage("accounting.export.columns.account_nbr", null, null);
        headerRow.createCell(1).setCellValue(accountNbr);
        String accountTitle = messageSource.getMessage("accounting.export.columns.accounting_title", null, null);
        String debit = messageSource.getMessage("accounting.export.columns.debit", null, null);
        String credit = messageSource.getMessage("accounting.export.columns.credit", null, null);
        headerRow.createCell(2).setCellValue(accountTitle);
        headerRow.createCell(3).setCellValue(debit);
        headerRow.createCell(4).setCellValue(credit);

        accountingSettingRepository.findTopOrderByIdDesc().ifPresent(accountingSetting -> {
            if (accountingSetting.getGroups() == null) {
                return;
            }
            LocalDate month = filter.getStartMonth().withDayOfMonth(1);
            while(!month.isAfter(filter.getEndMonth())) {
                String formattedMonth = month.withDayOfMonth(month.lengthOfMonth()).format(formatter);
                List<Object[]> accountingTotals =  accountingSettingRepository.getAccountingSectionsMonthlyTotal(month, SecurityUtils.getCurrentCompanyId());
                Map<Long, Double> totals = accountingTotals == null ? new HashMap<>() : accountingTotals
                        .stream()
                        .collect(Collectors.toMap(objects -> (Long) objects[0], objects -> (Double)objects[1]));
                accountingSetting.getGroups().forEach(group -> {
                    if (group.getAccounts() == null) {
                        return;
                    }
                    group.getAccounts().forEach(account -> {
                        Double accountMonthlyTotal = totals.getOrDefault(account.getId(), 0.0);
                        Row row = sheet.createRow(currentIndex.getAndIncrement());
                        row.createCell(0).setCellValue(formattedMonth);
                        row.createCell(1).setCellValue(account.getCode());
                        row.createCell(2).setCellValue(account.getTitle());
                        row.createCell(3).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ? String.valueOf(accountMonthlyTotal) : "");
                        row.createCell(4).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ?  "" : String.valueOf(accountMonthlyTotal));
                    });
                });
                month = month.plusMonths(1);
            }

        });

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();

    }
}

