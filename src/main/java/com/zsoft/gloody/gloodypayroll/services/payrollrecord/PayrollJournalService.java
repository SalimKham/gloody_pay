package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.enums.MaritalStatus;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.utils.CommonUtils;
import jakarta.persistence.Tuple;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.services.payrollrecord.PayrollRecordsUtils.setColumnWidthInPixels;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollJournalService {
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final SectionRepository sectionRepository;
    private Map<String, Section> sections;

    public byte[] generate(Long companyId, LocalDate month, Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet("Journal");
        setColumnWidthInPixels(sheet, 10);
        sections = sectionRepository.getMonthlySection(ids, month.withDayOfMonth(1)).stream()
                .collect(Collectors.toMap(
                        Section::getCode,
                        s -> s,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        AtomicInteger currentIndex = new AtomicInteger(1);
        int PAGE_SIZE = 500;
        List<Set<Long>> employeeIdsList = CommonUtils.splitSet(ids, PAGE_SIZE);

        for (Set<Long> employeeIds : employeeIdsList) {
            Page<Tuple> data = payslipElementSettingRepository.getClosedElementsByEmployeeIdAndMonth(employeeIds, month.withDayOfMonth(1), Pageable.unpaged());
            Map<Long, Map<String, Double>> employeeSectionValues = data.getContent().stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(0, Long.class),
                            Collectors.toMap(
                                    tuple -> tuple.get(1, String.class),
                                    tuple -> tuple.get(2, Double.class),
                                    (v1, v2) -> v1,
                                    LinkedHashMap::new
                            )
                    ));

            Set<Employee> employees = new HashSet<>(employeeRepository.findAllById(employeeIds));
            List<Set<Employee>> splitBy7 = CommonUtils.splitSet(employees, 7);
            for (Set<Employee> emps : splitBy7) {
                createFirstRow(workbook, sheet, currentIndex.getAndAdd(sections.keySet().size() + 10), emps, employeeSectionValues);
            }
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


    private void setBorderLeft(CellStyle style) {
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
    }
    private void setLeftAndRightBorders(CellStyle style) {
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
    }

    private void applyGreenBackground(XSSFCellStyle style) {
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setAlignment(HorizontalAlignment.LEFT);
    }

    private void applyFont(XSSFWorkbook workbook, XSSFCellStyle style) {
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Arial");
        style.setFont(font);
    }

    private XSSFCellStyle floatLeftStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        applyGreenBackground(style);
        applyFont(workbook, style);
        setLeftAndRightBorders(style);
        style.setIndention((short) 1);
        return style;
    }

    private XSSFCellStyle floatLeftNoBackground(XSSFWorkbook workbook) {
        XSSFCellStyle style = floatLeftStyle(workbook);
        style.setFillForegroundColor(IndexedColors.WHITE.index);
        style.getFont().setColor(IndexedColors.BLACK.getIndex());
        return style;
    }

    private XSSFCellStyle floatRightStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = floatLeftStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private String getMaritalStatus(MaritalStatus maritalStatus) {
        return switch (maritalStatus) {
            case PACS -> "Pacsé(e)";
            case SINGLE -> "Célibataire";
            case MARRIED -> "Marié(e)";
            case WIDOWED -> "Veuf(ve)";
            case DIVORCED -> "Divorcé(e)";
            case SEPARATED -> "Séparé(e)";
            case NOT_APPLICABLE -> "Non applicable";
            case DOMESTIC_PARTNERSHIP -> "Union libre";
            default -> "-";
        };
    }

    private void renderSectionValues(XSSFWorkbook workbook, Sheet sheet, int index, Set<Employee> employees, Map<Long, Map<String, Double>> sectionValues) {
        AtomicInteger currentIndex = new AtomicInteger(index);
        XSSFCellStyle style = workbook.createCellStyle();
        setBorderLeft(style);
        XSSFCellStyle floatLeftNoBackground = floatLeftNoBackground(workbook);
        sections.keySet().forEach(s -> {
            Row row = sheet.createRow(currentIndex.get());
            createCell(row, 0, floatLeftNoBackground, false, false);
            row.getCell(0).getCellStyle().setBorderRight(BorderStyle.NONE);
            row.getCell(0).setCellValue(sections.get(s).getIdentifier());
            createCell(row, 1, floatLeftNoBackground, false, false);
            row.getCell(1).setCellValue(sections.get(s).getLabel() );
            row.getCell(1).getCellStyle().setBorderLeft(BorderStyle.NONE);
            currentIndex.getAndAdd(1);
            int cellNum = 2;
            double total = 0;
            for (Employee employee : employees) {
                Double cellValue = sectionValues.containsKey(employee.getId()) && sectionValues.get(employee.getId()).containsKey(s) ? sectionValues.get(employee.getId()).get(s) : null;
                createCell(row, cellNum, floatLeftNoBackground, false, false);
                row.getCell(cellNum).setCellValue(Objects.nonNull(cellValue) ? String.format("%.2f", cellValue) : "");
                total += Objects.requireNonNullElse(cellValue, 0d);
                cellNum++;
            }
            createCell(row, 9, floatLeftNoBackground, false, false);
            row.getCell(9).setCellValue(String.format("%.2f", total));

        });
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 9000);
    }

    private void mergeCells(Sheet sheet, int rowIndex, int endRowIndex) {
        for (int i = rowIndex; i < endRowIndex; i++) {
            sheet.addMergedRegion(new CellRangeAddress(
                    i,
                    i,
                    0,
                    1
            ));
        }

    }

    private void createCell(Row row, int cellNum, XSSFCellStyle style, boolean applyTopBorder, boolean applyBottomBorder) {
        Cell cell = row.createCell(cellNum);
        cell.setCellStyle(style);
    }

    private void createFirstRow(XSSFWorkbook workbook, Sheet sheet, int index, Set<Employee> employees, Map<Long, Map<String, Double>> sectionValues) {
        XSSFCellStyle floatLeftStyle = floatLeftStyle(workbook);
        XSSFCellStyle floatRightStyle = floatRightStyle(workbook);
        int cellNum = 0;
        Row identificationRow = sheet.createRow(index);
        Row row2 = sheet.createRow(index + 1);
        Row lastNameRow = sheet.createRow(index + 2);
        Row firstNameRow = sheet.createRow(index + 3);
        Row MaritalStatusRow = sheet.createRow(index + 4);
        Row jobRow = sheet.createRow(index + 5);
        Row sectionRow = sheet.createRow(index + 6);
        for (int i = 0; i <= 9; i++) {
            createCell(identificationRow, cellNum + i, floatLeftStyle, true, false);
            createCell(row2, cellNum + i, floatLeftStyle, false, false);
            createCell(lastNameRow, cellNum + i, floatLeftStyle, false, false);
            createCell(firstNameRow, cellNum + i, floatLeftStyle, false, false);
            createCell(MaritalStatusRow, cellNum + i, floatLeftStyle, false, false);
            createCell(jobRow, cellNum + i, floatLeftStyle, false, false);
            createCell(sectionRow, cellNum + i, floatLeftStyle, false, true);
        }
        lastNameRow.getCell(0).setCellStyle(floatRightStyle);
        firstNameRow.getCell(0).setCellStyle(floatRightStyle);
        MaritalStatusRow.getCell(0).setCellStyle(floatRightStyle);
        jobRow.getCell(0).setCellStyle(floatRightStyle);
        identificationRow.getCell(1).setCellStyle(floatRightStyle);
        identificationRow.getCell(0).setCellValue("Ventilation");
        identificationRow.getCell(1).setCellValue("Matricule");
        identificationRow.getCell(9).setCellValue("Total");
        row2.getCell(0).setCellValue("par salarié");
        lastNameRow.getCell(0).setCellValue("Nom");
        firstNameRow.getCell(0).setCellValue("Prénom");
        MaritalStatusRow.getCell(0).setCellValue("Situation familiale");
        jobRow.getCell(0).setCellValue("Fonction");
        sectionRow.getCell(0).setCellValue("Rubriques");
        cellNum = 2;
        for (Employee employee : employees) {
            identificationRow.getCell(cellNum).setCellValue(Objects.requireNonNullElse(employee.getIdentification(), ""));
            lastNameRow.getCell(cellNum).setCellValue(Objects.requireNonNullElse(employee.getLastName(), ""));
            firstNameRow.getCell(cellNum).setCellValue(Objects.requireNonNullElse(employee.getFirstName(), ""));
            String maritalStatus = Objects.nonNull(employee.getMaritalStatus()) ? getMaritalStatus(employee.getMaritalStatus()) : "";
            MaritalStatusRow.getCell(cellNum).setCellValue(String.format("%s %s", maritalStatus, Objects.requireNonNullElse(employee.getNumberOfChildren(), "0")));
            jobRow.getCell(cellNum).setCellValue(Objects.requireNonNullElse(employee.getJobTitle(), ""));
            cellNum++;
        }
        mergeCells(sheet, index + 1, index + 7);
        renderSectionValues(workbook, sheet, index + 7, employees, sectionValues);
    }
}
