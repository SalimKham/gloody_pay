package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {AddressMapper.class, CompanyMapper.class})
public interface EmployeeMapper {
    @Mapping(source = "company", target = "companyId")
    EmployeeDto toDto(Employee employee);

    @Mapping(source = "companyId", target = "company")
    Employee toEntity(EmployeeDto employeeDto);

    default Long toId(Employee employee) {
        return (employee == null) ? null : employee.getId();
    }

    default Employee fromId(Long id) {
        return (id == null) ? null : Employee.builder().id(id).build();
    }
}

