package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.EmployeeTitle;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LegalLedgerService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final Map<String, Integer> columnToSectionCode = new HashMap<>() {{
        put("R_SALAIRE", 6);
        put("R_BRUT", 7);
        put("R_RET_SS", 8);
        put("R_BRUT_IMPOS", 9);
        put("R_RET_IRG", 10);
        put("R_NET_PAYE", 11);
    }};
    private final EmployeeRepository employeeRepository;

    public byte[] generate(LocalDate month, Set<Long> employeeIds) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet("Journal légal");
        createHeader(workbook, sheet, month);
        createFirstRow(workbook, sheet);
        AtomicInteger currentIndex = new AtomicInteger(3);

        int PAGE_SIZE = 500;
        Page<PayslipSetting> data = payslipSettingRepository.findAllClosed(month.withDayOfMonth(1), employeeIds, Pageable.ofSize(PAGE_SIZE));
        if (data.isEmpty()) {
            return null;
        }

        do {
            Map<Long, Employee> employees = employeeRepository.findAllById(data.map(PayslipSetting::getEmployee)).stream().collect(Collectors.toMap(Employee::getId, e -> e));
            data.forEach(payslipSetting -> {
                Employee employee = employees.get(payslipSetting.getEmployee());
                if (employee == null) {
                    return;
                }

                String fullName = Objects.requireNonNullElse(employee.getLastName(), " ") + " " +  Objects.requireNonNullElse(employee.getFirstName(), "");
                Row row = sheet.createRow(currentIndex.getAndIncrement());
                row.createCell(0).setCellValue(Objects.requireNonNullElse(employee.getIdentification(), "-"));
                row.createCell(1).setCellValue(fullName.toUpperCase());
                row.createCell(3).setCellValue(Objects.equals(employee.getEmployeeTitle(), EmployeeTitle.SIR) ? "1":  null);
                row.createCell(4).setCellValue(employee.getEmployeeTitle() != null && !Objects.equals(employee.getEmployeeTitle(), EmployeeTitle.SIR)  ? "1":  null);
                payslipSetting.getElements().forEach(element -> {
                    if (columnToSectionCode.containsKey(element.getSection().getCode())) {
                        row.createCell(columnToSectionCode.get(element.getSection().getCode())).setCellValue(element.getValue() == null ? null : PayrollRecordUtils.formatMoney(element.getValue()));
                    }
                });
            });
            if (data.hasNext()) {
                data = payslipSettingRepository.findAllClosed(month.withDayOfMonth(1), employeeIds, data.nextPageable());
            }
        } while (data.hasNext());


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();

    }

    private void createHeader(XSSFWorkbook workbook, Sheet sheet, LocalDate month) {
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 12);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.LEFT);

        XSSFCellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);

        XSSFCellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setAlignment(HorizontalAlignment.RIGHT);

        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        cell0.setCellValue("Livre de paie standard");
        cell0.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        Cell monthCell = headerRow.createCell(10);
        monthCell.setCellValue(month.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        monthCell.setCellStyle(dateStyle);

        Row dateRow = sheet.createRow(1);
        Cell fromDate = dateRow.createCell(10);
        fromDate.setCellStyle(dateStyle);

        Cell todate = dateRow.createCell(11);
        todate.setCellStyle(dateStyle);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        fromDate.setCellValue("Du " + month.withDayOfMonth(1).format(formatter));
        todate.setCellValue("au " + month.with(TemporalAdjusters.lastDayOfMonth()).format(formatter));

        for (int i = 0; i <= 11; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    private void createFirstRow(XSSFWorkbook workbook, Sheet sheet) {
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Header row
        Row headerRow = sheet.createRow(2); // example: row index 5

        String[] headers = {
                "Matricule", "Nom & Prénom", "Date", "Hom", "Fem", "Tot",
                "Salaire", "Salaire brut", "Ret SS", "Sal. Imposable", "Ret. IRG", "Net à payer"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
    }
}
