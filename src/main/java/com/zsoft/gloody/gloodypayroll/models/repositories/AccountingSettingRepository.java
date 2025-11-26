package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSetting;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingSettingRepository extends JpaRepository<AccountingSetting, Long> {
    @Query("SELECT acs FROM AccountingSetting acs ORDER BY acs.id LIMIT 1")
    Optional<AccountingSetting> findTopOrderByIdDesc();

    @Query(value = "SELECT  acc.id, SUM(e.value) FROM payslip_setting ps " +
            "LEFT JOIN payslip_setting_element e ON e.payslip_setting_id = ps.id  " +
            "LEFT JOIN section s ON s.id = e.section " +
            "LEFT join accounting_setting_account acc On s.accounting_id = acc.id " +
            "WHERE ps.closed AND  s.accounting_id IS  NOT NULL " +
            "AND  MONTH(ps.payment_date) = MONTH(:current_month) " +
            "AND YEAR(ps.payment_date) = YEAR (:current_month) AND ps.company_id = :company_id group by acc.id", nativeQuery = true)
    List<Object[]> getAccountingSectionsMonthlyTotal(@Param("current_month") LocalDate month, @Param("company_id") Long companyId);

    @Query(value = "SELECT  acc.id, ps.payslip_detail, SUM(e.value) FROM payslip_setting ps " +
            "LEFT JOIN payslip_setting_element e ON e.payslip_setting_id = ps.id  " +
            "LEFT JOIN section s ON s.id = e.section " +
            "LEFT join accounting_setting_account acc On s.accounting_id = acc.id " +
            "WHERE ps.closed AND  s.accounting_id IS  NOT NULL " +
            "AND  MONTH(ps.payment_date) = MONTH(:current_month) " +
            "AND YEAR(ps.payment_date) = YEAR (:current_month) AND ps.company_id = :company_id group by acc.id, ps.id", nativeQuery = true)
    List<Object[]> getAccountingSectionsMonthlyTotalByEmployee(@Param("current_month") LocalDate month, @Param("company_id") Long companyId);

    @Query(value = """
            SELECT  acc.id, ps.payslip_detail, SUM(e.value),  ps.contract_id, acc.detailed_by_employee  FROM payslip_setting ps 
            LEFT JOIN payslip_setting_element e ON e.payslip_setting_id = ps.id  
            LEFT JOIN section s ON s.id = e.section 
            LEFT join accounting_setting_account acc On s.accounting_id = acc.id 
            WHERE ps.closed AND  s.accounting_id IS  NOT NULL 
            AND  payment_date = :current_month 
            group by acc.id, ps.id
            """, nativeQuery = true)
    List<Tuple> getAccountingSectionsMonthlyTotalByContracts(@Param("current_month") LocalDate month);
}
