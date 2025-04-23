package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class PayslipSettingService extends CompanySupportService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private final EmployeeService employeeService;
    private final HRDataRepository hrDataRepository;

    public Optional<PayslipSettingDto> markAsDefault(Long templateId) {
        payslipSettingRepository.markAsDefault(templateId);
        return findById(templateId, null, null, null);
    }

    public Optional<PayslipSettingDto> findDefaultTemplate(Long employeeId, LocalDate date, Long contractId) {
        if (employeeId != null) {
            Optional<PayslipSettingDto> payslipSettingDto = findByEmployeeIdAndCurrentMonth(employeeId, date, contractId);
            if (payslipSettingDto.isPresent()) {
                return payslipSettingDto;
            }
        }
        return this.payslipSettingRepository.findTopByByDefaultOrderByIdAsc(true).map(this::toDto);
    }

    @Cacheable(
            cacheNames = "company_default_template",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public Optional<PayslipSettingDto> findDefaultTemplate() {
        return this.payslipSettingRepository.findTopByByDefaultOrderByIdAsc(true).map(this::toDto);
    }

    public Optional<PayslipSettingDto> findByEmployeeId(Long id) {
        return this.payslipSettingRepository.findTopByByDefaultOrderByIdAsc(true).map(this::toDto);
    }

    public Optional<PayslipSettingDto> findByEmployeeIdAndCurrentMonth(Long employeeId, LocalDate currentMonth, Long contractId) {
        if (currentMonth == null) {
            return Optional.empty();
        }
        List<PayslipSetting> templates =  this.payslipSettingRepository.findByEmployeeAndCurrentMonth(employeeId, currentMonth);
        return templates == null || templates.isEmpty()  ? Optional.empty() : Optional.ofNullable(payslipSettingMapper.toDto(templates.stream()
                .filter(t -> Objects.equals(t.getContract(), contractId))
                .findFirst()
                .orElse(templates.stream().filter(t -> Objects.equals(t.getContract(), -1L)).findAny().orElse(null))));
    }


    public Optional<PayslipSettingDto> findById(Long id, Long employeeId, LocalDate date, Long contractId) {
        if (employeeId != null) {
            Optional<PayslipSettingDto> payslipSettingDto = findByEmployeeIdAndCurrentMonth(employeeId, date, contractId );
            if (payslipSettingDto.isPresent()) {
                return payslipSettingDto;
            }
        }
        return this.payslipSettingRepository.findById(id).map(this::toDto);
    }

    @Cacheable(
            cacheNames = "company_template_by_id",
            key = "#id"
    )
    public Optional<PayslipSettingDto> findById(Long id) {
        return this.payslipSettingRepository.findById(id).map(this::toDto);
    }

    public Optional<PayslipSettingDto> findDraftById(Long id) {
        System.out.println("GETTING DRAFT TEMPLATE "+ id);
        return this.payslipSettingRepository.findDraftPayslip(id).map(this::toDto);
    }

    public Page<PayslipSettingDto> findAllTemplates(String query, Pageable pageable) {
        query = query == null ? "%%" : "%" + query + "%";
        return this.payslipSettingRepository.findAllByNameLikeAndTemplateIsTrue( query, pageable)
                .map(this::toDto);
    }

    private PayslipSettingDto toDto(PayslipSetting payslipSetting) {
        PayslipSettingDto  payslipSettingDto = payslipSettingMapper.toDto(payslipSetting);
        if (payslipSettingDto != null && Objects.nonNull(payslipSettingDto.getElements())) {
            payslipSettingDto.getElements().forEach(payslipSettingElementDto -> {
                entityManager.detach(payslipSetting);
                payslipSettingElementDto.setPayslipSettingId(payslipSettingDto.getId());
            });
        }
        return payslipSettingDto;
    }

    @Transactional
    public PayslipSettingDto save(PayslipSettingDto dto, Long resourceId) {
        PayslipSetting payslipSetting = this.payslipSettingMapper.toEntity(dto);
        boolean updateEmployeeMonthlyTemplate = false;
        if (dto.isTemplate()) {
            payslipSetting.setPaymentDate(null);
        }
        if (resourceId != null && Objects.nonNull(dto.getPaymentDate()) && Objects.nonNull(dto.getContract())) {
          payslipSettingRepository.deleteByContractAndPaymentDate(dto.getContract().getId(), dto.getPaymentDate().withDayOfMonth(1));
           payslipSetting.setId(null);
           payslipSetting.setEmployee(resourceId);
           payslipSetting.setPayslipDetail(null);
           updateEmployeeMonthlyTemplate = true;
        }
        payslipSetting = this.payslipSettingRepository.save(payslipSetting);
        if (updateEmployeeMonthlyTemplate) {
            updateHrTemplate(resourceId, dto.getContract().getId(), dto.getPaymentDate().withDayOfMonth(1), payslipSetting.getId());
        }
        return this.payslipSettingMapper.toDto(payslipSetting);
    }

    private void updateHrTemplate(Long resourceId, Long contractId, LocalDate month, Long templateId) {
        Long variableId = hrDataRepository
                .findHrMonthlyDataByVariableAndContractIdAndMonth("TEMPLATE", contractId, month)
                .map(HRMonthlyData::getId).orElse(null);
        hrDataRepository.save(new HRMonthlyData(variableId, resourceId, contractId, month, "TEMPLATE", templateId));
    }

    @Transactional
    public PayslipSettingDto update(PayslipSettingDto dto) {
        EmployeeDto employeeDto = dto.getEmployee();
        if (employeeDto == null) {
            throw new ApiRuntimeException("error.employee.not-found");
        }
        Long employeeId = employeeDto.getId();
        if (employeeId == null || employeeService.notExistsById(employeeId)) {
            employeeDto = employeeService.create(employeeDto);
        }
        Optional.ofNullable(employeeDto)
                .map(EmployeeDto::getId)
                .flatMap(this::findByEmployeeId)
                .map(PayslipSettingDto::getId)
                .ifPresent(dto::setId);
        PayslipSetting payslipSetting = this.payslipSettingMapper.toEntity(dto);
        payslipSetting.setPayslipDetail(null);
        payslipSetting = this.payslipSettingRepository.save(payslipSetting);
        return this.payslipSettingMapper.toDto(payslipSetting);
    }

    public Page<PayslipSettingDto> deleteTemplate(Long templateId, Pageable pageable) {
        payslipSettingRepository.deleteById(templateId);
        return findAllTemplates("", pageable);
    }


    public Map<Long, PayslipSettingDto> loadPayslipSettingsMap() {
        return payslipSettingRepository.findAllByDraftIsFalseAndClosedIsFalse().stream().map(this::toDto).collect(Collectors.toMap(PayslipSettingDto::getId, payslipSettingDto -> payslipSettingDto));
    }

}
