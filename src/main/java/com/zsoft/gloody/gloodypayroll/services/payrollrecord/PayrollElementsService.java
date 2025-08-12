package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.enums.MaritalStatus;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords.PayrollElementPayloadDTO;
import jakarta.persistence.Tuple;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollElementsService {
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final List<String> sectionCodes = List.of("R_BRUT", "R_RET_SS", "R_BRUT_IMPOS", "R_RET_IRG", "R_IRG_10", "R_NET_PAYE");

    public byte[] generate(PayrollElementPayloadDTO payload) throws IOException {
        if (employeeRepository.findById(payload.getId()).isPresent()) {
            PayrollElement data = new PayrollElement();
            data.setPrintDate(LocalDate.now().format( PayrollRecordsUtils.dateFormatter));
            PayrollElementPayloadDTO employee = payload;
            data.setEmployee(employee);
            if(employee.getGender() != null) {
                employee.setGender( Objects.equals(employee.getGender(),"SIR") ? "Masculin" : "Feminin");
            }
            employee.setIdentification(StringUtils.leftPad(employee.getIdentification(), 5, "0"));
            employee.setFirstName(Objects.requireNonNullElse(employee.getFirstName(),  "").toUpperCase());
            employee.setLastName(Objects.requireNonNullElse(employee.getLastName(),  "").toUpperCase());
            employee.setMaritalStatus(translateMaritalStatus(payload.getMaritalStatus()));
            employee.setBirthDate(employee.getBirthDate() != null ? LocalDate.parse(employee.getBirthDate()).format(PayrollRecordsUtils.dateFormatter) : "");
            employee.setHireDate(employee.getHireDate() != null ? LocalDate.parse(employee.getHireDate()).format(PayrollRecordsUtils.dateFormatter) : "");
            data.setTotal(new PayrollElementTotal());
            data.setItems( new ArrayList<>());
            LocalDate startMonth = payload.getStartMonth().withDayOfMonth(1);
            LocalDate endMonth = payload.getEndMonth().withDayOfMonth(1);

            Map<LocalDate, List<Tuple>> result = payslipElementSettingRepository.getClosedElementsByEmployeeIdAndCodeIn(payload.getId(),  sectionCodes, startMonth, endMonth).stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(0, LocalDate.class),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            while (!startMonth.isAfter(endMonth)) {
                PayrollElementItem item = new PayrollElementItem();
                item.setMonth(startMonth.format(PayrollRecordsUtils.fullMonthYearFormatter));
                List<Tuple> values = result.getOrDefault(startMonth, Collections.emptyList());
                values.forEach(tuple -> {
                    switch (tuple.get(1, String.class)) {
                        case "R_BRUT":
                            item.setBrut(BigDecimal.valueOf(tuple.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP));
                            break;
                        case "R_RET_SS":
                            item.setRet_ss(BigDecimal.valueOf(tuple.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP));
                            break;
                        case "R_BRUT_IMPOS":
                            item.setBrut_impos(BigDecimal.valueOf(tuple.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP));
                            break;
                        case "R_RET_IRG":
                            item.setIrg(BigDecimal.valueOf(tuple.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP));
                            break;
                        case "R_IRG_10":
                            item.setIrg10(BigDecimal.valueOf(tuple.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP));
                            break;
                        case "R_NET_PAYE":
                            item.setNet_payer(BigDecimal.valueOf(tuple.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP));
                            break;
                    }
                });
                updateTotal(data.getTotal(), item);
                data.getItems().add(item);
                startMonth = startMonth.plusMonths(1);
            }
            return PayrollRecordsUtils.generateDocFromTemplate("payroll_elements", data);
        }
        return null;
    }

    private String translateMaritalStatus(String maritalStatus) {
        if(maritalStatus == null || maritalStatus.isEmpty()) {
            return "";
        }
        return switch (MaritalStatus.valueOf(maritalStatus.toUpperCase())) {
            case SINGLE -> "Célibataire";
            case MARRIED -> "Marié(e)";
            case DIVORCED -> "Divorcé(e)";
            case WIDOWED -> "Veuf(ve)";
            case SEPARATED -> "séparé(e)";
            case UNKNOWN -> "inconnu";
            case DOMESTIC_PARTNERSHIP -> "concubinage";
            case PACS -> " pacsé(e)";
            case NOT_APPLICABLE -> "not_applicable";
        };
    }

    private void updateTotal(@NotNull  PayrollElementTotal total, @NotNull  PayrollElementItem item) {
        total.setBrut(total.getBrut().add(item.getBrut()).setScale(2, RoundingMode.HALF_UP));
        total.setRet_ss(total.getRet_ss().add(item.getRet_ss()).setScale(2, RoundingMode.HALF_UP));
        total.setBrut_impos(total.getBrut_impos().add(item.getBrut_impos()).setScale(2, RoundingMode.HALF_UP));
        total.setIrg(total.getIrg().add(item.getIrg()).setScale(2, RoundingMode.HALF_UP));
        total.setIrg10(total.getIrg10().add(item.getIrg10()).setScale(2, RoundingMode.HALF_UP));
        total.setNet_payer(total.getNet_payer().add(item.getNet_payer()).setScale(2, RoundingMode.HALF_UP));
    }



}
