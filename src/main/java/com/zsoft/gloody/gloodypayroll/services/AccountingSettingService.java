package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import com.zsoft.gloody.gloodypayroll.models.repositories.AccountingSettingRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeAccountMonthValueDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipCalculatePayload;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingExportFilterDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingMapper;
import com.zsoft.gloody.gloodypayroll.services.payrollrecord.PayrollRecordsUtils;
import com.zsoft.gloody.gloodypayroll.support.PayslipDetailConverter;
import jakarta.persistence.Tuple;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Service
@Transactional
public class AccountingSettingService  extends CompanySupportService  {
    private final AccountingSettingRepository accountingSettingRepository;
    private final AccountingSettingMapper accountingSettingMapper;
    private final MessageSource messageSource;
    private final PayslipService payslipService;
    private final PayslipDetailConverter payslipDetailConverter;
    private final PayslipSettingRepository payslipSettingRepository;
    private final CacheService cacheService;
    private static final Locale locale = Locale.getDefault();
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public AccountingSettingDTO save(AccountingSettingDTO validationModelDTO) {
        AccountingSetting accountingSetting = accountingSettingMapper.toEntity(validationModelDTO);
        accountingSetting = accountingSettingRepository.save(accountingSetting);
        cacheService.updateSectionMapCache();
        return accountingSettingMapper.toDto(accountingSetting);
    }

