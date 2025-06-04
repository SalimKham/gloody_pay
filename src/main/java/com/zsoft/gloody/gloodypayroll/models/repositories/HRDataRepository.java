package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface HRDataRepository extends JpaRepository<HRMonthlyData, Long> {
    Optional<HRMonthlyData> findTopHrMonthlyDataByVariableAndContractIdAndMonth(String variable, Long contractId, LocalDate month);
    List<HRMonthlyData> findAllByContractIdAndMonth(Long contractId, LocalDate month);


    @Query("SELECT  hr.contractId, hr.variable, hr.value  FROM HRMonthlyData hr WHERE hr.month = :month  AND hr.resourceId in :resource_ids GROUP BY hr.contractId")
    List<Tuple> getMonthlyValues(@Param("month") LocalDate month, @Param("resource_ids") Set<Long> resourceIds);

    @Query("SELECT  hr.resourceId, hr.contractId FROM HRMonthlyData hr WHERE hr.month = :month and  hr.resourceId in :resource_ids ORDER BY hr.resourceId ASC")
    List<Tuple> getContractIdsByMonthAndResourceIdIn(@Param("month") LocalDate month, @Param("resource_ids") Set<Long> resourceIds);

    @Query("SELECT distinct  hr.contractId, hr.resourceId FROM HRMonthlyData hr WHERE hr.month = :month")
    List<Tuple> getContractsByMonth(@Param("month") LocalDate month);

    @Query("SELECT distinct  hr.contractId, hr.value FROM HRMonthlyData hr WHERE hr.month = :month AND hr.variable = 'DRAFT_ID'")
    Set<Tuple> getDraftedTemplateIds(@Param("month") LocalDate month);

    @Modifying
    void deleteAllByVariableAndContractIdInAndMonth(String variable, Collection<Long> contractIds, LocalDate month);

    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Query(value = "DELETE hr FROM hr_monthly_data hr WHERE hr.contract_id in :contract_ids AND hr.hr_variable Rlike :variable and YEAR(hr.pay_month) = YEAR(:month) AND MONTH(hr.pay_month) = MONTH(:month)", nativeQuery = true)
    void deleteAllByVariableLikeAndContractIdInAndMonth(@Param("variable") String variable, @Param("contract_ids") Collection<Long> contractIds,@Param("month") LocalDate month);

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Query(value = "SELECT COUNT(hr.id) > 0 FROM HRMonthlyData hr WHERE hr.contractId = :contract_id AND hr.month = :month AND hr.variable = 'TEMPLATE' and hr.value != -1")
    boolean hasTemplate(@Param("contract_id") Long contractId,@Param("month") LocalDate month);
    @Query(value = """
            INSERT INTO `hr_monthly_data` (`pay_month`, `resource_id`,`contract_id`, `hr_variable`,  `hr_value`) 
            SELECT p.payment_date, p.employee_id, p.contract_id, 'DRAFT_ID', p.id FROM `payslip_setting`  p 
            WHERE p.payment_date = :month AND p.draft AND p.id in :payslip_ids
            """, nativeQuery = true)
    void bulkInsertDraftIds(@Param("month") LocalDate month, @Param("payslip_ids") List<Long> payslipIds);

    @Query(value = """
            INSERT INTO `hr_monthly_data` (`pay_month`, `resource_id`,`contract_id`, `hr_variable`,  `hr_value`)
            SELECT p.payment_date, p.employee_id, p.contract_id, 'TEMPLATE', p.id FROM `payslip_setting`  p
            WHERE p.payment_date = :month AND p.closed AND p.id in :payslip_ids
            """, nativeQuery = true)
    void bulkInsertClosedTemplateIds(@Param("month") LocalDate month, @Param("payslip_ids") List<Long> payslipIds);

    @Query("SELECT hr.resourceId FROM HRMonthlyData hr WHERE hr.month = :current_month AND hr.variable = 'TEMPLATE' AND hr.value = :template_id")
    Set<Long> findAllResourcesByTemplateId(@Param("current_month") LocalDate month, @Param("template_id") Long templateId);

    @Query("""
            SELECT DISTINCT hr.resourceId FROM HRMonthlyData hr
            WHERE (hr.month = :current_month AND hr.variable = 'TEMPLATE' AND hr.value = -1)
            OR (NOT EXISTS (SELECT 1 FROM HRMonthlyData hr2   WHERE hr2.resourceId = hr.resourceId
            AND hr2.month = :current_month  AND hr2.variable = 'TEMPLATE'))
            """)
    Set<Long> findAllResourcesByDefaultTemplate(@Param("current_month") LocalDate month);

}