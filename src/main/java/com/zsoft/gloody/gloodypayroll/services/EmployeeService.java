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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        Set<Long> ids = dtos.stream().map(EmployeeDto::getId).collect(Collectors.toSet());
        Map<Long, Long> addresses = employeeRepository.getAddresses(ids).stream().collect(Collectors.toMap(t ->t.get(0, Long.class), t -> t.get(1, Long.class)));
        List<Employee> employees = dtos.stream().map(dto -> {
            Employee entity = this.employeeMapper.toEntity(dto);
            if (dto.getAddress() != null) {
                if(addresses.containsKey(entity.getId())) {
                    entity.getAddress().setId(addresses.get(entity.getId()));
                }
            }
            return entity;
        }).toList();
        employeeRepository.saveAll(employees);
        log.info(successMsg("BULK UPDATING EMPLOYEE"));
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
