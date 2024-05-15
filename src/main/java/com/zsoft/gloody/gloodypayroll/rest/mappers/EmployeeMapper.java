package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {AddressMapper.class, CompanyMapper.class})
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);

    Employee toEntity(EmployeeDto employeeDto);

    default Long toId(Employee employee) {
        return (employee == null) ? null : employee.getId();
    }

    default Employee fromId(Long id) {
        return (id == null) ? null : Employee.builder().id(id).build();
    }
}

