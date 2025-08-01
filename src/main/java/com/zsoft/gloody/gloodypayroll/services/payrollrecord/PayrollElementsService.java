package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.enums.MaritalStatus;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords.PayrollElementPayloadDTO;
import jakarta.persistence.Tuple;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollElementsService {
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final List<String> sectionCodes = List.of("R_BRUT", "R_RET_SS", "R_BRUT_IMPOS", "R_RET_IRG", "R_IRG_10", "R_SAL_NET");

    public byte[] generate(PayrollElementPayloadDTO payload) throws IOException {
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (employeeRepository.findById(payload.getId()).isPresent()) {
            PayrollElement data = new PayrollElement();
            data.setPrintDate(LocalDate.now().format( dateFormatter));
            data.setEmployee(payload);
            if(data.getEmployee().getGender() != null) {
                data.getEmployee().setGender( Objects.equals(data.getEmployee().getGender(),"SIR") ? "masculin" : "feminin");
            }
            data.getEmployee().setMaritalStatus(translateMaritalStatus(payload.getMaritalStatus()));
            data.getEmployee().setBirthDate(data.getEmployee().getBirthDate() != null ? LocalDate.parse(data.getEmployee().getBirthDate()).format(dateFormatter) : "");
            data.getEmployee().setHireDate(data.getEmployee().getHireDate() != null ? LocalDate.parse(data.getEmployee().getHireDate()).format(dateFormatter) : "");
            data.setTotal(new PayrollElementTotal());
            data.setItems( new ArrayList<>());
            Map<LocalDate, List<Tuple>> result = payslipElementSettingRepository.getClosedElementsByEmployeeIdAndCodeIn(payload.getId(),  sectionCodes, payload.getYear()).stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(0, LocalDate.class),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            result.forEach((date, values) -> {
                PayrollElementItem item = new PayrollElementItem();
                item.setMonth(date.format(monthFormatter));
                values.forEach(tuple -> {
                    switch (tuple.get(1, String.class)) {
                        case "R_BRUT":
                            item.setBrut(tuple.get(2, Double.class));
                            break;
                        case "R_RET_SS":
                            item.setRet_ss(tuple.get(2, Double.class));
                            break;
                        case "R_BRUT_IMPOS":
                            item.setBrut_impos(tuple.get(2, Double.class));
                            break;
                        case "R_RET_IRG":
                            item.setIrg(tuple.get(2, Double.class));
                            break;
                        case "R_IRG_10":
                            item.setIrg10(tuple.get(2, Double.class));
                            break;
                        case "R_SAL_NET":
                            item.setNet_payer(tuple.get(2, Double.class));
                            break;
                    }
                });
                updateTotal(data.getTotal(), item);
                data.getItems().add(item);
            });

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
        total.setBrut(total.getBrut() + item.getBrut());
        total.setRet_ss(total.getRet_ss() + item.getRet_ss());
        total.setBrut_impos(total.getBrut_impos() + item.getBrut_impos());
        total.setIrg(total.getIrg() + item.getIrg());
        total.setIrg10(total.getIrg10() + item.getIrg10());
        total.setNet_payer(total.getNet_payer() + item.getNet_payer());
    }



}
