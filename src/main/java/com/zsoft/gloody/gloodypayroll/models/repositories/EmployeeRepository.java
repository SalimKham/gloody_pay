package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query("select e FROM Employee e left join PayslipSetting ps on e.id = ps.employee WHERE ps.closed and YEAR(ps.paymentDate) = :year ")
    Page<Employee> findYearlyEmployees(@Param("year") int year,  Pageable pageable);

    @Query("""
            SELECT e.id, e.address.id FROM Employee e WHERE e.id IN :employee_ids AND e.address IS NOT NULL
            """)
    List<Tuple> getAddresses(@Param("employee_ids")Collection<Long> employee_ids);

    @Query("""
            SELECT e.id FROM Employee e WHERE e.company = :company_id
            """)
    Set<Long> findAllIdsByCompanyId(@Param("company_id") Long companyId);
}