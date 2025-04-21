package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.EmployeeController;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import com.zsoft.gloody.gloodypayroll.utils.EmployeeListUtils;
import com.zsoft.gloody.gloodypayroll.utils.FieldAccessorUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class EmployeeService extends CompanySupportService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public Optional<EmployeeDto> findById(Long id) {
        return this.employeeRepository.findById(id)
                .map(this.employeeMapper::toDto);
    }

    public List<EmployeeDto> findAll() {
        return this.employeeRepository.findAll()
                .stream()
                .map(this.employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    public EmployeeDto create(EmployeeDto dto) {
        save(dto);
        return dto;
    }

    public void createBulk(List<EmployeeDto> dtos) {
        employeeRepository.saveAll(dtos.stream().map(employeeMapper::toEntity).toList());
    }

    public EmployeeDto update(EmployeeDto dto) {
        this.save(dto);
        return dto;
    }

    // todo optimize this
    public void updateFields(List<EmployeeController.UpdateEmployeeFieldPayload> payload) {
        if(payload == null || payload.isEmpty()) {
            return;
        }
        payload.forEach(p -> {
            this.findById(p.dto().getId())
                    .ifPresent(oldDto -> {
                        p.fields().forEach(fieldName -> {
                            Class<?> className = EmployeeDto.class;
                            Field field = FieldAccessorUtils.findField(className, fieldName);
                            if (field != null) {
                                FieldAccessorUtils.setFieldValue(className, field, oldDto, p.dto());
                            }
                        });
                        this.save(oldDto);
                    });
        });

    }

    public boolean deleteById(Long id) {
        if (this.employeeRepository.existsById(id)) {
            this.employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean notExistsById(Long id) {
        return !this.employeeRepository.existsById(id);
    }

    public void save(EmployeeDto dto) {
        Employee entity = this.employeeMapper.toEntity(dto);
        employeeRepository.findById(dto.getId()).ifPresent(employee -> {
            if (employee.getAddress() != null && dto.getAddress() != null) {
                entity.getAddress().setId(employee.getAddress().getId());
            }
        });
        employeeRepository.save(entity);
    }

}
