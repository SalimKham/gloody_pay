package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.zsoft.gloody.gloodypayroll.utils.LogUtility.successMsg;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class EmployeeService extends CompanySupportService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public Optional<EmployeeDto> findById(Long id) {
        return this.employeeRepository.findById(id)
                .map(this.employeeMapper::toDto);
    }


    public EmployeeDto create(EmployeeDto dto) {
        save(dto);
        return dto;
    }

    public void saveBulk(List<EmployeeDto> dtos) {
       dtos.forEach(this::save);
    }
    public EmployeeDto update(EmployeeDto dto) {
        this.save(dto);
        return dto;
    }

    public void save(EmployeeDto dto) {
        Employee entity = this.employeeMapper.toEntity(dto);
        employeeRepository.findById(dto.getId()).ifPresent(employee -> {
            if (employee.getAddress() != null && dto.getAddress() != null) {
                entity.getAddress().setId(employee.getAddress().getId());
            }
        });
        employeeRepository.save(entity);
        log.info(successMsg("UPDATING EMPLOYEE | ID: %s", dto.getId().toString()));
    }

}
