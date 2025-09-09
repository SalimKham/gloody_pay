package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipDetailDto;
import org.mapstruct.Mapper;

import java.time.LocalDate;

@Mapper(uses = {AddressMapper.class})
public interface EmployeeMapper {

    EmployeeDto toDto(Employee employee);

    Employee toEntity(EmployeeDto employeeDto);

    default Long toId(Employee employee) {
        return (employee == null) ? null : employee.getId();
    }

    default Employee fromId(Long id) {
        return (id == null) ? null : Employee.builder().id(id).build();
    }

    default PayslipDetailDto employeeToPayslipDetails(Employee employee, Long contractId, LocalDate month) {
        if(employee == null) {return null;}
        return PayslipDetailDto.builder()
                .fullName(employee.getFullName())
                .departmentName(employee.getDepartment())
                .jobTitle(employee.getJobTitle())
                .payslipDate(month)
                .resourceIdentification(employee.getIdentification())
                .socialSecurity(employee.getSocialSecurity())
                .build();
    }
}

