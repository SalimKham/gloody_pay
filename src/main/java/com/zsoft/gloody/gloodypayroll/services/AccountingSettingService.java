package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.beans.CompanySections;
import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import com.zsoft.gloody.gloodypayroll.models.repositories.AccountingSettingRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.*;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingExportFilterDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingMapper;
import com.zsoft.gloody.gloodypayroll.support.PayslipDetailConverter;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final PayslipService payslipService;
    private final PayslipSettingService payslipSettingService;
    private final CompanySections companySections;
    private static  final String CLOSED = "_CLOSED";
    private final PayslipDetailConverter payslipDetailConverter;
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private final CacheService cacheService;
    private static final Locale locale = Locale.getDefault();


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


    public List<AccountingSettingAccountDTO> mapPayslipToAccounting(TreeSet<PayslipElementDto> payslip,   EmployeeDto employeeDto, Map<String, AccountingSettingAccountDTO> sectionToAccountingMap ) {
         List<AccountingSettingAccountDTO> result = new ArrayList<>();
        payslip.stream()
                 .filter(elementDto -> sectionToAccountingMap.containsKey(elementDto.getCode()) && !elementDto.isZero())
                 .collect(Collectors.groupingBy(elementDto -> sectionToAccountingMap.get(elementDto.getCode())))
                .forEach((accountingSettingAccountDTO, elements) ->  {
                    try {
                        if (elements == null || elements.isEmpty()) {
                            return;
                        }
                        AccountingSettingAccountDTO clonedAccount = (AccountingSettingAccountDTO)  accountingSettingAccountDTO.clone();
                        Double sum = elements.stream().mapToDouble(el -> Objects.nonNull(el.getGain()) ? el.getGain().doubleValue() :  el.getDeduction().doubleValue()).sum();
                        clonedAccount.setEmployee(employeeDto.getFullName());
                        clonedAccount.setMonthlyTotal(sum);
                        result.add(clonedAccount);
                    }catch (Exception ignored) {

                    }
                });
        return result;
    }


    private AccountingSettingDTO mapMonthlyResult( List<AccountingSettingAccountDTO> byEmployeeAccounting, boolean detailedByEmployee) {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto)
                .map(accountingSetting -> {
                    AtomicReference<Double> accountingSettingDebitTotal = new AtomicReference<>(0.0);
                    AtomicReference<Double> accountingSettingCreditTotal = new AtomicReference<>(0.0);
                    if (accountingSetting.getGroups() != null) {
                        accountingSetting.getGroups().forEach(group -> {
                            AtomicReference<Double> debitTotal = new AtomicReference<>(0.0);
                            AtomicReference<Double> creditTotal = new AtomicReference<>(0.0);
                            if (group.getAccounts()!=null) {
                                List<AccountingSettingAccountDTO> accounts = new ArrayList<>();
                                group.getAccounts().forEach(account -> {
                                List<AccountingSettingAccountDTO> calculatedAccounts =  byEmployeeAccounting.stream().filter(calculatedAccount -> Objects.equals(calculatedAccount, account)).toList();
                                double sum = calculatedAccounts.stream().mapToDouble(AccountingSettingAccountDTO::getMonthlyTotal).sum();
                                if (account.getDetailedByEmployee() && detailedByEmployee) {
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
    public List<AccountingSettingDTO>  getAccountingMonthlyTotals(AccountingExportFilterDto filter) {
        Map<String, List<PayslipSettingDto>> payslipSettingDtoMap = filter.getHrData();
        List<AccountingSettingDTO> result = new ArrayList<>();
        List<PayslipSettingDto> draftDtos = new ArrayList<>();
        boolean saveDraft;
        for (String key: payslipSettingDtoMap.keySet()) {
            List<PayslipSettingDto> payslipSettings = payslipSettingDtoMap.get(key);
            boolean closed = key.contains(CLOSED);
            if (closed) {
                key = key.replace(CLOSED, "");
            }
            LocalDate date = LocalDate.parse(key);
            saveDraft = date.withDayOfMonth(1).equals(LocalDate.now().withDayOfMonth(1));
            List<AccountingSettingAccountDTO>  employeeAccountingList = new ArrayList<>();
            Map<String, AccountingSettingAccountDTO> sectionToAccountingMap = companySections.getSections().values().stream().filter(sectionDto -> Objects.nonNull(sectionDto.getAccounting()))
                    .collect(Collectors.toMap(SectionDto::getCode, SectionDto::getAccounting));
            if (closed && !filter.isDetailedByEmployee()) {
                List<Object[]> closedPayslipAccounting = accountingSettingRepository.getAccountingSectionsMonthlyTotal(date, SecurityUtils.getCurrentCompanyId());

                Map<Long, Double> monthlyValues = closedPayslipAccounting != null ? closedPayslipAccounting.stream().collect(Collectors.toMap(o -> (Long)o[0], o ->     BigDecimal.valueOf((Double) o[1]).setScale(2, RoundingMode.HALF_UP).doubleValue())) : Collections.emptyMap();
                employeeAccountingList = sectionToAccountingMap.values().stream().map(account -> {
                    account.setMonthlyTotal(monthlyValues.getOrDefault(account.getId(),  0.0));
                    return account;
                }).toList();
            } else {
                //Already draft payslips, or closed
                List<Long> draftIds = closed ? Collections.emptyList() : payslipSettings.stream().filter(payslipSettingDto -> Objects.equals(payslipSettingDto.getDraft(), true)).map(payslipSettingDto -> payslipSettingDto.getEmployee().getId()).toList();
                List<Object[]> closedPayslipAccounting =  closed ? accountingSettingRepository.getAccountingSectionsMonthlyTotalByEmployee(date, SecurityUtils.getCurrentCompanyId()) :
                        accountingSettingRepository.getAccountingSectionsMonthlyTotalByDraftEmployee(date, draftIds);

                List<AccountingSettingAccountDTO>  closedEmployeeAccountingList = new ArrayList<>();
                Map<Long, List<EmployeeAccountMonthValueDto>> monthlyValues = closedPayslipAccounting != null ? closedPayslipAccounting.stream()
                        .collect(Collectors.groupingBy(ob -> (Long)ob[0], Collectors.mapping(o-> new EmployeeAccountMonthValueDto((Long) o[0], payslipDetailConverter.convertToEntityAttribute((String) o[1]), (Double) o[2]), Collectors.toList())))
                        : Collections.emptyMap();

                sectionToAccountingMap.values().forEach(account -> {
                    if (monthlyValues.containsKey(account.getId())) {
                        List<AccountingSettingAccountDTO> accounts = monthlyValues.get(account.getId()).stream().map(acc -> {
                            try {
                                AccountingSettingAccountDTO accountingSettingAccountDTO = (AccountingSettingAccountDTO) account.clone();
                                accountingSettingAccountDTO.setMonthlyTotal(acc.getValue());
                                accountingSettingAccountDTO.setEmployee(acc.getEmployee());
                                return accountingSettingAccountDTO;
                            } catch (Exception ignore) {
                            }
                            return account;
                        }).toList();
                        Double sum = !account.getDetailedByEmployee() ? accounts.stream().mapToDouble(AccountingSettingAccountDTO::getMonthlyTotal).sum() : 0;
                        if (account.getDetailedByEmployee()) {
                            closedEmployeeAccountingList.addAll(accounts);
                        } else {
                            account.setMonthlyTotal(sum);
                            closedEmployeeAccountingList.add(account);
                        }
                    }
                });
                employeeAccountingList = closedEmployeeAccountingList;
            }

            //TODO DELETE 'CONTRACTiD = -1' ON PROD

            if (!closed) {
                payslipSettings = payslipSettings.stream().filter(payslipSettingDto -> !Objects.equals(payslipSettingDto.getDraft(), true)).toList();
                // Calculate payslips
                if (!payslipSettings.isEmpty()) {
                    List<PayslipSettingDto> templates =  payslipSettingService.findMonthlyTemplates(date, payslipSettings.stream().filter(payslipSettingDto -> Objects.nonNull(payslipSettingDto.getId())).map(PayslipSettingDto::getId).toList());
                    Map<Long, PayslipSettingDto> temmplatesMap = templates.stream().collect(Collectors.toMap(PayslipSettingDto::getId, payslipSetting -> payslipSetting));
                    PayslipSettingDto defaultTemplate = templates.stream().filter(PayslipSettingDto::isByDefault).findFirst().orElse(null);
                    Map<Long, List<PayslipSettingDto>> ByMonthAndResourceTemplate = templates.stream().filter(t -> t.getEmployee() != null && t.getPaymentDate() != null &&
                                                                                                                   date.getMonthValue() == t.getPaymentDate().getMonthValue() &&
                                                                                                                   date.getYear() == t.getPaymentDate().getYear())
                            .collect(Collectors.groupingBy(payslipSetting -> payslipSetting.getContract().getId() != -1 ? payslipSetting.getContract().getId() : payslipSetting.getEmployee().getId()));
                    for (PayslipSettingDto payslipSettingDto : payslipSettings) {

                       try {
                           PayslipSettingDto payslipSetting = ByMonthAndResourceTemplate.containsKey(payslipSettingDto.getContract().getId()) || (ByMonthAndResourceTemplate.containsKey(payslipSettingDto.getEmployee().getId()) ) ?
                                   ByMonthAndResourceTemplate.getOrDefault(payslipSettingDto.getContract().getId(), ByMonthAndResourceTemplate.get(payslipSettingDto.getEmployee().getId())).get(0) : payslipSettingDto.getId() != null ?  temmplatesMap.get(payslipSettingDto.getId()): defaultTemplate;

                        if (payslipSetting == null || payslipSetting.getElements() == null) {
                            continue;
                        }
                        PayslipSettingDto clonedDto = (PayslipSettingDto) payslipSetting.clone();
                        payslipSettingDto.setClosed(payslipSetting.isClosed());
                        payslipSettingDto.setElements(clonedDto.getElements());
                        TreeSet<PayslipElementDto> calculatedPayslip = payslipService.calculate(payslipSettingDto, null, false);
                        if (calculatedPayslip != null && !calculatedPayslip.isEmpty()) {
                            employeeAccountingList.addAll(mapPayslipToAccounting(calculatedPayslip, payslipSettingDto.getEmployee(), sectionToAccountingMap) );
                            if (saveDraft) {
                                draftDtos.add(getDraftDto(payslipSettingDto, calculatedPayslip));
                            }
                        }
                       }catch (Exception e) {
                           e.printStackTrace();
                       }
                    }
                }
            }

            AccountingSettingDTO accountingSettingDTO = mapMonthlyResult(employeeAccountingList, filter.isDetailedByEmployee());
            if (accountingSettingDTO != null) {
                result.add(accountingSettingDTO);
            }
        }
        updateDrafts(draftDtos);
        return result;
    }

    @Transactional
    public void updateDrafts(List<PayslipSettingDto> draftDtos) {
        if (!draftDtos.isEmpty()) {
            payslipSettingRepository.deleteByEmployeeInAndDraft(draftDtos.stream().map(dt -> dt.getEmployee().getId()).toList());
            payslipSettingRepository.saveAll(draftDtos.stream().map(payslipSettingMapper::toEntity).toList());
        }
    }

    private PayslipSettingDto getDraftDto(PayslipSettingDto payslipSettingDto, TreeSet<PayslipElementDto> calculatedPayslip) {
        Map<String,PayslipElementDto> mappedElements =  calculatedPayslip.stream().collect(Collectors.toMap(PayslipElementDto::getCode, elementDto -> elementDto));
        payslipSettingDto.getElements().forEach(payslipSettingElementDto -> {
            payslipSettingElementDto.setId(null);
            PayslipElementDto calculatedEl = mappedElements.get(payslipSettingElementDto.getSection());
            PayslipService.payslipElementToPayslipSettingElement(payslipSettingElementDto, calculatedEl);
        });
        payslipSettingDto.setDraft(true);
        payslipSettingDto.setId(null);
        payslipSettingDto.setTemplate(false);
        payslipSettingDto.setPaymentDate(null);

        return payslipSettingDto;
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
                    row.createCell(0).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ? debit: credit);
                    row.createCell(1).setCellValue(account.getCode());
                    row.createCell(2).setCellValue(account.getTitle());
                    row.createCell(3).setCellValue(Objects.equals(account.getDetailedByEmployee(), true) ? yes: no);
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
        List<String> months = filter.getHrData().keySet().stream().map(s -> LocalDate.parse(s.replace(CLOSED, "")).format(formatter)).toList();
        int index = 0;
        for (AccountingSettingDTO accountingSetting: monthlyTotals ) {
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
                        row.createCell(filter.isDetailedByEmployee() ? 5 : 4).setCellValue(Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT) ?  "" : String.valueOf(account.getMonthlyTotal()));
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

