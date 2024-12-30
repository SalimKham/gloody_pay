package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class PayslipSettingService extends CompanySupportService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private final EmployeeService employeeService;



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

    public List<Long> findAllEmployeesWithTemplates(LocalDate month) {
        return this.payslipSettingRepository.findAllEmployeesWithTemplates(month);
    }

    public List<PayslipSettingDto> findMonthlyTemplates(LocalDate month, List<Long> ids) {
        List<PayslipSetting> result =  this.payslipSettingRepository.findAllByMonthAndIdIn(month, Objects.requireNonNullElse(ids, Collections.emptyList()));
        List<PayslipSettingDto>  resultDTOs =  result.stream()
                .map(this::toDto).toList();
        return  resultDTOs;
    }
    @Transactional
    public PayslipSettingDto save(PayslipSettingDto dto) {
        PayslipSetting payslipSetting = this.payslipSettingMapper.toEntity(dto);
        boolean isDraft =  Objects.equals(payslipSetting.getDraft(), true);
        if (!isDraft) {
            payslipSetting.setPayslipDetail(null);
        }
        if (dto.isTemplate()) {
            payslipSetting.setPaymentDate(null);
        }
        if (payslipSetting.getEmployee() != null && !isDraft) {
           Long id =  payslipSettingRepository.findByEmployeeAndCurrentMonth(payslipSetting.getEmployee(), dto.getPaymentDate()).stream().filter(t -> Objects.equals(dto.getContract().getId(), t.getContract())).findFirst().map(PayslipSetting::getId).orElse(null);
           payslipSetting.setId(id);
        } else if (isDraft) {
            Long id  = payslipSettingRepository.findFirstByDraftAndContract(true, payslipSetting.getContract()).map(PayslipSetting::getId).orElse(null);
            payslipSetting.setId(id);
        }
        payslipSetting = this.payslipSettingRepository.save(payslipSetting);
        return this.payslipSettingMapper.toDto(payslipSetting);
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

    public List<Long> getDraftContracts() {
        return payslipSettingRepository.findDraftContractIds();
    }
}
