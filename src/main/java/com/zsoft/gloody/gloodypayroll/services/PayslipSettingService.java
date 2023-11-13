package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PayslipSettingService extends TenantSupportService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private final EmployeeService employeeService;

    public Optional<PayslipSettingDto> findByEmployeeId(Long employeeId) {
        return this.payslipSettingRepository.findByEmployeeId(employeeId)
                .stream()
                .findFirst()
                .map(this.payslipSettingMapper::toDto);
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
}