    @Transactional(readOnly = true)
    public Optional<AccountingSettingDTO> findOne() {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto);
    }

    public List<AccountingSettingAccountDTO> mapPayslipToAccounting(Set<PayslipElementDto> payslip, EmployeeDto employeeDto, Map<String, AccountingSettingAccountDTO> sectionToAccountingMap) {
        List<AccountingSettingAccountDTO> result = new ArrayList<>();
        payslip.stream()
                .filter(elementDto -> sectionToAccountingMap.containsKey(elementDto.getCode()) && !elementDto.isZero())
                .collect(Collectors.groupingBy(elementDto -> sectionToAccountingMap.get(elementDto.getCode())))
                .forEach((accountingSettingAccountDTO, elements) -> {
                    try {
                        if (elements == null || elements.isEmpty()) {
                            return;
                        }
                        AccountingSettingAccountDTO clonedAccount = (AccountingSettingAccountDTO) accountingSettingAccountDTO.clone();
                        Double sum = elements.stream().mapToDouble(el -> Objects.nonNull(el.getGain()) ? el.getGain().doubleValue() : el.getDeduction().doubleValue()).sum();
                        clonedAccount.setEmployee(employeeDto.getFullName());
                        clonedAccount.setMonthlyTotal(sum);
                        result.add(clonedAccount);
                    } catch (Exception ignored) {

                    }
                });
        return result;
    }

    private AccountingSettingDTO mapMonthlyResult(List<AccountingSettingAccountDTO> byEmployeeAccounting, boolean detailedByEmployee) {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto)
                .map(accountingSetting -> {
                    AtomicReference<Double> accountingSettingDebitTotal = new AtomicReference<>(0.0);
                    AtomicReference<Double> accountingSettingCreditTotal = new AtomicReference<>(0.0);
                    if (accountingSetting.getGroups() != null) {
                        accountingSetting.getGroups().forEach(group -> {
                            AtomicReference<Double> debitTotal = new AtomicReference<>(0.0);
                            AtomicReference<Double> creditTotal = new AtomicReference<>(0.0);
                            if (group.getAccounts() != null) {
                                List<AccountingSettingAccountDTO> accounts = new ArrayList<>();
                                group.getAccounts().forEach(account -> {
                                    List<AccountingSettingAccountDTO> calculatedAccounts = byEmployeeAccounting.stream().filter(calculatedAccount -> Objects.equals(calculatedAccount, account)).toList();
                                    double sum = calculatedAccounts.stream().mapToDouble(AccountingSettingAccountDTO::getMonthlyTotal).sum();
                                    if (account.getDetailedByEmployee() && detailedByEmployee) {
                                        calculatedAccounts.forEach(dto -> {
                                            dto.setCode(account.getCode());
                                            dto.setType(account.getType());
                                            dto.setDetailedByEmployee(account.getDetailedByEmployee());
                                        });
                                        accounts.addAll(calculatedAccounts);
                                    } else {
                                        account.setMonthlyTotal(sum);
                                        account.setEmployee(null);
                                        accounts.add(account);
                                    }

                                    if (Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT)) {
                                        debitTotal.updateAndGet(v -> v + sum);
                                    } else {
                                        creditTotal.updateAndGet(v -> v + sum);
                                    }
                                });
                                group.setAccounts(accounts);
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
                }).orElse(null);
    }

    @Transactional
    public List<AccountingSettingDTO> getAccountingMonthlyTotals(AccountingExportFilterDto filter) {
        List<AccountingSettingDTO> result = new ArrayList<>();
        LocalDate startMonth = filter.getStartMonth().withDayOfMonth(1);
        LocalDate endMonth = filter.getEndMonth().withDayOfMonth(1);
        try {
            while (!startMonth.isAfter(endMonth)) {
                LocalDate date = startMonth;
                //Map<Long, Long> contractResourceMap = hrDataRepository.getContractsByMonth(date).stream().collect(Collectors.toMap(t -> t.get(0, Long.class), t -> t.get(1, Long.class)));
                if (payslipSettingRepository.hasClosedPayslips(date)) {
                    List<AccountingSettingAccountDTO> employeeAccountingList = calculateDraftedPayslips(date);
                    //  contractResourceMap.keySet().removeAll(calculatedPayslipsContracts);
                    // todo for now we include only closed payslips
                    // calculateUnDraftedPayslips(sectionToAccountingMap, contractResourceMap, employeeAccountingList, date);
                    AccountingSettingDTO accountingSettingDTO = mapMonthlyResult(employeeAccountingList, filter.isDetailedByEmployee());
                    if (accountingSettingDTO != null) {
                        result.add(accountingSettingDTO);
                    }
                }
                startMonth = startMonth.plusMonths(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RequestContext.clear();
        }

        return result;
    }


    // todo check the case when we have more than one contract for the employee
    private List<AccountingSettingAccountDTO> calculateDraftedPayslips(LocalDate date) {
        List<Tuple> closedPayslipAccounting = accountingSettingRepository.getAccountingSectionsMonthlyTotalByContracts(date);
        List<AccountingSettingAccountDTO> closedEmployeeAccountingList = new ArrayList<>();
        Map<Long, List<EmployeeAccountMonthValueDto>> monthlyValues = closedPayslipAccounting != null ? closedPayslipAccounting.stream()
                .collect(Collectors.groupingBy(tuple -> tuple.get(0, Long.class), Collectors.mapping(tuple -> new EmployeeAccountMonthValueDto(tuple.get(0, Long.class), payslipDetailConverter.convertToEntityAttribute(tuple.get(1, String.class)), tuple.get(2, Double.class), tuple.get(4, Boolean.class)), Collectors.toList())))
                : Collections.emptyMap();

        monthlyValues.forEach((accountId, values) -> {
            Boolean detailedByEmployee = values.get(0).getDetailedByEmployee();
            if (detailedByEmployee) {
                List<AccountingSettingAccountDTO> accounts = values.stream().map(acc -> {
                    try {
                        AccountingSettingAccountDTO accountingSettingAccountDTO = new AccountingSettingAccountDTO();
                        accountingSettingAccountDTO.setId(accountId);
                        accountingSettingAccountDTO.setMonthlyTotal(acc.getValue());
                        accountingSettingAccountDTO.setEmployee(acc.getEmployee());
                        return accountingSettingAccountDTO;
                    } catch (Exception ignore) {
                    }
                    return null;
                }).filter(Objects::nonNull).toList();
                closedEmployeeAccountingList.addAll(accounts);
            } else {
                Double sum = values.stream().mapToDouble(EmployeeAccountMonthValueDto::getValue).sum();
                AccountingSettingAccountDTO accountingSettingAccountDTO = new AccountingSettingAccountDTO();
                accountingSettingAccountDTO.setId(accountId);
                accountingSettingAccountDTO.setMonthlyTotal(sum);
                closedEmployeeAccountingList.add(accountingSettingAccountDTO);
            }
        });
        return closedEmployeeAccountingList;
    }

    // todo ignored for now
    private void calculateUnDraftedPayslips(Map<String, AccountingSettingAccountDTO> sectionToAccountingMap, Map<Long, Long> contractResourceMap, List<AccountingSettingAccountDTO> employeeAccountingList, LocalDate date) {
        PayslipCalculatePayload calculatePayload = new PayslipCalculatePayload();
        calculatePayload.setMonth(date);
        if (!contractResourceMap.isEmpty()) {
            Map<Long, Employee> employeeMap = employeeRepository.findAllById(contractResourceMap.values()).stream().collect(Collectors.toMap(Employee::getId, em -> em));
            for (Long contractId : contractResourceMap.keySet()) {
                try {
                    calculatePayload.setContractId(contractId);
                    calculatePayload.setResourceId(contractResourceMap.get(contractId));
                    PayslipController.CalculatedPayslip calculatedPayslip = payslipService.calculate(calculatePayload, employeeMap.get(contractResourceMap.get(contractId)), true, true);
                    if (calculatedPayslip != null && calculatedPayslip.calculatedElements() != null && !calculatedPayslip.calculatedElements().isEmpty()) {
                        employeeAccountingList.addAll(mapPayslipToAccounting(calculatedPayslip.calculatedElements(), employeeMapper.toDto(employeeMap.getOrDefault(contractResourceMap.get(contractId), null)), sectionToAccountingMap));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<AccountingSettingDTO> findAll(Pageable pageable) {
        return accountingSettingRepository.findAll(pageable)
                .map(accountingSettingMapper::toDto);
    }

    public void delete(Long id) {
        accountingSettingRepository.deleteById(id);
    }

    public byte[] exportAccountingSetting(String fileName) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet(fileName);
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
        String type = messageSource.getMessage("accounting.export.plan.type", null, locale);
        headerRow.createCell(0).setCellValue(type);
        String accountNbr = messageSource.getMessage("accounting.export.plan.account_nbr", null, locale);
        headerRow.createCell(1).setCellValue(accountNbr);
        String accountTitle = messageSource.getMessage("accounting.export.plan.account_title", null, locale);
        String detailedByEmployee = messageSource.getMessage("accounting.export.plan.detailed_by_employee", null, locale);
        String yes = messageSource.getMessage("yes", null, locale);
        String no = messageSource.getMessage("no", null, locale);
        String credit = messageSource.getMessage("accounting.export.plan.credit", null, locale);
        String debit = messageSource.getMessage("accounting.export.plan.debit", null, locale);

        headerRow.createCell(2).setCellValue(accountTitle);
        headerRow.createCell(3).setCellValue(detailedByEmployee);

        accountingSettingRepository.findTopOrderByIdDesc().ifPresent(accountingSetting -> {
            if (accountingSetting.getGroups() == null) {
                return;
            }
            accountingSetting.getGroups().forEach(group -> {
                if (group.getAccounts() == null) {
                    return;
                }
                group.getAccounts().forEach(account -> {
                    Row row = sheet.createRow(currentIndex.getAndIncrement());
                    row.createCell(0).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ? debit : credit);
                    row.createCell(1).setCellValue(account.getCode());
                    row.createCell(2).setCellValue(account.getTitle());
                    row.createCell(3).setCellValue(Objects.equals(account.getDetailedByEmployee(), true) ? yes : no);
                });
            });
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


    public byte[] exportAccountingData(AccountingExportFilterDto filter) {
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
        String dateColumn = messageSource.getMessage("accounting.export.columns.date", null, locale);
        headerRow.createCell(0).setCellValue(dateColumn);
        String accountNbr = messageSource.getMessage("accounting.export.columns.account_nbr", null, locale);
        headerRow.createCell(1).setCellValue(accountNbr);
        String accountTitle = messageSource.getMessage("accounting.export.columns.accounting_title", null, locale);
        String employee = messageSource.getMessage("accounting.export.columns.employee", null, locale);
        String debit = messageSource.getMessage("accounting.export.columns.debit", null, locale);
        String credit = messageSource.getMessage("accounting.export.columns.credit", null, locale);
        headerRow.createCell(2).setCellValue(accountTitle);
        if (filter.isDetailedByEmployee()) {
            headerRow.createCell(3).setCellValue(employee);
        }
        headerRow.createCell(filter.isDetailedByEmployee() ? 4 : 3).setCellValue(debit);
        headerRow.createCell(filter.isDetailedByEmployee() ? 5 : 4).setCellValue(credit);
        List<AccountingSettingDTO> monthlyTotals = getAccountingMonthlyTotals(filter);
        long numberOfMonths = ChronoUnit.MONTHS.between(filter.getStartMonth().withDayOfMonth(1), filter.getEndMonth().withDayOfMonth(1)) + 1;
        List<String> months = Stream.iterate(filter.getStartMonth().withDayOfMonth(1), date -> date.plusMonths(1))
                .limit(numberOfMonths)
                .map(date -> date.format(PayrollRecordsUtils.dateFormatter))
                .toList();
        int index = 0;
        for (AccountingSettingDTO accountingSetting : monthlyTotals) {
            if (accountingSetting.getGroups() == null) {
                continue;
            }
            String formattedMonth = months.get(index);
            accountingSetting.getGroups().forEach(group -> {
                if (group.getAccounts() == null) {
                    return;
                }
                group.getAccounts().forEach(account -> {
                    if (Objects.equals(account.getMonthlyTotal(), 0.0)) {
                        return;
                    }
                    Row row = sheet.createRow(currentIndex.getAndIncrement());
                    row.createCell(0).setCellValue(formattedMonth);
                    row.createCell(1).setCellValue(account.getCode());
                    row.createCell(2).setCellValue(account.getTitle());
                    if (filter.isDetailedByEmployee()) {
                        row.createCell(3).setCellValue(Objects.nonNull(account.getEmployee()) && !Objects.equals("NOT_FOUND", account.getEmployee()) ? account.getEmployee() : "-");
                    }
                    row.createCell(filter.isDetailedByEmployee() ? 4 : 3).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ? String.valueOf(account.getMonthlyTotal()) : "");
                    row.createCell(filter.isDetailedByEmployee() ? 5 : 4).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ? "" : String.valueOf(account.getMonthlyTotal()));
                });
            });
            sheet.createRow(currentIndex.getAndIncrement());
            index++;
        }
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

