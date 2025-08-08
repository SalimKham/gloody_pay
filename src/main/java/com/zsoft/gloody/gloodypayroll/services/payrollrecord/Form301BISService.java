package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.enums.EmployeeTitle;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.utils.CommonUtils;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class Form301BISService {
    private final Map<String, Integer> columnToSectionCode = new HashMap<>() {{
        put("R_BRUT_IMPOS", 6);
        put("R_RET_IRG", 7);
        put("R_IRG_10", 8);
    }};
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;

    public byte[] generate(int year, Set<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return null;
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet("Form 301BIS");
        createFirstRow(workbook, sheet);
        AtomicInteger currentIndex = new AtomicInteger(1);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int PAGE_SIZE = 500;
        List<Set<Long>> employeeIdsList =  CommonUtils.splitSet(ids, PAGE_SIZE);


        CellStyle borderedStyle = getBorderStyle(workbook);


        for (Set<Long> employeeIds : employeeIdsList) {
            Page<Tuple> data = payslipElementSettingRepository.getClosedElementsByEmployeeInAndCodeIn(employeeIds, columnToSectionCode.keySet(), year, Pageable.unpaged());
            if (data.isEmpty()) {
                continue;
            }
            Map<Long, List<Tuple>> mappedData = data.getContent().stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(0, Long.class),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            Map<Long, Employee> employees = employeeRepository.findAllById(data.map(t -> t.get(0, Long.class))).stream().collect(Collectors.toMap(Employee::getId, e -> e));

            mappedData.forEach((employeeId, values) -> {
                Employee employee = employees.get(employeeId);
                if (employee == null) {
                    return;
                }
                String fullName = Objects.requireNonNullElse(employee.getLastName(), " ") + " " + Objects.requireNonNullElse(employee.getFirstName(), "");
                AtomicReference<Double> totalBrut = new AtomicReference<>((double) 0);
                AtomicReference<Double> totalIrg = new AtomicReference<>((double) 0);
                AtomicReference<Double> totalIrg10 = new AtomicReference<>((double) 0);
                values.stream().collect(Collectors.groupingBy(
                        tuple -> tuple.get(1, LocalDate.class),
                        LinkedHashMap::new,
                        Collectors.toList()
                )).forEach((date, monthlyValues) -> {
                    Row row = sheet.createRow(currentIndex.getAndIncrement());
                    row.createCell(0).setCellValue(Objects.requireNonNullElse(employee.getIdentification(), "-"));
                    row.createCell(1).setCellValue(fullName.toUpperCase());
                    row.createCell(2).setCellValue(date.format(dateFormatter));
                    row.createCell(3).setCellValue(Objects.equals(employee.getEmployeeTitle(), EmployeeTitle.SIR) ? "1" : null);
                    row.createCell(4).setCellValue(employee.getEmployeeTitle() != null && !Objects.equals(employee.getEmployeeTitle(), EmployeeTitle.SIR) ? "1" : null);
                    AtomicReference<Double> brut = new AtomicReference<>((double) 0);
                    AtomicReference<Double> irg = new AtomicReference<>((double) 0);
                    AtomicReference<Double> irg10 = new AtomicReference<>((double) 0);
                    monthlyValues.forEach(value -> {
                        switch (value.get(2, String.class)) {
                            case "R_BRUT_IMPOS":
                                brut.set(value.get(3, Double.class));
                                break;
                            case "R_RET_IRG":
                                irg.set(value.get(3, Double.class));
                                break;
                            case "R_IRG_10":
                                irg10.set(value.get(3, Double.class));
                                break;
                        }
                    });
                    totalBrut.set(totalBrut.get() + brut.get());
                    totalIrg.set(totalIrg.get() + irg.get());
                    totalIrg10.set(totalIrg10.get() + irg.get());
                    row.createCell(6).setCellValue(brut.get() == 0 ? "" : PayrollRecordUtils.formatMoney(brut.get()));
                    row.createCell(7).setCellValue(irg.get() == 0 ? "" : PayrollRecordUtils.formatMoney(brut.get()));
                    row.createCell(8).setCellValue(irg10.get() == 0 ? "" : PayrollRecordUtils.formatMoney(brut.get()));
                    row.createCell(9).setCellValue(irg10.get() == 0 ? "" : PayrollRecordUtils.formatMoney(brut.get() * 10 / 100));

                });

                Row row = sheet.createRow(currentIndex.getAndIncrement());
                row.setHeight((short) 400);
                createBorderedCell(row, borderedStyle, 0).setCellValue(Objects.requireNonNullElse(employee.getIdentification(), "-"));
                createBorderedCell(row, borderedStyle, 1).setCellValue(fullName.toUpperCase());
                createBorderedCell(row, borderedStyle, 2).setCellValue("");
                createBorderedCell(row, borderedStyle, 3).setCellValue("");
                createBorderedCell(row, borderedStyle, 4).setCellValue("");
                createBorderedCell(row, borderedStyle, 5).setCellValue("");
                createBorderedCell(row, borderedStyle, 6).setCellValue(totalBrut.get() == 0 ? "" : PayrollRecordUtils.formatMoney(totalBrut.get()));
                createBorderedCell(row, borderedStyle, 7).setCellValue(totalIrg.get() == 0 ? "" : PayrollRecordUtils.formatMoney(totalIrg.get()));
                createBorderedCell(row, borderedStyle, 8).setCellValue(totalIrg10.get() == 0 ? "" : PayrollRecordUtils.formatMoney(totalIrg10.get()));
                createBorderedCell(row, borderedStyle, 9).setCellValue(totalIrg10.get() == 0 ? "" : PayrollRecordUtils.formatMoney(totalIrg10.get() * 10 / 100));
            });
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

    private Cell createBorderedCell(Row row, CellStyle borderStyle, int cellNum) {
        Cell cell = row.createCell(cellNum);
        cell.setCellStyle(borderStyle);
        return cell;
    }

    private CellStyle getBorderStyle(Workbook workbook) {
        CellStyle borderedStyle = workbook.createCellStyle();
        borderedStyle.setBorderTop(BorderStyle.MEDIUM);
        borderedStyle.setBorderBottom(BorderStyle.MEDIUM);
        borderedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return borderedStyle;
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
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Header row
        Row headerRow = sheet.createRow(0); // example: row index 5

        String[] headers = {
                "Matricule", "Nom   Prénom", "Date", "Hom", "Fem", "Tot",
                "SAL_IMPOS", "IRG", "IRG10% BASE", "IRG 10%"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(0, 4600);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 4600);
        sheet.setColumnWidth(6, 4600);
        sheet.setColumnWidth(7, 4600);
        sheet.setColumnWidth(8, 4600);
        sheet.setColumnWidth(9, 4600);
    }
}
