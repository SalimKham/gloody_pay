package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipFunctionElement;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Repository
public interface PayslipElementSettingRepository extends JpaRepository<PayslipSettingElement, Long> {
    @Query("SELECT NEW com.zsoft.gloody.gloodypayroll.rest.dto.PayslipFunctionElement(pse.payslipSetting.employee, pse.section.code, pse.payslipSetting.paymentDate, pse.value)  FROM PayslipSettingElement  pse WHERE pse.section.code in :codes " +
            "AND pse.value IS NOT NULL AND pse.payslipSetting.employee in :resource_ids")
    List<PayslipFunctionElement> findAllByEmployeeIdAndClosed(@Param("resource_ids") Set<Long> resourceIds, @Param("codes") List<String> codes);

    @Query("""
            SELECT pse.payslipSetting.employee,
            SUM(CASE WHEN pse.section.type = 'DEDUCTION' THEN -pse.value ELSE pse.value END)
            FROM PayslipSettingElement pse
            JOIN pse.payslipSetting ps
            JOIN pse.section s
            WHERE ps.closed = true
            AND pse.value IS NOT NULL
            AND pse.payslipSetting.employee IS NOT NULL 
            AND s.code = 'R_BRUT'
            AND ps.paymentDate BETWEEN :start_date AND :end_date 
            GROUP BY pse.payslipSetting.employee
            """)
    List<Tuple> findQuarterlyContribution(@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate);

    @Query(value = "SELECT hr.resource_id , :days_nbr - SUM(hr.hr_value) FROM hr_monthly_data hr WHERE  hr.hr_variable in ('ABS_TTL' , 'CONTRACT_OFF_DAYS') AND hr.pay_month between :start_date AND :end_date GROUP BY hr.resource_id", nativeQuery = true)
    List<Tuple> findQuarterlyWorkingDays(@Param("days_nbr") Double daysNbr, @Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate);

    @Query("SELECT SUM(pse.value) FROM PayslipSettingElement  pse where pse.payslipSetting.closed AND  pse.value IS NOT NULL AND pse.payslipSetting.employee = :resource_id AND pse.section.code = 'R_BRUT' AND  pse.payslipSetting.paymentDate  BETWEEN :start_date AND :end_date")
    Double findEmployeeGrossSalary(@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate, @Param("resource_id") Long resourceId);

    @Query(value = "SELECT  :days_nbr - SUM(hr.hr_value) FROM hr_monthly_data hr WHERE hr.resource_id = :resource_id AND hr.hr_variable in ('ABS_TTL' , 'CONTRACT_OFF_DAYS') AND hr.pay_month between :start_date AND :end_date", nativeQuery = true)
    Double findEmployeeWorkingDays(@Param("days_nbr") Double daysNbr, @Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate, @Param("resource_id") Long resourceId);


    @Query("SELECT SUM(IF(pse.section.type = 'DEDUCTION', 0 - pse.value, pse.value )) FROM PayslipSettingElement  pse LEFT JOIN Employee e on e.id = pse.payslipSetting.employee  where pse.payslipSetting.closed AND  pse.value IS NOT NULL AND e.affiliate = :affiliate AND pse.section.code = 'R_BRUT' AND pse.payslipSetting.paymentDate BETWEEN :start_date AND :end_date")
    Double sumQuarterContribution(@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate, @Param("affiliate") String affiliate);


    @Query("""
            SELECT pse.payslipSetting.paymentDate, pse.section.code,  pse.value FROM PayslipSettingElement  pse WHERE pse.section.code in :codes 
            AND pse.value IS NOT NULL AND pse.payslipSetting.employee = :resource_id AND pse.payslipSetting.closed AND  pse.payslipSetting.paymentDate BETWEEN :start_month AND :end_month
            ORDER BY pse.payslipSetting.paymentDate ASC
            """)
    List<Tuple> getClosedElementsByEmployeeIdAndCodeIn(@Param("resource_id") Long resourceId, @Param("codes") List<String> codes, @Param("start_month") LocalDate startMonth,   @Param("end_month") LocalDate endMonth);


    @Query("""
            SELECT pse.payslipSetting.employee, pse.section.code,  pse.value FROM PayslipSettingElement  pse WHERE pse.value IS NOT NULL AND pse.payslipSetting.employee IN :resource_ids AND pse.payslipSetting.closed AND  pse.payslipSetting.paymentDate = :selected_month
            ORDER BY pse.payslipSetting.paymentDate ASC
            """)
    Page<Tuple> getClosedElementsByEmployeeIdAndCodeInAndMonth(@Param("resource_ids") Set<Long> resourceIds, @Param("selected_month") LocalDate month, Pageable pageable);




    @Query("SELECT pse.payslipSetting.employee, pse.payslipSetting.paymentDate, pse.section.code,  pse.value FROM PayslipSettingElement  pse WHERE pse.section.code in :codes " +
            "AND pse.value IS NOT NULL AND pse.payslipSetting.employee in :resource_ids AND pse.payslipSetting.closed AND YEAR(pse.payslipSetting.paymentDate) = :year  ORDER BY pse.payslipSetting.paymentDate ASC ")
    Page<Tuple> getClosedElementsByEmployeeInAndCodeIn(@Param("resource_ids") Set<Long> resourceIds, @Param("codes") Set<String> codes, @Param("year") int year, Pageable pageable);

}