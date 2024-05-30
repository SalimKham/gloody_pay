package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
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

    public Page<EmployeeDto> findAllByPage(Pageable pageable) {
        Specification<Employee> specification = Specification.where(((root, q, cb) -> q.getRestriction()));
        Pageable requestPageable = EmployeeListUtils.getPageable(pageable);
        Page<Employee> page = this.employeeRepository.findAll(specification, requestPageable);
        List<EmployeeDto> content = page.stream()
                .map(this.employeeMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional
    public EmployeeDto create(EmployeeDto dto) {
        return this.save(dto);
    }

    @Transactional
    public EmployeeDto update(EmployeeDto dto) {
        if (dto.getId() == null) {
            return null;
        }
        if (!this.employeeRepository.existsById(dto.getId())) {
            return null;
        }
        return this.save(dto);
    }

    @Transactional
    public Optional<EmployeeDto> updateFields(Long id, EmployeeDto dto, List<String> fields) {
        if (!this.employeeRepository.existsById(id)) {
            return Optional.empty();
        }
        return this.findById(id)
                .map(oldDto -> {
                    fields.forEach(fieldName -> {
                        Class<?> className = EmployeeDto.class;
                        Field field = FieldAccessorUtils.findField(className, fieldName);
                        if (field != null) {
                            FieldAccessorUtils.setFieldValue(className, field, oldDto, dto);
                        }
                    });
                    return oldDto;
                })
                .map(this::save);
    }

    @Transactional
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

    private EmployeeDto save(EmployeeDto dto) {
        Employee employee = this.employeeMapper.toEntity(dto);
        employee = this.employeeRepository.save(employee);
        return this.employeeMapper.toDto(employee);
    }

    public List<LocalDate> getWorkingDays(EmployeeDto employeeDto, Period period) {
        // todo: use employee profile to define working days in period (without weekends & holidays)
        return DateUtils.getWorkingDays(period.getStart(), period.getEnd());
    }
}
