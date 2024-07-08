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
import java.util.List;
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
        return findById(templateId, null, null);
    }

    public Optional<PayslipSettingDto> findDefaultTemplate(Long employeeId, LocalDate date) {
        if (employeeId != null) {
            Optional<PayslipSettingDto> payslipSettingDto = findByEmployeeIdAndCurrentMonth(employeeId, date);
            if (payslipSettingDto.isPresent()) {
                return payslipSettingDto;
            }
        }
        return this.payslipSettingRepository.findTopByByDefaultOrderByIdAsc(true).map(this.payslipSettingMapper::toDto);
    }

    public Optional<PayslipSettingDto> findByEmployeeId(Long id) {
        return this.payslipSettingRepository.findTopByByDefaultOrderByIdAsc(true).map(this.payslipSettingMapper::toDto);
    }

    public Optional<PayslipSettingDto> findByEmployeeIdAndCurrentMonth(Long employeeId, LocalDate currentMonth) {
        if (currentMonth == null) {
            return Optional.empty();
        }
        return this.payslipSettingRepository.findByEmployeeAndCurrentMonth(employeeId, currentMonth).map(this.payslipSettingMapper::toDto);
    }


    public Optional<PayslipSettingDto> findById(Long id, Long employeeId, LocalDate date) {
        if (employeeId != null) {
            Optional<PayslipSettingDto> payslipSettingDto = findByEmployeeIdAndCurrentMonth(employeeId, date);
            if (payslipSettingDto.isPresent()) {
                return payslipSettingDto;
            }
        }
        return this.payslipSettingRepository.findById(id).map(this.payslipSettingMapper::toDto);
    }

    public Page<PayslipSettingDto> findAllTemplates(String query, Pageable pageable) {
        query = query == null ? "%%" : "%" + query + "%";
        return this.payslipSettingRepository.findAllByNameLikeAndTemplateIsTrue( query, pageable)
                .map(this.payslipSettingMapper::toDto);
    }

    public List<PayslipSettingDto> findAllTemplates() {
        return this.payslipSettingRepository.findAll()
                .stream()
                .map(this.payslipSettingMapper::toDto).toList();
    }

    @Transactional
    public PayslipSettingDto save(PayslipSettingDto dto) {
        PayslipSetting payslipSetting = this.payslipSettingMapper.toEntity(dto);
        if (dto.isTemplate()) {
            payslipSetting.setPaymentDate(null);
        }
        if (payslipSetting.getEmployee() != null) {
           Long id =  payslipSettingRepository.findByEmployeeAndCurrentMonth(payslipSetting.getEmployee(), dto.getPaymentDate()).map(PayslipSetting::getId).orElse(null);
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
        payslipSetting = this.payslipSettingRepository.save(payslipSetting);
        return this.payslipSettingMapper.toDto(payslipSetting);
    }

    public Page<PayslipSettingDto> deleteTemplate(Long templateId, Pageable pageable) {
        payslipSettingRepository.deleteById(templateId);
        return findAllTemplates("", pageable);
    }
}
