package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.enums.PayTransferType;
import com.zsoft.gloody.gloodypayroll.models.enums.PaymentMethod;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.utils.CommonUtils;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.services.payrollrecord.PayrollRecordsUtils.setColumnWidthInPixels;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollTransferService {
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private static String NET_SALARY_CODE = "R_NET_PAYE";
    private XSSFCellStyle contentColumnsStyle;
    private XSSFCellStyle headerColumnsStyle;
    private XSSFCellStyle totalColumnsStyle;

    public byte[] generate(LocalDate month, PayTransferType type, Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        PaymentMethod paymentMethod = Objects.equals(type, PayTransferType.POSTAL) ? PaymentMethod.POSTAL_TRANSFER : PaymentMethod.TRANSFER;
        XSSFWorkbook workbook = new XSSFWorkbook();
        String name = Objects.equals(type, PayTransferType.POSTAL) ? "virements CCP" : "virements bancaires";
        initStyles(workbook);
        final Sheet sheet = workbook.createSheet(name);
        setColumnWidthInPixels(sheet, 10);
        createHeaderRow(sheet, paymentMethod);
        int currentIndex = 1;
        int PAGE_SIZE = 500;
        List<Set<Long>> employeeIdsList = CommonUtils.splitSet(ids, PAGE_SIZE);
        double total = 0;

        for (Set<Long> employeeIds : employeeIdsList) {
            Page<Tuple> data = payslipElementSettingRepository.getClosedElementsByEmployeeIdAndCodeAndMonth(employeeIds, Collections.singletonList(NET_SALARY_CODE), month.withDayOfMonth(1), Pageable.unpaged());
            Map<Long, Double> mappedValues = data.stream().collect(Collectors.toMap(t -> t.get(0, Long.class), t -> t.get(2, Double.class)));
            Set<Employee> employees = new HashSet<>(employeeRepository.findAllById(employeeIds));
            for (Employee employee : employees) {
                if (!Objects.equals(employee.getPaymentMode(), paymentMethod)) {
                    continue;
                }
                Double value = mappedValues.getOrDefault(employee.getId(), 0d);
                total += value;
                createEmployeeRow(sheet, currentIndex++, employee, value, paymentMethod);
            }
        }

        createTotalRow(sheet, currentIndex, Objects.equals(paymentMethod, PaymentMethod.TRANSFER) ? 4 : 3, total);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();

    }

    private void initStyles(XSSFWorkbook workbook) {
        contentColumnsStyle = workbook.createCellStyle();
        contentColumnsStyle.setAlignment(HorizontalAlignment.LEFT);
        headerColumnsStyle = workbook.createCellStyle();
        headerColumnsStyle.setAlignment(HorizontalAlignment.LEFT);
        totalColumnsStyle = workbook.createCellStyle();
        totalColumnsStyle.setAlignment(HorizontalAlignment.LEFT);
        applyFont(workbook, headerColumnsStyle, true);
        applyFont(workbook, contentColumnsStyle, false);
        applyFont(workbook, totalColumnsStyle, true);
        //apply border
        totalColumnsStyle.setBorderBottom(BorderStyle.MEDIUM);
        totalColumnsStyle.setBorderTop(BorderStyle.MEDIUM);
        totalColumnsStyle.setBorderLeft(BorderStyle.MEDIUM);
        totalColumnsStyle.setBorderRight(BorderStyle.MEDIUM);
    }
    private void applyFont(XSSFWorkbook workbook, XSSFCellStyle style, boolean bold) {
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontName("Arial");
        style.setFont(font);
    }

    private Cell createHeaderCell(Row row, int cellNum) {
        Cell cell = row.createCell(cellNum);
        if (headerColumnsStyle != null) {
            cell.setCellStyle(headerColumnsStyle);
        }
        return cell;
    }

    private Cell createTotalCell(Row row, int cellNum) {
        Cell cell = row.createCell(cellNum);
        if (totalColumnsStyle != null) {
            cell.setCellStyle(totalColumnsStyle);
        }
        return cell;
    }


    private Cell createCell(Row row, int cellNum) {
        Cell cell = row.createCell(cellNum);
        if (contentColumnsStyle != null) {
            cell.setCellStyle(contentColumnsStyle);
        }
        return cell;
    }

    private void createHeaderRow(Sheet sheet, PaymentMethod paymentMethod) {
        int index = 0;
        Row row = sheet.createRow(0);
        createHeaderCell(row, index++).setCellValue("Matricule");
        createHeaderCell(row, index++).setCellValue("Nom");
        createHeaderCell(row, index++).setCellValue("Prénom");
        if (Objects.equals(paymentMethod, PaymentMethod.TRANSFER)) {
            createHeaderCell(row, index++).setCellValue("Nom de la banque");
        }
        createHeaderCell(row, index++).setCellValue(Objects.equals(paymentMethod, PaymentMethod.TRANSFER) ? "RIB" : "CCP");
        createHeaderCell(row, index).setCellValue("Net à payer");
    }

    private void createTotalRow(Sheet sheet, int index, int cellNum, double total) {
        Row row = sheet.createRow(index);
        createTotalCell(row, cellNum).setCellValue("Total");
        createTotalCell(row, cellNum + 1).setCellValue(String.format("%.2f", total));
    }

    private void createEmployeeRow(Sheet sheet, int index, Employee employee, Double value, PaymentMethod paymentMethod) {
        int columnNum = 0;
        Row row = sheet.createRow(index);
        createCell(row, columnNum++).setCellValue(Objects.requireNonNullElse(employee.getIdentification(), "-"));
        createCell(row, columnNum++).setCellValue(StringUtils.capitalize(Objects.requireNonNullElse(employee.getLastName(), "")));
        createCell(row, columnNum++).setCellValue(StringUtils.capitalize(Objects.requireNonNullElse(employee.getFirstName(), "")));
        if (Objects.equals(paymentMethod, PaymentMethod.TRANSFER)) {
            createCell(row, columnNum++).setCellValue(Objects.requireNonNullElse(employee.getBankName(), "-"));
        }
        createCell(row, columnNum++).setCellValue(Objects.requireNonNullElse(employee.getAccountNumber(), "-"));
        createCell(row, columnNum).setCellValue(Objects.nonNull(value) ? String.format("%.2f", value) : "-");
    }
}
